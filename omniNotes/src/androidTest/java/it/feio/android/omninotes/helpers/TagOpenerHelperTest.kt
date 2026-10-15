/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.feio.android.omninotes.helpers

import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.feio.android.omninotes.helpers.TagOpenerHelper.Companion.openOrGetIntent
import it.feio.android.omninotes.testutils.BaseAndroidTestCase
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagOpenerHelperTest : BaseAndroidTestCase() {

    @Before
    fun setUpIntents() {
        Intents.init()
        // External apps must never be really launched: stubbing keeps the tests hermetic and
        // avoids stealing the window focus from the tests running after these ones
        intending(hasAction(Intent.ACTION_SENDTO)).respondWith(ActivityResult(0, null))
        intending(hasAction(Intent.ACTION_DIAL)).respondWith(ActivityResult(0, null))
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(0, null))
    }

    @After
    fun tearDownIntents() {
        Intents.release()
    }

    @Test
    fun openOrGetIntent_mailto() =
        assertNull(openOrGetIntent(testContext, "mailto:random@address.com"))

    @Test
    fun openOrGetIntent_tel() = assertNull(openOrGetIntent(testContext, "tel:123456"))

    @Test
    fun openOrGetIntent_tag() =
        assertNotNull(openOrGetIntent(testContext, "hashtag:thisCouldBeATag"))

    @Test
    fun openOrGetIntent_other() = assertNull(openOrGetIntent(testContext, "https://somesite.eu"))

}
