package net.votebrian.apps;

import java.io.File;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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
		Log.d(TAG, "OnCreate");
		
		super.onCreate();
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
	
	/*  ------------------------------------------------
	 *                      ENGINE
	 *  ------------------------------------------------
	 */
	class StaticEngine extends Engine
		implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		private final Handler mHandler = new Handler();
		private SharedPreferences mPrefs;
		private boolean mVisible;
		private Bitmap mBackgroundImage;
		private static final String TAG = "StaticEngine";
		
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
        }

		public void onSharedPreferenceChanged(
				SharedPreferences prefs, String key) {
			Log.d(TAG, "onSharedPreferenceChanged");
			
			String path = prefs.getString("static_background", "default");
			File f = new File(path);
            if (f.exists()) {
            	mBackgroundImage = BitmapFactory.decodeFile(path);
            	//f.delete();
            } else {
            	mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.dark);
            }
            
            drawFrame();
            
		}
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			Log.d(TAG, "onCreate");
			
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
			//setTouchEventsEnabled(true);
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
			if(visible) {
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
			
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mDrawBG);
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
			if(mVisible) {
				// nothing
			}
		}
	}
}