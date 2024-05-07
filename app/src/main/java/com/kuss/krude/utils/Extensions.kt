package com.kuss.krude.utils

import android.provider.Settings
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType

object Extensions {
    const val WECHAT_SCAN_SCHEME = "krude://openuri/wechat_scan"
    val EMBEDDED_EXTENSIONS = listOf(
        Extension("支付宝-扫一扫", "Alipay QR Scan", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=10000007"),
        Extension("支付宝-收付款", "Alipay Pay/Collect", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=20000056"),
        Extension("支付宝-出行", "Alipay Transport", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=200011235"),
        Extension("支付宝-菜鸟", "Alipay Cai Niao", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=2021001141626787"),
        Extension("微信-扫一扫", "Wechat QR Scan", ExtensionType.SCHEME, WECHAT_SCAN_SCHEME),
        Extension("设置-WiFi", "Wifi Setting", ExtensionType.ACTION, Settings.ACTION_WIFI_SETTINGS),
        Extension("设置-蓝牙", "Bluetooth Setting", ExtensionType.ACTION, Settings.ACTION_BLUETOOTH_SETTINGS),
        Extension("设置-应用", "App Setting", ExtensionType.ACTION, Settings.ACTION_APPLICATION_SETTINGS),
        Extension("设置-开发者选项", "Developer Options", ExtensionType.ACTION, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
        Extension("设置-日期时间", "Date Time Setting", ExtensionType.ACTION, Settings.ACTION_DATE_SETTINGS),
        Extension("设置-语言", "Language Setting", ExtensionType.ACTION, Settings.ACTION_LOCALE_SETTINGS),
        Extension("设置-位置", "Location Setting", ExtensionType.ACTION, Settings.ACTION_LOCATION_SOURCE_SETTINGS),
        Extension("设置-存储", "Storage Setting", ExtensionType.ACTION, Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
        Extension("设置-通知", "Notification Setting", ExtensionType.ACTION, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
        Extension("设置-安全", "Security Setting", ExtensionType.ACTION, Settings.ACTION_SECURITY_SETTINGS),
        Extension("设置-声音", "Sound Setting", ExtensionType.ACTION, Settings.ACTION_SOUND_SETTINGS),
        Extension("设置-显示", "Display Setting", ExtensionType.ACTION, Settings.ACTION_DISPLAY_SETTINGS),
        Extension("设置-无障碍", "Accessibility Setting", ExtensionType.ACTION, Settings.ACTION_ACCESSIBILITY_SETTINGS),
        Extension("设置-应用权限", "App Permission Setting", ExtensionType.ACTION, Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS),
    )

}
