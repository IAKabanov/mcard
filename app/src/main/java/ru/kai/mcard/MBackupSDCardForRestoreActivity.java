package ru.kai.mcard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.backup.BackupAgent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.FileChannel;
import java.util.Map;

import ru.kai.mcard.utility.Common;

@Deprecated
public class MBackupSDCardForRestoreActivity extends AppCompatActivity {

    File mDataBaseFile;
    File mPrefsFile;
    File mFotosDir;

    String inCopyFromZipTargetPrefsFileName;
    String inCopyFromZipTargetDBFileName;
    String inZipFileName;
    File inputZipFile;

    private final String curTAG = "MBackupSDCardForRestoreActivity";

    ProgressDialog loadingDialog;

    final public static String TAG = BackupAgent.class.getSimpleName();
    final private static String BACKUP_KEY_FILES = "Files_Backup_";
    final private static String BACKUP_KEY_PREFS = "prefs";
    final private static String BACKUP_KEY_DB = "db";
    final private static String DB_NAME = DBMethods.DB_NAME;

    // настройки
    private SharedPreferences mSettings;

    TextView tvBackupSDCardRestoreDirFile;
    TextView tvBackupToSDCardRestoreHelp;
    RelativeLayout rlBackupBackupSDCardRestoreContainer;
    Button btnDoRestoreFromSDCard;

    String pathForRestore = "";

    Boolean backupIsOn = false; // - показывает есть backup или нет

    FragmentManager manager;
    RestoreSDLoadingDialogFragment myRestoreDialogFragment;

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // настройки
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_backup_sdcard_restore);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA


        Toolbar mToolBar = (Toolbar) findViewById(R.id.mMBackupSDCardRestoreToolbar);
        setSupportActionBar(mToolBar);

        tvBackupSDCardRestoreDirFile = (TextView)findViewById(R.id.tvBackupSDCardRestoreDirFile);
        rlBackupBackupSDCardRestoreContainer = (RelativeLayout)findViewById(R.id.rlBackupBackupSDCardRestoreContainer);
        btnDoRestoreFromSDCard = (Button)findViewById(R.id.btnDoRestoreFromSDCard);

        String initPath = "/";
        try {
            initPath = Common.getSDcardPath();
        } catch (Exception e) {
            Log.e(curTAG, e.getMessage());
            e.printStackTrace();
        }
        pathForRestore = initPath;  // инициализация

        if (mSettings.contains(Constants.RESTORE_PATH_FROM_SD_CARD)) {
            pathForRestore = mSettings.getString(Constants.RESTORE_PATH_FROM_SD_CARD, initPath);
        }
        tvBackupSDCardRestoreDirFile.setText(pathForRestore);
        try {
            inputZipFile = new File(pathForRestore);
        }catch (Exception e){
            Log.e(curTAG, e.getMessage());
            e.printStackTrace();
        }
        setFilesPath();
        checkBackup();

        mDataBaseFile = new File(getApplicationContext().getDatabasePath(DB_NAME).getAbsolutePath());
        mPrefsFile = new File(Environment.getDataDirectory() + "/data/" + getPackageName() + "/shared_prefs/" + Constants.APP_PREFERENCES + ".xml");

        manager = getSupportFragmentManager();
        myRestoreDialogFragment = new RestoreSDLoadingDialogFragment(); //  <-- внутренний класс

        // не показывать обучающую информацию
        tvBackupToSDCardRestoreHelp = (TextView)findViewById(R.id.tvBackupToSDCardRestoreHelp);

        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_BACKUP_SD_CARD_RESTORE_HELP)) {
            // Получаем число из настроек
            int showBackupToSDCardRestoreHelp = mSettings.getInt(Constants.APP_PREFERENCES_SHOW_BACKUP_SD_CARD_RESTORE_HELP, 1);
            if (showBackupToSDCardRestoreHelp == 0)tvBackupToSDCardRestoreHelp.setVisibility(View.GONE);
        }

        tvBackupToSDCardRestoreHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.do_not_show_help_info);
                //String message = getResources().getString(R.string.do_not_show_help_info);
                String btnPositiveString = getResources().getString(R.string.but_continue_help);
                String btnNegativeString = getResources().getString(R.string.but_cancel_help);

                AlertDialog.Builder ad = new AlertDialog.Builder(MBackupSDCardForRestoreActivity.this);
                ad.setTitle(title);  // заголовок
                //ad.setMessage(message); // сообщение
                ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putInt(Constants.APP_PREFERENCES_SHOW_BACKUP_SD_CARD_RESTORE_HELP, 0);
                        editor.apply();
                        tvBackupToSDCardRestoreHelp.setVisibility(View.GONE);
                    }
                });
                ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        //setResult(RESULT_CANCELED);
                        //finish();
                    }
                });
                ad.show();
            }
        });
        //-------------------------


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Restore с SD карты (MBackupSDCardForRestoreActivity)");
        mTracker.setScreenName("Restore с SD карты (MBackupSDCardForRestoreActivity)");
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
                backupsSDCardActivityClose();
                break;
            case R.id.common_img_Close:
                backupsSDCardActivityClose();
                break;
        }

        return true;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {return;}
        if(resultCode == RESULT_OK) {
            pathForRestore = data.getStringExtra("chosen_file_name");
            tvBackupSDCardRestoreDirFile.setText(pathForRestore);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(Constants.RESTORE_PATH_FROM_SD_CARD, pathForRestore);
            editor.apply();

            //inZipFileName = pathForRestore + "m_sd_card_copy_" + sdf.format(curTimeStamp) + ".mcc";
            try {
                inputZipFile = new File(pathForRestore);
            }catch (Exception e){
                Log.e(curTAG, e.getMessage());
                e.printStackTrace();
            }

            setFilesPath();
            checkBackup();
        }
    }

    private void setFilesPath(){

        mFotosDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (mFotosDir != null) {
            if (!mFotosDir.exists()) {
                mFotosDir.mkdir();
            }

            // копия mPrefsFile
            inCopyFromZipTargetPrefsFileName = mFotosDir + "/shared_prefs_copy.xml";
            // копия БД
            inCopyFromZipTargetDBFileName = mFotosDir + "/database_copy.db";
        }

    }


    @SuppressLint("LongLogTag")
    private Boolean getBacupIsOn(){
/*
        File dir = new File(pathForBackup);
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.getAbsolutePath().equals(inCopyFromZipTargetDBFileName)) {
                return true;
            }else if (file.getAbsolutePath().equals(inCopyFromZipTargetPrefsFileName)) {
                return true;
            }
        }
*/
        if (pathForRestore.equals("")){
            return true;
        }

        try {
            inputZipFile = new File(pathForRestore);
        }catch (Exception e){
            Log.e(curTAG, e.getMessage());
            e.printStackTrace();
        }

        if (inputZipFile.isDirectory()){  // при инициализации (чтобы не выдавалось сообщение, что нет бэкапа)
            return true;
        }

        if ((inputZipFile.exists())&&(inputZipFile.getName().contains("m_sd_card_copy_"))&&(inputZipFile.getName().contains(".mcc"))){
            return true;
        }
        return false;
    }

    private void setHomeDirectory(){

    }

    private void checkBackup(){
        backupIsOn = getBacupIsOn();
        if (!backupIsOn) {
            Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardForRestoreActivity.this, rlBackupBackupSDCardRestoreContainer, getString(R.string.backup_sdcard_backup_is_epsend));
            snackbar.show();
        }

        if (!backupIsOn){
            btnDoRestoreFromSDCard.setEnabled(false);
        }else {
            btnDoRestoreFromSDCard.setEnabled(true);
        }

    }


    private void backupsSDCardActivityClose() {

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void chooseBackupSDCardRestoreFile(View view) {

        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Backup")
                .setAction("ChosenDirActivity from RestoreSDCard open")
                .build());

        Intent intent = new Intent(MBackupSDCardForRestoreActivity.this, MChosenDirActivity.class);
        intent.putExtra("operation", "restore");
        intent.putExtra("path", pathForRestore);
        startActivityForResult(intent, Constants.CHOOSE_DIR);
    }

    public void doRestoreFromSDCard(View view) {
        //Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardActivity.this, rlBackupsContainer, getString(R.string.backup_sdcard_restore_begin));
        //snackbar.show();

        if (!backupIsOn) {
            Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardForRestoreActivity.this, rlBackupBackupSDCardRestoreContainer, getString(R.string.backup_sdcard_backup_is_epsend));
            snackbar.show();
            return;
        }
        if(pathForRestore.equals("")){
            Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardForRestoreActivity.this, rlBackupBackupSDCardRestoreContainer, getString(R.string.backup_sdcard_backup_is_epsend));
            snackbar.show();
            return;
        }


        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Backup")
                .setAction("do RestoreSDCard")
                .build());


        // асинхронная загрузка restore-а
        new LoadingTaskRestoreSD().execute();


        //btnDoRestoreFromSDCard.setEnabled(false);
        //myRestoreDialogFragment.show(manager, "loading");
/*
        try {
            doRestore();
        } catch (IOException e) {
            e.printStackTrace();
        }
        myRestoreDialogFragment.dismiss();
        Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardForRestoreActivity.this, rlBackupBackupSDCardRestoreContainer, getString(R.string.backup_sdcard_restore_fin));
        snackbar.show();
*/



        //new LoadingTaskRestoreSD().execute();
    }

    @SuppressLint("LongLogTag")
    public void doRestore() throws IOException {
        Log.d(TAG, "onRestore");

        // сначала почистим от старых фоток, чтобы не получилось наложения
        File[] oldFotosList = mFotosDir.listFiles();
        for (File fotoFile : oldFotosList) {
            fotoFile.delete();
        }

/*
        // переименуем
        String mccFilePath = mFotosDir + "/" + inputZipFile.getName();
        File mccFile = new File(mccFilePath);
        File newNamedInputFile = new File(mccFilePath.replace(".mcc", ".zip"));
        mccFile.renameTo(newNamedInputFile);
*/

        Common.decompress(inputZipFile, mFotosDir);
        //File dir = new File(pathForBackup);


        //File dir = new File(pathForBackup);
        //File[] listFiles = dir.listFiles();
        File[] listFiles = mFotosDir.listFiles();


        for (File file : listFiles) {

            if (file.getAbsolutePath().equals(inCopyFromZipTargetDBFileName)) {
                // файл базы данных /////////////////
                File backupDB = null;
                try {
                    backupDB = new File(inCopyFromZipTargetDBFileName);
                } catch (Exception e) {
                    Log.e(curTAG, e.getMessage());
                    e.printStackTrace();
                }

                if (backupDB != null) {
                    try {
                        if (mDataBaseFile.exists()) {
                            FileChannel src = new FileInputStream(backupDB).getChannel();
                            FileChannel dst = new FileOutputStream(mDataBaseFile).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        }
                    } catch (Exception e) {
                        Log.e(curTAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }else if (file.getAbsolutePath().equals(inCopyFromZipTargetPrefsFileName)) {
                // файл настроек /////////////////
                ObjectInputStream inputPrefs = null;
                try {
                    inputPrefs = new ObjectInputStream(new FileInputStream(inCopyFromZipTargetPrefsFileName));
                    SharedPreferences.Editor prefEdit = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE).edit();
                    prefEdit.clear();
                    Map<String, ?> entries = (Map<String, ?>) inputPrefs.readObject();
                    for (Map.Entry<String, ?> entry : entries.entrySet()) {
                        Object v = entry.getValue();
                        String prefsKey = entry.getKey();

                        if (v instanceof Boolean)
                            prefEdit.putBoolean(prefsKey, ((Boolean) v).booleanValue());
                        else if (v instanceof Float)
                            prefEdit.putFloat(prefsKey, ((Float) v).floatValue());
                        else if (v instanceof Integer)
                            prefEdit.putInt(prefsKey, ((Integer) v).intValue());
                        else if (v instanceof Long)
                            prefEdit.putLong(prefsKey, ((Long) v).longValue());
                        else if (v instanceof String)
                            prefEdit.putString(prefsKey, ((String) v));
                    }
                    prefEdit.commit();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputPrefs != null) {
                            inputPrefs.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }else {
                // файлы фотографии уже перенесены /////////////////
/*
                try {
                    FileChannel src = new FileInputStream(file).getChannel();
                    FileChannel dst = new FileOutputStream(mFotosDir + "/" + file.getName()).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } catch (Exception e) {
                            Log.e(curTAG, e.getMessage());
                            e.printStackTrace();
                }
*/
            }
        }


    }

    static public class RestoreSDLoadingDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
            dialog.setMessage(getResources().getString(R.string.loading));
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            return dialog;

        }

    }


    ProgressDialog createDialog(){
        ProgressDialog dialog = new ProgressDialog(MBackupSDCardForRestoreActivity.this);
        dialog.setMessage(getResources().getString(R.string.loading));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        return dialog;
    }

    class LoadingTaskRestoreSD extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //myRestoreDialogFragment.show(manager, "loading");
            loadingDialog = createDialog();
            loadingDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                doRestore();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //myRestoreDialogFragment.dismiss();
            loadingDialog.dismiss();
            loadingDialog = null;

            //String title = getResources().getString(R.string.deleting_visit_title);
            String message = getResources().getString(R.string.backup_sdcard_restore_fin);
            String btnPositiveString = getResources().getString(R.string.button_ok);
            AlertDialog.Builder ab = new AlertDialog.Builder(MBackupSDCardForRestoreActivity.this);
            //ab.setTitle(title);  // заголовок
            ab.setMessage(message); // сообщение
            ab.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                }
            });
            ab.setCancelable(false);
            ab.show();

/*
            Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardForRestoreActivity.this, rlBackupBackupSDCardRestoreContainer, getString(R.string.backup_sdcard_restore_fin));
            snackbar.show();
*/

            //btnDoRestoreFromSDCard.setEnabled(true);
        }
    }



}
