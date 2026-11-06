package com.example

import com.example.data.db.AppOverrideEntity
import com.example.data.db.HomeItemEntity
import com.example.model.HomeItemType
import com.example.model.LauncherSettings
import com.example.model.ThemeMode
import com.example.viewmodel.LauncherFullBackupPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify full launcher backup and restore json serialization`() {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(LauncherFullBackupPayload::class.java)

        val customSettings = LauncherSettings(
            homeGridColumns = 5,
            homeGridRows = 6,
            themeMode = ThemeMode.AMOLED_BLACK,
            weatherCityAr = "الرياض"
        )
        val items = listOf(
            HomeItemEntity(
                id = 1,
                page = 0,
                row = 3,
                col = 0,
                itemType = HomeItemType.APP.name,
                packageName = "oneui.shortcut.gallery",
                title = "الاستوديو"
            )
        )
        val overrides = listOf(
            AppOverrideEntity(
                packageName = "oneui.shortcut.gallery",
                customLabel = "معرض الصور"
            )
        )

        val payload = LauncherFullBackupPayload(
            settings = customSettings,
            homeItems = items,
            appOverrides = overrides
        )

        val json = adapter.toJson(payload)
        val restored = adapter.fromJson(json)

        assertNotNull(restored)
        assertEquals(5, restored?.settings?.homeGridColumns)
        assertEquals(6, restored?.settings?.homeGridRows)
        assertEquals(ThemeMode.AMOLED_BLACK, restored?.settings?.themeMode)
        assertEquals("الرياض", restored?.settings?.weatherCityAr)
        assertEquals("الاستوديو", restored?.homeItems?.firstOrNull()?.title)
        assertEquals("معرض الصور", restored?.appOverrides?.firstOrNull()?.customLabel)
    }
}
