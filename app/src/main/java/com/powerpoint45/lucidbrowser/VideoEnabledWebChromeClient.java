package com.powerpoint45.lucidbrowser;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import views.CustomWebView;

public class VideoEnabledWebChromeClient extends WebChromeClient implements OnPreparedListener, OnCompletionListener, OnErrorListener
{
    public interface ToggledFullscreenCallback
    {
        public void toggledFullscreen(boolean fullscreen);
    }

    private CustomWebView activityNonVideoView;
    private View loadingView;
    private CustomWebView webView;
    private ProgressBar PB;

    private boolean isVideoFullscreen; // Indicates if the video is being displayed using a custom view (typically full-screen)
    private FrameLayout videoViewContainer;
    private CustomViewCallback videoViewCallback;

    private ToggledFullscreenCallback toggledFullscreenCallback;

    /**
     * Never use this constructor alone.
     * This constructor allows this class to be defined as an inline inner class in which the user can override methods
     */
    public VideoEnabledWebChromeClient()
    {
    }

    /**
     * Builds a video enabled WebChromeClient.
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param activityVideoView A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     */
    public VideoEnabledWebChromeClient(CustomWebView activityNonVideoView)
    {
        this.activityNonVideoView = activityNonVideoView;
        this.loadingView = null;
        this.webView = null;
        this.isVideoFullscreen = false;
    }

    public void onProgressChanged(WebView view, int progress) 
    {	
    	CustomWebView WV = (CustomWebView) MainActivity.webLayout.findViewById(R.id.browser_page);
    	if (WV!=null){
    		if (WV==activityNonVideoView){
		    	if (PB==null)
					PB = (ProgressBar) MainActivity.webLayout.findViewById(R.id.webpgbar);
			    if (PB!=null)
			        PB.setProgress(progress);
    		}
    	}
    }
    
    
    
    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    public boolean isVideoFullscreen()
    {
        return isVideoFullscreen;
    }

    /**
     * Set a callback that will be fired when the video starts or finishes displaying using a custom view (typically full-screen)
     * @param callback A VideoEnabledWebChromeClient.ToggledFullscreenCallback callback
     */
    public void setOnToggledFullscreen(ToggledFullscreenCallback callback)
    {
        this.toggledFullscreenCallback = callback;
    }
    
    public void showActionBar(){
//    	MainActivity.actionBar.show();
//    	Properties.ActionbarSize= Tools.getActionBarSize();
//		MainActivity.StatusMargine = Tools.getStatusMargine();
//    	MainActivity.browserListView.setPadding(0, 0, 0, MainActivity.NavMargine+MainActivity.StatusMargine);
//    	MainActivity.browserListView.setY(MainActivity.StatusMargine);			
//		MainActivity.webLayout.setPadding(0, MainActivity.StatusMargine, 0, 0);
//		if (MainActivity.tintManager!=null && Properties.appProp.TransparentStatus)
//    		MainActivity.tintManager.setStatusBarAlpha(1.0f);
    }
    
    public void hideActionBar(){
//    	MainActivity.actionBar.hide();
//    	MainActivity.webLayout.setPadding(0, 0, 0, 0);
//    	if (MainActivity.tintManager!=null && Properties.appProp.TransparentStatus)
//    		MainActivity.tintManager.setStatusBarAlpha(0);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback)
    {
        if (view instanceof FrameLayout)
        {
        	hideActionBar();
            // A video wants to be shown
            FrameLayout frameLayout = (FrameLayout) view;
            View focusedChild = frameLayout.getFocusedChild();

            // Save video related variables
            activityNonVideoView.setVideoPlaying(true);
            this.isVideoFullscreen = true;
            this.videoViewContainer = frameLayout;
            this.videoViewCallback = callback;

            // Hide the non-video view, add the video view, and show it
            activityNonVideoView.setVisibility(View.GONE);
            ViewGroup activityVideoView = ((ViewGroup) MainActivity.webLayout.findViewById(R.id.webviewholder));

            System.out.println("VIDV1"+activityVideoView);

            activityVideoView.addView(videoViewContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            activityVideoView.setVisibility(View.VISIBLE);

            if (focusedChild instanceof android.widget.VideoView)
            {
                // android.widget.VideoView (typically API level <11)
                android.widget.VideoView videoView = (android.widget.VideoView) focusedChild;

                // Handle all the required events
                videoView.setOnPreparedListener(this);
                videoView.setOnCompletionListener(this);
                videoView.setOnErrorListener(this);
            }
            else
            {
                // Other classes, including:
                // - android.webkit.HTML5VideoFullScreen$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 11-18)
                // - android.webkit.HTML5VideoFullScreen$VideoTextureView, which inherits from android.view.TextureView (typically API level 11-18)
                // - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 19+)

                // Handle HTML5 video ended event only if the class is a SurfaceView
                // Test case: TextureView of Sony Xperia T API level 16 doesn't work fullscreen when loading the javascript below
                if (webView != null && webView.getSettings().getJavaScriptEnabled() && focusedChild instanceof SurfaceView)
                {
                    // Run javascript code that detects the video end and notifies the Javascript interface
                    String js = "javascript:";
                    js += "var _ytrp_html5_video_last;";
                    js += "var _ytrp_html5_video = document.getElementsByTagName('video')[0];";
                    js += "if (_ytrp_html5_video != undefined && _ytrp_html5_video != _ytrp_html5_video_last) {";
                    {
                        js += "_ytrp_html5_video_last = _ytrp_html5_video;";
                        js += "function _ytrp_html5_video_ended() {";
                        {
                            js += "_VideoEnabledWebView.notifyVideoEnd();"; // Must match Javascript interface name and method of VideoEnableWebView
                        }
                        js += "}";
                        js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);";
                    }
                    js += "}";
                    webView.loadUrl(js);
                }
            }

            // Notify full-screen change
            if (toggledFullscreenCallback != null)
            {
                toggledFullscreenCallback.toggledFullscreen(true);
            }
        }
    }

    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Available in API level 14+, deprecated in API level 18+
    {
        onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView()
    {
        // This method should be manually called on video end in all cases because it's not always called automatically.
        // This method must be manually called on back key press (from this class' onBackPressed() method).

        if (isVideoFullscreen)
        {	showActionBar();
        	activityNonVideoView.setVideoPlaying(false);
            // Hide the video view, remove it, and show the non-video view
        	ViewGroup activityVideoView = ((ViewGroup) MainActivity.webLayout.findViewById(R.id.webviewholder));
            activityVideoView.removeView(videoViewContainer);
            activityNonVideoView.setVisibility(View.VISIBLE);

            // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
            if (videoViewCallback != null && !videoViewCallback.getClass().getName().contains(".chromium."))
            {
                videoViewCallback.onCustomViewHidden();
            }

            // Reset video related variables
            isVideoFullscreen = false;
            videoViewContainer = null;
            videoViewCallback = null;

            // Notify full-screen change
            if (toggledFullscreenCallback != null)
            {
                toggledFullscreenCallback.toggledFullscreen(false);
            }
        }
    }

    @Override
    public View getVideoLoadingProgressView() // Video will start loading
    {
        if (loadingView != null)
        {
            loadingView.setVisibility(View.VISIBLE);
            return loadingView;
        }
        else
        {
            return super.getVideoLoadingProgressView();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) // Video will start playing, only called in the case of android.widget.VideoView (typically API level <11)
    {
        if (loadingView != null)
        {
            loadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) // Video finished playing, only called in the case of android.widget.VideoView (typically API level <11)
    {
        onHideCustomView();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) // Error while playing video, only called in the case of android.widget.VideoView (typically API level <11)
    {
        return false; // By returning false, onCompletion() will be called
    }
    

    /**
     * Notifies the class that the back key has been pressed by the user.
     * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
     * @return Returns true if the event was handled, and false if was not (video view is not visible)
     */
    public boolean onBackPressed()
    {
        if (isVideoFullscreen)
        {
            onHideCustomView();
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    
    
    
    
    
    public static ValueCallback<Uri> mUploadMessage;  
    public static ValueCallback<Uri[]> mUploadMessageLol;
    public final static int FILECHOOSER_RESULTCODE=1;  
    
    //The undocumented magic method override   
    //Eclipse will swear at you if you try to put @Override here   
	// For Android 3.0+
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {  

     mUploadMessage = uploadMsg;  
     Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
     i.addCategory(Intent.CATEGORY_OPENABLE);  
     i.setType("image/*");  
     MainActivity.activity.startActivityForResult(Intent.createChooser(i,
    		 MainActivity.activity.getResources().getString(R.string.choose_upload)), FILECHOOSER_RESULTCODE);  

    } 

    // For Android 3.0+
    public void openFileChooser( ValueCallback uploadMsg, String acceptType ) {
    mUploadMessage = uploadMsg;
    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
    i.addCategory(Intent.CATEGORY_OPENABLE);
    i.setType("*/*");
    MainActivity.activity.startActivityForResult(
    Intent.createChooser(i, 
    		MainActivity.activity.getResources().getString(R.string.choose_upload)),
    FILECHOOSER_RESULTCODE);
    }
    
    

    //For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
        mUploadMessage = uploadMsg;  
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
        i.addCategory(Intent.CATEGORY_OPENABLE);  
        i.setType("image/*");  
        MainActivity.activity.startActivityForResult( Intent.createChooser( i, 
        		MainActivity.activity.getResources().getString(R.string.choose_upload) ), FILECHOOSER_RESULTCODE );

    }
    
    //lollipop
    @SuppressLint("NewApi")
	public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        // make sure there is no existing message 
        if (mUploadMessageLol != null) { 
        	mUploadMessageLol.onReceiveValue(null); 
        	mUploadMessageLol = null; 
        } 
 
        mUploadMessageLol = filePathCallback;
 
        Intent intent = fileChooserParams.createIntent();
        try { 
            MainActivity.activity.startActivityForResult(intent, FILECHOOSER_RESULTCODE);
        } catch (ActivityNotFoundException e) {
        	mUploadMessageLol = null; 
            Toast.makeText(MainActivity.activity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
            return false; 
        } 
 
        return true; 
    } 

}