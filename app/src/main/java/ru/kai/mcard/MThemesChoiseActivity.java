package ru.kai.mcard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.kai.mcard.utility.Common;


public class MThemesChoiseActivity extends AppCompatActivity {

    int newTheme;
    String newThemeLitera;
    Toolbar mToolBar;

    // настройки
    private SharedPreferences mSettings;

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // настройки
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        // установка темы
        setTheme(Common.getCurrentTheme(this));
/*
        //int currentTheme = R.style.Theme_StandartGreen;
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME)) {
            // Получаем число из настроек
            newTheme = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_THEME, R.style.Theme_StandartGreen);
            setTheme(newTheme);
        }
*/

        setContentView(R.layout.m_activity_themes_choise);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        mToolBar = (Toolbar) findViewById(R.id.mThemesChoiseToolbar);
        setSupportActionBar(mToolBar);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Выбор цветовой темы (MThemesChoiseActivity)");
        mTracker.setScreenName("Выбор цветовой темы (MThemesChoiseActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

/*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/

        switch (id) {
            case R.id.common_img_OK:
                themesChoiceSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                themesChoiceClose();
                break;
        }

        return true;
    }

    public void themesChoiceClose() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void themesChoiceSave() {

        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Применили цветовую тему.")
                .build());

        // Запоминаем тему
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(Constants.APP_PREFERENCES_CURRENT_THEME_S, newThemeLitera);
        editor.apply();
        Intent intent = new Intent();
        intent.putExtra("currentTheme", newTheme);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void themesChoiceSave(View view) {
        themesChoiceSave();
    }

    public void choiseTheme(View view) {
        AppBarLayout mThemesChoiseAppBarLayout = (AppBarLayout)findViewById(R.id.mThemesChoiseAppBarLayout);
        switch (view.getId()){
            case R.id.imageView:
                newTheme = R.style.Theme_NewYear;
                setTheme(newTheme);
                getWindow().setStatusBarColor(Common.getPrimeryDarkColor(newTheme));
                setContentView(R.layout.m_activity_themes_choise);
                break;
            case R.id.textView:
                newTheme = R.style.Theme_NewYear;
                setTheme(newTheme);
                setContentView(R.layout.m_activity_themes_choise);
                // здесь лучше показывать романтическое описание темы
                break;
            case R.id.ivGreenStandart:
                newTheme = R.style.Theme_StandartGreen;
                newThemeLitera = Constants.THEME_LITERA_G;
                setTheme(newTheme);
                setContentView(R.layout.m_activity_themes_choise);
                break;
            case R.id.ivLemonStandart:
                newTheme = R.style.Theme_StandartLemon;
                newThemeLitera = Constants.THEME_LITERA_L;
                setTheme(newTheme);
                setContentView(R.layout.m_activity_themes_choise);
                break;
            case R.id.ivPinkStandart:
                newTheme = R.style.Theme_StandartPink;
                newThemeLitera = Constants.THEME_LITERA_P;
                setTheme(newTheme);
                setContentView(R.layout.m_activity_themes_choise);
                break;
            case R.id.ivCornflowersStandart:
                newTheme = R.style.Theme_StandartCornflower;
                newThemeLitera = Constants.THEME_LITERA_C;
                setTheme(newTheme);
                setContentView(R.layout.m_activity_themes_choise);
                break;

        }

        mToolBar = (Toolbar) findViewById(R.id.mThemesChoiseToolbar);
        setSupportActionBar(mToolBar);
        // почему-то цвет StatusBar-а не устанавливается при смене темы. Установим его при помощи этой процедуры
        Common.setStatusBarColor(this, newTheme);

    }

    public void setStatusBarColor(int newTheme) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = this.getWindow();

            int statusBarColor1 = getResources().getColor(Common.getPrimeryDarkColor(newTheme));

            if (statusBarColor1 == Color.BLACK && window.getNavigationBarColor() == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setStatusBarColor(statusBarColor1);
        }
    }
}
