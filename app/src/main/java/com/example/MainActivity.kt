package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.data.database.AppDatabase
import com.example.data.repository.SahakaarRepository
import com.example.ui.SahakaarApp
import com.example.ui.theme.SahakaarTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: SahakaarRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        repository = SahakaarRepository(database)

        // Run seed and Supabase sync in lifecycleScope so background sync is resilient to composition changes
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repository.ensureDataSeeded()
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Initial data seeding notice: ${e.message}")
            }
        }

        setContent {
            SahakaarTheme {
                SahakaarApp(repository = repository)
            }
        }
    }
}
