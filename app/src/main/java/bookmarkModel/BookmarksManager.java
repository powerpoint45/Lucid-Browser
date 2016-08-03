package bookmarkModel;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

import com.powerpoint45.lucidbrowser.MainActivity;

public class BookmarksManager implements Serializable {
	private static final long serialVersionUID = 1L;
	public BookmarkFolder root;
	public BookmarkFolder displayedFolder;

	public static int amountOfFolders = 0;
	public static int amountOfBookmarks = 0;
	
	public BookmarksManager() {
		this.root = new BookmarkFolder("/");
		this.root.parentFolder = root;
		this.root.isRoot = true;
		this.displayedFolder = this.root;
	}

	public void saveBookmarksManager() {
		FileOutputStream fos;
		try {
			fos = MainActivity.ctxt.openFileOutput("bookmarkData",
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(this);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BookmarksManager loadBookmarksManager() {
		ObjectInputStream inputStream = null;

		try {
			// Construct the ObjectInputStream object
			inputStream = new ObjectInputStream(MainActivity.ctxt.openFileInput("bookmarkData"));

			Object obj = null;

			obj = inputStream.readObject();

			if (obj instanceof BookmarksManager) {
				return (BookmarksManager) obj;
			}

		} catch (EOFException ex) { // This exception will be caught when EOF is
									// reached
			System.out.println("End of file reached.");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the ObjectInputStream
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

}
