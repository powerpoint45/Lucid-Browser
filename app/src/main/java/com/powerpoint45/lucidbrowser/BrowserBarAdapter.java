package com.powerpoint45.lucidbrowser;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.powerpoint45.lucidbrowser.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import bookmarkModel.Bookmark;

public class BrowserBarAdapter extends ArrayAdapter<Suggestion> {
	private List<Suggestion> suggestions;

	public BrowserBarAdapter(MainActivity context, int viewResourceId) {
		super(context, R.layout.browser_bar_suggestion_item, R.id.webTitle);
		this.suggestions =new Vector<Suggestion>();
	}

	class ViewHolder {
		ImageView icon;
		TextView title;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder views;

		if (convertView== null) {
			views = new ViewHolder();
			LayoutInflater inflater = ((MainActivity) getContext()).getLayoutInflater();
			convertView = inflater.inflate(R.layout.browser_bar_suggestion_item, parent, false);
			views.title = (TextView) convertView.findViewById(R.id.webTitle);
			views.icon = ((ImageView) convertView.findViewById(R.id.webImage));
			convertView.setTag(views);
		}else {
			views = (ViewHolder)convertView.getTag();
		}

		//detect if item is a direct link or not
		if (getItem(position).url != null
				|| (getItem(position).title.contains(".") && !getItem(position).title.contains(" ")))
			views.icon.setImageResource(R.drawable.ic_link);
		else
			views.icon.setImageResource(R.drawable.ic_search);

		//if the theme is light then make icon dark
		views.icon.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

		//set item title
		views.title.setText(getItem(position).title);

		return convertView;
	}


	@Override
	public Filter getFilter() {
		return nameFilter;
	}

	Filter nameFilter = new Filter() {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if(constraint != null) {


				String responseString = null;
				suggestions.clear();
				if (!constraint.toString().equals(getContext().getResources().getString(R.string.urlbardefault))){

					//add results from bookmarks if constraint size is over 1
					if (constraint.length()>1) {
						if (BookmarksActivity.bookmarksMgr != null) {
							for (Bookmark b : BookmarksActivity.bookmarksMgr.root.getAllBookMarks()) {
								if (b.getDisplayName().toLowerCase().contains(constraint.toString().toLowerCase())
										|| b.getUrl().replace("www", "").replace("http://", "").replace("https://", "")
										.contains(constraint.toString().toLowerCase())) {
									suggestions.add(new Suggestion(b.getDisplayName(), b.getUrl()));
								}
							}
						}
					}


					HttpClient httpclient = new DefaultHttpClient();
					HttpResponse response;
					try {
						String url = String.format("https://www.google.com/complete/search?hl=%s&client=firefox&q=%s",
								Locale.getDefault().getCountry(),
								URLEncoder.encode(constraint.toString(), "utf-8"));

						response = httpclient.execute(new HttpGet(url));
						StatusLine statusLine = response.getStatusLine();
						if(statusLine.getStatusCode() == HttpStatus.SC_OK){
							responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
						} else{
							//Closes the connection.
							response.getEntity().getContent().close();
							throw new IOException(statusLine.getReasonPhrase());
						}
					} catch (ClientProtocolException e) {
						//TODO Handle problems..
					} catch (IOException e) {
						//TODO Handle problems..
					}
				}

				if (responseString!=null){

					try{
						JSONArray jArray = new JSONArray(responseString).getJSONArray(1);


						for (int i=0; i< jArray.length(); i++){
							suggestions.add(new Suggestion(jArray.getString(i),null));
						}


						FilterResults filterResults = new FilterResults();
						filterResults.values = suggestions;
						filterResults.count = suggestions.size();
						return filterResults;
					}catch(Exception e){
						return null;
					}
				}else
					return null;
			} else {
				return null;
			}
		}
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results!=null){
				Vector<Suggestion> filteredList = (Vector<Suggestion>) ((Vector<Suggestion>) results.values).clone();
				if(results != null && results.count > 0) {
					clear();
					for (Suggestion c : filteredList) {
						add(c);
					}
					notifyDataSetChanged();
				}
			}else{
				clear();
				notifyDataSetInvalidated();
			}
		}
	};



}

class Suggestion{
	Suggestion(String title, String url){
		this.title = title;
		this.url =url;
	}

	@Override
	public String toString() {
		if (url!=null)
			return url;
		else
			return title;
	}

	String title;
	String url;
}