package com.gevorg89.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import java.io.File

/**
 * Implementation of App Widget functionality.
 */
class PhotoWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

private val separator = File.separator

private fun cachePath(context: Context, widgetId: Int) =
    "${context.cacheDir.path}${separator}compressor$separator$widgetId"

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.photo_widget)
    val file = File(cachePath(context, appWidgetId))
    val addButton = R.id.appwidget_add
    if (file.exists()) {
        views.setViewVisibility(addButton, View.GONE)
        views.setViewVisibility(R.id.appwidget_progress, View.VISIBLE)
        val widgetBitmap = file.getBitmap()
        views.setImageViewBitmap(R.id.appwidget_img, widgetBitmap)
        views.setViewVisibility(R.id.appwidget_progress, View.GONE)
    } else {
        views.setViewVisibility(addButton, View.VISIBLE)
        val configIntent = Intent(context, MainActivity::class.java)
        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
        views.setOnClickPendingIntent(addButton, configPendingIntent)
    }
    appWidgetManager.updateAppWidget(appWidgetId, views)
}