package com.kuss.krude.utils

import com.github.promeg.pinyinhelper.Pinyin

class FilterHelper {
    companion object {
        private fun toAbbr(full: String): String {
            val isChinese = Pinyin.toPinyin(full, "") != full
            if (isChinese) {
                val pinyin = Pinyin.toPinyin(full, ",")
                return pinyin.split(",").joinToString("") {
                    it.substring(0, 1)
                }
            }
            return full.split(" ").joinToString("") {
                it.substring(0, 1)
            }
        }

        fun toTarget(label: String, packageName: String): String {
            return "$label, $packageName, ${toAbbr(label)}"
        }
    }
}