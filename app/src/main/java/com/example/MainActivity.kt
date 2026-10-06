package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.FuelManagerApp
import com.example.ui.FuelManagerViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: FuelManagerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FuelManagerApp(viewModel = viewModel)
    }
  }
}
