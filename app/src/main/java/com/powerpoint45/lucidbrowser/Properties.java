package com.powerpoint45.lucidbrowser;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;


public class Properties extends MainActivity {
	public static int ActionbarSize=0; //used for getting the actual actionbar size + anny padding
	public static class appProp{
		public static int actionBarColor;
		public static int primaryIntColor;
		public static int urlBarColor;
		public static boolean fullscreen;
		public static boolean transparentNav;
		static boolean TransparentStatus;
		static boolean systemPersistent;
		public static boolean darkTheme;
	}
	public static class sidebarProp{
		static int SidebarIconSize;
		static int SidebarIconPadding;
		static int SidebarSize;
		static String theme;
		static boolean showLabel;
		static int sideBarColor;
		static int sideBarTextColor;
		public static boolean swapLayout;
	}
	public static class webpageProp{
		static boolean showBackdrop;
		public static boolean useDesktopView;
		static boolean disablesuggestions;
		static boolean clearonexit;
		static boolean closetabsonexit;
		public static boolean enableimages;
		static boolean exitconfirmation;
		//static boolean enablejavascript; //uncomment if wanted by users
		public static boolean enablecookies;
		public static int     fontSize;
		public static String engine; //search engine
		public static String assetHomePage = "file:///android_asset/ehome.html";
	}

	
	public static void update_preferences(MainActivity context){
		webpageProp.showBackdrop=MainActivity.globalPrefs.getBoolean("showbrowserbackdrop",true);
		webpageProp.useDesktopView=MainActivity.globalPrefs.getBoolean("usedesktopview",false);
		webpageProp.disablesuggestions=MainActivity.globalPrefs.getBoolean("disablesuggestions", false);
		webpageProp.clearonexit=MainActivity.globalPrefs.getBoolean("clearonexit",false);
		webpageProp.enableimages=MainActivity.globalPrefs.getBoolean("enableimages", true);
		//webpageProp.enablejavascript=MainActivity.globalPrefs.getBoolean("enablejavascript", true);
		//uncomment if wanted by users
		webpageProp.enablecookies=    MainActivity.globalPrefs.getBoolean("enablecookies"  ,true);
		webpageProp.fontSize     =    MainActivity.globalPrefs.getInt    ("webfontsize"    , 2);
		webpageProp.closetabsonexit = MainActivity.globalPrefs.getBoolean("closetabsonexit", false);
		webpageProp.exitconfirmation= MainActivity.globalPrefs.getBoolean("exitconfirmation", false);
		webpageProp.engine          = MainActivity.globalPrefs.getString("setsearchengine", "ec");

		if (webpageProp.engine.equals("ec")) {
			webpageProp.assetHomePage = "file:///android_asset/ehome.html";
			//I need to check if I can use my custom ecosia URL
			if (!webpageProp.engine.equals("https://www.ecosia.org/search?tt=lucid&q=")) {
				if (context.globalPrefs.getBoolean("useCustomEcosia",true))
					webpageProp.engine = "https://www.ecosia.org/search?tt=lucid&q=";
				else
					webpageProp.engine = "https://www.ecosia.org/search?q=";
				new GEOIPParser(context).new setEcosiaURL().start();
			}
		}else if (webpageProp.engine.equals("g")) {
			webpageProp.assetHomePage = "file:///android_asset/home.html";
			webpageProp.engine = "https://www.google.com/search?q=";
		}else if (webpageProp.engine.equals("y")) {
			webpageProp.engine = "https://search.yahoo.com/search?p=";
			webpageProp.assetHomePage = "file:///android_asset/yhome.html";
		}else if (webpageProp.engine.equals("b")) {
			webpageProp.engine = "https://www.bing.com/search?q=";
			webpageProp.assetHomePage = "file:///android_asset/bhome.html";
		}else if (webpageProp.engine.equals("d")) {
			webpageProp.engine = "https://www.duckduckgo.com/?q=";
			webpageProp.assetHomePage = "file:///android_asset/dhome.html";
		}else if (webpageProp.engine.equals("a")) {
			webpageProp.engine = "http://www.ask.com/web?q=";
			webpageProp.assetHomePage = "file:///android_asset/ahome.html";
		}else if (webpageProp.engine.equals("i")) {
			webpageProp.engine = "https://www.ixquick.com/do/search?q=";
			webpageProp.assetHomePage = "file:///android_asset/ihome.html";
		}else if (webpageProp.engine.equals("yd")) {
			webpageProp.engine = "https://www.yandex.com/search/?text=";
			webpageProp.assetHomePage = "file:///android_asset/ydhome.html";
		}else if (webpageProp.engine.equals("bd")) {
			webpageProp.engine = "https://www.baidu.com/s?wd=";
			webpageProp.assetHomePage = "file:///android_asset/bdhome.html";
		}else if (webpageProp.engine.equals("nv")) {
			webpageProp.engine = "https://search.naver.com/search.naver?query=";
			webpageProp.assetHomePage = "file:///android_asset/nvhome.html";
		}


		ActionbarSize= Tools.getActionBarSize(context);

		appProp.fullscreen=MainActivity.globalPrefs.getBoolean       ("fullscreen"           ,false);
		appProp.transparentNav=MainActivity.globalPrefs.getBoolean   ("transparentnav"       ,false);
		appProp.TransparentStatus=MainActivity.globalPrefs.getBoolean("transparentstatus"    ,true);
		appProp.systemPersistent=MainActivity.globalPrefs.getBoolean ("systempersistent"     ,false);
		appProp.darkTheme =MainActivity.globalPrefs.getBoolean         ("holodark"             ,false);
		appProp.primaryIntColor=MainActivity.globalPrefs.getInt      ("textcolor",Color.BLACK);
		appProp.actionBarColor=MainActivity.globalPrefs.getInt       ("actionbarcolor", context.getResources().getColor(R.color.urlback));
		appProp.urlBarColor=MainActivity.globalPrefs.getInt          ("urlbarcolor", context.getResources().getColor(R.color.urlfront));
		
		sidebarProp.SidebarIconSize=numtodp(MainActivity.globalPrefs.getInt    ("sidebariconsize"  ,80),context);
		sidebarProp.SidebarIconPadding=numtodp(MainActivity.globalPrefs.getInt ("sidebariconpadding",10),context);
		sidebarProp.theme=MainActivity.globalPrefs.getString                   ("sidebartheme", "w");
		sidebarProp.sideBarColor=MainActivity.globalPrefs.getInt               ("sidebarcolor"    , Color.BLACK);
        sidebarProp.sideBarTextColor=MainActivity.globalPrefs.getInt           ("sidebartextcolor", Color.WHITE);
		sidebarProp.showLabel=MainActivity.globalPrefs.getBoolean              ("showfavoriteslabels", true);
		sidebarProp.swapLayout     =MainActivity.globalPrefs.getBoolean        ("swapLayout"          ,false);
		if (sidebarProp.showLabel)
			sidebarProp.SidebarSize=numtodp(250,context);
		else
			sidebarProp.SidebarSize= sidebarProp.SidebarIconSize;
		
		float alpha= (sidebarProp.sideBarColor >> 24) & 0xFF;
		
		if (alpha>254f){
			sidebarProp.sideBarColor = SetupLayouts.addTransparencyToColor(254, sidebarProp.sideBarColor);
		}


		int id = context.getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus)
				if (id == 0) {//transparency is not supported
					Properties.appProp.transparentNav   =false;
					Properties.appProp.TransparentStatus=false;
				}
	}
	
	public static int numtodp(int in, Context context){
		int out =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, in, context.getResources().getDisplayMetrics());
		return out;
	}



	

}