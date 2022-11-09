package ru.kai.mcard;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AboutActivity extends Activity {

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
        // установка темы
        SharedPreferences mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        //int currentTheme = R.style.Theme_StandartGreen;
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME)) {
            // Получаем число из настроек
            int currentTheme = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_THEME, R.style.Theme_StandartGreen);
            setTheme(currentTheme);
        }
*/

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_about);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_for_about_activity);

        TextView tvAbout = (TextView)findViewById(R.id.tvAbout);

        String versionName = getString(R.string.text_version);
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String versionCode = "";
        try {
            versionCode = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
            versionCode = " (" + versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String versionAbout = getString(R.string.text_about) + " ";
        tvAbout.setText(versionAbout + versionName + versionCode);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "AboutActivity");
        mTracker.setScreenName("ScreenName: " + "AboutActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }




}
