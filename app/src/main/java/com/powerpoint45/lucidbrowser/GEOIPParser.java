package com.powerpoint45.lucidbrowser;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import views.CustomWebView;

class GEOIPParser {

    static InputStream is = null;
    private static JSONObject jObj = null;
    private static String json = "";
    MainActivity activity;

    // constructor
    GEOIPParser(MainActivity activity) {
        this.activity = activity;
    }

    // function get json from url
    // by making HTTP POST or GET method
    private JSONObject makeHttpRequest(String urlString) throws IOException {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }

            json = buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }


    //this detects the user's countryCode so that
    //I can check if I can use my custom ecosia url string.
    class setEcosiaURL extends Thread{

        @Override
        public void run() {
            super.run();
            try {
                JSONObject obj = makeHttpRequest("http://ip-api.com/json/");
                if (obj!=null) {
                    String countryCode = obj.getString("countryCode");
                    String countryCodes = activity.getResources().getString(R.string.country_codes);

                    if (countryCodes.contains(countryCode)) {
                        updateData();
                    } else {
                        Properties.webpageProp.engine = "https://www.ecosia.org/search?q=";
                        MainActivity.globalPrefs.edit().putBoolean("useCustomEcosia", false).apply();
                    }
                }else {
                    if (MainActivity.globalPrefs.getBoolean("useCustomEcosia",false))
                        updateData();
                }


            } catch (IOException e) {
                e.printStackTrace();
                if (MainActivity.globalPrefs.getBoolean("useCustomEcosia",false))
                    updateData();
            } catch (JSONException e){
                e.printStackTrace();
                if (MainActivity.globalPrefs.getBoolean("useCustomEcosia",false))
                    updateData();
            }
        }

        void updateData(){
            Properties.webpageProp.engine = "https://www.ecosia.org/search?tt=lucid&q=";
            MainActivity.globalPrefs.edit().putBoolean("useCustomEcosia", true).apply();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (activity.webWindows!=null && activity.webLayout!=null) {
                        for (CustomWebView wv : activity.webWindows) {
                            if (wv.getUrl() != null && wv.getUrl().contains("file:///android_asset/ehome.html")) {
                                String js = "javascript:(function() { ";
                                js += "document.getElementById('add').name = 'tt';";
                                js += "document.getElementById('add').value = 'lucid';";
                                js += "})()";

                                wv.loadUrl(js);
                            }
                        }
                    }
                }
            });
        }
    }
}