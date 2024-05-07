package com.kuss.krude.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.kuss.krude.R
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


object SponsorHelper {
    const val ALIPAY_SPONSOR_URI = "alipays://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/fkx05853auiq7gkm8jbmccb"

    @JvmStatic
    fun saveWechatSponsor(context: Context) {
        val dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val dcimPath = dcimDirectory?.path
        val destPath = "$dcimPath/Screenshots/rewind-reward-thanks.png"
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.wechat_reward)
        saveBitmapToFile(context, bitmap, "rewind-reward-thanks")
        MediaScannerConnection.scanFile(
            context,
            arrayOf(destPath), arrayOf("images/*")
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, name: String) {
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.png")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Screenshots")
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            Timber.i("saveBitmapToFile: $imageUri")
            fos = resolver.openOutputStream(imageUri!!)
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString()
            val image = File(imagesDir, "Screenshots/$name.png")
            Timber.i("saveBitmapToFile: $image")
            fos = FileOutputStream(image)
        }
        if (fos != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        }
    }
}