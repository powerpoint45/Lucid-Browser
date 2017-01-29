package views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.powerpoint45.lucidbrowser.MainActivity;
import com.powerpoint45.lucidbrowser.Properties;

public class WebLayoutView extends LinearLayout{


	public WebLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public WebLayoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public WebLayoutView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	float downY;
	float upY;
	
	float actualY;
	
	boolean fingerDown;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (event.getPointerCount() == 1){
			switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				fingerDown = true;
				downY = event.getY();
				actualY = event.getRawY();
				//canceled = false;
				break;
				
			case MotionEvent.ACTION_MOVE:
				if (fingerDown){
					if (Math.abs(event.getRawY()-actualY)> Properties.numtodp(20, getContext())){
						MainActivity.actionBarControls.move(event.getY()-downY);
					}else{
						downY = event.getY();
					}
				}
				break;
				
			case MotionEvent.ACTION_UP:
				if (fingerDown){
					fingerDown = false;
					MainActivity.actionBarControls.showOrHide();
				}
				break;
				
			case MotionEvent.ACTION_CANCEL:
				if (fingerDown){
					fingerDown = false;
					MainActivity.actionBarControls.actionCanceled();
				}
				break;
			
			}
		}else{
			fingerDown = false;
		}
	    return false;
	    
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {//used when 0 tabs are active
		onInterceptTouchEvent(event);
		return true;
	}

}
