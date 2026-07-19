package com.powerpoint45.lucidbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Arrays;

public class AdPreference extends Object {

	//your ad id goes here
	//ca-app-pub-XXXXXXXXXXXXXXXXXX/XXXXXXXXXX
	final String AD_UNIT_ID = "ca-app-pub-5849487494074701/2903707073";
	private InterstitialAd mInterstitialAd;
	public boolean isShowingAd = false;
	SharedPreferences globalPref;
	Context context;
	long lastAdShown = -1;
	
	public AdPreference(SharedPreferences p, Context ctxt){
		globalPref = p;
		context = ctxt;
	}

	public void resumeCalled(){
		if (isShowingAd){
			isShowingAd = false;
		}
		if (hasAdAlreadyShown())
			newAd(false);
	}
    
    public void setUpAd() {
		MobileAds.initialize(context, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {
				newAd(false);
			}
		});
    }

	public void newAd(boolean show){
		new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("7D3D370F981E619A39047CC3FD3B5108")).build();
		AdRequest adRequest = new AdRequest.Builder().build();
		InterstitialAd.load(context,AD_UNIT_ID, adRequest,
				new InterstitialAdLoadCallback() {
					@Override
					public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
						// The mInterstitialAd reference will be null until
						// an ad is loaded.
						mInterstitialAd = interstitialAd;
						if (show) {
							mInterstitialAd.show((Activity) context);
							lastAdShown = mInterstitialAd.hashCode();
							isShowingAd = true;
						}
						Log.i("mazeg", "onAdLoaded");
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						// Handle the error
						Log.i("mazeg", loadAdError.getMessage());
						mInterstitialAd = null;
					}
				});
	}

	public boolean hasAdAlreadyShown(){
		if (lastAdShown == -1 || mInterstitialAd == null)
			return false;

		return  lastAdShown == mInterstitialAd.hashCode();
	}

	public void showAd(){
		if (mInterstitialAd!=null) {
			isShowingAd = true;
			mInterstitialAd.show((Activity) context);
			lastAdShown = mInterstitialAd.hashCode();
		}else {
			newAd(true);
		}
	}
} 