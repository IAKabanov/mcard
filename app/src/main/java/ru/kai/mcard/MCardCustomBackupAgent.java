package ru.kai.mcard;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Map;

import static com.google.android.gms.analytics.internal.zzy.e;


/**
 * Created by akabanov on 12.10.2016.
 */

public class MCardCustomBackupAgent extends BackupAgent {

    File mDataBaseFile;
    File mPrefsFile;

    final public static String TAG = BackupAgent.class.getSimpleName();
    final private static String BACKUP_KEY_FILES = "Files_Backup_";
    final private static String BACKUP_KEY_PREFS = "prefs";
    final private static String BACKUP_KEY_DB = "db";
    final private static String DB_NAME = DBMethods.DB_NAME;

    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    String outTempPrefsFileName;
    String outTempDBFileName;

    @Override
    public void onCreate() {
        super.onCreate();
        // Cache a File for the app's data
        mDataBaseFile = new File(getApplicationContext().getDatabasePath(DB_NAME).getAbsolutePath());
        mPrefsFile = new File(Environment.getDataDirectory() + "/data/" + getPackageName() + "/shared_prefs/" + Constants.APP_PREFERENCES + ".xml");
        //mPrefsFile = new File(getApplicationContext().getDir("shared_prefs", MODE_PRIVATE) + "/" + Constants.APP_PREFERENCES + ".xml");
        //mDataBaseFile = new File(getApplicationContext().getDatabasePath(DB_NAME).getAbsolutePath(), DB_NAME);

        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();

        // промежуточная копия mPrefsFile
        outTempPrefsFileName = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/shared_prefs_copy.xml";

        // промежуточная копия БД
        //String outTempDBFileName = Environment.getExternalStorageDirectory()+"/database_copy.db";
        outTempDBFileName = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/database_copy.db";

    }

    private void writeNewState(ParcelFileDescriptor newState ) throws IOException {
        FileOutputStream outstream = new FileOutputStream(newState.getFileDescriptor());
        DataOutputStream out = new DataOutputStream(outstream);

        //Date date = new Date();
        //long modified = date.getTime();
        long modified = mDataBaseFile.lastModified();
        out.writeLong(modified);
        out.close();
    }

    public byte[] read( File file ) throws IOException {
        int length;
        byte[] tmp = new byte[1001024];
        ByteArrayOutputStream out = new ByteArrayOutputStream( );
        InputStream in = new FileInputStream( file );

        while( (length = in.read( tmp )) >= 0 ) {
            out.write( tmp, 0, length );
        }

        return out.toByteArray( );
    }

    public byte[] readFileToByteArray(File file) throws IOException{
        ByteArrayOutputStream out = null;
        InputStream input = null;
        try{
            out = new ByteArrayOutputStream();
            input = new BufferedInputStream(new FileInputStream(file));
            int data = 0;
            while ((data = input.read()) != -1){
                out.write(data);
            }
        }
        finally{
            if (null != input){
                input.close();
            }
            if (null != out){
                out.close();
            }
        }
        return out.toByteArray();
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {

        Log.d(TAG, "Files_Backup");

        // получаем timestamp oldState последнего backup-a  ////////////////
        long oldStateModified = 0l;
        FileInputStream instream = null;
        DataInputStream in = null;
        try {
            // Get the oldState input stream
            instream = new FileInputStream(oldState.getFileDescriptor());
            in = new DataInputStream(instream);

            // Get the last modified timestamp from the state file and data file
            oldStateModified = in.readLong();
        } catch (IOException e) {
            // Unable to read state file... be safe and do a backup
        } finally {
            try{
                if (instream != null){
                    instream.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
            try{
                if (in != null){
                    in.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
        }
        ///////////////////////////////////



        editor.putLong(Constants.LAST_BACKUPS_OLD_STATE, oldStateModified);
        editor.apply();

        // получаем timestamp newStateBefore последнего backup-a  ////////////////
        long smNewStateBefore = 0l;
        FileInputStream instreamNSBefore = null;
        DataInputStream inNSBefore = null;
        try {
            // Get the newStateBefore input stream
            instreamNSBefore = new FileInputStream(newState.getFileDescriptor());
            inNSBefore = new DataInputStream(instreamNSBefore);

            // Get the last modified timestamp from the state file and data file
            smNewStateBefore = inNSBefore.readLong();
        } catch (IOException e) {
            // Unable to read state file... be safe and do a backup
        } finally {
            try{
                if (instreamNSBefore != null){
                    instreamNSBefore.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
            try{
                if (inNSBefore != null){
                    inNSBefore.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
        }

        editor.putLong(Constants.LAST_BACKUPS_NEW_STATE_BEFORE, smNewStateBefore);
        editor.apply();


        long fileModified1 = mDataBaseFile.lastModified();
        long fileModified2 = mPrefsFile.lastModified();
        long fileModified = 0l;
        if (fileModified1 > fileModified2){
            fileModified = fileModified1;
        }else {
            fileModified = fileModified2;
        }

        if ((oldStateModified == 0l)||(oldStateModified < fileModified)) {

            // файл настроек /////////////////
            // сначала выгрузим все настройки в промежуточный файл, и затем забэкапируем его.
            ObjectOutputStream outputPrefsFile = null;
            try {
                outputPrefsFile = new ObjectOutputStream(new FileOutputStream(outTempPrefsFileName));
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

            FileInputStream fisPrefs = null;
            try {
                fisPrefs = new FileInputStream(outTempPrefsFileName);
                byte[] bufferPrefs = new byte[fisPrefs.available()];
                fisPrefs.read(bufferPrefs, 0, fisPrefs.available());

                int lenPrefs = bufferPrefs.length;

                data.writeEntityHeader(BACKUP_KEY_PREFS, lenPrefs);
                data.writeEntityData(bufferPrefs, lenPrefs);
            }catch (IOException e){
                e.printStackTrace();
            }
            finally {
                try{
                    if (fisPrefs != null){
                        fisPrefs.close();
                    }
                } catch (IOException ex){
                    System.out.println(ex.getMessage());
                }
            }

            // старая версия файл настроек
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
                outputDB = new FileOutputStream(outTempDBFileName);
                // записывая БД в копию, получим нужный для передачи в backup буфер
                int length;
                while ((length = fisDB.read(buffer))>0){
                    outputDB.write(buffer, 0, length);
                }

                //fis.read(buffer, 0, fis.available());

                int len = buffer.length;

                data.writeEntityHeader(BACKUP_KEY_DB, len);
                data.writeEntityData(buffer, len);
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


            // файлы фотографии /////////////////
            File dir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File[] listFiles = dir.listFiles();
            //String[] filesPaths = new String[listFiles.length + 1];
            try {
                int i = 1;
                for (File file:listFiles) {

                    //filesPaths[i] = "../files/" + file.getName();
                    //filesPaths[i] = (new File(dir, file.getName())).getAbsolutePath();

                    byte[] buffer = readFileToByteArray(file);
                    int len = buffer.length;

                    data.writeEntityHeader(BACKUP_KEY_FILES + file.getName(), len);
                    data.writeEntityData(buffer, len);

                    i = i + 1;
                }
                //writeNewState(newState);
            }catch (Exception e){}

            ///////////////////////////////////

            editor.putBoolean(Constants.LAST_BACKUPS_IN_CLOUD_YES_NO, true);
            editor.apply();

            Date curDateTime = new Date();
            long curTimeStamp = curDateTime.getTime();
            editor.putLong(Constants.LAST_BACKUPS_IN_CLOUD_LAST_DATE_TIME, curTimeStamp);
            editor.apply();


        } else {
            // Don't back up because the file hasn't changed
            //return;
            editor.putBoolean(Constants.LAST_BACKUPS_IN_CLOUD_YES_NO, false);
            editor.apply();
        }


        Date curDateTime = new Date();
        long curTimeStamp = curDateTime.getTime();
        editor.putLong(Constants.LAST_BACKUPS_IN_CLOUD_DONE, curTimeStamp);
        editor.apply();

        writeNewState(newState);


        // получаем timestamp newStateAfter последнего backup-a  ////////////////
        long smNewStateAfter = 0l;
        FileInputStream instreamNSAfter = null;
        DataInputStream inNSAfter = null;
        try {
            // Get the smNewStateAfter input stream
            instreamNSAfter = new FileInputStream(newState.getFileDescriptor());
            inNSAfter = new DataInputStream(instreamNSAfter);

            // Get the last modified timestamp from the state file and data file
            smNewStateAfter = inNSAfter.readLong();
        } catch (IOException e) {
            // Unable to read state file... be safe and do a backup
        } finally {
            try{
                if (instreamNSAfter != null){
                    instreamNSAfter.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
            try{
                if (inNSAfter != null){
                    inNSAfter.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
        }

        editor.putLong(Constants.LAST_BACKUPS_NEW_STATE_AFTER, smNewStateAfter);
        editor.apply();



    }


    public File getFileFromBytes(byte[] bytes, String fileName) {

        File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        try {
            FileOutputStream os = new FileOutputStream(file);
            os.write(bytes);
            os.close();
        } catch (Exception e) {
        }

        return file;
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        Log.d(TAG, "onRestore");

        // There should be only one entity, but the safest
        // way to consume it is using a while loop
        while (data.readNextHeader()) {
            String key = data.getKey();
            int dataSize = data.getDataSize();

            if (BACKUP_KEY_DB.equals(key)) {

                File backupDB = null;
                try {
                    backupDB = new File(outTempDBFileName);
                }catch (Exception e){}

                if (backupDB != null) {
                    try {
                        if (mDataBaseFile.exists()) {
                            FileChannel src = new FileInputStream(backupDB).getChannel();
                            FileChannel dst = new FileOutputStream(mDataBaseFile).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        }
                    } catch (Exception e) {}
                }else {

                    byte[] buffer = new byte[dataSize];
                    data.readEntityData(buffer, 0, dataSize);

                    try (FileOutputStream fos = new FileOutputStream(mDataBaseFile)) {
                        fos.write(buffer, 0, buffer.length);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }

            }else if (BACKUP_KEY_PREFS.equals(key)){
                byte[] buffer = new byte[dataSize];
                data.readEntityData(buffer, 0, dataSize);

                // сначала восстановим все настройки в промежуточный файл
                //try(FileOutputStream fos = new FileOutputStream(mPrefsFile)){
                try(FileOutputStream fos = new FileOutputStream(outTempPrefsFileName)){
                    fos.write(buffer, 0, buffer.length);
                }catch (Exception e){
                    Log.e(TAG, e.getMessage(), e);
                }

                // теперь скопируем их из промежуточного в конечный
                ObjectInputStream inputPrefs = null;
                try {
                    inputPrefs = new ObjectInputStream(new FileInputStream(outTempPrefsFileName));
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
                }finally {
                    try {
                        if (inputPrefs != null) {
                            inputPrefs.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }


            } else if (key.contains(BACKUP_KEY_FILES)){

                String fileName = key.replace(BACKUP_KEY_FILES, "");

                byte[] buffer = new byte[dataSize];
                data.readEntityData(buffer, 0, dataSize);
                try{

                    File file = getFileFromBytes(buffer, fileName);

                    ByteArrayInputStream baStream = new ByteArrayInputStream(buffer);
                    //DataInputStream in = new DataInputStream(baStream);


                }catch (Exception e){
                    Log.e(TAG, e.getMessage(), e);
                }

            }

        }

        //writeNewState(newState);


        // получаем timestamp newStateAfter последнего backup-a  ////////////////
        long smNewStateRestore = 0l;
        FileInputStream instreamNSRestore = null;
        DataInputStream inNSRestore = null;
        try {
            // Get the smNewStateRestore input stream
            instreamNSRestore = new FileInputStream(newState.getFileDescriptor());
            inNSRestore = new DataInputStream(instreamNSRestore);

            // Get the last modified timestamp from the state file and data file
            smNewStateRestore = inNSRestore.readLong();
        } catch (IOException e) {
            // Unable to read state file... be safe and do a backup
        } finally {
            try{
                if (instreamNSRestore != null){
                    instreamNSRestore.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
            try{
                if (inNSRestore != null){
                    inNSRestore.close();
                }
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
        }

        editor.putLong(Constants.LAST_BACKUPS_NEW_STATE_RESTORE, smNewStateRestore);
        editor.apply();


        Date curDateTime = new Date();
        long curTimeStamp = curDateTime.getTime();
        editor.putLong(Constants.LAST_BACKUPS_ON_RESTORE_DONE, curTimeStamp);
        editor.apply();




    }

    @Override
    public void onRestoreFinished() {
        super.onRestoreFinished();

        File backupDB = null;
        try {
            backupDB = new File(outTempDBFileName);
        }catch (Exception e){}

        if (backupDB != null) {
            try {
                if (mDataBaseFile.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(mDataBaseFile).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } catch (Exception e) {}
        }

    }

    // метод для запроса бэкапа. Следует вызывать
    // метод всякий раз, когда данные изменились.
    public static void requestBackup(Context context) {
        BackupManager backupManager = new BackupManager(context);
        backupManager.dataChanged();
        //System.out.println("backuping");
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
        //System.out.println("restoring");
    }


}
