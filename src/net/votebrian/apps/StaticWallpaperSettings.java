package net.votebrian.apps;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.Display;
import android.widget.Toast;

public class StaticWallpaperSettings extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String TEMP_BG_FILE = "temp_holder.jpg";
	private File _f;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getPreferenceManager().setSharedPreferencesName(StaticWallpaper.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.static_settings);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		// create an empty image file to temporarily store the cropped, scaled bg image.
		createTempFile();
		
		getPreferenceManager().findPreference("static_background").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
		    
		    public boolean onPreferenceClick(Preference preference)
		    {
		        Display display = getWindowManager().getDefaultDisplay(); 
		        int width = display.getWidth();
		        int height = display.getHeight();
		        //Toast.makeText(getBaseContext(), "Select Image - " + (width) + " x " + height , Toast.LENGTH_LONG).show(); 
		        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT, null)
		        	.setType("image/*")
		        	.putExtra("crop", "true")
		        	.putExtra("aspectX", width)
		        	.putExtra("aspectY", height)
		        	.putExtra("outputX", width)
		        	.putExtra("outputY", height)
		        	.putExtra("scale", "true")
		        	.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile( getTempUri()) )
		        	.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

		        startActivityForResult(photoPickerIntent, 1);
		        return true;
		    }
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {		
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data); 
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
			  //File f = getTempUri();
			  SharedPreferences customSharedPreference = getSharedPreferences(StaticWallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE); 
			  SharedPreferences.Editor editor = customSharedPreference.edit ();
			  //RealPath = getRealPathFromURI (selectedImage);
			  //RealPath = getRealPathFromURI(getTempUri());
			  editor.putString("static_background", getTempFileName());
			  editor.commit(); 
			}
		}
	}
	
	public String getRealPathFromURI(Uri contentUri) {          
		String [] proj={MediaColumns.DATA};  
		Cursor cursor = managedQuery( contentUri,  
		        proj, // Which columns to return  
		        null,       // WHERE clause; which rows to return (all rows)  
		        null,       // WHERE clause selection arguments (none)  
		        null); // Order-by clause (ascending by name)  
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);  
		cursor.moveToFirst();  
		return cursor.getString(column_index);
	}
	
	private File getTempUri() {
		File f = new File(Environment.getExternalStorageDirectory(), TEMP_BG_FILE);
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
}