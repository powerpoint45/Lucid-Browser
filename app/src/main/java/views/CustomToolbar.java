package views;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.powerpoint45.lucidbrowser.Properties;
import com.powerpoint45.lucidbrowser.R;


public class CustomToolbar extends Toolbar{

	public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public CustomToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CustomToolbar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		colorizeToolbar(this, Properties.appProp.primaryIntColor);
	}



	/**
	 * Use this method to colorize toolbar icons to the desired target color
	 * @param toolbarView toolbar view being colored
	 * @param toolbarIconsColor the target color of toolbar icons
	 */
	public static void colorizeToolbar(Toolbar toolbarView, int toolbarIconsColor) {
		final PorterDuffColorFilter colorFilter
				= new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.SRC_IN);

		for(int i = 0; i < toolbarView.getChildCount(); i++) {
			final View v = toolbarView.getChildAt(i);

			doColorizing(v, colorFilter, toolbarIconsColor);
		}

		//Step 3: Changing the color of title and subtitle.
		toolbarView.setTitleTextColor(toolbarIconsColor);
		toolbarView.setSubtitleTextColor(toolbarIconsColor);
	}

	public static void doColorizing(View v, final ColorFilter colorFilter, int toolbarIconsColor){

		/**
		 * The documentation for 'Known Direct Subclasses' of View on the developer site,
		 * tells us that there is only a few types to look for:
		 * ImageView, TextView and ViewGroup
		 * Most other types, that we might expect in a Toolbar, extends from one of these.
		 *
		 * http://developer.android.com/reference/android/view/View.html
		 */

		if(v instanceof ImageView) {

			if (v.getId()!=R.id.backdrop) {
				((ImageView) v).getDrawable().setAlpha(255);
				((ImageView) v).getDrawable().setColorFilter(colorFilter);
			}

		} else if(v instanceof TextView) {

			TextView tv = ((TextView)v);
			tv.setTextColor(toolbarIconsColor);
			tv.setHintTextColor(toolbarIconsColor);

			int drawablesCount = tv.getCompoundDrawables().length;
			for(int k = 0; k < drawablesCount; k++) {
				if(tv.getCompoundDrawables()[k] != null) {
					tv.getCompoundDrawables()[k].setColorFilter(colorFilter);
				}
			}

		} else if (v instanceof ViewGroup){

			for (int lli =0; lli< ((ViewGroup)v).getChildCount(); lli ++) {
				doColorizing(((ViewGroup) v).getChildAt(lli), colorFilter, toolbarIconsColor);
			}

		} else {

			Log.d("CustomToolbar", "Unknown class: " + v.getClass().getSimpleName());

		}
	}



}
