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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.kai.mcard.utility.Common;

@Deprecated
public class MBackupSDCardForBackupActivity extends AppCompatActivity {

    // Backup делается так: файлы БД и настроек переносятся в директорию фотографий,
    // затем сжимаются и сохраняются в директории на SD карте

    File mDataBaseFile;
    File mPrefsFile;
    File mFotosDir;

    String copyForZipBackupPrefsFileName;
    String copyForZipBackupDBFileName;
    String outTargetZipFileName;

    private final String curTAG = "MBackupSDCardForBackupActivity";

    ProgressDialog loadingDialog;

    final public static String TAG = BackupAgent.class.getSimpleName();
    final private static String BACKUP_KEY_FILES = "Files_Backup_";
    final private static String BACKUP_KEY_PREFS = "prefs";
    final private static String BACKUP_KEY_DB = "db";
    final private static String DB_NAME = DBMethods.DB_NAME;

    // настройки
    private SharedPreferences mSettings;

    TextView tvBackupSDCardBackupDir;
    TextView tvBackupToSDCardBackupHelp;
    RelativeLayout rlBackupSDCardBackupContainer;

    String pathForBackup = "";

    FragmentManager manager;
    BackupSDLoadingDialogFragment myBackupDialogFragment;
    AlertDialog.Builder ad;

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

        setContentView(R.layout.m_activity_backup_sdcard_backup);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA


        Toolbar mToolBar = (Toolbar) findViewById(R.id.mMBackupSDCardBackupToolbar);
        setSupportActionBar(mToolBar);

        tvBackupSDCardBackupDir = (TextView)findViewById(R.id.tvBackupSDCardBackupDir);
        rlBackupSDCardBackupContainer = (RelativeLayout)findViewById(R.id.rlBackupSDCardBackupContainer);

//        pathForBackup = new File(System.getenv("SECONDARY_STORAGE") + "/Android/data/ru.kai.mcard/files/Pictures").exists();

        try {
            pathForBackup = Common.getSDcardPath();  // инициализация
        } catch (Exception e) {
            Log.e(curTAG, e.getMessage());
            e.printStackTrace();
        }
/*
        pathForBackup = "/storage/sdcard1/";  // инициализация
        File files = new File(pathForBackup);
        if ((files == null)|(!files.isDirectory())) {
            pathForBackup = "/";
        }
*/


        if (mSettings.contains(Constants.BACKUPS_PATH_ON_SD_CARD)) {
            pathForBackup = mSettings.getString(Constants.BACKUPS_PATH_ON_SD_CARD, pathForBackup);
            // загрузили из настроек. Но путь в настройках может привести к краху, например, если перед этим SD-карту демонтировали из устройства
            pathForBackup = initFilePath(pathForBackup);
        }

        tvBackupSDCardBackupDir.setText(pathForBackup);

        mDataBaseFile = new File(getApplicationContext().getDatabasePath(DB_NAME).getAbsolutePath());
        mPrefsFile = new File(Environment.getDataDirectory() + "/data/" + getPackageName() + "/shared_prefs/" + Constants.APP_PREFERENCES + ".xml");

        setFilesPath();

        manager = getSupportFragmentManager();
        myBackupDialogFragment = new BackupSDLoadingDialogFragment(); //  <-- внутренний класс

/*
        // вопрос перед backup-ом
        String title = getResources().getString(R.string.backup_sdcard_backup_attantion);
        String message = getResources().getString(R.string.backup_sdcard_dir_clea);
        String button1String = getResources().getString(R.string.edit_profiles_delete_profile_positive_button_text);
        String button2String = getResources().getString(R.string.edit_profiles_delete_profile_negative_button_text);

        ad = new AlertDialog.Builder(MBackupSDCardForBackupActivity.this);
        ad.setTitle(title);  // заголовок
        ad.setMessage(message); // сообщение
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                // асинхронная выгрузка backup-а
                new LoadingTaskBackupSD().execute();

                setResult(RESULT_OK);
                //finish();
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                setResult(RESULT_CANCELED);
                //finish();
            }
        });
*/

        // не показывать обучающую информацию
        tvBackupToSDCardBackupHelp = (TextView)findViewById(R.id.tvBackupToSDCardBackupHelp);

        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_BACKUP_SD_CARD_BACKUP_HELP)) {
            // Получаем число из настроек
            int showBackupToSDCardBackupHelp = mSettings.getInt(Constants.APP_PREFERENCES_SHOW_BACKUP_SD_CARD_BACKUP_HELP, 1);
            if (showBackupToSDCardBackupHelp == 0)tvBackupToSDCardBackupHelp.setVisibility(View.GONE);
        }

        tvBackupToSDCardBackupHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.do_not_show_help_info);
                //String message = getResources().getString(R.string.do_not_show_help_info);
                String btnPositiveString = getResources().getString(R.string.but_continue_help);
                String btnNegativeString = getResources().getString(R.string.but_cancel_help);

                AlertDialog.Builder ad = new AlertDialog.Builder(MBackupSDCardForBackupActivity.this);
                ad.setTitle(title);  // заголовок
                //ad.setMessage(message); // сообщение
                ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putInt(Constants.APP_PREFERENCES_SHOW_BACKUP_SD_CARD_BACKUP_HELP, 0);
                        editor.apply();
                        tvBackupToSDCardBackupHelp.setVisibility(View.GONE);
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
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Backup на SD карту (MBackupSDCardForBackupActivity)");
        mTracker.setScreenName("Backup на SD карту (MBackupSDCardForBackupActivity)");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {return;}
        if(resultCode == RESULT_OK) {
            pathForBackup = data.getStringExtra("path");
            tvBackupSDCardBackupDir.setText(pathForBackup);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(Constants.BACKUPS_PATH_ON_SD_CARD, pathForBackup);
            editor.apply();

            setFilesPath();
        }

/*
        // из другой программы (файлового менеджера):
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String FilePath = data.getData().getPath();
                    textFile.setText(FilePath);
                }
                break;
        }
*/
    }

    String initFilePath(String path){
        File files = new File(path);
/*
            if ((files == null)|(!files.isDirectory())|Common.getTotalMemorySize(pathForBackup).equals("0")) {
                pathForBackup = "/";
            }
*/
        if(files == null){
            path = "/";
        }else{
            if (!files.isDirectory()){
                path = "/";
            }else {
                if (Common.getTotalMemorySize(path).equals("0")){
                    path = "/";
                }
            }
        }
        return path;
    }

    private void setFilesPath(){

        mFotosDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (mFotosDir != null) {
            if (!mFotosDir.exists()) {
                mFotosDir.mkdir();
            }

            // копия mPrefsFile
            copyForZipBackupPrefsFileName = mFotosDir + "/shared_prefs_copy.xml";
            // копия БД
            copyForZipBackupDBFileName = mFotosDir + "/database_copy.db";
        }

    }

    private void backupsSDCardActivityClose() {

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void chooseBackupSDCardBackupDir(View view) {

        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Backup")
                .setAction("ChosenDirActivity from BackupSDCard open")
                .build());


        Intent intent = new Intent(MBackupSDCardForBackupActivity.this, MChosenDirActivity.class);
        intent.putExtra("operation", "backup");
        intent.putExtra("path", pathForBackup);
        startActivityForResult(intent, Constants.CHOOSE_DIR);

        // из другой программы (файлового менеджера):
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("file/*");
        //startActivityForResult(intent, PICKFILE_RESULT_CODE);
        // чтобы все время выводилось окно с выбором программы, via которую выбирать файл
        //Intent chosenIntent = Intent.createChooser(intent, "Заголовок в диалоговом окне");
        //startActivityForResult(chosenIntent, PICKFILE_RESULT_CODE);

    }

    public void doBackupToSDCard(View view) {

        File pathForBackupDir = new File(pathForBackup);
        if (!pathForBackupDir.exists()){
            Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardForBackupActivity.this, rlBackupSDCardBackupContainer, getString(R.string.backup_sdcard_backup_bad_dir));
            snackbar.show();
            return;
        }

        //String title = getResources().getString(R.string.deleting_visit_title);
        String message0 = "Количество доступной памяти ";
        String message1 = Common.getFreeMemorySize(pathForBackup);
        String message2 = " MB из ";
        String message3 = Common.getTotalMemorySize(pathForBackup);
        String message4 = " MB. Продолжить?";
        StringBuffer message = new StringBuffer();
        message.append(message0).append(message1).append(message2).append(message3).append(message4);

        String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
        String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);
        AlertDialog.Builder ab = new AlertDialog.Builder(MBackupSDCardForBackupActivity.this);
        //ab.setTitle(title);  // заголовок
        ab.setMessage(message); // сообщение
        ab.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Backup")
                        .setAction("do BackupSDCard")
                        .build());



                new LoadingTaskBackupSD().execute();

            }
        });
        ab.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ab.setCancelable(true);
        ab.show();


    }

    public void doBackup(){

        Log.d(TAG, "Files_Backup");

        long curTimeStamp = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd.HH.mm");
        //SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd");
        outTargetZipFileName = pathForBackup + "m_sd_card_copy_" + sdf.format(curTimeStamp) + ".zip";
        File outTargetZipFile = new File(outTargetZipFileName); // здесь неизвестно, создан файл или нет,
                                                                // может, на SD карту запрещена запись аппаратно (всяко бывает)...

/*
        // сначала почистим от старых файлов, чтобы не получилось наложения
        File[] oldFilesList = new File(pathForBackup).listFiles();
        for (File oldFile : oldFilesList) {
            oldFile.delete();
        }
*/

        // файл настроек /////////////////
        // сначала выгрузим все настройки в промежуточный файл, и затем забэкапируем его.
        ObjectOutputStream outputPrefsFile = null;
        try {
            outputPrefsFile = new ObjectOutputStream(new FileOutputStream(copyForZipBackupPrefsFileName));
            outputPrefsFile.writeObject(mSettings.getAll());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (outputPrefsFile != null) {
                    outputPrefsFile.flush();
                    outputPrefsFile.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // старая версия для файла настроек
/*
            FileInputStream fisPrefs = new FileInputStream(mPrefsFile);
            byte[] bufferPrefs = new byte[fisPrefs.available()];
            fisPrefs.read(bufferPrefs, 0, fisPrefs.available());

            int lenPrefs = bufferPrefs.length;

            data.writeEntityHeader(BACKUP_KEY_PREFS, lenPrefs);
            data.writeEntityData(bufferPrefs, lenPrefs);
*/

        // файл базы данных /////////////////
        FileInputStream fisDB = null;
        OutputStream outputDB = null;
        try {
            fisDB = new FileInputStream(mDataBaseFile);
            byte[] buffer = new byte[fisDB.available()];

            // Open the empty db as the output stream
            outputDB = new FileOutputStream(copyForZipBackupDBFileName);
            // записывая БД в копию, получим нужный для передачи в backup буфер
            int length;
            while ((length = fisDB.read(buffer))>0){
                outputDB.write(buffer, 0, length);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try{
                if (fisDB != null){
                    fisDB.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
            try{
                if (outputDB != null){
                    outputDB.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
        }


        // файлы фотографий уже находятся в нужном месте /////////////////

/*
        File dir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] listFiles = dir.listFiles();
        for (File file:listFiles) {

            try {
                FileChannel src = new FileInputStream(file).getChannel();
                FileChannel dst = new FileOutputStream(pathForBackup + file.getName()).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } catch (Exception e) {
                        Log.e(curTAG, e.getMessage());
                        e.printStackTrace();
            }

        }
*/

        // выгрузка в архив
        Common.compress(outTargetZipFile, mFotosDir);

        // переименуем
        File newNamedOutputFile = new File(pathForBackup + "m_sd_card_copy_" + sdf.format(curTimeStamp) + ".mcc");
        outTargetZipFile.renameTo(newNamedOutputFile);
        ///////////////////////////////////

        //Date curDateTime = new Date();
        //long curTimeStamp = curDateTime.getTime();
        //editor.putLong(Constants.LAST_BACKUPS_IN_CLOUD_LAST_DATE_TIME, curTimeStamp);
        //editor.apply();





    }

    static public class BackupSDLoadingDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
            dialog.setMessage(getResources().getString(R.string.unloading));
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            return dialog;

        }
    }

    ProgressDialog createDialog(){
        ProgressDialog dialog = new ProgressDialog(MBackupSDCardForBackupActivity.this);
        dialog.setMessage(getResources().getString(R.string.unloading));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        return dialog;
    }

    class LoadingTaskBackupSD extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //myBackupDialogFragment.show(manager, "loading");
            loadingDialog = createDialog();
            loadingDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            doBackup();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //myBackupDialogFragment.dismiss();
            loadingDialog.dismiss();
            loadingDialog = null;

            String message = getResources().getString(R.string.backup_sdcard_backup_fin); // <- ошибка - а может и не сохранены, если, например,
                                                                        // запись на SD карту запрещена аппаратно... Переделать с rx

            String btnPositiveString = getResources().getString(R.string.button_ok);
            AlertDialog.Builder ab = new AlertDialog.Builder(MBackupSDCardForBackupActivity.this);
            //ab.setTitle(title);  // заголовок
            ab.setMessage(message); // сообщение
            ab.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                }
            });
            ab.setCancelable(false);
            ab.show();

/*
            Snackbar snackbar = Common.getCustomSnackbar(MBackupSDCardForBackupActivity.this, rlBackupSDCardBackupContainer, getString(R.string.backup_sdcard_backup_fin));
            snackbar.show();
*/
        }
    }



}
