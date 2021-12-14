package com.gevorg89.photowidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.InputStream

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