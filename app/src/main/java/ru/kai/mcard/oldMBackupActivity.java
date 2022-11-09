package ru.kai.mcard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;

import ru.kai.mcard.utility.Common;

public class oldMBackupActivity extends AppCompatActivity {

    TextView tvBackupDateTime;
    TextView tvBackupInCloudDone;
    TextView tvBackupInCloudYesNo;
    TextView tvBackupInCloudDateTime;
    TextView tvBackupOldState;
    TextView tvBackupNewStateBeforeBackup;
    TextView tvBackupNewStateAfterBackup;
    TextView tvRestoreNewStateRestore;
    TextView tvOnRestoreDone;
    RelativeLayout rlBackupsContainer;

    // настройки
    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);

        setContentView(R.layout.old_m_activity_backup);

        Toolbar mToolBar = (Toolbar) findViewById(R.id.mBackupsToolbar);
        setSupportActionBar(mToolBar);

        tvBackupDateTime = (TextView) findViewById(R.id.tvBackupDateTime_old);
        tvBackupInCloudDone = (TextView) findViewById(R.id.tvBackupInCloudDone_old);
        tvBackupInCloudYesNo = (TextView) findViewById(R.id.tvBackupInCloudYesNo_old);
        tvBackupInCloudDateTime = (TextView) findViewById(R.id.tvBackupInCloudDateTime_old);
        tvBackupOldState = (TextView) findViewById(R.id.tvBackupOldState_old);
        tvBackupNewStateBeforeBackup = (TextView) findViewById(R.id.tvBackupNewStateBeforeBackup_old);
        tvBackupNewStateAfterBackup = (TextView) findViewById(R.id.tvBackupNewStateAfterBackup_old);
        tvRestoreNewStateRestore = (TextView) findViewById(R.id.tvRestoreNewStateRestore_old);
        tvOnRestoreDone = (TextView) findViewById(R.id.tvOnRestoreDone_old);
        rlBackupsContainer = (RelativeLayout) findViewById(R.id.rlBackupsContainer_old);

        if (mSettings.contains(Constants.LAST_BACKUPS_BTN_PRESS)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_BTN_PRESS, 0L);
            setBackupPressDateTime(curTimeStamp);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_IN_CLOUD_DONE)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_IN_CLOUD_DONE, 0L);
            setBackupInCloudDone(curTimeStamp);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_IN_CLOUD_YES_NO)) {
            Boolean curYesNo = mSettings.getBoolean(Constants.LAST_BACKUPS_IN_CLOUD_YES_NO, false);
            setBackupYesNo(curYesNo);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_IN_CLOUD_LAST_DATE_TIME)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_IN_CLOUD_LAST_DATE_TIME, 0L);
            setBackupInCloudLastDateTime(curTimeStamp);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_OLD_STATE)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_OLD_STATE, 0L);
            setBackupOldState(curTimeStamp);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_NEW_STATE_BEFORE)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_NEW_STATE_BEFORE, 0L);
            setBackupNewStateBefore(curTimeStamp);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_NEW_STATE_AFTER)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_NEW_STATE_AFTER, 0L);
            setBackupNewStateAfter(curTimeStamp);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_NEW_STATE_RESTORE)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_NEW_STATE_RESTORE, 0L);
            setBackupNewStateRestore(curTimeStamp);
        }
        if (mSettings.contains(Constants.LAST_BACKUPS_ON_RESTORE_DONE)) {
            long curTimeStamp = mSettings.getLong(Constants.LAST_BACKUPS_ON_RESTORE_DONE, 0L);
            setBackupOnRestoreDone(curTimeStamp);
        }

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

/*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/

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

    private void setBackupPressDateTime(long lastBackupPressTimeStamp) {
        if (lastBackupPressTimeStamp == 0L) {
            tvBackupDateTime.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupPressTimeStamp);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupPressTimeStamp, DateUtils.FORMAT_SHOW_TIME);
            tvBackupDateTime.setText(date + "  " + time);

        }
    }

    private void setBackupInCloudDone(long lastBackupInCloudTimeStamp) {
        if (lastBackupInCloudTimeStamp == 0L) {
            tvBackupInCloudDone.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupInCloudTimeStamp);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupInCloudTimeStamp, DateUtils.FORMAT_SHOW_TIME);
            tvBackupInCloudDone.setText(date + "  " + time);

        }
    }

    private void setBackupYesNo(Boolean curYesNo) {
        if (curYesNo){
            tvBackupInCloudYesNo.setText("YES !!!");
        }else {
            tvBackupInCloudYesNo.setText("NO (((");
        }

    }

    private void setBackupInCloudLastDateTime(long lastBackupTimeStamp) {
        if (lastBackupTimeStamp == 0L) {
            tvBackupInCloudDateTime.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupTimeStamp);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupTimeStamp, DateUtils.FORMAT_SHOW_TIME);
            tvBackupInCloudDateTime.setText(date + "  " + time);

        }
    }

    private void setBackupOldState(long lastBackupOldState) {
        //if (lastBackupOldState == 0L) {
        //    tvBackupOldState.setText("");
        //} else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupOldState);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupOldState, DateUtils.FORMAT_SHOW_TIME);
            tvBackupOldState.setText(date + "  " + time);

        //}
    }

    private void setBackupNewStateBefore(long lastBackupNewStateBefore) {
        //if (lastBackupNewStateBefore == 0L) {
        //    tvBackupNewStateBeforeBackup.setText("");
        //} else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupNewStateBefore);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupNewStateBefore, DateUtils.FORMAT_SHOW_TIME);
            tvBackupNewStateBeforeBackup.setText(date + "  " + time);

        //}
    }

    private void setBackupNewStateAfter(long lastBackupNewStateAfter) {
        //if (lastBackupNewStateAfter == 0L) {
        //    tvBackupNewStateAfterBackup.setText("");
        //} else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupNewStateAfter);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupNewStateAfter, DateUtils.FORMAT_SHOW_TIME);
            tvBackupNewStateAfterBackup.setText(date + "  " + time);

        //}
    }

    private void setBackupNewStateRestore(long lastBackupNewStateRestore) {
        if (lastBackupNewStateRestore == 0L) {
            tvRestoreNewStateRestore.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupNewStateRestore);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupNewStateRestore, DateUtils.FORMAT_SHOW_TIME);
            tvRestoreNewStateRestore.setText(date + "  " + time);

        }
    }

    private void setBackupOnRestoreDone(long lastBackupOnRestoreDone) {
        if (lastBackupOnRestoreDone == 0L) {
            tvOnRestoreDone.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", lastBackupOnRestoreDone);
            CharSequence time = DateUtils.formatDateTime(oldMBackupActivity.this, lastBackupOnRestoreDone, DateUtils.FORMAT_SHOW_TIME);
            tvOnRestoreDone.setText(date + "  " + time);

        }
    }

    public void doBackup(View view) {
        MCardCustomBackupAgent.requestBackup(getApplicationContext());

        Date curDateTime = new Date();
        long curTimeStamp = curDateTime.getTime();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(Constants.LAST_BACKUPS_BTN_PRESS, curTimeStamp);
        editor.apply();
        setBackupPressDateTime(curTimeStamp);

        Snackbar snackbar = Common.getCustomSnackbar(oldMBackupActivity.this, rlBackupsContainer, getString(R.string.backup_btn_backup_done));
        snackbar.show();
    }

    public void doRestore(View view) {
        MCardCustomBackupAgent.requestRestore(getApplicationContext());
        Snackbar snackbar = Common.getCustomSnackbar(oldMBackupActivity.this, rlBackupsContainer, getString(R.string.backup_btn_restore_done));
        snackbar.show();
    }

    public void backupToSDCard(View view) {
        Intent intent = new Intent(oldMBackupActivity.this, MBackupSDCardActivity.class);
        startActivity(intent);
    }
}
