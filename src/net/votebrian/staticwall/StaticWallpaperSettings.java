package net.votebrian.staticwall;

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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Display;

public class StaticWallpaperSettings extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {
  //private static final String TAG = "StaticWallpaperSettings";

  private PreferenceManager mPrefManager;

  @Override
  protected void onCreate(Bundle savedInsatanceState) {
    //Log.d(TAG, "onCreate");
    super.onCreate(savedInsatanceState);

    addPreferencesFromResource(R.xml.static_settings);

    mPrefManager = getPreferenceManager();
    mPrefManager.setSharedPreferencesName(StaticWallpaper.SHARED_PREFS_NAME);
    mPrefManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    mPrefManager.findPreference("static_prefs")
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {

          public boolean onPreferenceClick(Preference preference) {
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            File f = new File(Environment.getExternalStorageDirectory(), StaticWallpaper.EXT_BG_FILENAME);
            StaticWallpaper.EXT_BG_FILE = f;
            try {
              f.createNewFile();
            } catch (IOException e) {
              //Log.d(TAG, "Count not create temp file");
              throw new RuntimeException("Could not create temp file to External storage", e);
            }

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null)
                .setType("image/*")
                .putExtra("crop", "true")
                .putExtra("aspectX", width)
                .putExtra("aspectY", height)
                .putExtra("outputX", width)
                .putExtra("outputY", height)
                .putExtra("scale", true)
                .putExtra("scaleUpIfNeeded", true)
                .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
                .putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            startActivityForResult(intent, 1);
            return true;
          }
        });
  }

  @Override
  protected void onResume() {
    //Log.d(TAG, "onResume");
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    //Log.d(TAG, "onDestroy");
    getPreferenceManager()
      .getSharedPreferences()
      .unregisterOnSharedPreferenceChangeListener(this);
    super.onDestroy();
  }

  public void onSharedPreferenceChanged(
      SharedPreferences sharedPreferences,
      String key) {
    //Log.d(TAG, "onSharedPreferenceChanged");
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1) {
      if (resultCode == Activity.RESULT_OK) {
        //Log.d(TAG, "RESULT_OK");
        SharedPreferences customSharedPreference = getSharedPreferences(
            StaticWallpaper.SHARED_PREFS_NAME,
            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = customSharedPreference.edit ();
        editor.putBoolean("isSet", true);
        editor.commit();
      }
    }

    // Close the settings page
    finish();
  }
}