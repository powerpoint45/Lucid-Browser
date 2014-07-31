package com.powerpoint45.lucidbrowser;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;


public class Properties extends MainActivity {
	public static int ActionbarSize=0; //used for getting the actual actionbar size + anny padding
	public static class controls{
		static boolean navBarHidden;
	}
	public static class appProp{
		static int actionBarColor;
		public static int primaryIntColor;
		static int urlBarColor;
		static boolean fullscreen;
		static boolean transparentNav;
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
		static boolean useDesktopView;
		static boolean disablesuggestions;
		static boolean clearonexit;
		static boolean closetabsonexit;
		static boolean enableimages;
		static boolean exitconfirmation;
		//static boolean enablejavascript; //uncomment if wanted by users
		static boolean enablecookies;
		static int     fontSize;
	}

	
	public static void update_preferences(){
		webpageProp.showBackdrop=MainActivity.mGlobalPrefs.getBoolean("showbrowserbackdrop",true);
		webpageProp.useDesktopView=MainActivity.mGlobalPrefs.getBoolean("usedesktopview",false);
		webpageProp.disablesuggestions=MainActivity.mGlobalPrefs.getBoolean("disablesuggestions", false);
		webpageProp.clearonexit=MainActivity.mGlobalPrefs.getBoolean("clearonexit",false);
		webpageProp.enableimages=MainActivity.mGlobalPrefs.getBoolean("enableimages", true);
		//webpageProp.enablejavascript=MainActivity.mGlobalPrefs.getBoolean("enablejavascript", true);
		//uncomment if wanted by users
		webpageProp.enablecookies=    MainActivity.mGlobalPrefs.getBoolean("enablecookies"  ,true);
		webpageProp.fontSize     =    MainActivity.mGlobalPrefs.getInt    ("webfontsize"    , 2);
		webpageProp.closetabsonexit = MainActivity.mGlobalPrefs.getBoolean("closetabsonexit", false);
		webpageProp.exitconfirmation=MainActivity.mGlobalPrefs.getBoolean("exitconfirmation", false);
		
		int actionBarHeight = LayoutParams.MATCH_PARENT;//fallback size
		TypedValue tv = new TypedValue();
		if (MainActivity.activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
		    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,MainActivity.activity.getResources().getDisplayMetrics());
		}
		
		ActionbarSize= actionBarHeight;

		appProp.fullscreen=MainActivity.mGlobalPrefs.getBoolean       ("fullscreen"           ,false);
		appProp.transparentNav=MainActivity.mGlobalPrefs.getBoolean   ("transparentnav"       ,false);
		appProp.TransparentStatus=MainActivity.mGlobalPrefs.getBoolean("transparentstatus"    ,true);
		appProp.systemPersistent=MainActivity.mGlobalPrefs.getBoolean ("systempersistent"     ,false);
		appProp.holoDark=MainActivity.mGlobalPrefs.getBoolean         ("holodark"             ,false);
		appProp.primaryIntColor=MainActivity.mGlobalPrefs.getInt      ("textcolor",Color.BLACK);
		appProp.actionBarColor=MainActivity.mGlobalPrefs.getInt       ("actionbarcolor", MainActivity.activity.getResources().getColor(R.color.urlback));
		appProp.urlBarColor=MainActivity.mGlobalPrefs.getInt          ("urlbarcolor", MainActivity.activity.getResources().getColor(R.color.urlfront));
		
		sidebarProp.SidebarIconSize=numtodp(MainActivity.mGlobalPrefs.getInt    ("sidebariconsize"  ,80));
		sidebarProp.SidebarIconPadding=numtodp(MainActivity.mGlobalPrefs.getInt ("sidebariconpadding",10));
		sidebarProp.theme=MainActivity.mGlobalPrefs.getString                   ("sidebartheme", "b");
		sidebarProp.sideBarColor=MainActivity.mGlobalPrefs.getInt               ("sidebarcolor"    , Color.BLACK);
        sidebarProp.sideBarTextColor=MainActivity.mGlobalPrefs.getInt           ("sidebartextcolor", Color.WHITE);
		sidebarProp.showLabel=MainActivity.mGlobalPrefs.getBoolean              ("showfavoriteslabels", true);
		sidebarProp.swapLayout     =MainActivity.mGlobalPrefs.getBoolean        ("swapLayout"          ,false);
		if (sidebarProp.showLabel)
			sidebarProp.SidebarSize=numtodp(250);
		else
			sidebarProp.SidebarSize=sidebarProp.SidebarIconSize;
		
		float alpha= (sidebarProp.sideBarColor >> 24) & 0xFF;
		
		if (alpha>254f){
			sidebarProp.sideBarColor = SetupLayouts.addTransparencyToColor(254,sidebarProp.sideBarColor);
		}
		
		
	}
	
	public static int numtodp(int in){
		int out =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, in, MainActivity.activity.getResources().getDisplayMetrics());
		return out;
	}
	
	

}
