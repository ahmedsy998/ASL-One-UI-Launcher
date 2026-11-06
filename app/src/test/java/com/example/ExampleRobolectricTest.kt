package com.example

import com.example.engine.WallpaperEngine
import com.example.model.BuiltInWidgetType
import com.example.model.GestureAction
import com.example.model.IconPackPreset
import com.example.model.IconShapeStyle
import com.example.model.LauncherSettings
import com.example.model.PageTransitionEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `verify one ui 9 wallpaper library and arabic categories`() {
        assertTrue(WallpaperEngine.builtInWallpapers.size >= 10)
        assertTrue(WallpaperEngine.categories.contains("سامسونج One UI"))
        assertTrue(WallpaperEngine.categories.contains("AMOLED"))
        val defaultWp = WallpaperEngine.findById("one_ui_9_signature")
        assertEquals("أمواج One UI 9 البنفسجية", defaultWp.titleAr)
    }

    @Test
    fun `verify built in widgets and customization presets`() {
        assertTrue(BuiltInWidgetType.entries.size >= 8)
        assertTrue(IconShapeStyle.entries.contains(IconShapeStyle.ONE_UI_SQUIRCLE))
        assertTrue(IconPackPreset.entries.contains(IconPackPreset.ONE_UI_9_DEFAULT))
        assertTrue(PageTransitionEffect.entries.contains(PageTransitionEffect.CAROUSEL_3D))

        val settings = LauncherSettings()
        assertEquals(4, settings.homeGridColumns)
        assertEquals(5, settings.homeGridRows)
        assertEquals(GestureAction.OPEN_APP_DRAWER, settings.swipeUpGesture)
        assertEquals("دمشق", settings.weatherCityAr)
    }
}
