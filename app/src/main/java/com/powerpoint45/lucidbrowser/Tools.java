package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ViewConfiguration;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import views.CustomWebView;

public abstract class Tools {

	public static String fixURL(String q){
		while (q.endsWith(" "))
			q= q.substring(0, q.length()-1);
		if (q.contains(".") && !q.contains(" ")){
			if (q.startsWith("http://")||q.startsWith("https://"))
				return q;
			else if (q.startsWith("www."))
				return "http://"+q;
			else if (q.startsWith("file:"))
				return q;
			else
				return "http://"+q;
		}
		else if (q.startsWith("about:home"))
			return Properties.webpageProp.assetHomePage;
		else if (q.startsWith("about:")||q.startsWith("file:"))
			return q;
		else
			return Properties.webpageProp.engine+q.replace(" ", "%20").replace("+", "%2B");
	}
	
	public static String getFileNameFromHeader(String header){
		int pos = 0;
		
		String fileName = null;
		
        if ((pos = header.toLowerCase().lastIndexOf("filename=")) >= 0) {
            fileName = header.substring(pos + 9);
            pos = fileName.lastIndexOf(";");
 
            if (pos > 0) {
                fileName = fileName.substring(0, pos - 1);
            } 
        } 
        if (fileName!=null)
        	fileName=fileName.replaceAll("\"", "");
        
        return fileName;
	}
	
	public static class DownloadAsyncTask extends AsyncTask<String, String, Boolean> {
		 
	    private String urlToDownload;
	    private String fileName;
	    String header;
	    String mimetype;
	    String userAgent;
		MainActivity activity;
	 
	    public DownloadAsyncTask(String url, MainActivity activity) {
			this.activity = activity;
	    	urlToDownload = url;
	    	userAgent = MainActivity.webWindows.get(activity.getTabNumber()).getSettings().getUserAgentString();
	    }

		@Override
		protected Boolean doInBackground(String... params) {
			mimetype = Tools.getMimeType(urlToDownload);
			Log.d("browser", "mime:"+mimetype);
			URL actualURL = null;
			
			try {
				Log.d("browser", "url:"+urlToDownload);
				actualURL = new URL(urlToDownload);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			if (actualURL!=null)
				fileName = Tools.getFileName(actualURL);
			Log.d("browser", "fileName:"+fileName);
	        if (actualURL!=null){
	        	
				HttpURLConnection con;
				try {
					con = (HttpURLConnection) actualURL.openConnection();
					header = con.getHeaderField("Content-Disposition");
					if (mimetype != null){
						mimetype = con.getContentType();
					}
					Log.d("browser", "contype"+mimetype);
					Log.d("browser", "head:"+header);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (fileName == null || fileName.equals("")){
					if (header!=null && header.contains("filename=")){
						fileName = header.split("=")[1];
						fileName= fileName.replace("\"", "");
						Log.d("browser", "base:"+fileName);
					}else{
						if (mimetype!=null){
							String extention = mimetype.substring(mimetype.indexOf("/")+1);
							fileName = "untitled."+extention;
							Log.d("browser", "newfname:"+fileName);
						}
						fileName = "untitled.png";
					}
					
				}
				
				if (mimetype == null){
					if(fileName.lastIndexOf(".") != -1) { 
					    String ext = fileName.substring(fileName.lastIndexOf(".")+1);
					    MimeTypeMap mime = MimeTypeMap.getSingleton();
					    mimetype = mime.getMimeTypeFromExtension(ext);
					}
				}
				Log.d("browser", "mime:"+mimetype);
				return true;
		        	
	        }
			return false;
		}
		
		@Override 
	    protected void onPostExecute(Boolean download) {
			if (download)
				CustomWebView.onDownloadStartNoStream(activity, urlToDownload, userAgent, header, mimetype, fileName, false);
	    } 

	} 
	
	public static String getFileName(URL extUrl) {
		//URL: "http://photosaaaaa.net/photos-ak-snc1/v315/224/13/659629384/s659629384_752969_4472.jpg" 
		String filename = "";
		//PATH: /photos-ak-snc1/v315/224/13/659629384/s659629384_752969_4472.jpg 
		String path = extUrl.getPath();
		//Checks for both forward and/or backslash  
		//NOTE:**While backslashes are not supported in URL's  
		//most browsers will autoreplace them with forward slashes 
		//So technically if you're parsing an html page you could run into  
		//a backslash , so i'm accounting for them here; 
		String[] pathContents = path.split("[\\\\/]");
		if(pathContents != null){
			int pathContentsLength = pathContents.length;
			System.out.println("Path Contents Length: " + pathContentsLength);
			for (int i = 0; i < pathContents.length; i++) {
				System.out.println("Path " + i + ": " + pathContents[i]);
			} 
			//lastPart: s659629384_752969_4472.jpg 
			String lastPart = pathContents[pathContentsLength-1];
			String[] lastPartContents = lastPart.split("\\.");
			if(lastPartContents != null && lastPartContents.length > 1){
				int lastPartContentLength = lastPartContents.length;
				System.out.println("Last Part Length: " + lastPartContentLength);
				//filenames can contain . , so we assume everything before 
				//the last . is the name, everything after the last . is the  
				//extension 
				String name = "";
				for (int i = 0; i < lastPartContentLength; i++) {
					System.out.println("Last Part " + i + ": "+ lastPartContents[i]);
					if(i < (lastPartContents.length -1)){
						name += lastPartContents[i] ;
						if(i < (lastPartContentLength -2)){
							name += ".";
						} 
					} 
				} 
				String extension = lastPartContents[lastPartContentLength -1];
				filename = name + "." +extension;
				System.out.println("Name: " + name);
				System.out.println("Extension: " + extension);
				System.out.println("Filename: " + filename);
			} 
		} 
		return filename;
	} 
	
	public static void toastString(String s, Context c){
		Toast.makeText(c, s, Toast.LENGTH_LONG).show();
	}
	
	public static void toastString(int rid, Context c){
		String s = c.getResources().getString(rid);
		Toast.makeText(c,s, Toast.LENGTH_LONG).show();
	}
	
	public static boolean isDownloadManagerAvailable(Context context) {
	    try {
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public static void setActionBarColor(int c){
		ColorDrawable colorDrawable = new ColorDrawable(c);
  		MainActivity.actionBar.setBackgroundDrawable(colorDrawable);
	}
	
	public static String getMimeType(String url) {
	    String type = null;
	    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
	    if (extension != null) {
	        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	    } 
	    return type;
	}


	@SuppressLint("NewApi")
	   public static boolean hasSoftNavigation(Context context)
	   {
		   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
		        return !ViewConfiguration.get(context).hasPermanentMenuKey();
		    }
		    return true;
	   }
	public static int getStatusBarHeight(Resources res) {
        int result = 0;
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        } 
        return result;
  } 
  public static int getNavBarHeight(Resources res){
	   int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
	   if (resourceId > 0) {
	       return res.getDimensionPixelSize(resourceId);
	   }
	   return 0;
  }
  
  public static float pxToDp(Context context, float px) {
      if (context == null) {
          return -1;
      }
      return px / context.getResources().getDisplayMetrics().density;
  }
  
  public static int getStatusMargine(Context context){
	  int margine =0;
		int id = context.getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus){
			if (id != 0) {
		        if (Properties.appProp.fullscreen && Properties.appProp.transparentNav){
		        	//do nothing
		        }else if (Properties.appProp.fullscreen){
		        	margine= Properties.ActionbarSize;
		        }else if (Properties.appProp.transparentNav){
		        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		        		margine= Properties.ActionbarSize;
		        	else
		        		margine= Properties.ActionbarSize+ Tools.getStatusBarHeight(context.getResources());
		        }else{
		        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		        		margine= Properties.ActionbarSize;
		        	else
		        		margine= Properties.ActionbarSize+ Tools.getStatusBarHeight(context.getResources());
		        }

		        if (Properties.appProp.fullscreen){
		        	margine= Properties.ActionbarSize;
		        }
		    }else
		    	margine = Properties.ActionbarSize;
		}else
			margine = Properties.ActionbarSize;
		
		return margine;
	}
  
  public static int getStatusSize(Context context){
	  int margine =0;
		int id = context.getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
		if (Properties.appProp.transparentNav || Properties.appProp.TransparentStatus)
			if (id != 0) {
		        if (Properties.appProp.fullscreen && Properties.appProp.transparentNav){
		        	//do nothing
		        }else if (Properties.appProp.fullscreen){
		        	margine=0;
		        }else if (Properties.appProp.transparentNav){
		        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		        		margine=0;
		        	else
		        		margine= Tools.getStatusBarHeight(context.getResources());
		        }else{
		        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		        		margine=0;
		        	else
		        		margine= Tools.getStatusBarHeight(context.getResources());
		        }
		    }
		return margine;
	}
  
  public static int getActionBarSize(Context context){
//	  int actionBarHeight = LayoutParams.MATCH_PARENT;//fallback size
//		TypedValue tv = new TypedValue();
//		if (MainActivity.activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
//		{
//		    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,MainActivity.activity.getResources().getDisplayMetrics());
//		}
		
		return (int) context.getResources().getDimension(R.dimen.actionBarSize);
  }

	public static NotificationManager setUpSystemPersistence(Context c){
		if (Properties.appProp.systemPersistent){
			NotificationManager mNotificationManager;
			Intent notificationIntent = new Intent(c, MainActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(c, 0, notificationIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(c)
							.setSmallIcon(R.drawable.ic_stat_location_web_site)
							.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher))
							.setOngoing(true)
							.setContentIntent(contentIntent)
							.setPriority(2)
							.setContentTitle(c.getResources().getString(R.string.label));
			mNotificationManager =
					(NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(8, mBuilder.build());
			return mNotificationManager;
		}
		return null;
	}

  
}
