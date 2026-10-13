package helium314.keyboard.compat

import android.content.res.Configuration
import android.os.Build
import java.util.Locale

fun Configuration.locale(): Locale =
    locales[0]
