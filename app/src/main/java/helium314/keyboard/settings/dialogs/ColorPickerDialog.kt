// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.previewDark
import helium314.keyboard.settings.isWideScreen

@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    initialColor: Int,
    title: String,
    showDefault: Boolean,
    onDefault: () -> Unit,
    onConfirmed: (Int) -> Unit,
) {
    val barHeight = 35.dp
    val initialString = initialColor.toUInt().toString(16)
    var currentColor by remember { mutableStateOf(Color(initialColor)) }
    val useWideLayout = isWideScreen()
    @Composable
    fun topBar() {
        Row {
            Surface(
                color = Color(initialColor),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(start = 10.dp)
                    .height(barHeight)
            ) { }
            Surface(
                color = currentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp)
                    .height(barHeight)
            ) { }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme(previewDark) {
        ColorPickerDialog({}, -0x0f4488aa, "color name", true, {}, {})
    }
}

// for some reason this is cut of while both previews are shown
@Preview(device = "spec:orientation=landscape,width=400dp,height=780dp")
@Composable
private fun WidePreview() {
    Theme(previewDark) {
        ColorPickerDialog({}, -0x0f4488aa, "color name", true, {}, {})
    }
}
