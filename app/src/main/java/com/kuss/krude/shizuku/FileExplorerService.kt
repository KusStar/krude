package com.kuss.krude.shizuku

import android.R.attr
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import com.kuss.krude.shizuku.bean.BeanFile
import timber.log.Timber
import java.io.File
import java.io.IOException


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

    override fun openPfd(path: String): ParcelFileDescriptor? {
        try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                Timber.e("File does not exist or is not a file: " + attr.path)
                return null
            }
        } catch (e: IOException) {
            Timber.e("Error opening file: " + attr.path, e)
            return null
        }
    }
}
