package com.jakting.duolingo

import android.icu.util.TimeZone as IcuTimeZone
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.time.ZoneId
import java.util.TimeZone

/** Installed only in com.duolingo; never changes the device's system time zone. */
internal object TimeZoneHooks {
    private const val TAG = "TIGERBEANST_DUOLINGO"

    fun install(id: String) {
        // getTimeZone silently falls back to GMT for invalid IDs. Do not hide bad preferences.
        if (id !in TimeZone.getAvailableIDs()) {
            XposedBridge.log("$TAG: invalid configured time zone '$id'; keeping system default")
            return
        }
        val javaZone = TimeZone.getTimeZone(id)
        val zoneId = javaZone.toZoneId()
        val icuZone = IcuTimeZone.getTimeZone(id)

        // Seed caches before the application creates calendars/formatters. Replacing getID
        // alone leaves the original offset/rules and corrupts explicitly requested UTC zones.
        attempt("seed Java default") { TimeZone.setDefault(javaZone) }

        replaceDefault(TimeZone::class.java, "getDefault") { javaZone.clone() }
        // Android Calendar can use this internal accessor instead of the public getter.
        // Optional: a ROM may rename/remove it; the public API hooks still install.
        replaceDefault(TimeZone::class.java, "getDefaultRef") { javaZone.clone() }
        replaceDefault(ZoneId::class.java, "systemDefault") { zoneId }
        replaceDefault(IcuTimeZone::class.java, "getDefault") { icuZone.cloneAsThawed() }

        // A system time-zone broadcast may clear the defaults after startup. Keep new
        // defaults consistent as well, without touching explicitly constructed time zones.
        pinDefault(TimeZone::class.java) { javaZone.clone() }
    }

    private fun replaceDefault(type: Class<*>, name: String, value: () -> Any) {
        attempt("${type.name}.$name") {
            XposedBridge.hookMethod(type.getDeclaredMethod(name), object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any = value()
            })
        }
    }

    private fun pinDefault(type: Class<*>, value: () -> Any) {
        attempt("${type.name}.setDefault") {
            XposedBridge.hookMethod(type.getDeclaredMethod("setDefault", type), object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[0] = value()
                }
            })
        }
    }

    private inline fun attempt(label: String, action: () -> Unit) {
        try {
            action()
        } catch (error: Throwable) {
            // One unavailable framework method must not prevent the remaining hooks/dialog.
            XposedBridge.log("$TAG: failed $label: $error")
        }
    }
}
