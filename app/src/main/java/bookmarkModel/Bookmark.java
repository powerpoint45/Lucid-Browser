package bookmarkModel;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public class Bookmark implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String url;
	private String displayName;
	private BookmarkFolder inFolder;
	private String internalName;
	private String pathToFavicon;
	
	public Bookmark(){
		super();
	}
	
	public Bookmark(String url, String displayName){
		setUrl(url);
		setDisplayName(displayName);
		
		setInternalName("bookmark_"+UUID.randomUUID());
		BookmarksManager.amountOfBookmarks++;
		}
	
	
	
	public String getPathToFavicon() {
		return pathToFavicon;
	}

	public void setPathToFavicon(String pathToFavicon) {
		this.pathToFavicon = pathToFavicon;
	}

	
	public URL getURL() {
		URL urlToReturn;
		try {
			urlToReturn = new URL(url);
			return urlToReturn;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public BookmarkFolder getInFolder() {
		return inFolder;
	}

	public void setInFolder(BookmarkFolder inFolder) {
		this.inFolder = inFolder;
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}
	
	public String toString(){
		String result = "Bookmark - Title: %s, URL: %s, Favicon-Path: %s";
		return String.format(result,
				this.getDisplayName(),
				this.getUrl(),
				this.getPathToFavicon());
		
	}

}
