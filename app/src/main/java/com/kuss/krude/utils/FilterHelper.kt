package com.kuss.krude.utils

import com.github.promeg.pinyinhelper.Pinyin
import com.kuss.krude.data.AppInfo
import java.text.Collator
import java.util.*

object FilterHelper {
    @JvmStatic
    private fun toPinyinWithAbbr(label: String): String {
        val pinyin = Pinyin.toPinyin(label, "")
        val isChinese = pinyin != label
        var abbr = if (isChinese) {
            toAbbr(Pinyin.toPinyin(label, ","), ",")
        } else {
            toAbbr(label)
        }
        if (abbr.contains(" ")) {
            abbr += toAbbr(abbr)
        }
        return "$pinyin, $abbr"
    }

    @JvmStatic
    private fun toAbbr(str: String, delimiter: String = " "): String {
        return str.split(delimiter).joinToString("") {
            it.substring(0, 1)
        }
    }

    @JvmStatic
    fun toTarget(label: String, packageName: String): String {
        return "$label, $packageName, ${toPinyinWithAbbr(label)}"
    }

    @JvmStatic
    fun getFiltered(search: String, apps: List<AppInfo>): List<AppInfo> {
        return apps.filter { app ->
            app.filterTarget.toLowerCase(Locale.ROOT)
                .contains(search.toLowerCase(Locale.ROOT))
        }
    }

    @JvmStatic
    fun getSorted(apps: List<AppInfo>): List<AppInfo> {
        return apps.sortedWith() { s1, s2 ->
            Collator.getInstance(Locale.CHINESE).compare(s1.label, s2.label)
        }
    }
}
