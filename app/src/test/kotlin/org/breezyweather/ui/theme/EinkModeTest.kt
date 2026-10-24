/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.breezyweather.R
import org.breezyweather.common.options.DarkMode
import org.breezyweather.common.options.NotificationTextColor
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter.Companion.getWidgetConfig
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter.WidgetColor
import org.breezyweather.ui.theme.weatherView.WeatherView
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate
import org.junit.jupiter.api.Test

class EinkModeTest {
    @Test
    fun einkOverridesNightAndWidgetAppearance() {
        val context = mockk<Context>()
        val settings = mockk<SettingsManager>()
        mockkObject(SettingsManager.Companion)
        try {
            every { SettingsManager.getInstance(context) } returns settings
            every { settings.isEinkMode } returns true
            for (mode in DarkMode.entries) {
                every { settings.darkMode } returns mode
                for (daylight in listOf(true, false, null)) {
                    ThemeManager.isLightTheme(context, daylight) shouldBe true
                }
            }
            for (style in listOf("none", "auto", "light", "dark")) {
                val colors = WidgetColor(context, style, "light", false)
                colors.showCard shouldBe true
                colors.isLightThemed shouldBe true
                colors.textColor shouldBe Color.BLACK
                colors.minimalIconColor shouldBe NotificationTextColor.DARK
            }
            val delegate = MaterialWeatherThemeDelegate { true }
            delegate.isLightBackground(context, WeatherView.WEATHER_KIND_CLEAR, false) shouldBe true
            delegate.getBackgroundColor(context, WeatherView.WEATHER_KIND_CLEAR, false) shouldBe Color.WHITE
            delegate.getOnBackgroundColor(context, WeatherView.WEATHER_KIND_CLEAR, false) shouldBe Color.BLACK
            delegate.getThemeColors(context, WeatherView.WEATHER_KIND_CLEAR, false).all {
                (it ushr 24) == 255
            } shouldBe true

            val preferences = mockk<SharedPreferences>()
            every { context.getSharedPreferences(any(), any()) } returns preferences
            every { context.getString(any()) } answers { firstArg<Int>().toString() }
            every { preferences.getString(any(), any()) } answers {
                if (firstArg<String>() == R.string.key_card_style.toString()) "dark" else secondArg()
            }
            every { preferences.getInt(any(), any()) } returns 10
            every { preferences.getBoolean(any(), any()) } returns false
            getWidgetConfig(context, "widget").apply {
                cardStyle shouldBe "light"
                cardAlpha shouldBe 100
                textColor shouldBe "dark"
            }
            verify(exactly = 0) { preferences.edit() }

            every { settings.isEinkMode } returns false
            getWidgetConfig(context, "widget").apply {
                cardStyle shouldBe "dark"
                cardAlpha shouldBe 10
                textColor shouldBe "light"
            }
            every { settings.darkMode } returns DarkMode.LIGHT
            ThemeManager.isLightTheme(context, daylight = false) shouldBe false
            ThemeManager.isLightTheme(context, daylight = true) shouldBe true
        } finally {
            unmockkObject(SettingsManager.Companion)
        }
    }
}
