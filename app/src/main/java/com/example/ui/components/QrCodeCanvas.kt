package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun QrCodeCanvas(
    payload: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 160.dp,
    qrColor: Color = Color(0xFF0F172A),
    backgroundColor: Color = Color.White
) {
    // Generate a deterministic 21x21 QR-like matrix based on payload hash
    val matrix = remember(payload) {
        val size = 21
        val grid = Array(size) { BooleanArray(size) }
        val seed = payload.hashCode().toLong()
        val random = Random(seed)

        // Fill pseudo random data
        for (r in 0 until size) {
            for (c in 0 until size) {
                grid[r][c] = random.nextBoolean()
            }
        }

        // Draw 3 standard Finder Patterns (7x7) at Top-Left, Top-Right, Bottom-Left
        fun drawFinder(startR: Int, startC: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    grid[startR + r][startC + c] = isBorder || isCenter
                }
            }
            // Clear separators
            for (r in -1..7) {
                for (c in -1..7) {
                    val actualR = startR + r
                    val actualC = startC + c
                    if (actualR in 0 until size && actualC in 0 until size) {
                        if (r == -1 || r == 7 || c == -1 || c == 7) {
                            grid[actualR][actualC] = false
                        }
                    }
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, size - 7)
        drawFinder(size - 7, 0)

        // Timing patterns
        for (i in 7 until size - 7) {
            grid[6][i] = (i % 2 == 0)
            grid[i][6] = (i % 2 == 0)
        }

        grid
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val numCells = 21
            val cellSize = size.width / numCells

            for (r in 0 until numCells) {
                for (c in 0 until numCells) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}
