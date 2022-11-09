package ru.kai.mcard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.kai.mcard.utility.Common;

public class MBackupSDCardActivity extends AppCompatActivity {

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));
/*
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        //int currentTheme = R.style.Theme_StandartGreen;
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME)) {
            // Получаем число из настроек
            int currentTheme = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_THEME, R.style.Theme_StandartGreen);
            setTheme(currentTheme);
        }
*/

        setContentView(R.layout.m_activity_backup_sdcard);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA


        Toolbar mToolBar = (Toolbar) findViewById(R.id.mMBackupSDCardToolbar);
        setSupportActionBar(mToolBar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_common, menu);

        //Button common_img_OK = (Button) findViewById(R.id.common_img_OK);
        //common_img_OK.setVisibility(View.GONE);
        menu.findItem(R.id.common_img_OK).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.common_img_OK:
                backupsSDCardActivityClose();
                break;
            case R.id.common_img_Close:
                backupsSDCardActivityClose();
                break;
        }

        return true;
    }

    private void backupsSDCardActivityClose() {

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void goForBackupToSDCard(View view) {

        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Backup")
                .setAction("BackupSDCard open")
                .build());

        Intent intent = new Intent(MBackupSDCardActivity.this, MBackupSDCardForBackupActivity.class);
        startActivityForResult(intent, 0);
    }


    public void goForRestoreFromSDCard(View view) {

        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Backup")
                .setAction("RestoreSDCard open")
                .build());

        Intent intent = new Intent(MBackupSDCardActivity.this, MBackupSDCardForRestoreActivity.class);
        startActivityForResult(intent, 0);
    }


}
