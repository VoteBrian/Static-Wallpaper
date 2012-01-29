package net.votebrian.staticwall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class StaticWallpaper extends WallpaperService {
  public static final String SHARED_PREFS_NAME="staticSettings";
  
  // Internal Storage File Information
  public static final String INT_BG_FILENAME = "staticBG.jpg";
  public static File INT_BG_FILE = null;
  public static Uri INT_BG_URI = null;
  
  // External Storage File Information
  public static final String EXT_BG_FILENAME = "temp_holder.jpg";
  public static File EXT_BG_FILE = null;
  
  // State checks
  public static Boolean INT_SET = false;
  public static Boolean NEW_BG = false;
  
  private static final String TAG = "StaticWallpaper";
  
    /** Called when the activity is first created. */
  @Override
  public void onCreate() {
    super.onCreate();
    //android.os.Debug.waitForDebugger();
    Log.d(TAG, "OnCreate");
    
    // create the internal file
    File path = getDir("staticBG", MODE_PRIVATE);
    
    INT_BG_FILE = new File(path, INT_BG_FILENAME);
    try {
      // createNewFile returns 1 if it creates the file, 0 if the file already exists.
      if (INT_BG_FILE.createNewFile()) {
    	  Log.d(TAG, "create New File");
    	  INT_SET = false;
      } else {
    	  INT_SET = true;
      }
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
    Log.d(TAG, "onCreateEngine");
    return new StaticEngine();
  }



  class StaticEngine extends Engine 
      implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "StaticEngine";

    private SharedPreferences mPrefs;
    private Bitmap mBackgroundImage;
    
    public int mWidth = 0;
    public int mHeight = 0;
    
    StaticEngine() {
      Log.d(TAG, "StaticEngine Constructor");
      mPrefs = StaticWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
      mPrefs.registerOnSharedPreferenceChangeListener(this);
      onSharedPreferenceChanged(mPrefs, null);
    }

    public void onSharedPreferenceChanged(
        SharedPreferences prefs, String key) {
      Log.d(TAG, "onSharedPreferenceChanged");

      Boolean result = prefs.getBoolean("isSet", false);
      Log.d(TAG, result.toString());
      if (result) {
        INT_SET = false;
        NEW_BG = true;
        
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
    }
    
    @Override
    public void onVisibilityChanged(boolean visible) {
      //Log.d(TAG, "onVisibilityChanged");
      drawFrame();

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw new RuntimeException("Thread.sleep failed", e);
      }
      drawFrame();
    }
    
    @Override
    public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      //Log.d(TAG, "onSurfaceChanged");
      mWidth = width;
      mHeight = height;

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
      
      super.onSurfaceDestroyed(holder);
    }
    
    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
        float xStep, float yStep, int xPixels, int yPixels) {
      //Log.d(TAG, "onOffsetsChanged");
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
          c.drawBitmap(mBackgroundImage, null, new Rect(0,0,mWidth,mHeight), null);
          c.restore();
        }
      } finally {
        if(c != null) {
          holder.unlockCanvasAndPost(c);
        }
      }
    }
    
    private void setImage() { 
      //Log.d(TAG, "setImage()");
      if(INT_SET) {
        mBackgroundImage = BitmapFactory.decodeFile(INT_BG_FILE.toString());
      } else if(!INT_SET && NEW_BG) {
        Log.d(TAG, "copy temp");
        try {
          copyFile(EXT_BG_FILE, INT_BG_FILE);
        } catch (IOException e) {
          throw new RuntimeException("Could not copy temp file to Internal Storage", e);
        }
        mBackgroundImage = BitmapFactory.decodeFile(INT_BG_FILE.toString());

        INT_SET = true;
        NEW_BG = false;
        EXT_BG_FILE.delete();
      } else {
        mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_bg);
      }
    }
    
    public void copyFile(File src, File dst) throws IOException
    {
      FileChannel inChannel = new FileInputStream(src).getChannel();
      FileChannel outChannel = new FileOutputStream(dst).getChannel();
      
      try {
          inChannel.transferTo(0, inChannel.size(), outChannel);
      }
      finally {
        if (inChannel != null) {
          inChannel.close();
        }
        if (outChannel != null) {
          outChannel.close();
        }
      }
    }
  }
}