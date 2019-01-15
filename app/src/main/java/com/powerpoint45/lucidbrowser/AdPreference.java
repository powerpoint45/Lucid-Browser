package com.powerpoint45.lucidbrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Calendar;

public class AdPreference extends Object {

	//your ad id goes here
	//ca-app-pub-XXXXXXXXXXXXXXXXXX/XXXXXXXXXX
	final String AD_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXXXX/XXXXXXXXXX";
	InterstitialAd interstitial;
	SharedPreferences globalPref;
	Context context;
	
	public AdPreference(SharedPreferences p, Context ctxt){
		globalPref = p;
		context = ctxt;
	}
    
    public void setUpAd() {
    	Calendar c = Calendar.getInstance(); 
		int day = c.get(Calendar.DATE);
		int lastTimeShownAd = globalPref.getInt("adDisplayDate", -1);
		
		Log.d("Ads", "today is "+ day);
		Log.d("Ads", "and last time I showed an ad was on "+ globalPref.getInt("adDisplayDate", -1));
		if (lastTimeShownAd!=day)
			Log.d("Ads", "so I will start loading up the ad");
		else
			Log.d("Ads", "so I will not load the ad");
			
		if (lastTimeShownAd!=day){
			interstitial = new InterstitialAd(context);
			interstitial.setAdUnitId(AD_UNIT_ID);
			 
			AdRequest adRequest = new AdRequest.Builder()
	        		.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice("0F46937400609363476E493E7CC11512")
	        		.build();
			interstitial.loadAd(adRequest);
			
			interstitial.setAdListener(new AdListener() {

				@Override
				public void onAdLoaded() {
					// TODO Auto-generated method stub
					super.onAdLoaded();
					
					displayInterstitial();
					Calendar c = Calendar.getInstance(); 
					int day = c.get(Calendar.DATE);
					globalPref.edit().putInt("adDisplayDate", day).commit();
				}
				
			});
		}
 
    }
    
    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
          interstitial.show();
        }
      }
} 