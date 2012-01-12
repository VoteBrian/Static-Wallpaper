package net.votebrian.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class StaticWallpaper extends WallpaperService {
	public static final String SHARED_PREFS_NAME="staticSettings";
	
	public static final String INT_BG_FILENAME = "staticBG.jpg";
	public static final String EXT_BG_FILENAME = "temp_holder.jpg";
	
	public static File INT_BG_FILE = null;
	public static File EXT_BG_FILE = null;
	
	public static Uri INT_BG_URI = null;
	
	public static Boolean BG_FILE_EXISTS = false;
	
	private Boolean INT_SET = false;
	private Boolean NEW_BG = false;
	
	//private static final String TAG = "StaticWallpaper";
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();
		//android.os.Debug.waitForDebugger();
		//Log.d(TAG, "OnCreate");
		
		// create the internal file
		File path = getDir("staticBG", MODE_PRIVATE);
		
		INT_BG_FILE = new File(path, INT_BG_FILENAME);
		try {
			// createNewFile returns 1 if it creates the file, 0 if the file already exists.
			BG_FILE_EXISTS = !INT_BG_FILE.createNewFile();
		} catch (IOException e) {
			throw new RuntimeException("Could not create Internal Storage file to hold background image.", e);
		}
		INT_BG_URI = Uri.fromFile(INT_BG_FILE);
	}
	
	@Override
	public void onDestroy() {
		//Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		//Log.d(TAG, "onCreateEngine");
		return new StaticEngine();
	}
	
	/*  ------------------------------------------------
	 *                      ENGINE
	 *  ------------------------------------------------
	 */
	class StaticEngine extends Engine
		implements SharedPreferences.OnSharedPreferenceChangeListener {
		//private static final String TAG = "StaticEngine";
		
		private final Handler mHandler = new Handler();
		private SharedPreferences mPrefs;
		private boolean mVisible;
		private Bitmap mBackgroundImage;
		
		private final Runnable mDrawBG = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        
        StaticEngine() {
        	//Log.d(TAG, "StaticEngine");
        	mPrefs = StaticWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
            
            if(!BG_FILE_EXISTS) {
            	//Log.d(TAG, "BG File does NOT exist");
	            SharedPreferences.Editor editor = mPrefs.edit();
	            editor.putBoolean("isSet", false);
	            editor.commit();
            }
        }

		public void onSharedPreferenceChanged(
				SharedPreferences prefs, String key) {
			//Log.d(TAG, "onSharedPreferenceChanged");
			
			// check if the background image has been set
			Boolean result = prefs.getBoolean("isSet", false);
			if (result) {
				NEW_BG = true;
				INT_SET = false;
				
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putBoolean("isSet", false);
				editor.commit();
			}

			setImage();
            drawFrame();
            
		}
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			//Log.d(TAG, "onCreate");
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(false);
		}
		
		@Override
		public void onDestroy() {
			//Log.d(TAG, "onDestroy");
			super.onDestroy();
			mHandler.removeCallbacks(mDrawBG);
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			//Log.d(TAG, "onVisibilityChanged");
			mVisible = visible;
			if(mVisible) {
				drawFrame();
			} else {
				mHandler.removeCallbacks(mDrawBG);
			}
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			//Log.d(TAG, "onSurfaceChanged");
			drawFrame();
		}
		
		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			//Log.d(TAG, "onSurfaceCreated");
			super.onSurfaceCreated(holder);
		}
		
		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			//Log.d(TAG, "onSurfaceDestroyed");
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
			// Log.d(TAG, "onOffsetsChanged");
		}
		
		public void drawFrame() {
			//Log.d(TAG, "drawFrame");
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
			if(NEW_BG && INT_SET) {
				mBackgroundImage = BitmapFactory.decodeFile(INT_BG_FILE.toString());
			} else if(NEW_BG && EXT_BG_FILE.exists()) {
				try {
					copyFile(EXT_BG_FILE, INT_BG_FILE);
					INT_SET = true;
				} catch (IOException e) {
					throw new RuntimeException("Could not copy temp file to Internal Storage", e);
				}
				mBackgroundImage = BitmapFactory.decodeFile(INT_BG_FILE.toString());
				
				EXT_BG_FILE.delete();
			} else {
				mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_bg);
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