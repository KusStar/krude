package com.kuss.krude.utils

import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType

object Extensions {
    const val WECHAT_SCAN_SCHEME = "krude://openuri/wechat_scan"
    val EMBEDDED_EXTENSIONS = listOf(
        Extension("支付宝-扫一扫", "Alipay QR Scan", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=10000007"),
        Extension("支付宝-收付款", "Alipay Pay/Collect", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=20000056"),
        Extension("支付宝-出行", "Alipay Transport", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=200011235"),
        Extension("支付宝-菜鸟", "Alipay Cai Niao", ExtensionType.SCHEME, "alipayqr://platformapi/startapp?saId=2021001141626787"),
        Extension("微信-扫一扫", "Wechat QR Scan", ExtensionType.SCHEME, WECHAT_SCAN_SCHEME)
    )
}
