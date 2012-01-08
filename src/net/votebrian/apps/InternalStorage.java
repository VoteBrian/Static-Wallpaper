package net.votebrian.apps;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import android.net.Uri;
import android.util.Log;

public class InternalStorage {
	private static final String TAG = "InternalStorage";
	
	public static final String FILENAME = "staticBG.jpg";
	
	public static Uri FILE_URI;
	public static File BG_PATH;
	public static File BG_FILE;
	
	public static void setPath(File p) {
		BG_PATH = p;
		
		BG_FILE = new File(BG_PATH, FILENAME);
		try {
			BG_FILE.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "setPath - createNewFile failed");
		}
		FILE_URI = Uri.fromFile(BG_FILE);
	}
	
	public static Uri getURI() {
		return FILE_URI;
	}
	
	public static File getFile() {
		if(BG_FILE.exists()) {
			Log.d("getFile", "Exists");
		}
		return BG_FILE;
	}
	
	public static String getFileString() {
		return BG_FILE.toString();
	}
}
