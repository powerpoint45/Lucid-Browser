package com.powerpoint45.lucidbrowser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by michael on 27/01/17.
 */

public class BundleManager {
    MainActivity activity;

    public BundleManager(MainActivity activity){
        this.activity = activity;
    }

    public void saveToPreferences(Bundle in) {
        Parcel parcel = Parcel.obtain();
        String serialized = null;
        try {
            in.writeToParcel(parcel, 0);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //IOUtils.write(parcel.marshall(), bos);
            bos.write(parcel.marshall(),0,parcel.marshall().length);

            serialized = Base64.encodeToString(bos.toByteArray(), 0);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.toString(), e);
        } finally {
            parcel.recycle();
        }
        if (serialized != null) {
            SharedPreferences settings = activity.getSharedPreferences("instance", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("parcel", serialized);
            editor.commit();
        }
    }

    public Bundle restoreFromPreferences() {
        Bundle bundle = null;
        SharedPreferences settings = activity.getSharedPreferences("instance", 0);
        String serialized = settings.getString("parcel", null);

        if (serialized != null) {
            Parcel parcel = Parcel.obtain();
            try {
                byte[] data = Base64.decode(serialized, 0);
                parcel.unmarshall(data, 0, data.length);
                parcel.setDataPosition(0);
                bundle = parcel.readBundle();
            } finally {
                parcel.recycle();
            }
        }
        return bundle;
    }

}
