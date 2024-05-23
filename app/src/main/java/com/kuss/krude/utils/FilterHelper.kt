package com.kuss.krude.utils

import com.github.promeg.pinyinhelper.Pinyin
import com.kuss.krude.db.AppInfo
import com.kuss.krude.interfaces.Extension
import java.text.Collator
import java.util.Locale

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
    fun getAbbr(label: String): String {
        val pinyin = Pinyin.toPinyin(label, "")
        val isChinese = pinyin != label
        return if (isChinese) {
            toAbbr(Pinyin.toPinyin(label, ","), ",")
        } else {
            toAbbr(label)
        }
    }

    @JvmStatic
    fun toTarget(label: String, packageName: String): String {
        return "$label, $packageName, ${toPinyinWithAbbr(label)}"
    }

    @JvmStatic
    fun toTarget(extension: Extension): String {
        val keywordString =
            if (extension.keywords != null) {
                val keywordsPlain = extension.keywords!!.joinToString(",")
                val keywordsPinyin = extension.keywords!!.joinToString(",") {
                    toPinyinWithAbbr(it)
                }

                ",${keywordsPlain},${keywordsPinyin}}"
            } else {
                ""
            }
        return "${extension.name},${extension.description},${toPinyinWithAbbr(extension.name)}$keywordString"
    }

    @JvmStatic
    fun getFiltered(search: String, apps: List<AppInfo>): List<AppInfo> {
        return apps.filter { app ->
            app.filterTarget.lowercase(Locale.ROOT)
                .contains(search.lowercase(Locale.ROOT))
        }
    }

    @JvmStatic
    fun getSorted(apps: List<AppInfo>): List<AppInfo> {
        return apps.sortedWith { s1, s2 ->
            Collator.getInstance(Locale.CHINESE).compare(s1.abbr, s2.abbr)
        }
    }
}
