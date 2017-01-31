package com.powerpoint45.lucidbrowser;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;


public class Properties extends MainActivity {
	public static int ActionbarSize=0; //used for getting the actual actionbar size + anny padding
	public static class appProp{
		static int actionBarColor;
		public static int primaryIntColor;
		static int urlBarColor;
		static boolean fullscreen;
		public static boolean transparentNav;
		static boolean TransparentStatus;
		static boolean systemPersistent;
		static boolean holoDark;
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
		webpageProp.showBackdrop= MainActivity.mGlobalPrefs.getBoolean("showbrowserbackdrop",true);
		webpageProp.useDesktopView= MainActivity.mGlobalPrefs.getBoolean("usedesktopview",false);
		webpageProp.disablesuggestions= MainActivity.mGlobalPrefs.getBoolean("disablesuggestions", false);
		webpageProp.clearonexit= MainActivity.mGlobalPrefs.getBoolean("clearonexit",false);
		webpageProp.enableimages= MainActivity.mGlobalPrefs.getBoolean("enableimages", true);
		//webpageProp.enablejavascript=MainActivity.mGlobalPrefs.getBoolean("enablejavascript", true);
		//uncomment if wanted by users
		webpageProp.enablecookies=    MainActivity.mGlobalPrefs.getBoolean("enablecookies"  ,true);
		webpageProp.fontSize     =    MainActivity.mGlobalPrefs.getInt    ("webfontsize"    , 2);
		webpageProp.closetabsonexit = MainActivity.mGlobalPrefs.getBoolean("closetabsonexit", false);
		webpageProp.exitconfirmation= MainActivity.mGlobalPrefs.getBoolean("exitconfirmation", false);
		webpageProp.engine          = MainActivity.mGlobalPrefs.getString("setsearchengine", "ec");

		if (webpageProp.engine.equals("ec")) {

            webpageProp.assetHomePage = "file:///android_asset/ehome.html";
			webpageProp.engine = "https://www.ecosia.org/search?q=";
		}else if (webpageProp.engine.equals("g")) {
            webpageProp.assetHomePage = "file:///android_asset/home.html";
            webpageProp.engine = "https://www.google.com/search?q=";
        }else if (webpageProp.engine.equals("y"))
			webpageProp.engine = "https://www.search.yahoo.com/search?q=";
		else if (webpageProp.engine.equals("b"))
			webpageProp.engine = "https://www.bing.com/search?q=";
		else if (webpageProp.engine.equals("d"))
			webpageProp.engine = "https://www.duckduckgo.com/?q=";
		else if (webpageProp.engine.equals("a"))
			webpageProp.engine = "https://www.ask.com/web?q=";
		else if (webpageProp.engine.equals("i"))
			webpageProp.engine = "https://www.ixquick.com/do/search?q=";
		else if (webpageProp.engine.equals("bl"))
			webpageProp.engine = "https://www.blekko.com/#?q=";
		else if (webpageProp.engine.equals("yd"))
			webpageProp.engine = "https://www.yandex.com/search/?text=";
		
		
		
		
		
		//http://www.baidu.com/s?wd=
		
		ActionbarSize= Tools.getActionBarSize(context);

		appProp.fullscreen= MainActivity.mGlobalPrefs.getBoolean       ("fullscreen"           ,false);
		appProp.transparentNav= MainActivity.mGlobalPrefs.getBoolean   ("transparentnav"       ,false);
		appProp.TransparentStatus= MainActivity.mGlobalPrefs.getBoolean("transparentstatus"    ,true);
		appProp.systemPersistent= MainActivity.mGlobalPrefs.getBoolean ("systempersistent"     ,false);
		appProp.holoDark= MainActivity.mGlobalPrefs.getBoolean         ("holodark"             ,false);
		appProp.primaryIntColor= MainActivity.mGlobalPrefs.getInt      ("textcolor",Color.BLACK);
		appProp.actionBarColor= MainActivity.mGlobalPrefs.getInt       ("actionbarcolor", context.getResources().getColor(R.color.urlback));
		appProp.urlBarColor= MainActivity.mGlobalPrefs.getInt          ("urlbarcolor", context.getResources().getColor(R.color.urlfront));
		
		sidebarProp.SidebarIconSize=numtodp(MainActivity.mGlobalPrefs.getInt    ("sidebariconsize"  ,80),context);
		sidebarProp.SidebarIconPadding=numtodp(MainActivity.mGlobalPrefs.getInt ("sidebariconpadding",10),context);
		sidebarProp.theme= MainActivity.mGlobalPrefs.getString                   ("sidebartheme", "w");
		sidebarProp.sideBarColor= MainActivity.mGlobalPrefs.getInt               ("sidebarcolor"    , Color.BLACK);
        sidebarProp.sideBarTextColor= MainActivity.mGlobalPrefs.getInt           ("sidebartextcolor", Color.WHITE);
		sidebarProp.showLabel= MainActivity.mGlobalPrefs.getBoolean              ("showfavoriteslabels", true);
		sidebarProp.swapLayout     = MainActivity.mGlobalPrefs.getBoolean        ("swapLayout"          ,false);
		if (sidebarProp.showLabel)
			sidebarProp.SidebarSize=numtodp(250,context);
		else
			sidebarProp.SidebarSize= sidebarProp.SidebarIconSize;
		
		float alpha= (sidebarProp.sideBarColor >> 24) & 0xFF;
		
		if (alpha>254f){
			sidebarProp.sideBarColor = SetupLayouts.addTransparencyToColor(254, sidebarProp.sideBarColor);
		}
		
		
	}
	
	public static int numtodp(int in, Context context){
		int out =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, in, context.getResources().getDisplayMetrics());
		return out;
	}



	

}