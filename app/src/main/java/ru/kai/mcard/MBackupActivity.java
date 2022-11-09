package ru.kai.mcard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.kai.mcard.utility.Common;

public class MBackupActivity extends AppCompatActivity {

    RelativeLayout rlBackupsContainer;

    final int NUMBER_OF_REQUEST = 23401;  // проверка запроса разрешения на доступ к SD карте

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

        setContentView(R.layout.m_activity_backup);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA


        rlBackupsContainer = (RelativeLayout) findViewById(R.id.rlBackupsContainer);

        Toolbar mToolBar = (Toolbar) findViewById(R.id.mBackupsToolbar);
        setSupportActionBar(mToolBar);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Окно Backup-ов (MBackupActivity)");
        mTracker.setScreenName("Окно Backup-ов (MBackupActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
                backupsActivityClose();
                break;
            case R.id.common_img_Close:
                backupsActivityClose();
                break;
        }

        return true;
    }

    private void backupsActivityClose() {

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void backupToSDCard(View view) {

/*      не будем делать проверку, т.к. не всегда корректно работает со всеми устройствами. Пользователи сами знают, есть у них sd карта или нет.
        В конце концов они могут захотеть сделать backup просто в файловую систему, затем перенести его на компьютер и хранить там.
        if (Common.getSDcardPath() == null){
            Snackbar snackbar = Common.getCustomSnackbar(MBackupActivity.this, rlBackupsContainer, getString(R.string.backup_sdcard_is_epsend));
            snackbar.show();
            return;
        }
*/

        // проверим для Android M - 6:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int canRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int canWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (canRead != PackageManager.PERMISSION_GRANTED || canWrite != PackageManager.PERMISSION_GRANTED) {
/*
                //Нужно ли нам показывать объяснения , зачем нам нужно это разрешение
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //показываем объяснение
                } else {
                    //просим разрешение
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, NUMBER_OF_REQUEST);
                }
*/
                //просим разрешение
                ActivityCompat.requestPermissions(MBackupActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, NUMBER_OF_REQUEST);

            } else {

                //ваш код
                Intent intent = new Intent(MBackupActivity.this, MBackupSDCardActivity.class);
                startActivity(intent);
            }
        }else {
            Intent intent = new Intent(MBackupActivity.this, MBackupSDCardActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case NUMBER_OF_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("TAG", "Пользователь дал разрешение");

                    Intent intent = new Intent(MBackupActivity.this, MBackupSDCardActivity.class);
                    startActivity(intent);

                } else {
                    Log.e("TAG", "Пользователь отклонил разрешение");
                }
                return;
            }
        }
    }

}
