package bookmarkModel;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BookmarkFolder implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<Bookmark> containedBookmarks = new LinkedList<Bookmark>();
	private List<BookmarkFolder> containedFolders = new LinkedList<BookmarkFolder>();

	public BookmarkFolder parentFolder;
	public boolean isRoot = false;
	private String displayName;
	private String internalName;

	// Constructor
	public BookmarkFolder(String displayName) {
		setDisplayName(displayName);
		setInternalName("folder_" + UUID.randomUUID());
		BookmarksManager.amountOfFolders++;
	}
	
	public String toString() {
		String result = "BookmarkFolder - Title: %s";
		return String.format(result, this.getDisplayName());

	}

	// Getter
	public Bookmark getBookmark(String internalName) {
		Bookmark foundBookmark = null;
		
		for (Bookmark bm : containedBookmarks) {
			if (bm.getInternalName().equals(internalName)) {
				foundBookmark = bm;
				break;
			}
		}
		
		return foundBookmark;
	}
	
	public Bookmark getBookmark(int idx) {
		return this.containedBookmarks.get(idx);
	}
	
	public BookmarkFolder getFolder(int idx) {
		return this.containedFolders.get(idx);
	}
	
	public BookmarkFolder getFolder(String folderName) {
		for (BookmarkFolder each : getContainedFolders()) {
			if (each.getInternalName().equals(folderName)) {
				return each;
			}
		}
		
		return null;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getInternalName() {
		return internalName;
	}
	
	public List<Bookmark> getContainedBookmarks() {
		return containedBookmarks;
	}

	public List<BookmarkFolder> getContainedFolders() {
		return containedFolders;
	}

	public boolean isEmptyFolder() {
		if (containedBookmarks.isEmpty() && containedFolders.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	// Setter
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	// Bookmark Operations (Add, Remove, Search...)
	// -------------------------------------------------------
	public void addBookmark(Bookmark newBookmark) {
		newBookmark.setInFolder(this);
		containedBookmarks.add(newBookmark);

		//sort bookmarks
		final Collator collator = Collator.getInstance(Locale
				.getDefault());
		collator.setStrength(Collator.SECONDARY);

		Collections.sort(getContainedBookmarks(), new Comparator<Bookmark>() {
			@Override
			public int compare(Bookmark b1, Bookmark b2) {
				String label1 = b1.getDisplayName();
				String label2 = b2.getDisplayName();

				return collator.compare(label1, label2);
			}
		});
	}
	
	/**
	 * Tries to remove a bookmark with a given internal name
	 * @param internalName
	 * @return true if remove was successfull, else return false
	 */
	public boolean removeBookmark(String internalName) {
		for (Bookmark each:this.containedBookmarks){
			if (each.getInternalName().equals(internalName)){
				return this.containedBookmarks.remove(each);								
			}
		}
		
		return false; // Didn't find it, didn't delete it
	}
	
	public boolean removeBookmark(int idx) {
		if (this.containedBookmarks.remove(idx) != null){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean removeBookmarkDeep(String internalName) {
		if (removeBookmark(internalName) == false) {
			for (BookmarkFolder each : containedFolders) {
				if (each.removeBookmark(internalName) == true) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Method will look for a bookmark object with the given URL
	 * and searches in this folder's sub-folders if necessary
	 * 
	 * @param url - URL to search for
	 * @return Internal name of bookmark object if found, else null
	 */
	public String containsBookmarkDeep(String url) {

		// Is it in the executing folder?
		for (Bookmark bm : containedBookmarks) {
			if (bm.getUrl().equals(url)) {
				return bm.getInternalName();
			}
		}
		
		for (BookmarkFolder each : containedFolders) {
			for (Bookmark bm : each.containedBookmarks) {
				if (bm.getUrl().equals(url)) {
					return bm.getInternalName();
				}
			}
		}
		
		return null; // Didn't find URL
	}
	
	/**
	 * Method search all folders and returns an ArrayList of bookmarks
	 * and searches in this folder's sub-folders if necessary
	 */
	public ArrayList<Bookmark> getAllBookMarks() {
		ArrayList<Bookmark> bookmarksToReturn = new ArrayList<Bookmark>();
		
		// Is it in the executing folder?
		for (Bookmark bm : containedBookmarks) {
				bookmarksToReturn.add(bm);
		}
		
		for (BookmarkFolder each : containedFolders) {
			for (Bookmark bm : each.containedBookmarks) {
				bookmarksToReturn.add(bm);
			}
		}
		
		return bookmarksToReturn;
	}
	
	// Folder Operations (Add, remove, Search, ...)
	// -------------------------------------------------------
	public BookmarkFolder getFolderDeep(String folderName) {
		if (this.getInternalName().equals(folderName)) {
			return this;
		}

		BookmarkFolder result = this.getFolder(folderName);

		if (result == null) {

			for (BookmarkFolder each : this.containedFolders) {
				BookmarkFolder subResult = each.getFolderDeep(folderName);
				if (subResult != null) {
					return result;
				}
			}
		} else {
			return result;
		}
		return null;
	}


	public void addFolder(BookmarkFolder newBMFolder) {
		newBMFolder.parentFolder = this;
		containedFolders.add(newBMFolder);
	}

	/**
	 * Tries to remove a bookmark folder with a given internal name
	 * @param internalName
	 * @return true if remove was successful, else return false
	 */
	public boolean removeFolder(String internalName) {
		for (BookmarkFolder each:this.containedFolders){
			if (each.getInternalName().equals(internalName)){
				return this.containedFolders.remove(each);								
			}
		}
		
		return false; // Didn't find it, didn't delete it
	}
	
	public boolean removeFolder(int idx) {
		if (this.containedFolders.remove(idx) != null){
			return true;
		} else {
			return false;
		}
	}

}
