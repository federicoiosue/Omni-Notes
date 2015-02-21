/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;


public class IntentChecker {

    /**
     * Checks intent and features availability
     *
     * @param ctx
     * @param intent
     * @param features
     * @return
     */
    public static boolean isAvailable(Context ctx, Intent intent, String[] features) {
        boolean res = true;
        final PackageManager mgr = ctx.getPackageManager();
        // Intent resolver
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        res = list.size() > 0;
        // Features
        if (features != null) {
            for (String feature : features) {
                res = res && mgr.hasSystemFeature(feature);
            }
        }
        return res;
    }
}
