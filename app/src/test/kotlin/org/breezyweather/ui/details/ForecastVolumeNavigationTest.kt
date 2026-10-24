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

package org.breezyweather.ui.details

import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ForecastVolumeNavigationTest {
    @Test
    fun pagesWithinDayThenChangesDaysWithoutWrapping() = runTest {
        val list = mockk<LazyListState>()
        val layout = mockk<LazyListLayoutInfo>()
        val scroll = mockk<ScrollScope>()
        every { list.layoutInfo } returns layout
        every { layout.viewportStartOffset } returns 20
        every { layout.viewportEndOffset } returns 1020
        every { list.canScrollForward } returns true
        every { list.canScrollBackward } returns true
        every { scroll.scrollBy(any()) } answers { firstArg() }
        coEvery { list.scroll(any(), any()) } coAnswers {
            secondArg<suspend ScrollScope.() -> Unit>().invoke(scroll)
        }
        val days = mutableListOf<Int>()
        navigateForecastWithVolumeKey(true, list, 1, 3) { days.add(it) }
        navigateForecastWithVolumeKey(false, list, 1, 3) { days.add(it) }
        verifyOrder {
            scroll.scrollBy(800f)
            scroll.scrollBy(-800f)
        }
        days shouldBe emptyList()

        every { list.canScrollForward } returns false
        every { list.canScrollBackward } returns false
        navigateForecastWithVolumeKey(true, list, 0, 3) { days.add(it) }
        navigateForecastWithVolumeKey(false, list, 2, 3) { days.add(it) }
        days shouldBe listOf(1, 1)

        days.clear()
        navigateForecastWithVolumeKey(false, list, 0, 3) { days.add(it) }
        navigateForecastWithVolumeKey(true, list, 2, 3) { days.add(it) }
        navigateForecastWithVolumeKey(true, list, 0, 1) { days.add(it) }
        days shouldBe emptyList()
    }
}
