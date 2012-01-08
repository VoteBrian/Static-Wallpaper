package net.votebrian.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class StaticWallpaper extends WallpaperService {
	public static final String SHARED_PREFS_NAME="staticSettings";
	private static final String TAG = "StaticWallpaper";
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "OnCreate");
		
		// Check if the bg image exists
		String f = InternalStorage.FILENAME;
		InternalStorage.setPath(getDir("staticBG", MODE_PRIVATE));
		//android.os.Debug.waitForDebugger();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		Log.d(TAG, "onCreateEngine");
		
		return new StaticEngine();
	}
	
	private void createFile() throws IOException {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(InternalStorage.FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File Not Found");
		}
		
		fos.close();
	}
	
	/*  ------------------------------------------------
	 *                      ENGINE
	 *  ------------------------------------------------
	 */
	class StaticEngine extends Engine
		implements SharedPreferences.OnSharedPreferenceChangeListener {
		private static final String TAG = "StaticEngine";
		
		private final Handler mHandler = new Handler();
		private SharedPreferences mPrefs;
		private boolean mVisible;
		private Bitmap mBackgroundImage;
		private String _staticBG;
		private boolean bg_set = false;
		
		private final Runnable mDrawBG = new Runnable() {
            public void run() {
            	Log.d(TAG, "Runnable");
            	
                drawFrame();
            }
        };
        
        StaticEngine() {
        	Log.d(TAG, "StaticEngine");
        	
        	mPrefs = StaticWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
            
            //SharedPreferences.Editor editor = mPrefs.edit();
            //editor.putBoolean()
        }

		public void onSharedPreferenceChanged(
				SharedPreferences prefs, String key) {
			Log.d(TAG, "onSharedPreferenceChanged");
			
			// check if the background image has been set
			//bg_set = prefs.getBoolean("bg_set", false);
			String result = prefs.getString("isSet", "notSet");
			Log.d(TAG, "result:" + result);
			if (result == "set") {
				bg_set = true;
			}
			//_path = prefs.getString("static_background", "default");
			
			//store the image to mBackgroundImage
			setImage();
            drawFrame();
            
		}
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			Log.d(TAG, "onCreate");
			
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
		}
		
		@Override
		public void onDestroy() {
			Log.d(TAG, "onDestroy");
			
			super.onDestroy();
			mHandler.removeCallbacks(mDrawBG);
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			Log.d(TAG, "onVisibilityChanged");
			
			mVisible = visible;
			if(mVisible) {
				drawFrame();
			} else {
				mHandler.removeCallbacks(mDrawBG);
			}
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Log.d(TAG, "onSurfaceChanged");
			
			drawFrame();
		}
		
		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "onSurfaceCreated");
			super.onSurfaceCreated(holder);
		}
		
		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "onSurfaceDestroyed");
			
			mVisible = false;
			mHandler.removeCallbacks(mDrawBG);
			mBackgroundImage.recycle();
			mBackgroundImage = null;
			stopSelf();
			
			super.onSurfaceDestroyed(holder);
		}
		
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xStep, float yStep, int xPixels, int yPixels) {
			Log.d(TAG, "onOffsetsChanged");
		}
		
		public void drawFrame() {
			Log.d(TAG, "drawFrame");
			
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try{
				c = holder.lockCanvas();
				if(c != null) {
					c.save();
					c.drawColor(0xff000000);
					c.drawBitmap(mBackgroundImage, 0,0, null);
					c.restore();
				}
			} finally {
				if(c != null) {
					holder.unlockCanvasAndPost(c);
				}
			}
			mHandler.removeCallbacks(mDrawBG);
		}
		
		private void setImage() {
			File external1 = new File(Environment.getExternalStorageDirectory(), "temp_holder.jpg");
			File external2 = new File(Environment.getExternalStorageDirectory(), "temp_holder2.jpg");
			File internal = new File(InternalStorage.getFileString());
			
			try {
				copyFile(external1, internal);
			} catch (IOException e) {
				Log.d(TAG, "copyFile1 failed");
			}
			
			try {
				copyFile(internal, external2);
			} catch (IOException e) {
				Log.d(TAG, "copyFile2 failed");
			}
			
			Log.d(TAG, "bg_set: " + bg_set);
			bg_set = true;
			if(bg_set) {
				mBackgroundImage = BitmapFactory.decodeFile(InternalStorage.getFileString());
			} else {
				mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.dark);
			}
		}
		
		public void copyFile(File src, File dst) throws IOException
		{
		    FileChannel inChannel = new FileInputStream(src).getChannel();
		    FileChannel outChannel = new FileOutputStream(dst).getChannel();
		    try
		    {
		        inChannel.transferTo(0, inChannel.size(), outChannel);
		    }
		    finally
		    {
		        if (inChannel != null)
		            inChannel.close();
		        if (outChannel != null)
		            outChannel.close();
		    }
		}
	}
}