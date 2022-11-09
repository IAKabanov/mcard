package ru.kai.mcard.data.datasource;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import ru.kai.mcard.Constants;

/**
 * Created by akabanov on 31.08.2017.
 * Откроем БД при запуске приложения. При открытии каждой активности будем проверять.
 */

public final class DBHelper extends SQLiteOpenHelper {

    final static String dbName = Constants.DB_NAME;
    final static int DATABASE_VERSION = Constants.DATABASE_VERSION;

    //private static DBHelper instance;
    private SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, dbName, null, DATABASE_VERSION);
    }

/*
    public static synchronized DBHelper getInstance(Context context){
        if(instance == null){
            instance = new DBHelper(context);
        }
        return instance;
    }
*/

    public SQLiteDatabase open() {
        if ((db == null) || (!db.isOpen())) {
            try {
                db = getWritableDatabase();
            } catch (SQLiteException ex) {
                db = getReadableDatabase();
            }
        }
        return db;
    }

    public void close() {
        db.close();             // ??????????????????
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
/*

        /*/
/*****************************************************
        if (oldVersion < 1) {
            // создание базы и первоначальное заполнение
            updateDB(db, 1);
        }

        /*/
/*****************************************************
        if (oldVersion < 2) {
            //Код для добавления новой таблицы "Фильтры"
            updateDB(db, 2);
        }

        /*/
/*****************************************************
        if (oldVersion < 3) {
            //Код для добавления новой таблицы "Профили" и поля "Профиль" для таблиц визитов:
            updateDB(db, 3);
            if (DATABASE_VERSION == 3){
                return;
            }

            // создадим первый профиль и заполним ссылкой на него нужные таблицы
*/
/*
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
*//*


            // также создадим первый профиль и заполним ссылкой на него нужные таблицы
            // делать это будем в другом потоке, чтобы сначала завершить транзакцию по созданию таблицы профилей
            //AsyncUpdateTask asyncUpdateTask = new AsyncUpdateTask();
            //asyncUpdateTask.execute();

            // заполнение первой строки таблицы профилем по умолчанию произведем в onCreate()  z_old_MainActivity1, т.к. в одной транзакции не получилось (видимо, создание таблицы еще не зафиксировалось)

        }

        /*/
/*****************************************************
        if (oldVersion < 4) {
            //Код для добавления новой таблицы "Список исследований" для визита-исследования
            updateDB(db, 4);
            if (DATABASE_VERSION == 4){
                return;
            }
        }


*/
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
        if (newVersion < 4) {
//            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ANALISES_VISITS_ANALISES_LIST);
            //db.execSQL("alter table " + DB_TABLE_VISITS_DIRECTIONS + " RENAME TO " + TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID + "_old;");
        }
        if (newVersion < 3) {
//            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_PROFILES);
        }
        if (newVersion < 2) {
//            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_FILTERS);
        }
    }

}
