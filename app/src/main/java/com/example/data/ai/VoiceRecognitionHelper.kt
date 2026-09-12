package com.example.data.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SpeechLanguage(val code: String, val label: String, val nativeLabel: String) {
    HINDI("hi-IN", "Hindi", "हिंदी"),
    ENGLISH("en-IN", "English", "English"),
    AUTO("hi-Latn", "Auto", "सभी भाषाएं")
}

/**
 * Robust Voice-to-Text & Speech Manager for Sahakaar Sahayak.
 * Supports:
 * 1. Native Android SpeechRecognizer with real-time partial streaming & audio waveform RMS
 * 2. Multi-language speech recognition (Hindi, English, Auto/Hinglish)
 * 3. Text-to-Speech (TTS) readout for hands-free audio answers on the go
 */
class VoiceRecognitionHelper(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _liveRmsDb = MutableStateFlow(0f)
    val liveRmsDb: StateFlow<Float> = _liveRmsDb.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _isFinal = MutableStateFlow(false)
    val isFinal: StateFlow<Boolean> = _isFinal.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(SpeechLanguage.HINDI)
    val selectedLanguage: StateFlow<SpeechLanguage> = _selectedLanguage.asStateFlow()

    // Text-to-Speech support
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingId = MutableStateFlow<String?>(null)
    val currentlySpeakingId: StateFlow<String?> = _currentlySpeakingId.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    val hiLocale = Locale.forLanguageTag("hi-IN")
                    val result = tts?.setLanguage(hiLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.language = Locale.ENGLISH
                    }
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                            _currentlySpeakingId.value = utteranceId
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentlySpeakingId.value = null
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentlySpeakingId.value = null
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                            _currentlySpeakingId.value = null
                        }
                    })
                } else {
                    isTtsReady = false
                }
            }
        } catch (e: Exception) {
            Log.w("VoiceRecognitionHelper", "TTS init notice: ${e.message}")
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun setLanguage(language: SpeechLanguage) {
        _selectedLanguage.value = language
    }

    fun startListening(language: SpeechLanguage = _selectedLanguage.value, onResult: ((String) -> Unit)? = null) {
        mainHandler.post {
            try {
                stopListening()

                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _errorMessage.value = "Speech recognition service not active on device"
                    return@post
                }

                _selectedLanguage.value = language
                _spokenText.value = ""
                _isFinal.value = false
                _errorMessage.value = null

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _liveRmsDb.value = 1f
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            _liveRmsDb.value = rmsdB.coerceIn(0f, 10f)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap mic to try again."
                                SpeechRecognizer.ERROR_NETWORK -> "Network issue in speech recognition."
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                                SpeechRecognizer.ERROR_CLIENT -> "Voice recognition client unavailable."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                                else -> "Speech recognition ended. Tap mic to speak."
                            }
                            Log.d("VoiceRecognitionHelper", "SpeechRecognizer error: $error -> $msg")
                            // If empty, set friendly error message
                            if (_spokenText.value.isBlank()) {
                                _errorMessage.value = msg
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val recognized = matches?.firstOrNull() ?: ""
                            if (recognized.isNotBlank()) {
                                _spokenText.value = recognized
                                _isFinal.value = true
                                _errorMessage.value = null
                                onResult?.invoke(recognized)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partial = matches?.firstOrNull() ?: ""
                            if (partial.isNotBlank()) {
                                _spokenText.value = partial
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.code)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Sahakaar Sahayak...")
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("VoiceRecognitionHelper", "Failed to start speech recognizer: ${e.message}")
                _isListening.value = false
                _errorMessage.value = "Voice input unavailable: ${e.localizedMessage}"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
            _isListening.value = false
            _liveRmsDb.value = 0f
        }
    }

    fun clearSpokenText() {
        _spokenText.value = ""
        _isFinal.value = false
        _errorMessage.value = null
    }

    fun setDirectSpokenText(text: String) {
        _spokenText.value = text
        _isFinal.value = true
        _errorMessage.value = null
    }

    /**
     * Read aloud text using TTS (Hindi or English)
     */
    fun speakText(text: String, id: String = System.currentTimeMillis().toString()) {
        try {
            if (_isSpeaking.value && _currentlySpeakingId.value == id) {
                stopSpeaking()
                return
            }

            stopSpeaking()

            val clean = text
                .replace(Regex("[*#_`>]"), "")
                .replace(Regex("\\[.*?\\]\\(.*?\\)"), "")
                .trim()

            if (clean.isBlank()) return

            // Auto-detect if Hindi characters exist
            val hasHindi = clean.any { it.code in 0x0900..0x097F }
            val locale = if (hasHindi) Locale.forLanguageTag("hi-IN") else Locale.ENGLISH
            tts?.language = locale

            _isSpeaking.value = true
            _currentlySpeakingId.value = id
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, id)
        } catch (e: Exception) {
            Log.w("VoiceRecognitionHelper", "TTS speak failed: ${e.message}")
            _isSpeaking.value = false
            _currentlySpeakingId.value = null
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _currentlySpeakingId.value = null
    }

    fun release() {
        stopListening()
        stopSpeaking()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}
