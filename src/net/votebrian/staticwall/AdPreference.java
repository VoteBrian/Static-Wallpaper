package net.votebrian.staticwall;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdPreference extends Preference {

  public AdPreference(Context context) {
    super(context);
  }

  public AdPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected View onCreateView(ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    return inflater.inflate(R.xml.ad_preference, null);
  }

}
