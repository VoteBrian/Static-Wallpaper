package net.votebrian.apps;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;

public class StaticWallpaperSettings extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "StaticWallpaperSettings";
	
	//private static final String TEMP_BG_FILE = "temp_holder.jpg";
	//private File _f;
	
	@Override
	protected void onCreate(Bundle icicle) {
		Log.d(TAG, "onCreate");
		
		super.onCreate(icicle);
		getPreferenceManager().setSharedPreferencesName(StaticWallpaper.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.static_settings);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		getPreferenceManager().findPreference("static_background").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
		    
		    public boolean onPreferenceClick(Preference preference)
		    {
		        Display display = getWindowManager().getDefaultDisplay(); 
		        int width = display.getWidth();
		        int height = display.getHeight();
		        File f = new File(Environment.getExternalStorageDirectory(), "temp_holder.jpg");
		        try {
		        	f.createNewFile();
		        } catch (IOException e) {
		        	//nothing
		        }
		        // TODO: find out why "crop" seems to require "true" be in quotes while scaleUpIfNeeded seems to require "true" not have quotes
		        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT, null)
		        	.setType("image/*")
		        	.putExtra("crop", "true")
		        	.putExtra("aspectX", width)
		        	.putExtra("aspectY", height)
		        	.putExtra("outputX", width)
		        	.putExtra("outputY", height)
		        	.putExtra("scale", true)
		        	.putExtra("scaleUpIfNeeded", true)
		        	.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
		        	.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		        	//.putExtra(MediaStore.EXTRA_OUTPUT, InternalStorage.getFile())
		        	//.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile( getTempUri()) );

		        startActivityForResult(photoPickerIntent, 1);

		        Log.d(TAG, "Output X: " + width + " Output Y: " + height);
		        return true;
		    }
		});
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {		
		Log.d(TAG, "onSharedPreferenceChanged");
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		
		super.onActivityResult(requestCode, resultCode, data); 
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
			  SharedPreferences customSharedPreference = getSharedPreferences(StaticWallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE); 
			  SharedPreferences.Editor editor = customSharedPreference.edit ();
			  //editor.putString("static_background", InternalStorage.getFileString());
			  editor.putString("isSet", "set");
			  //editor.putBoolean("bg_set", true);
			  editor.commit(); 
			} else {
				Log.d(TAG, "resultCode:" + resultCode);
			}
		} else {
			Log.d(TAG, "requestCode:" + requestCode);
		}
	}
	
	/*
	private File getTempUri() {
		File f = new File(Environment.getExternalStorageDirectory(), TEMP_BG_FILE);
		File x = new File(getFilesDir(), TEMP_BG_FILE);
		return f;
	}
	
	private String getTempFileName() {
		
		File f = new File(Environment.getExternalStorageDirectory(), TEMP_BG_FILE);
		return f.toString();
	}
	
	private void createTempFile() {
		_f = new File(getTempFileName());
		try {
			_f.createNewFile();
		} catch (IOException e) {
			Toast toast = Toast.makeText(getApplicationContext(), "file creation failed", Toast.LENGTH_SHORT);
		}
	}
	*/
}