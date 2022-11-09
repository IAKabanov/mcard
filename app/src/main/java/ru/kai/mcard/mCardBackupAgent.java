package ru.kai.mcard;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupManager;
import android.app.backup.FileBackupHelper;
import android.app.backup.RestoreObserver;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

import static ru.kai.mcard.DBMethods.DB_NAME;

/**
 * Created by akabanov on 03.10.2016.
 */

public class mCardBackupAgent extends BackupAgentHelper {

    private class DbBackupHelper extends FileBackupHelper{
        public DbBackupHelper(Context ctx, String dbName) {
            super(ctx, ctx.getDatabasePath(dbName).getAbsolutePath());
        }
    }

    @Override
    public void onCreate() {

/*
        FileBackupHelper fileBackupHelper = new FileBackupHelper(this,
                //MCardBackupAgent.this.getDatabasePath(DB_NAME));
                "../databases/" + DBMethods.DB_NAME);

        Log.i("DB_Backup", DBMethods.DB_NAME);
        addHelper("db", fileBackupHelper);
*/


        //addHelper("db", new DbBackupHelper(this, DBMethods.DB_NAME));


/*
    try {
//        File[] listFiles = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
        File[] listFiles = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
        int i = 0;
        for (File file:listFiles) {
            i = i + 1;
            //FileBackupHelper fileBackupFotoHelper = new FileBackupHelper(this, file.getAbsolutePath());
            //i = i + 1;
            //String fotosKey = "fotos" + String.valueOf(i);
            //Log.i("fotosKey", fotosKey);
            //addHelper(fotosKey, fileBackupFotoHelper);
            //addHelper("db", fileBackupFotoHelper);
        }

        N = i;

    }catch (Exception e){}
*/

        //File dir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File dir = getApplicationContext().getFilesDir();
        File dir = new File(getApplicationContext().getFilesDir().getPath());
        File[] listFiles = dir.listFiles();

        String[] filesPaths = new String[listFiles.length + 1];
        filesPaths[0] = "../databases/" + DBMethods.DB_NAME;
/*
        try {
            int i = 1;
            for (File file:listFiles) {

                filesPaths[i] = "../files/" + file.getName();
                //filesPaths[i] = (new File(dir, file.getName())).getAbsolutePath();

                //FileBackupHelper fileBackupFotoHelper = new FileBackupHelper(this, file.getAbsolutePath());
                //i = i + 1;
                //String fotosKey = "fotos" + String.valueOf(i);
                //Log.i("fotosKey", fotosKey);
                //addHelper(fotosKey, fileBackupFotoHelper);
                //addHelper("db", fileBackupFotoHelper);

                i = i + 1;
            }

        }catch (Exception e){}
*/


        FileBackupHelper fileBackupHelper = new FileBackupHelper(this, filesPaths);


/*
        String filePath1 = "";
        if (listFiles.length > 0){
            filePath1 = listFiles[0].getAbsolutePath();
        }
        FileBackupHelper fileBackupHelper = new FileBackupHelper(this, "../databases/" + DBMethods.DB_NAME, filePath1);
*/


        Log.i("Files_Backup", "Files_Backup");
        addHelper("db", fileBackupHelper);


/*
        FileBackupHelper fileBackupFotoHelper = new FileBackupHelper(this,
                getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "*/
/*");
        addHelper("fotos", fileBackupFotoHelper);
*/


        // Через запятую указываем названия файлов, в которых
        // хранятся ваши настройки. Обратите внимание на последний параметр, в
        // файле под таким названием хранит данные
        // PreferenceManager(PreferenceManager.getDefaultSharedPreferences(context))

        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(
                //this, Constants.APP_PREFERENCES, getPackageName() + "_preferences");
                //this, "../shared_prefs/" + Constants.APP_PREFERENCES);
                this, Constants.APP_PREFERENCES);

        Log.i("SharedPreferencesBackup", Constants.APP_PREFERENCES);
        addHelper("prefs", helper);

    }

    // метод для запроса бэкапа. Следует вызывать
    // метод всякий раз, когда данные изменились.
    public static void requestBackup(Context context) {
        BackupManager backupManager = new BackupManager(context);
        backupManager.dataChanged();
        System.out.println("backuping");
    }

    public static void requestRestore(Context context){
        BackupManager backupManager = new BackupManager(context);
        backupManager.requestRestore(new RestoreObserver() {
            @Override
            public void restoreStarting(int numPackages) {
                super.restoreStarting(numPackages);
            }

            @Override
            public void restoreFinished(int error) {
                super.restoreFinished(error);
            }

            @Override
            public void onUpdate(int nowBeingRestored, String currentPackage) {
                super.onUpdate(nowBeingRestored, currentPackage);
            }
        });
        System.out.println("restoring");
    }

}
