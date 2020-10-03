package com.kuss.krude.utils

import com.github.promeg.pinyinhelper.Pinyin

class FilterHelper {
    companion object {
        private fun toPinyinWithAbbr(label: String): String {
            val pinyin = Pinyin.toPinyin(label, "")
            val isChinese = pinyin != label
            val abbr = if (isChinese) {
                Pinyin.toPinyin(label, ",")
                    .split(",")
                    .joinToString("") {
                        it.substring(0, 1)
                    }
            } else {
                label.split(" ").joinToString("") {
                    it.substring(0, 1)
                }
            }
            return "$pinyin, $abbr"
        }

        fun toTarget(label: String, packageName: String): String {
            return "$label, $packageName, ${toPinyinWithAbbr(label)}"
        }
    }
}
