package it.feio.android.omninotes.factory;

import android.provider.MediaStore;

import android.net.Uri;

/**
 * Created by Relf on 11/24/2015.
 */
public class MediaStoreFactory {
    public Uri createURI(String type){
        switch (type) {
            case "image":
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            case "video":
                return  MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case "audio":
                return  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        return null;
    }
}
