package com.jakting.duolingo

import android.app.AlertDialog
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import java.util.TimeZone

private const val PACKAGE_NAME_HOOKED = "com.duolingo"
private const val TAG = "TIGERBEANST_DUOLINGO"

@InjectYukiHookWithXposed
object MainHook : IYukiHookXposedInit {

    override fun onInit() {
        YukiHookAPI.configs {
            debugLog {
                tag = "TIGERBEANST_DUOLINGO"
                isEnable = true
                isRecord = false
                elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
            }
            isDebug = BuildConfig.DEBUG
            isEnableModuleAppResourcesCache = true
            isEnableHookSharedPreferences = false
            isEnableDataChannel = true
        }
    }

    override fun onHook() = YukiHookAPI.encase {
        val nowTimeZone =
            prefs("tiger_duolingo").getString("now_timezone", TimeZone.getDefault().id)
        val isSaveMode = prefs("tiger_duolingo").getBoolean("is_save", false)
        loadApp(name = PACKAGE_NAME_HOOKED) {
            TimeZoneHooks.install(nowTimeZone)

            if (isSaveMode == true) {
                "com.duolingo.splash.LaunchActivity".toClass().method {
                    name = "onCreate"
                    param(BundleClass)
                    returnType = UnitType
                }.hook {
                    after {
//                        YLog.info("Hooked LaunchActivity.onCreate")
                        AlertDialog.Builder(instance())
                            .setMessage(
                                String.format(
                                    moduleAppResources.getText(R.string.rescue_dialog).toString(),
                                    nowTimeZone
                                )
                            )
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }
    }
}


//class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit /* Optional */ {
//    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
//        if (lpparam.packageName == PACKAGE_NAME_HOOKED) {
//            // Init EzXHelper
//            EzXHelper.initHandleLoadPackage(lpparam)
//            EzXHelper.setLogTag(TAG)
//            EzXHelper.setToastTag(TAG)
//            // Init hooks
//            initHooks(TimeZoneHook)
//        }
//    }
//
//    // Optional
//    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
//        EzXHelper.initZygote(startupParam)
//    }
//
//    private fun initHooks(vararg hook: BaseHook) {
//        hook.forEach {
//            runCatching {
//                if (it.isInit) return@forEach
//                it.init()
//                it.isInit = true
//                Log.i("Inited hook: ${it.javaClass.simpleName}")
//            }.logexIfThrow("Failed init hook: ${it.javaClass.simpleName}")
//        }
//    }
//}
