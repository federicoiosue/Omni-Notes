/*
 * Copyright (C) 2013-2025 Federico Iosue (developer@omninotes.app)
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

import it.feio.android.omninotes.helpers.location.LocationProviderFactory
import it.feio.android.omninotes.testutils.BaseAndroidTestCase
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocationProviderFactoryTest : BaseAndroidTestCase() {

    @Test
    @Throws(Exception::class)
    fun checkUtilityClassWellDefined() {
        assertUtilityClassWellDefined(LocationProviderFactory::class.java, true, true)
    }

    @Test
    fun provider() {
        assertNotNull(LocationProviderFactory.getProvider())
    }

}