package org.dromio

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
  Window(
  onCloseRequest = ::exitApplication,
  title = "Avelyn Shop",
  resizable = false)
  { App() }
}
