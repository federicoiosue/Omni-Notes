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

package it.feio.android.omninotes.async.upgrade;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import roboguice.util.Ln;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Processor used to perform asynchronous tasks on database upgrade.
 * It's not intended to be used to perform actions strictly related to DB (for this
 * {@link it.feio.android.omninotes.db.DbHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)}
 * DbHelper.onUpgrade()} is used
 */
public class UpgradeProcessor {

    private final static String METHODS_PREFIX = "onUpgradeTo";
    private static Class classObject;

    private static UpgradeProcessor instance;


    private UpgradeProcessor() {
    }


    private static UpgradeProcessor getInstance() {
        if (instance == null) {
            instance = new UpgradeProcessor();
        }
        return instance;
    }


    public static void process(int dbOldVersion, int dbNewVersion) {
        try {
            List<Method> methodsToLaunch = getInstance().getMethodsToLaunch(dbOldVersion, dbNewVersion);
            for (Method methodToLaunch : methodsToLaunch) {
                methodToLaunch.invoke(getInstance());
            }
        } catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
            Ln.d("Explosion processing upgrade!", e);
        }
    }


    private List<Method> getMethodsToLaunch(int dbOldVersion, int dbNewVersion) {
        List<Method> methodsToLaunch = new ArrayList<>();
        classObject = getInstance().getClass();
        Method[] declaredMethods = classObject.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().contains(METHODS_PREFIX)) {
                int methodVersionPostfix = Integer.parseInt(declaredMethod.getName().replace(METHODS_PREFIX, ""));
                if (dbOldVersion <= methodVersionPostfix && methodVersionPostfix <= dbNewVersion) {
                    methodsToLaunch.add(declaredMethod);
                }
            }
        }
        return methodsToLaunch;
    }


    /**
     * Adjustment of all the old attachments without mimetype field set into DB
     */
    private void onUpgradeTo476() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                final DbHelper dbHelper = DbHelper.getInstance(OmniNotes.getAppContext());
                for (Attachment attachment : dbHelper.getAllAttachments()) {
                    if (attachment.getMime_type() == null) {
                        String mimeType = StorageHelper.getMimeType(attachment.getUri().toString());
                        if (!TextUtils.isEmpty(mimeType)) {
                            String type = mimeType.replaceFirst("/.*", "");
                            switch (type) {
                                case "image":
                                    attachment.setMime_type(Constants.MIME_TYPE_IMAGE);
                                    break;
                                case "video":
                                    attachment.setMime_type(Constants.MIME_TYPE_VIDEO);
                                    break;
                                case "audio":
                                    attachment.setMime_type(Constants.MIME_TYPE_AUDIO);
                                    break;
                                default:
                                    attachment.setMime_type(Constants.MIME_TYPE_FILES);
                                    break;
                            }
                            dbHelper.updateAttachment(attachment);
                        } else {
                            attachment.setMime_type(Constants.MIME_TYPE_FILES);
                        }
                    }
                }
                return null;
            }
        }.execute();
    }


    /**
     * Upgrades all the old audio attachments to the new format 3gpp to avoid to exchange them for videos
     */
    private void onUpgradeTo477() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                final DbHelper dbHelper = DbHelper.getInstance(OmniNotes.getAppContext());
                for (Attachment attachment : dbHelper.getAllAttachments()) {
                    if (attachment.getMime_type().equals("audio/3gp")) {

                        // File renaming
                        File from = new File(attachment.getUriPath());
                        File to = new File(from.getParent(), from.getName().replace(".3gp",
                                Constants.MIME_TYPE_AUDIO_EXT));
                        from.renameTo(to);

                        // Note's attachment update
                        attachment.setUri(Uri.fromFile(to));
                        attachment.setMime_type(Constants.MIME_TYPE_AUDIO);
                        dbHelper.updateAttachment(attachment);
                    }
                }
                return null;
            }
        }.execute();
    }

}
