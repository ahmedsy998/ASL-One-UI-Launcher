package com.example.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

data class SystemWidgetProviderItem(
    val providerInfo: AppWidgetProviderInfo,
    val appLabel: String,
    val widgetLabel: String,
    val minCellsX: Int,
    val minCellsY: Int
)

class LauncherWidgetHost(context: Context) {

    companion object {
        const val HOST_ID = 1024
    }

    private val appContext = context.applicationContext
    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(appContext)
    val appWidgetHost: AppWidgetHost = AppWidgetHost(appContext, HOST_ID)

    fun startListening() {
        runCatching { appWidgetHost.startListening() }
    }

    fun stopListening() {
        runCatching { appWidgetHost.stopListening() }
    }

    fun allocateWidgetId(): Int {
        return runCatching { appWidgetHost.allocateAppWidgetId() }.getOrDefault(-1)
    }

    fun deleteWidgetId(appWidgetId: Int) {
        if (appWidgetId != -1) {
            runCatching { appWidgetHost.deleteAppWidgetId(appWidgetId) }
        }
    }

    fun createHostView(context: Context, appWidgetId: Int): AppWidgetHostView? {
        return runCatching {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return null
            appWidgetHost.createView(context, appWidgetId, info).apply {
                setAppWidget(appWidgetId, info)
            }
        }.getOrNull()
    }

    fun loadInstalledSystemWidgets(): List<SystemWidgetProviderItem> {
        val pm = appContext.packageManager
        val providers = runCatching { appWidgetManager.installedProviders }.getOrDefault(emptyList())
        return providers.mapNotNull { info ->
            runCatching {
                val widgetLabel = info.loadLabel(pm) ?: info.provider.shortClassName
                val appInfo = pm.getApplicationInfo(info.provider.packageName, 0)
                val appLabel = pm.getApplicationLabel(appInfo).toString()
                val cellsX = ((info.minWidth + 50) / 70).coerceIn(1, 4)
                val cellsY = ((info.minHeight + 50) / 70).coerceIn(1, 4)
                SystemWidgetProviderItem(
                    providerInfo = info,
                    appLabel = appLabel,
                    widgetLabel = widgetLabel,
                    minCellsX = cellsX,
                    minCellsY = cellsY
                )
            }.getOrNull()
        }
    }
}
