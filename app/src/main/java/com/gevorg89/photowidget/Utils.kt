package com.gevorg89.photowidget

import android.R.attr
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import android.R.attr.bitmap




fun String.file(context: Context): File {
    return File(context.cacheDir(), this)
}

fun Uri.toFileFromUri(context: Context, name: String): File {
    val file = File(context.cacheDir(), name)
    val content = context.contentResolver
    val inputStream = content.openInputStream(this)
    file.copyInputStreamToFile(inputStream!!)
    return file
}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
}

fun Context.updateWidget() {
    val widgetUpdateIntent = Intent(this, PhotoWidget::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(
            AppWidgetManager.EXTRA_APPWIDGET_IDS,
            AppWidgetManager.getInstance(this@updateWidget).getAppWidgetIds(
                ComponentName(
                    this@updateWidget,
                    PhotoWidget::class.java
                )
            )
        )
    }
    sendBroadcast(widgetUpdateIntent)
}

fun Context.cacheDir(): String {
    return externalCacheDir!!.absolutePath
}

fun File.getBitmap(): Bitmap {
    return BitmapFactory.decodeFile(absolutePath)
}

fun compressImage(image: Bitmap, compressSize: Int): Bitmap? {
    /*val baos = ByteArrayOutputStream()
    image.compress(
        Bitmap.CompressFormat.JPEG,
        100,
        baos
    ) //Compression quality, here 100 means no compression, the storage of compressed data to baos
    var options = 90
    while (baos.toByteArray().size / 1024 > compressSize) {  //Loop if compressed picture is greater than 400kb, than to compression
        Log.d("compressImage","${baos.toByteArray().size}")
        baos.reset() //Reset baos is empty baos
        image.compress(
            Bitmap.CompressFormat.JPEG,
            options,
            baos
        ) //The compression options%, storing the compressed data to the baos
        options -= 10 //Every time reduced by 10
    }
    val isBm =
        ByteArrayInputStream(baos.toByteArray()) //The storage of compressed data in the baos to ByteArrayInputStream
    val t = BitmapFactory.decodeStream(isBm, null, null)
    Log.d("compressImage final","${t?.byteCount}")
    return t*/

    var stream: ByteArrayOutputStream= ByteArrayOutputStream()
    var currSize: Int
    var currQuality = 100
    var bmp2: Bitmap = image.copy(image.config, true)
    do {
        bmp2 = image.copy(image.config, true)
        stream.reset()
        bmp2.compress(Bitmap.CompressFormat.JPEG, currQuality, stream)
        currSize = stream.toByteArray().size
        // limit quality by 5 percent every time
        currQuality -= 5
    } while (currSize >= compressSize)
    val t =  BitmapFactory.decodeByteArray(stream.toByteArray(),0,stream.toByteArray().size)
    return t
}

fun File.writeBitmap(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    quality: Int
) {
    if (!this.exists()) {
        this.createNewFile()
    }
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}