// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.keyboard
import android.graphics.Typeface
import android.widget.TextView

object KeyboardTypeface {
    @JvmStatic
    fun resolve(
        text: CharSequence?,
        defaultTypeface: Typeface = Typeface.DEFAULT,
    ): Typeface {
        return defaultTypeface
    }

    @JvmStatic
    fun applyToTextView(textView: TextView, text: CharSequence?, defaultTypeface: Typeface) {
        textView.typeface = resolve(text, defaultTypeface = defaultTypeface)
    }

}
