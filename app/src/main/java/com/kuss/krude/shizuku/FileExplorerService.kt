package com.kuss.krude.shizuku

import android.os.RemoteException
import com.kuss.krude.shizuku.bean.BeanFile
import java.io.File

// thanks: https://github.com/MagicianGuo/Android-FileExplorerDemo/tree/a9b0d1771f9e896c8cabe95819c78a577cd6e662?tab=readme-ov-file#6android-14
class FileExplorerService : IFileExplorerService.Stub() {
    @Throws(RemoteException::class)
    override fun listFiles(path: String): List<BeanFile> {
        val list: MutableList<BeanFile> = ArrayList()
        val files = File(path).listFiles()
        if (files != null) {
            for (f in files) {
                list.add(BeanFile(f))
            }
        }
        return list
    }
}
