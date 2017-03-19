package com.powerpoint45.lucidbrowser;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import views.CustomWebView;

public class GEOIPParser {

    static InputStream is = null;
    private static JSONObject jObj = null;
    private static String json = "";
    MainActivity activity;

    // constructor
    public GEOIPParser(MainActivity activity) {
        this.activity = activity;
    }

    // function get json from url
    // by making HTTP POST or GET method
    public JSONObject makeHttpRequest(String url) throws IOException {

        // Making HTTP request
        try {
            // request method is GET
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
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
                JSONObject obj = makeHttpRequest("http://freegeoip.net/json/");
                if (obj!=null) {
                    String countryCode = obj.getString("country_code");
                    String countryCodes = activity.getResources().getString(R.string.country_codes);

                    if (countryCodes.contains(countryCode)) {
                        updateData();
                    } else {
                        Properties.webpageProp.engine = "https://www.ecosia.org/search?q=";
                        MainActivity.mGlobalPrefs.edit().putBoolean("useCustomEcosia", false).apply();
                    }
                }else {
                    if (MainActivity.mGlobalPrefs.getBoolean("useCustomEcosia",false))
                        updateData();
                }


            } catch (IOException e) {
                e.printStackTrace();
                if (MainActivity.mGlobalPrefs.getBoolean("useCustomEcosia",false))
                    updateData();
            } catch (JSONException e){
                e.printStackTrace();
                if (MainActivity.mGlobalPrefs.getBoolean("useCustomEcosia",false))
                    updateData();
            }
        }

        public void updateData(){
            Properties.webpageProp.engine = "https://www.ecosia.org/search?tt=lucid&q=";
            MainActivity.mGlobalPrefs.edit().putBoolean("useCustomEcosia", true).apply();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (CustomWebView wv : MainActivity.webWindows) {
                        if (wv.getUrl()!=null && wv.getUrl().contains("file:///android_asset/ehome.html")) {
                            String js = "javascript:(function() { ";
                            js += "document.getElementById('add').name = 'tt';";
                            js += "document.getElementById('add').value = 'lucid';";
                            js+="})()";

                            wv.loadUrl(js);
                        }
                    }
                }
            });
        }
    }
}