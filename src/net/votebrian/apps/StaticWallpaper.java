package net.votebrian.apps;

import java.io.File;
import java.io.IOException;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class StaticWallpaper extends WallpaperService {
	public static final String SHARED_PREFS_NAME="staticSettings";
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();
		//android.os.Debug.waitForDebugger();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
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
		private final Paint mPaint = new Paint();
		private int mWidth;
		private int mHeight;
		private Bitmap mBackgroundImage;
		
		//private Picture mPicture = new Picture();
		
		private final Runnable mDrawBG = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        
        StaticEngine() {
        	// create the Paint
        	final Paint paint = mPaint;
        	paint.setColor(0xffffffff);
        	paint.setAntiAlias(true);
        	paint.setStrokeWidth(2);
        	paint.setStrokeCap(Paint.Cap.ROUND);
        	paint.setStyle(Paint.Style.STROKE);
        	
        	//mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        	
        	mPrefs = StaticWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
        }

		public void onSharedPreferenceChanged(
				SharedPreferences prefs, String key) {
			
			String path = prefs.getString("static_background", "default");
			File f = new File(path);
            if (f.exists()) {
            	mBackgroundImage = BitmapFactory.decodeFile(path);
            	f.delete();
            } else {
            	mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.dark);
            }
            
            //drawFrame();
            
		}
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
			//setTouchEventsEnabled(true);
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mDrawBG);
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if(visible) {
				drawFrame();
			} else {
				mHandler.removeCallbacks(mDrawBG);
			}
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// TODO: something with the height and width of the the surface.
			mWidth = width;
			mHeight = height;
			//mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, mWidth, mHeight, true);
			drawFrame();
		}
		
		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
		}
		
		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mDrawBG);
		}
		
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xStep, float yStep, int xPixels, int yPixels) {
			// Do nothing if we don't have to.  Else, run drawFrame().
			// drawFrame();
		}
		
		public void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try{
				c = holder.lockCanvas();
				if(c != null) {
					// TODO: draw a line
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