package com.kuss.krude.utils

import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.pinyinhelper.PinyinMapDict

class PinyinHelper {
    companion object {
        fun initDict() {
            Pinyin.init(
                Pinyin.newConfig()
                    .with(object : PinyinMapDict() {
                        override fun mapping(): Map<String, Array<String>> {
                            val map = HashMap<String, Array<String>>()
                            map["音乐"] = arrayOf("YIN", "YUE")
                            return map
                        }
                    })
            )
        }
    }
}