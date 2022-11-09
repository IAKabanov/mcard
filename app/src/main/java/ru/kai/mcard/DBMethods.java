package ru.kai.mcard;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.format.DateFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dady on 18.12.2015.
 */

/**
 * Instead this class use DBMethodsS class
 */
@Deprecated
public class DBMethods {

    Context fContext;
    DBHelper dbHelper;
    SQLiteDatabase db;

    // таблицы
    public static final String DB_NAME = "mCard.db";
    private static final String DB_TABLE_CLINICS = "clinics";
    private static final String DB_TABLE_CURES = "cures";  // назначения (препараты, уколы, процедуры)
    private static final String DB_TABLE_RECOMMENDATIONS = "recommendations";  // рекомендации по режиму
    private static final String DB_TABLE_SPECIALIZATIONS = "specializations"; // специализации докторов
    private static final String DB_TABLE_DOCTORS = "doctors";  // доктора - реальные люди
    private static final String DB_TABLE_VISITS = "visits";
    private static final String DB_TABLE_DIAGNOSES = "diagnoses";
    private static final String DB_TABLE_ANALISES_TYPES = "analises_types";  // виды исследований (анализы, УЗИ, рентгенография и т.д.)
    private static final String DB_TABLE_VISITS_CURES = "visits_cures";
    private static final String DB_TABLE_VISITS_RECOMMENDATIONS = "visits_recommendations";
    private static final String DB_TABLE_VISITS_DIRECTIONS = "visits_directions";
    private static final String DB_TABLE_VISITS_PHOTOS = "visits_photos";
    private static final String DB_TABLE_ANALISES_VISITS = "analises_visits";
    private static final String DB_TABLE_ANALISES_VISITS_ANALISES_LIST = "analises_visits_analises_list"; // таблица анализов (исследований) для одного визита-исследования  - табличная часть документа визит-исследование
    private static final String DB_TABLE_FILTERS = "filters";
    private static final String DB_TABLE_PROFILES = "profiles";

    // поля таблицы "clinics"
    class TableClinics implements BaseColumns{
        static final String COLUMN_CLINICS_NAME = "name";    // название клиники
        static final String COLUMN_CLINICS_DESCR = "descr";  // описание клиники (адрес и др.)
        //public static final String COLUMN_CLINICS_PHONES = "phones";  // телефоны клиники
    }

    // поля таблицы "cures"
    class TableCures implements BaseColumns{
        static final String COLUMN_CURES_NAME = "name";    // название назначения
        static final String COLUMN_CURES_DESCR = "descr";  // доп описание (дозировка и др.)
    }

    // поля таблицы "recommendations"
    class TableRecommendations implements BaseColumns{
        static final String COLUMN_RECOMMENDATIONS_NAME = "name"; // название рекомендации
    }

    // поля таблицы "specializations"
    class TableSpecializations implements BaseColumns{
        static final String COLUMN_SPECIALIZATIONS_NAME = "name"; // название специализации доктора
    }

    // поля таблицы "doctors"
    class TableDoctors implements BaseColumns{
        static final String COLUMN_DOCTORS_NAME = "name";  // ФИО
        static final String COLUMN_DOCTORS_SPECIALIZATION_ID = "specialization_id";
        //public static final String COLUMN_DOCTORS_PHONES = "phones";  // телефоны доктора
    }

    // поля таблицы "visits"
    class TableVisits implements BaseColumns{

        //public static final String COLUMN_VISITS_DATE = "visit_date"; // дата визита
        //public static final String COLUMN_VISITS_TIME = "visit_time"; // время визита
        static final String COLUMN_VISITS_DATE_TIME = "visit_date_time"; // дата-время визита
        static final String COLUMN_VISITS_CLINIC_ID = "clinic";
        static final String COLUMN_VISITS_DOCTOR_ID = "doctor"; // доктор ФИО
        static final String COLUMN_VISITS_SPECIALIZATION_ID = "specialization"; // специализация доктора (если выбран доктор, подставляется автоматически)
        static final String COLUMN_VISITS_COMMENT = "comment"; // комментарий к визиту
        static final String COLUMN_VISITS_DIAGNOSIS_ID = "diagnosis"; //  id диагноза
        static final String COLUMN_VISITS_REF_ID = "visit_reference";  // тип vizit id ссылка на основание - кто направил
        static final String COLUMN_VISITS_PROFILE_ID = "visits_profile";  // тип profile id ссылка на профиль (кому принадлежит визит)
        //public static final String COLUMN_VISITS_CURES_ID = "visits_cures"; // назначениe
    }

    // поля таблицы "diagnoses"
    class TableDiagnoses implements BaseColumns{
        static final String COLUMN_DIAGNOSES_NAME = "name"; // название диагноза
    }

    // поля таблицы "analises_types"
    class TableAnalisesTypes implements BaseColumns{
        static final String COLUMN_ANALISES_TYPES_NAME = "name"; // название исследования
    }

    // поля таблицы "назначения по визиту" (как регистр сведений). Самостоятельная таблица. Связь с табл. визитов опосредованная. Один визит - несколько назначений.
    class TableVisitsCures implements BaseColumns{
        static final String COLUMN_BASIC_VISITS_ID = "visits_id";
        static final String COLUMN_CURES_ID = "cures_id";
        static final String COLUMN_CURES_DESCR = "cures_descr"; // описание приема назначения (например, "2 раза в день после еды")
        //public static final String COLUMN_PROCEDURES_VISITS_ID = "procedures_visits_id"; // визит, на котором будет выполняться процедура
    }

    // поля таблицы "рекомендации по визиту" (как регистр сведений). Самостоятельная таблица. Связь с табл. визитов опосредованная. Один визит - несколько рекомендаций.
    class TableVisitsRecommendations implements BaseColumns{
        static final String COLUMN_BASIC_VISITS_ID = "visits_id";
        static final String COLUMN_RECOMMENDATIONS_ID = "recommendations_id";
    }

    // поля таблицы "направления" (как регистр сведений). Самостоятельная таблица. Связь с табл. визитов опосредованная. Один визит - несколько направлений.
    class TableVisitsDirections implements BaseColumns{
        static final String COLUMN_BASIC_VISITS_ID = "basic_visits_id";  // id текущего визита - основания (с которого будут направления)
        static final String COLUMN_DIRECTION_VISITS_TYPE = "direction_visits_type";  // тип визита - направления
        static final String COLUMN_AN_TYPES_SPECIALISATIONS_ID = "an_types_specialisations_id";  // id типа исследования или специализации доктора, к которому направили. Constants.DIRECTIONS_VISITS_TYPE_0 and Constants.DIRECTIONS_ANALISIS_TYPE_1
        static final String COLUMN_DIRECTION_VISITS_ID = "direction_visits_id";  // id визита-направления (вводится, если клиент пошел по этому направлению)
    }

    // поля таблицы "фоторесурсы" (как регистр сведений). Самостоятельная таблица. Связь с табл. визитов опосредованная. Один визит - несколько фоторесурсов.
    class TableVisitsPhotos implements BaseColumns{
        static final String COLUMN_BASIC_VISITS_TYPE = "basic_visits_type";  // тип текущего визита - основания (basic_id для разных визитов могут быть одинаковыми) (type - int)
        static final String COLUMN_BASIC_VISITS_ID = "basic_visits_id";  // id текущего визита - основания (к которому будут прикреплены фотографии) (type - int)
        static final String COLUMN_PHOTOS_TYPE = "photos_type";  // из камеры (0) фотография или из галереи (1) (type - int)
        static final String COLUMN_PHOTOS_NAME = "photos_name";  // имя фотографии для идентификации у пользователя   (type - String)
        static final String COLUMN_PHOTOS_URI = "photos_uri";  // путь размещения фоторесурса - уникальный(своеобразный id)  (type - String)
    }

    // поля таблицы "analises_visits"
    class TableAnalisesVisits implements BaseColumns{
        static final String COLUMN_ANALISES_VISITS_DATE_TIME = "analisis_visits_date_time"; // дата-время визита
        static final String COLUMN_ANALISES_VISITS_CLINIC_ID = "clinic_id";
        static final String COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID = "analises_types_id"; // id исследования // устаревшее поле. С версии БД 4 хранятсяв таблице TableAnalisisVisitsAnalisesList
        static final String COLUMN_ANALISES_VISITS_COMMENT = "comment"; // комментарий к визиту
        static final String COLUMN_ANALISES_VISITS_REF_ID = "analisis_visits_reference_id";  // тип vizit id ссылка на основание - кто направил
        static final String COLUMN_ANALISES_VISITS_PROFILE_ID = "analisis_visits_profile";  // тип profile id ссылка на профиль (кому принадлежит визит)
    }

    // поля таблицы "analises_visits_analises_list". Подчиненная таблица для таблицы визитов-исследований.
    class TableAnalisisVisitsAnalisesList implements BaseColumns{
        static final String COLUMN_BASIC_ANALISES_VISITS_ID = "basic_analisis_visits_id";  // id визита - основания (визит, на котором исследования производятся)
        static final String COLUMN_ANALISIS_TYPES_ID = "analisis_types_id";  // id исследования
        static final String COLUMN_REFERENCES_VISITS_ID = "references_visits_id";  // id визита-консультации, на котором выдали направление на это исследование
    }

    // поля таблицы "filters"
    class TableFilters implements BaseColumns{
        static final String COLUMN_FILTERS_NAME = "name"; // название фильтра
        static final String COLUMN_FILTERS_ONLY_WHITH_DATE = "only_whith_date"; // не показывать без даты (только с датой) integer 0 или 1
        static final String COLUMN_FILTERS_FIRST_DATE = "first_date"; // начальная дата
        static final String COLUMN_FILTERS_LAST_DATE = "last_date"; // конечная дата
        static final String COLUMN_FILTERS_CLINICS_ID = "clinics_id";  // id клиники
        static final String COLUMN_FILTERS_DOCTORS_ID = "doctors_id";  // id доктора
        static final String COLUMN_FILTERS_SPECIALIZATIONS_ID = "specializations_id";  // id специализации
        static final String COLUMN_FILTERS_CURES_ID = "cures_id";  // id назначения
        static final String COLUMN_FILTERS_DIAGNOSIS_ID = "diagnosis_id";  // id диагноза
        static final String COLUMN_FILTERS_ANALISES_TYPES_ID = "analises_types_id";  // id исследования
        static final String COLUMN_FILTERS_RECOMMENDATIONS_ID = "recommendations_id";  // id рекомендации по режиму
    }

    // поля таблицы "profiles"
    class TableProfiles implements BaseColumns{
        static final String COLUMN_PROFILES_NAME = "name"; // имя профиля (я, сын, муж, мама...)
        static final String COLUMN_PROFILES_SEX = "sex"; // пол
        static final String COLUMN_PROFILES_BIRTHDAY = "birthday"; // двта рождения
        static final String COLUMN_PROFILES_COMMENT = "comment"; // комментарий
    }

    // запрос на создание таблицы "cliniks"
    private static final String CREATE_DB_TABLE_CLINIKS = "create table " + DB_TABLE_CLINICS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableClinics.COLUMN_CLINICS_NAME + " text not null, "
            + TableClinics.COLUMN_CLINICS_DESCR + " text" + ");";
    // запрос на создание таблицы "cures"
    private static final String CREATE_DB_TABLE_CURES = "create table " + DB_TABLE_CURES + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableCures.COLUMN_CURES_NAME + " text not null, "
            + TableCures.COLUMN_CURES_DESCR + " text" + ");";
    // запрос на создание таблицы "recommendations"
    private static final String CREATE_DB_TABLE_RECOMMENDATIONS = "create table " + DB_TABLE_RECOMMENDATIONS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableRecommendations.COLUMN_RECOMMENDATIONS_NAME + " text not null" + ");";
    // запрос на создание таблицы "specializations"
    private static final String CREATE_DB_TABLE_SPECIALIZATIONS = "create table " + DB_TABLE_SPECIALIZATIONS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableSpecializations.COLUMN_SPECIALIZATIONS_NAME + " text not null" + ");";
    // запрос на создание таблицы "doctors"
    private static final String CREATE_DB_TABLE_DOCTORS = "create table " + DB_TABLE_DOCTORS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableDoctors.COLUMN_DOCTORS_NAME + " text not null, "
            + TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID + " integer" + ");";  // id specializations
    // запрос на создание таблицы "visits"
    private static final String CREATE_DB_TABLE_VISITS = "create table " + DB_TABLE_VISITS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableVisits.COLUMN_VISITS_DATE_TIME + " long, "
            + TableVisits.COLUMN_VISITS_CLINIC_ID + " integer, "
            + TableVisits.COLUMN_VISITS_DOCTOR_ID + " integer, "
            + TableVisits.COLUMN_VISITS_SPECIALIZATION_ID + " integer, "
            + TableVisits.COLUMN_VISITS_COMMENT + " text, "
            + TableVisits.COLUMN_VISITS_DIAGNOSIS_ID + " integer, "
            + TableVisits.COLUMN_VISITS_REF_ID + " integer"
            //+ TableVisits.COLUMN_VISITS_CURES_ID + " integer"
            + ");";
    // запрос на создание таблицы "diagnoses"
    private static final String CREATE_DB_TABLE_DIAGNOSES = "create table " + DB_TABLE_DIAGNOSES + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableDiagnoses.COLUMN_DIAGNOSES_NAME + " text not null" + ");";
    // запрос на создание таблицы "analises"
    private static final String CREATE_DB_TABLE_ANALISES_TYPES = "create table " + DB_TABLE_ANALISES_TYPES + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME + " text not null" + ");";
    // запрос на создание таблицы "visits_cures"
    private static final String CREATE_DB_TABLE_VISITS_CURES = "create table " + DB_TABLE_VISITS_CURES + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableVisitsCures.COLUMN_BASIC_VISITS_ID + " integer, "
            + TableVisitsCures.COLUMN_CURES_ID + " integer, "
            + TableVisitsCures.COLUMN_CURES_DESCR + " text"
            + ");";
    // запрос на создание таблицы "visits_recommendations"
    private static final String CREATE_DB_TABLE_VISITS_RECOMMENDATIONS = "create table " + DB_TABLE_VISITS_RECOMMENDATIONS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableVisitsRecommendations.COLUMN_BASIC_VISITS_ID + " integer, "
            + TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID + " integer"
            + ");";
    // запрос на создание таблицы "directions"
    // направления бывают двух видов: на визит-консультацию и на визит-исследование
    // в доп. поле будет ханиться либо идентификатор специализация доктора (для консультации), либо идентификатор типа исследования.
    // не всегда человек идет на назначаемое исследование, а зафиксировать, что направляли, надо.
    private static final String CREATE_DB_TABLE_VISITS_DIRECTIONS = "create table " + DB_TABLE_VISITS_DIRECTIONS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableVisitsDirections.COLUMN_BASIC_VISITS_ID + " integer,"
            + TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE + " integer,"
            + TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID + " integer"
            + ");";
    // запрос на создание таблицы "visits_photos"
    private static final String CREATE_DB_TABLE_VISITS_PHOTOS = "create table " + DB_TABLE_VISITS_PHOTOS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE + " integer not null, "
            + TableVisitsPhotos.COLUMN_BASIC_VISITS_ID + " integer not null, "
            + TableVisitsPhotos.COLUMN_PHOTOS_TYPE + " integer not null, "
            + TableVisitsPhotos.COLUMN_PHOTOS_NAME + " text not null, "
            + TableVisitsPhotos.COLUMN_PHOTOS_URI + " text not null"
            + ");";
    // запрос на создание таблицы "analises_visits"
    private static final String CREATE_DB_TABLE_ANALISES_VISITS = "create table " + DB_TABLE_ANALISES_VISITS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME + " long, "
            + TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID + " integer, "
            + TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID + " integer, "
            + TableAnalisesVisits.COLUMN_ANALISES_VISITS_COMMENT + " text, "
            + TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID + " integer"
            + ");";
    // запрос на создание таблицы "analises_visits_analises_list"  - табличная часть документа визит-исследование
    private static final String CREATE_DB_TABLE_ANALISES_VISITS_ANALISES_LIST = "create table if not exists " + DB_TABLE_ANALISES_VISITS_ANALISES_LIST + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID + " integer,"
            + TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID + " integer,"
            + TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID + " integer"
            + ");";
    // запрос на создание таблицы "filters"
    private static final String CREATE_DB_TABLE_FILTERS = "create table " + DB_TABLE_FILTERS + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableFilters.COLUMN_FILTERS_NAME + " text, "
            + TableFilters.COLUMN_FILTERS_ONLY_WHITH_DATE + " integer, " //   0 или 1
            + TableFilters.COLUMN_FILTERS_FIRST_DATE + " long, "
            + TableFilters.COLUMN_FILTERS_LAST_DATE + " long, "
            + TableFilters.COLUMN_FILTERS_CLINICS_ID + " integer, "
            + TableFilters.COLUMN_FILTERS_DOCTORS_ID + " integer,"
            + TableFilters.COLUMN_FILTERS_SPECIALIZATIONS_ID + " integer,"
            + TableFilters.COLUMN_FILTERS_CURES_ID + " integer,"
            + TableFilters.COLUMN_FILTERS_DIAGNOSIS_ID + " integer,"
            + TableFilters.COLUMN_FILTERS_ANALISES_TYPES_ID + " integer,"
            + TableFilters.COLUMN_FILTERS_RECOMMENDATIONS_ID+ " integer"
            + ");";
    // запрос на создание таблицы "profiles"
    private static final String CREATE_DB_TABLE_PROFILES = "create table " + DB_TABLE_PROFILES + "("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + TableProfiles.COLUMN_PROFILES_NAME + " text, "
            + TableProfiles.COLUMN_PROFILES_SEX + " text, "  // m/w
            + TableProfiles.COLUMN_PROFILES_BIRTHDAY + " long, "
            + TableProfiles.COLUMN_PROFILES_COMMENT+ " text"
            + ");";

    public static final int DATABASE_VERSION = 4;

    private class DBHelper extends SQLiteOpenHelper{

        DBHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
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

            //*****************************************************
            if (oldVersion < 1) {
                // создание базы и первоначальное заполнение
                updateDB(db, 1);
            }

            //*****************************************************
            if (oldVersion < 2) {
                //Код для добавления новой таблицы "Фильтры"
                updateDB(db, 2);
            }

            //*****************************************************
            if (oldVersion < 3) {
                //Код для добавления новой таблицы "Профили" и поля "Профиль" для таблиц визитов:
                updateDB(db, 3);
                if (DATABASE_VERSION == 3){
                    return;
                }

                // создадим первый профиль и заполним ссылкой на него нужные таблицы
/*
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
*/

                // также создадим первый профиль и заполним ссылкой на него нужные таблицы
                // делать это будем в другом потоке, чтобы сначала завершить транзакцию по созданию таблицы профилей
                //AsyncUpdateTask asyncUpdateTask = new AsyncUpdateTask();
                //asyncUpdateTask.execute();

                // заполнение первой строки таблицы профилем по умолчанию произведем в onCreate()  z_old_MainActivity1, т.к. в одной транзакции не получилось (видимо, создание таблицы еще не зафиксировалось)

            }

            //*****************************************************
            if (oldVersion < 4) {
                //Код для добавления новой таблицы "Список исследований" для визита-исследования
                updateDB(db, 4);
                if (DATABASE_VERSION == 4){
                    return;
                }
            }


        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //super.onDowngrade(db, oldVersion, newVersion);
            if (newVersion < 4) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ANALISES_VISITS_ANALISES_LIST);
                //db.execSQL("alter table " + DB_TABLE_VISITS_DIRECTIONS + " RENAME TO " + TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID + "_old;");
            }
            if (newVersion < 3) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_PROFILES);
            }
            if (newVersion < 2) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_FILTERS);
            }
        }
    }

    public DBMethods(Context context){
        this.fContext = context;
    }

    public void open(){
        dbHelper = new DBHelper(fContext);
        try {
            db = dbHelper.getWritableDatabase();
        }catch (SQLiteException ex){
            db = dbHelper.getReadableDatabase();
        }

    }

    // здесь закрываем все соединения с базой и класс-помощник
    // ??????????????????
    public void close() {
        db.close();             // ??????????????????
        dbHelper.close();       // ??????????????????
    }

    public Cursor getMainVisitList(){

        final String queryString = "" +
                "select * " +
                "from " +
                "(" +
                "select " +
                    "0 as type_of_visit, " +
                    TableVisits._ID + " as id, " +
                    TableVisits.COLUMN_VISITS_DATE_TIME + " as date, " +
                    TableVisits.COLUMN_VISITS_CLINIC_ID + " as clinicID, " +
                    TableVisits.COLUMN_VISITS_SPECIALIZATION_ID + " as specializationID ," +
                    TableVisits.COLUMN_VISITS_DOCTOR_ID + " as doctorID" +
                " from visits " +
                "union all " +
                "select " +
                    "1 as type_of_visit, " +
                    TableAnalisesVisits._ID + " as id, " +
                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME + " as date, " +
                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID + " as clinicID, " +
                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID + " as specializationID ," +
                    " 0"+" as doctorID" +
                " from analises_visits" +
                ") " +
                "order by date ASC";

        Cursor cursor = db.rawQuery(queryString, null);

        return cursor;
    }

    public String getMainFiltratedVisitListText(int chosenFilterID, int currentProfilesID, boolean order_down){
        String queryString = "";
        if (chosenFilterID != -1){ // т.е. фильтр выбран
            if (DATABASE_VERSION < 4) { // старый вариант БД - до 04.08.2016

                Bundle bundle = getFiltersFildsByFiltersID(chosenFilterID);
                if (bundle != null) {

                    int onlyWithDate = (bundle.getBoolean("onlyWithDate")) ? 1 : 0;
                    Long firstDate = bundle.getLong("firstDate");
                    Long lastDate = bundle.getLong("lastDate");
                    int mClinicsID = bundle.getInt("clinicsID");
                    int mDoctorsID = bundle.getInt("doctorsID");
                    int mSpecializationsID = bundle.getInt("specializationsID");
                    int mCuresID = bundle.getInt("curesID");
                    int mDiagnosisID = bundle.getInt("diagnosisID");
                    int mAnalisesTypesID = bundle.getInt("analisesTypesID");
                    int mRecommendationsID = bundle.getInt("recommendationsID");

                    queryString = "" +
                            "select * " +
                            "from " +
                            "(" +
                            "select " +
                            Constants.DIRECTIONS_VISITS_TYPE_0 + " as type_of_visit, " + // нужно заменить на Constants.DIRECTIONS_VISITS_TYPE_0
                            //TableVisits._ID + " as id, " +
                            "visits._id as id, " +
                            TableVisits.COLUMN_VISITS_DATE_TIME + " as date, " +
                            TableVisits.COLUMN_VISITS_CLINIC_ID + " as clinicID, " +
                            TableVisits.COLUMN_VISITS_SPECIALIZATION_ID + " as specializationID, " +
                            TableVisits.COLUMN_VISITS_DOCTOR_ID + " as doctorID";

                    if (mCuresID != 0) {
                        queryString = queryString + ", " + TableVisitsCures.COLUMN_CURES_ID + " as curesID";
                    }
//                        TableVisits.COLUMN_VISITS_DIAGNOSIS_ID + " as diagnosisID, " +
//                        TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID + " as recommendationsID" +
                    queryString = queryString + " from visits ";
                    if (mCuresID != 0) {
                        queryString = queryString + "left join visits_cures" +
                                " on visits._id = visits_cures.visits_id ";
                    }
/*
                if (mRecommendationsID != 0){
                    queryString = queryString + "left join visits_recommendations" +
                                                " on visits._id = visits_recommendations.visits_id ";
                }
*/
                    //queryString = queryString + "where 1=1 ";
                    queryString = queryString + "where " + TableVisits.COLUMN_VISITS_PROFILE_ID + " = " + currentProfilesID + " ";

                    if (onlyWithDate == 1) {
                        queryString = queryString + " and date <> 0 ";
                    }
                    queryString = queryString + " and date >= " + firstDate + " ";
                    if (lastDate != 0) {
                        queryString = queryString + " and date <= " + lastDate + " ";
                    }
                    if (mClinicsID != 0) {
                        queryString = queryString + " and clinic = " + mClinicsID + " ";
                    }
                    if (mDoctorsID != 0) {
                        queryString = queryString + " and doctor = " + mDoctorsID + " ";
                    }
                    if (mSpecializationsID != 0) {
                        queryString = queryString + " and specialization = " + mSpecializationsID + " ";
                    }
                    if (mCuresID != 0) {
                        queryString = queryString + " and curesID = " + mCuresID + " ";
                    }

                    if (mDiagnosisID != 0) {
                        queryString = queryString + " and diagnosisID = " + mDiagnosisID + " ";
                    }
                    if (mAnalisesTypesID != 0) {
                        queryString = queryString + " and 1 = 2 ";
                    }
                    if (mRecommendationsID != 0) {
                        queryString = queryString + " and recommendationsID = " + mRecommendationsID + " ";
                    }


                    queryString = queryString +
                            "union all " +
                            "select " +
                            Constants.DIRECTIONS_ANALISIS_TYPE_1 + " as type_of_visit, " +
                            TableAnalisesVisits._ID + " as id, " +
                            TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME + " as date, " +
                            TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID + " as clinicID, " +
                            TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID + " as specializationID, " +
                            " 0" + " as doctorID" +
                            " from analises_visits " +
                            "";
                    //"where 1=1";

                    queryString = queryString + "where " + TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID + " = " + currentProfilesID + " ";

                    if (onlyWithDate == 1) {
                        queryString = queryString + " and date <> 0 ";
                    }
                    queryString = queryString + " and date >= " + firstDate + " ";
                    if (lastDate != 0) {
                        queryString = queryString + " and date <= " + lastDate + " ";
                    }
                    if (mClinicsID != 0) {
                        queryString = queryString + " and clinicID = " + mClinicsID + " ";
                    }
                    if (mDoctorsID != 0) {
                        queryString = queryString + " and 1 = 2 ";
                    }
                    if (mSpecializationsID != 0) {
                        queryString = queryString + " and 1 = 2 ";
                    }
                    if (mCuresID != 0) {
                        queryString = queryString + " and 1 = 2 ";
                    }

                    if (mDiagnosisID != 0) {
                        queryString = queryString + " and 1 = 2 ";
                    }
                    if (mAnalisesTypesID != 0) {
                        queryString = queryString + " and specializationID = " + mAnalisesTypesID + " ";
                    }
                    if (mRecommendationsID != 0) {
                        queryString = queryString + " and 1 = 2 ";
                    }

                    if (order_down) {
                        queryString = queryString +
                                ") " +
                                "order by date ASC";
                    }else {
                        queryString = queryString +
                                ") " +
                                "order by date DESC";
                    }
                }
            }else { // (chosenFilterID != 0)   and   !(DATABASE_VERSION < 4) // фильтр выбран И новая версия БД (4 и выше)

                Bundle bundle = getFiltersFildsByFiltersID(chosenFilterID);
                if (bundle != null) {
                    int mAnalisesTypesID = bundle.getInt("analisesTypesID");
                    if (mAnalisesTypesID == 0){
                        queryString = getFiltredQueryStringWithoutAnalisisType(bundle, currentProfilesID, order_down);
                    }else {
                        queryString = getFiltredQueryStringWithAnalisisType(bundle, currentProfilesID, order_down);
                    }
                }
            }
        }else {   // фильтр не выбран

        //if (queryString == ""){      // chosenFilterID == 0  // если все-таки строку не сформировали
            if (DATABASE_VERSION < 4) {  // старый вариант до 04.08.2016
                queryString = queryString +
                        "select * " +
                        "from " +
                            "(" +
                                "select " +
                                    "0 as type_of_visit, " +
                                    //TableVisits._ID + " as id, " +
                                    "visits._id as id, " +
                                    TableVisits.COLUMN_VISITS_DATE_TIME + " as date, " +
                                    TableVisits.COLUMN_VISITS_CLINIC_ID + " as clinicID, " +
                                    TableVisits.COLUMN_VISITS_SPECIALIZATION_ID + " as specializationID ," +
                                    TableVisits.COLUMN_VISITS_DOCTOR_ID + " as doctorID" +
                                " from visits " +
                                "where " + TableVisits.COLUMN_VISITS_PROFILE_ID + " = " + currentProfilesID + " " +
                            "union all " +
                                "select " +
                                    "1 as type_of_visit, " +
                                    //TableAnalisesVisits._ID + " as id, " +
                                    "analises_visits._id as id, " +
                                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME + " as date, " +
                                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID + " as clinicID, " +
                                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID + " as specializationID, " +
                                    " 0"+" as doctorID" +
                                " from analises_visits " +
                                "where " + TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID + " = " + currentProfilesID + " ";
                                if (order_down) {
                                    queryString = queryString +
                                            ") " +
                                            "order by date ASC";
                                } else {
                                    queryString = queryString +
                                            ") " +
                                            "order by date DESC";
                                }

            }else {   //   (chosenFilterID == 0)   and   !(DATABASE_VERSION < 4)  // фильтр не выбран И новая версия БД (4 и выше)
                try {
                    queryString = queryString +
                            "select * " +
                            "from " +
                                "(" +
                                    "select " +
                                        "0 as type_of_visit, " +
                                       //TableVisits._ID + " as id, " +
                                        "visits._id as id, " +
                                        TableVisits.COLUMN_VISITS_DATE_TIME + " as date, " +
                                        TableVisits.COLUMN_VISITS_CLINIC_ID + " as clinicID, " +
                                        TableVisits.COLUMN_VISITS_SPECIALIZATION_ID + " as specializationID, " +
                                        TableVisits.COLUMN_VISITS_DOCTOR_ID + " as doctorID" +
                                    " from visits " +
                                    "where " + TableVisits.COLUMN_VISITS_PROFILE_ID + " = " + currentProfilesID + " " +
                                "union all " +
                                    "select " +
                                        "1 as type_of_visit, " +
                                        //TableAnalisesVisits._ID + " as id, " +
                                        "analises_visits._id as id, " +
                                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME + " as date, " +
                                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID + " as clinicID, " +

                                        "(select " +
                                            TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID + " " +
                                        "from " +
                                            DB_TABLE_ANALISES_VISITS_ANALISES_LIST +
                                        " where " +
                                            " analises_visits._id = analises_visits_analises_list.basic_analisis_visits_id " +
                                        ") as specializationID," +
                                        " 0" + " as doctorID" +
                                    " from analises_visits " +
/*
                                    "left join analises_visits_analises_list " +
                                        "on analises_visits._id = analises_visits_analises_list.basic_analisis_visits_id " +
*/
                                    "where " + TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID + " = " + currentProfilesID + " ";

                                    if (order_down) {
                                        queryString = queryString +
                                                ") " +
                                                "order by date ASC";
                                    } else {
                                        queryString = queryString +
                                                ") " +
                                                "order by date DESC";
                                    }

                }catch (Error e) {

                    }
            }

        }

        return queryString;

    }

    public Cursor getMainFiltratedVisitList(int chosenFilterID, int currentProfilesID, boolean order_down){

        Cursor cursor;
        String queryString;
        try {
            //int a = 1/0;
            queryString = getMainFiltratedVisitListText(chosenFilterID, currentProfilesID, order_down);
            return db.rawQuery(queryString, null);
        }catch (Exception e){
            //Toast.makeText(fContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            try {
                //int a = 1/0;
                queryString = getMainFiltratedVisitListText(-1, currentProfilesID, order_down);
                return db.rawQuery(queryString, null);
            }catch (Exception e1){
                //Toast.makeText(fContext, e1.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }
    }

    private String getFiltredQueryStringWithoutAnalisisType(Bundle bundle, int currentProfilesID, boolean order_down){

        String queryString = "";

        int onlyWithDate = (bundle.getBoolean("onlyWithDate")) ? 1 : 0;
        Long firstDate = bundle.getLong("firstDate");
        Long lastDate = bundle.getLong("lastDate");
        int mClinicsID = bundle.getInt("clinicsID");
        int mDoctorsID = bundle.getInt("doctorsID");
        int mSpecializationsID = bundle.getInt("specializationsID");
        int mCuresID = bundle.getInt("curesID");
        int mDiagnosisID = bundle.getInt("diagnosisID");
        int mAnalisesTypesID = bundle.getInt("analisesTypesID");
        int mRecommendationsID = bundle.getInt("recommendationsID");

        queryString = "" +
                "select * " +
                "from " +
                "(" +
                    "select " +
                        Constants.DIRECTIONS_VISITS_TYPE_0 + " as type_of_visit, " +
                        "visits._id as id, " +
                        TableVisits.COLUMN_VISITS_DATE_TIME + " as date, " +
                        TableVisits.COLUMN_VISITS_CLINIC_ID + " as clinicID, " +
                        TableVisits.COLUMN_VISITS_SPECIALIZATION_ID + " as specializationID, " +
                        TableVisits.COLUMN_VISITS_DOCTOR_ID + " as doctorID";
                        if (mCuresID != 0) {
                            queryString = queryString + ", " + TableVisitsCures.COLUMN_CURES_ID + " as curesID";
                        }
//                                      TableVisits.COLUMN_VISITS_DIAGNOSIS_ID + " as diagnosisID, " +
//                                      TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID + " as recommendationsID" +
                    queryString = queryString + " from visits ";
                    if (mCuresID != 0) {
                        queryString = queryString + "left join visits_cures" +
                            " on visits._id = visits_cures.visits_id ";
                    }
/*
                                        if (mRecommendationsID != 0){
                                            queryString = queryString + "left join visits_recommendations" +
                                                    " on visits._id = visits_recommendations.visits_id ";
                                        }
*/
                    //queryString = queryString + "where 1=1 ";
                    queryString = queryString + "where " + TableVisits.COLUMN_VISITS_PROFILE_ID + " = " + currentProfilesID + " ";

                    if (onlyWithDate == 1) {
                        queryString = queryString + " and date <> 0 ";
                    }
                    queryString = queryString + " and date >= " + firstDate + " ";
                    if (lastDate != 0) {
                        queryString = queryString + " and date <= " + lastDate + " ";
                    }
                    if (mClinicsID != 0) {
                        queryString = queryString + " and clinic = " + mClinicsID + " ";
                    }
                    if (mDoctorsID != 0) {
                        queryString = queryString + " and doctor = " + mDoctorsID + " ";
                    }
                    if (mSpecializationsID != 0) {
                        queryString = queryString + " and specialization = " + mSpecializationsID + " ";
                    }
                    if (mCuresID != 0) {
                        queryString = queryString + " and curesID = " + mCuresID + " ";
                    }

//                    if (mDiagnosisID != 0) {
//                        queryString = queryString + " and diagnosisID = " + mDiagnosisID + " ";
//                    }
//                    if (mAnalisesTypesID != 0) {
//                        queryString = queryString + " and 1 = 2 ";
//                    }
//                    if (mRecommendationsID != 0) {
//                        queryString = queryString + " and recommendationsID = " + mRecommendationsID + " ";
//                    }

        if ((mDoctorsID == 0)&
                (mSpecializationsID == 0)&
                (mCuresID == 0)&
                (mDiagnosisID == 0)&
                (mRecommendationsID == 0)) {

            queryString = queryString +
                    "union all " +
                        "select " +
                            Constants.DIRECTIONS_ANALISIS_TYPE_1 + " as type_of_visit, " +
                            "analises_visits._id as id, " +
                            TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME + " as date, " +
                            TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID + " as clinicID, " +
                            " 0 as specializationID, " +
                            " 0 as doctorID" +
                        " from analises_visits ";
                        //"where 1=1";
                        queryString = queryString + "where " + TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID + " = " + currentProfilesID + " ";

                        if (onlyWithDate == 1) {
                            queryString = queryString + " and date <> 0 ";
                        }
                        queryString = queryString + " and date >= " + firstDate + " ";
                        if (lastDate != 0) {
                            queryString = queryString + " and date <= " + lastDate + " ";
                        }
                        if (mClinicsID != 0) {
                            queryString = queryString + " and clinicID = " + mClinicsID + " ";
                        }


        }
        if (order_down) {
            queryString = queryString +
                    ") " +
                    "order by date ASC";
        } else {
            queryString = queryString +
                    ") " +
                    "order by date DESC";
        }


        return queryString;
    }

    private String getFiltredQueryStringWithAnalisisType(Bundle bundle, int currentProfilesID, boolean order_down){

        String queryString = "";

        int onlyWithDate = (bundle.getBoolean("onlyWithDate")) ? 1 : 0;
        Long firstDate = bundle.getLong("firstDate");
        Long lastDate = bundle.getLong("lastDate");
        int mClinicsID = bundle.getInt("clinicsID");
        int mDoctorsID = bundle.getInt("doctorsID");
        int mSpecializationsID = bundle.getInt("specializationsID");
        int mCuresID = bundle.getInt("curesID");
        int mDiagnosisID = bundle.getInt("diagnosisID");
        int mAnalisesTypesID = bundle.getInt("analisesTypesID");
        int mRecommendationsID = bundle.getInt("recommendationsID");

        queryString = "" +
                "select " +
                    Constants.DIRECTIONS_ANALISIS_TYPE_1 + " as type_of_visit, " +
                    "analises_visits._id as id, " +
                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME + " as date, " +
                    TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID + " as clinicID, " +

                    "(select " +
                            TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID + " " +
                        "from " +
                            DB_TABLE_ANALISES_VISITS_ANALISES_LIST +
                        " where " +
                            " analises_visits._id = analises_visits_analises_list.basic_analisis_visits_id " +
                            "and " + TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID + " = " + mAnalisesTypesID + " " +
                        "LIMIT 1) as specializationID," +
                    " 0 as doctorID" +
                " from analises_visits " +
                "where " + TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID + " = " + currentProfilesID + " ";

                if (onlyWithDate == 1) {
                    queryString = queryString + " and date <> 0 ";
                }
                queryString = queryString + " and date >= " + firstDate + " ";
                if (lastDate != 0) {
                    queryString = queryString + " and date <= " + lastDate + " ";
                }
                if (mClinicsID != 0) {
                    queryString = queryString + " and clinicID = " + mClinicsID + " ";
                }
                if (mAnalisesTypesID != 0) {
                    queryString = queryString + " and specializationID = " + mAnalisesTypesID + " ";
                }

                if (order_down) {
                    queryString = queryString +
                            ") " +
                            "order by date ASC";
                } else {
                    queryString = queryString +
                            ") " +
                            "order by date DESC";
                }


        return queryString;
    }

    // возвращаем курсор со списком всех визмтов
    Cursor getAllVisits() {
        //SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DB_TABLE_VISITS,
                new String[] {TableVisits._ID,
                              TableVisits.COLUMN_VISITS_DATE_TIME,
                              TableVisits.COLUMN_VISITS_CLINIC_ID,
                              TableVisits.COLUMN_VISITS_SPECIALIZATION_ID,
                              TableVisits.COLUMN_VISITS_DOCTOR_ID},
                null, null, null, null, "visit_date_time ASC");
    }

    //*****************************************************************
    // SPECIALIZATIONS
    //****************
    @Deprecated
    // возвращаем курсор со списком всех специализаций докторов
    Cursor getAllSpecialisations() {
        return db.query(DB_TABLE_SPECIALIZATIONS,
                new String[] {TableSpecializations._ID,
                        TableSpecializations.COLUMN_SPECIALIZATIONS_NAME},
                null, null, null, null, "name ASC");
    }

    @Deprecated
    // возвращаем ID specialization
    int getIDSpecializationsByName(String specializationName) {
        int specializationsID;
        Cursor cursor = db.query(DB_TABLE_SPECIALIZATIONS, new String[] {TableSpecializations._ID},
                "name = ?", new String[]{specializationName}, null, null, null);

        if (cursor.getCount()>0){
            cursor.moveToFirst();
            specializationsID = cursor.getInt(cursor.getColumnIndex(TableSpecializations._ID));
        }else specializationsID = 0;
        cursor.close();

        return specializationsID;
    }

    @Deprecated
    // возвращаем название specialization по ее ID
    String getSpecializationsNameByID(int specializationsID) {
        Cursor cursor = db.query(DB_TABLE_SPECIALIZATIONS, new String[] {TableSpecializations.COLUMN_SPECIALIZATIONS_NAME},
                "_id = ?", new String[]{String.valueOf((long) specializationsID)}, null, null, null);


        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            String specializationsName = cursor.getString(cursor.getColumnIndex(TableSpecializations.COLUMN_SPECIALIZATIONS_NAME));
            cursor.close();
            return specializationsName;
        }else {
            cursor.close();
            return "";
        }
    }

    @Deprecated
    // insert докторскую специализацию
    int insertSpecialization(String strName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableSpecializations.COLUMN_SPECIALIZATIONS_NAME, strName);
        return (int) db.insert(DB_TABLE_SPECIALIZATIONS, null, newValue);
    }

    @Deprecated
    // uptade докторскую специализацию
    void updateSpecialization(int chooseID, String chooseName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableSpecializations.COLUMN_SPECIALIZATIONS_NAME, chooseName);
        db.update(DB_TABLE_SPECIALIZATIONS, newValue, "_id = ?", new String[]{Long.toString(chooseID)});
    }

/*
    // delete докторскую специализацию
    void deleteSpecializationOldMethod(int choosenID) {
        db.delete(DB_TABLE_SPECIALIZATIONS, "_id = ?", new String[]{Long.toString(choosenID)});
    }

*/
    @Deprecated
    // delete докторскую специализацию
    int deleteSpecialization(int choosenID) {
        return db.delete(DB_TABLE_SPECIALIZATIONS, "_id = ?", new String[]{Long.toString(choosenID)});
    }

    @Deprecated
    //  проверка использования специализации.  TableDoctors  and  TableVisits
    // вернем истина, если клиника где-нибудь используется
    boolean checkOfUsageSpecialization(int specializationsID){

        // сначала TableDoctors
        Cursor cursor = db.query(DB_TABLE_DOCTORS, new String[]
                        {TableDoctors._ID,
                                TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID,},
                "specialization_id = ?", new String[]{Integer.toString(specializationsID)}, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();
        if (cursorCount > 0){
            return true;
        }

        // предыдущей проверки вообще-то достаточно, но если мы дошли до сюда, то значит у докторов не нашли. Будем искать в визитах:
        Cursor cursorV = db.query(DB_TABLE_VISITS, new String[]
                        {TableVisits._ID,
                                TableVisits.COLUMN_VISITS_SPECIALIZATION_ID,},
                "specialization = ?", new String[]{Integer.toString(specializationsID)}, null, null, null);

        int cursorVCount = cursorV.getCount();
        cursorV.close();
        return cursorVCount > 0;

    }

    //*****************************************************************
    // CLINICS
    //****************
    // возвращаем курсор со списком всех clinics
    Cursor getAllClinics() {
        return db.query(DB_TABLE_CLINICS,
                new String[] {TableClinics._ID,
                        TableClinics.COLUMN_CLINICS_NAME, TableClinics.COLUMN_CLINICS_DESCR},
                null, null, null, null, "name ASC");
    }

    // возвращаем поля (реквизиты) клиники по ее ID
    Bundle getClinicsFildsByClinicsID(int clinicsID) {
        Cursor cursor = db.query(DB_TABLE_CLINICS,
                new String[] {TableClinics._ID,
                        TableClinics.COLUMN_CLINICS_NAME,
                        //TableClinics.COLUMN_CLINICS_PHONES,
                        TableClinics.COLUMN_CLINICS_DESCR},
                "_id = ?", new String[]{String.valueOf(clinicsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            String mClinicsName = cursor.getString(cursor.getColumnIndex(TableClinics.COLUMN_CLINICS_NAME));
            bundle.putString("mClinicsName", mClinicsName);
            //String mClinicsPhones = cursor.getString(cursor.getColumnIndex(TableClinics.COLUMN_CLINICS_PHONES));
            //bundle.putString("mClinicsPhones", mClinicsPhones);
            String mClinicsDescr = cursor.getString(cursor.getColumnIndex(TableClinics.COLUMN_CLINICS_DESCR));
            bundle.putString("mClinicsDescr", mClinicsDescr);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // возвращаем ID clinic
    int getIDClinicsByName(String clinicName) {
        int clinicID;
        Cursor cursor = db.query(DB_TABLE_CLINICS, new String[] {TableClinics._ID},
                "name = ?", new String[]{clinicName}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            clinicID = cursor.getInt(cursor.getColumnIndex(TableClinics._ID));
        }else {clinicID = 0;}
        cursor.close();

        return clinicID;
    }

    // возвращаем название клиники по ее ID
    String getClinicsNameByID(int clinicsID) {
        Cursor cursor = db.query(DB_TABLE_CLINICS, new String[] {TableClinics._ID, TableClinics.COLUMN_CLINICS_NAME},
                "_id = ?", new String[]{String.valueOf(clinicsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            String clinicsName = cursor.getString(cursor.getColumnIndex(TableClinics.COLUMN_CLINICS_NAME));
            cursor.close();
            return clinicsName;
        }else {
            return "";
        }
    }

    // insert new clinic
    long insertClinic(String strName, String strAdress) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableClinics.COLUMN_CLINICS_NAME, strName);
        newValue.put(TableClinics.COLUMN_CLINICS_DESCR, strAdress);
        return db.insert(DB_TABLE_CLINICS, null, newValue);
    }

    // uptade clinic
    void updateClinic(int chooseID, String chooseName, String chooseAdress) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableClinics.COLUMN_CLINICS_NAME, chooseName);
        newValue.put(TableClinics.COLUMN_CLINICS_DESCR, chooseAdress);
        db.update(DB_TABLE_CLINICS, newValue, "_id = ?", new String[]{Integer.toString(chooseID)});
    }

    // delete clinic
    void deleteClinic(int choosenID) {
        db.delete(DB_TABLE_CLINICS, "_id = ?", new String[]{Integer.toString(choosenID)});
    }

    //  проверка использования клиники.  TableVisits  and  TableVisitsDirections
    // вернем истина, если клиника где-нибудь используется
    boolean checkOfUsageClinic(int clinicsID){

        // сначала TableVisits
        Cursor cursor = db.query(DB_TABLE_VISITS, new String[]
                        {TableVisits._ID,
                                TableVisits.COLUMN_VISITS_CLINIC_ID,},
                "clinic = ?", new String[]{Integer.toString(clinicsID)}, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();
        if (cursorCount > 0){
            return true;
        }

        // предыдущей проверки вообще-то достаточно, но если мы дошли до сюда, то значит в визитах не нашли. Будем искать в визитах-исследованиях:
        Cursor cursorAV = db.query(DB_TABLE_ANALISES_VISITS, new String[]
                        {TableAnalisesVisits._ID,
                                TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID,},
                "clinic_id = ?", new String[]{Integer.toString(clinicsID)}, null, null, null);

        int cursorAVCount = cursorAV.getCount();
        cursorAV.close();
        return cursorAVCount > 0;

    }

    //*****************************************************************
    // DOCTORS
    //****************
    // возвращаем курсор со списком всех doctors
    Cursor getAllDoctors() {
        return db.query(DB_TABLE_DOCTORS,
                new String[] {TableDoctors._ID,
                        TableDoctors.COLUMN_DOCTORS_NAME,
                        TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID},
                null, null, null, null, "name ASC");
    }

    // возвращаем ID doctor по его имени
    int getIDDoctorsByName(String doctorName) {
        int doctorsID;
        Cursor cursor = db.query(DB_TABLE_DOCTORS, new String[] {TableDoctors._ID},
                "name = ?", new String[]{doctorName}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            doctorsID = cursor.getInt(cursor.getColumnIndex(TableDoctors._ID));
        }else doctorsID = 0;
        cursor.close();

        return doctorsID;
    }

    // возвращаем имя doctor по его ID
    String getDoctorsNameByID(int doctorsID) {
        Cursor cursor = db.query(DB_TABLE_DOCTORS, new String[] {TableDoctors.COLUMN_DOCTORS_NAME},
                "_id = ?", new String[]{String.valueOf(doctorsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            String doctorsName = cursor.getString(cursor.getColumnIndex(TableDoctors.COLUMN_DOCTORS_NAME));
            cursor.close();
            return doctorsName;
        }else {
            cursor.close();
            return "";
        }
    }

    // insert new doctor
    long insertDoctor(String strName, int choosenSpecializationsID) {
        int item_id = 0;
        if (choosenSpecializationsID != 0) {
            item_id = choosenSpecializationsID;
        }
        ContentValues newValue = new ContentValues();
        newValue.put(TableDoctors.COLUMN_DOCTORS_NAME, strName);
        newValue.put(TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID, item_id);
        return db.insert(DB_TABLE_DOCTORS, null, newValue);
    }

    // uptade doctor
    void updateDoctor(int choosenID, String choosenName, int choosenSpecializationsID) {
        int item_id = 0;
        if (choosenSpecializationsID != 0) {
            item_id = choosenSpecializationsID;
        }
        ContentValues newValue = new ContentValues();
        newValue.put(TableDoctors.COLUMN_DOCTORS_NAME, choosenName);
        newValue.put(TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID, item_id);
        db.update(DB_TABLE_DOCTORS, newValue, "_id = ?", new String[]{Integer.toString(choosenID)});
    }

    // delete doctor
    void deleteDoctor(int choosenID) {
        db.delete(DB_TABLE_DOCTORS, "_id = ?", new String[]{Long.toString(choosenID)});
    }

    // вернуть специализацию доктора по его ID (id доктора)
    int getDoctorsSpecializationIDByDoctorsID(int dsID){
        //  сначала получим ID специализации доктора
        Cursor cursor = db.query(DB_TABLE_DOCTORS,
                new String[]{TableDoctors._ID, TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID},
                "_id = ?",
                new String[]{String.valueOf(dsID)},
                null, null, null);
        cursor.moveToFirst();
        int item_id = cursor.getInt(cursor.getColumnIndex(TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID));
        cursor.close();
        return item_id;

    }

    // вернуть специализацию доктора по его имени (по имени доктора)
    String getDoctorsSpecializationByName(String dName){
        //  сначала получим ID специализации доктора
        Cursor cursor = db.query(DB_TABLE_DOCTORS,
                new String[]{TableDoctors._ID, TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID},
                "name = ?",
                new String[]{dName},
                null, null, null);
        cursor.moveToFirst();
        int item_id = cursor.getInt(cursor.getColumnIndex(TableDoctors.COLUMN_DOCTORS_SPECIALIZATION_ID));
        //cursor.close();

        if (item_id != 0){
            // теперь получим значение специализации по ID
            cursor = db.query(DB_TABLE_SPECIALIZATIONS,
                    new String[]{TableSpecializations._ID, TableSpecializations.COLUMN_SPECIALIZATIONS_NAME},
                    "_id = ?",
                    new String[]{Long.toString(item_id)},
                    null, null, null);
            cursor.moveToFirst();
            String SpecializationName = cursor.getString(cursor.getColumnIndex(TableSpecializations.COLUMN_SPECIALIZATIONS_NAME));
            cursor.close();

            if (SpecializationName.length() != 0)
                return SpecializationName;
            else
                return "";}
        else
            cursor.close();
            return "";
    }

    //  проверка использования доктора.  TableVisits
    // вернем истина, если доктор где-нибудь используется
    boolean checkOfUsageDoctor(int doctorsID){

        // Будем искать в визитах:
        Cursor cursorV = db.query(DB_TABLE_VISITS, new String[]
                        {TableVisits._ID,
                                TableVisits.COLUMN_VISITS_DOCTOR_ID,},
                "doctor = ?", new String[]{Integer.toString(doctorsID)}, null, null, null);

        int cursorVCount = cursorV.getCount();
        cursorV.close();
        return cursorVCount > 0;

    }

    //*****************************************************************
    // CURES
    //****************
    // возвращаем курсор со списком всех назначений
    Cursor getAllCures() {
        return db.query(DB_TABLE_CURES,
                new String[] {TableCures._ID,
                        TableCures.COLUMN_CURES_NAME,
                        TableCures.COLUMN_CURES_DESCR},
                null, null, null, null, "name ASC");
    }

    // возвращаем поля (реквизиты) cure по его ID
    Bundle getCuresFildsByCuresID(int curesID) {
        Cursor cursor = db.query(DB_TABLE_CURES,
                new String[] {TableCures._ID,
                        TableCures.COLUMN_CURES_NAME,
                        TableCures.COLUMN_CURES_DESCR},
                "_id = ?", new String[]{String.valueOf(curesID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            String curesName = cursor.getString(cursor.getColumnIndex(TableCures.COLUMN_CURES_NAME));
            bundle.putString("curesName", curesName);
            String curesFeature = cursor.getString(cursor.getColumnIndex(TableCures.COLUMN_CURES_DESCR));
            bundle.putString("curesFeature", curesFeature);

            cursor.close();

            return bundle;
        }else {
            cursor.close();
            return null;
        }
    }

    // возвращаем название назначения по его ID
    String getCuresNameByID(int curesID) {
        String curesName;
        Cursor cursor = db.query(DB_TABLE_CURES, new String[]{TableCures._ID, TableCures.COLUMN_CURES_NAME},
                "_id = ?", new String[]{String.valueOf(curesID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            curesName = cursor.getString(cursor.getColumnIndex(TableCures.COLUMN_CURES_NAME));
        }else {
            curesName = "";
        }
        cursor.close();
        return curesName;
    }

    // возвращаем название назначения по его ID
    String getCuresDescrByID(int curesID) {
        String curesDescr;
        Cursor cursor = db.query(DB_TABLE_CURES, new String[]{TableCures._ID, TableCures.COLUMN_CURES_DESCR},
                "_id = ?", new String[]{String.valueOf(curesID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            curesDescr = cursor.getString(cursor.getColumnIndex(TableCures.COLUMN_CURES_DESCR));
        }else {
            curesDescr = "";
        }
        cursor.close();
        return curesDescr;
    }

    // возвращаем ID cures
    int getIDCuresByName(String curesName) {
        int curesID;
        Cursor cursor = db.query(DB_TABLE_CURES, new String[] {TableCures._ID},
                "name = ?", new String[]{curesName}, null, null, null);

        if (cursor.getCount()>0){
            cursor.moveToFirst();
            curesID = cursor.getInt(cursor.getColumnIndex(TableCures._ID));
        }else curesID = 0;
        cursor.close();

        return curesID;
    }

    // insert назначение
    int insertCure(String strName, String strDescr) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableCures.COLUMN_CURES_NAME, strName);
        newValue.put(TableCures.COLUMN_CURES_DESCR, strDescr);
        return (int) db.insert(DB_TABLE_CURES, null, newValue);
    }

    // uptade назначение
    void updateCure(int choosenID, String choosenName, String choosenDescr) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableCures.COLUMN_CURES_NAME, choosenName);
        newValue.put(TableCures.COLUMN_CURES_DESCR, choosenDescr);
        db.update(DB_TABLE_CURES, newValue, "_id = ?", new String[]{Integer.toString(choosenID)});
    }

    // delete назначение
    void deleteCure(int choosenID) {
        db.delete(DB_TABLE_CURES, "_id = ?", new String[]{Integer.toString(choosenID)});
    }

    //  проверка использования рекомендации.  TableVisitsRecommendations
    // вернем истина, если рекомендация где-нибудь используется
    boolean checkOfUsageCure(int curesID){

        // Будем искать в визитах:
        Cursor cursor = db.query(DB_TABLE_VISITS_CURES, new String[]
                        {TableVisitsCures._ID,
                                TableVisitsCures.COLUMN_CURES_ID,},
                "cures_id = ?", new String[]{Integer.toString(curesID)}, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount > 0;

    }

    //*****************************************************************
    // RECOMMENDATIONS
    //****************
    // возвращаем курсор со списком всех рекомендаций
    Cursor getAllRecommendations() {
        return db.query(DB_TABLE_RECOMMENDATIONS,
                new String[] {TableRecommendations._ID,
                        TableRecommendations.COLUMN_RECOMMENDATIONS_NAME},
                null, null, null, null, "name ASC");
    }

    // возвращаем название рекомендации по ее ID
    String getRecommendationsNameByID(int recommendationsID) {
        String recommendationsName;
        Cursor cursor = db.query(DB_TABLE_RECOMMENDATIONS, new String[] {TableRecommendations._ID, TableRecommendations.COLUMN_RECOMMENDATIONS_NAME},
                "_id = ?", new String[]{String.valueOf(recommendationsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            recommendationsName = cursor.getString(cursor.getColumnIndex(TableRecommendations.COLUMN_RECOMMENDATIONS_NAME));
        }else {
            recommendationsName = "";
        }
        cursor.close();
        return recommendationsName;
    }

    // возвращаем ID рекомендации
    private int getIDRecommendationsByName(String recommendationsName) {
        int recommendationsID;
        Cursor cursor = db.query(DB_TABLE_RECOMMENDATIONS, new String[] {TableRecommendations._ID},
                "name = ?", new String[]{recommendationsName}, null, null, null);

        if (cursor.getCount()>0){
            cursor.moveToFirst();
            recommendationsID = cursor.getInt(cursor.getColumnIndex(TableRecommendations._ID));
        }else recommendationsID = 0;
        cursor.close();

        return recommendationsID;
    }

    // insert рекомендацию
    int insertRecommendations(String recommendationsName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableRecommendations.COLUMN_RECOMMENDATIONS_NAME, recommendationsName);
        return (int) db.insert(DB_TABLE_RECOMMENDATIONS, null, newValue);
    }

    // uptade рекомендацию
    void updateRecommendations(int chosenID, String chosenName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableRecommendations.COLUMN_RECOMMENDATIONS_NAME, chosenName);
        db.update(DB_TABLE_RECOMMENDATIONS, newValue, "_id = ?", new String[]{Integer.toString(chosenID)});
    }

    // delete рекомендацию
    void deleteRecommendations(int chosenID) {
        db.delete(DB_TABLE_RECOMMENDATIONS, "_id = ?", new String[]{String.valueOf(chosenID)});
    }

    //*****************************************************************
    // DIAGNOSES
    //****************
    // возвращаем курсор со списком всех диагнозов
    Cursor getAllDiagnoses() {
        return db.query(DB_TABLE_DIAGNOSES,
                new String[] {TableDiagnoses._ID,
                        TableDiagnoses.COLUMN_DIAGNOSES_NAME},
                null, null, null, null, "name ASC");
    }

    // возвращаем название диагноза по его ID
    String getDiagnosesNameByID(int diagnosesID) {
        String diagnosesName;
        Cursor cursor = db.query(DB_TABLE_DIAGNOSES, new String[] {TableDiagnoses._ID, TableDiagnoses.COLUMN_DIAGNOSES_NAME},
                "_id = ?", new String[]{String.valueOf(diagnosesID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            diagnosesName = cursor.getString(cursor.getColumnIndex(TableDiagnoses.COLUMN_DIAGNOSES_NAME));
        }else {
            diagnosesName = "";
        }
        cursor.close();
        return diagnosesName;
    }

    // возвращаем ID диагноза
    int getIDDiagnosesByName(String diagnosesName) {
        int diagnosesID;
        Cursor cursor = db.query(DB_TABLE_DIAGNOSES, new String[] {TableDiagnoses._ID},
                "name = ?", new String[]{diagnosesName}, null, null, null);

        if (cursor.getCount()>0){
            cursor.moveToFirst();
            diagnosesID = cursor.getInt(cursor.getColumnIndex(TableDiagnoses._ID));
        }else diagnosesID = 0;
        cursor.close();

        return diagnosesID;
    }

    // insert диагноз
    long insertDiagnoses(String strName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableDiagnoses.COLUMN_DIAGNOSES_NAME, strName);
        return db.insert(DB_TABLE_DIAGNOSES, null, newValue);
    }

    // uptade диагноз
    void updateDiagnoses(int choosenID, String choosenName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableDiagnoses.COLUMN_DIAGNOSES_NAME, choosenName);
        db.update(DB_TABLE_DIAGNOSES, newValue, "_id = ?", new String[]{Integer.toString(choosenID)});
    }

    // delete диагноз
    void deleteDiagnoses(int choosenID) {
        db.delete(DB_TABLE_DIAGNOSES, "_id = ?", new String[]{String.valueOf(choosenID)});
    }

    //  проверка использования диагноза.  TableVisits
    // вернем истина, если диагноз где-нибудь используется
    boolean checkOfUsageDiagnosis(int diagnosisID){

        // Будем искать в визитах:
        Cursor cursorV = db.query(DB_TABLE_VISITS, new String[]
                        {TableVisits._ID,
                                TableVisits.COLUMN_VISITS_DIAGNOSIS_ID,},
                "diagnosis = ?", new String[]{Integer.toString(diagnosisID)}, null, null, null);

        int cursorVCount = cursorV.getCount();
        cursorV.close();
        return cursorVCount > 0;

    }

    //*****************************************************************
    // ANALISES_TYPES
    //****************
    // возвращаем курсор со списком всех видов анализов
    Cursor getAllAnalisesTypes() {
        return db.query(DB_TABLE_ANALISES_TYPES,
                new String[] {TableAnalisesTypes._ID,
                        TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME},
                null, null, null, null, "name ASC");
    }

    // возвращаем название вида анализа по его ID
    String getAnalisesTypesNameByID(int analisesTypesID) {
        String analisesTypesName;
        Cursor cursor = db.query(DB_TABLE_ANALISES_TYPES, new String[] {TableAnalisesTypes._ID, TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME},
                "_id = ?", new String[]{String.valueOf(analisesTypesID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            analisesTypesName = cursor.getString(cursor.getColumnIndex(TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME));
        }else {
            analisesTypesName = "";
        }
        cursor.close();
        return analisesTypesName;
    }

    // возвращаем ID вида анализа
    int getIDAnalisesTypesByName(String analisesTypesName) {
        int analisesTypesID;
        Cursor cursor = db.query(DB_TABLE_ANALISES_TYPES, new String[] {TableAnalisesTypes._ID},
                "name = ?", new String[]{analisesTypesName}, null, null, null);

        if (cursor.getCount()>0){
            cursor.moveToFirst();
            analisesTypesID = cursor.getInt(cursor.getColumnIndex(TableAnalisesTypes._ID));
        }else analisesTypesID = 0;
        cursor.close();

        return analisesTypesID;
    }

    // insert вид анализа
    int insertAnalisesType(String strName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME, strName);
        return (int) db.insert(DB_TABLE_ANALISES_TYPES, null, newValue);
    }

    // uptade вид анализа
    void updateAnalisesType(int choosenID, String choosenName) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME, choosenName);
        db.update(DB_TABLE_ANALISES_TYPES, newValue, "_id = ?", new String[]{Integer.toString(choosenID)});
    }

    // delete вид анализа
    void deleteAnalisesType(int choosenID) {
        db.delete(DB_TABLE_ANALISES_TYPES, "_id = ?", new String[]{String.valueOf(choosenID)});
    }

    //  проверка использования вида анализа.  TableAnalisisVisitsAnalisesList  and  TableVisitsDirections
    // вернем истина, если вид анализа где-нибудь используется
    boolean checkOfUsageAnalisisType(int analisisTypesID){

        // сначала TableAnalisisVisitsAnalisesList
        Cursor cursor = db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST, new String[]
                {TableAnalisisVisitsAnalisesList._ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID},
                "analisis_types_id = ?", new String[]{Integer.toString(analisisTypesID)}, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();
        if (cursorCount > 0){
            return true;
        }

        // предыдущей проверки вообще-то достаточно, но если мы дошли до сюда, то значит в визитах-исследованиях не нашли. Будем искать в направлениях визита:
        Cursor cursorD = db.query(DB_TABLE_VISITS_DIRECTIONS, new String[]
                        {TableVisitsDirections._ID,
                                TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE,
                                TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID},
                "direction_visits_type = ? and an_types_specialisations_id = ?", new String[]{Integer.toString(Constants.DIRECTIONS_ANALISIS_TYPE_1), Integer.toString(analisisTypesID)}, null, null, null);

        int cursorDCount = cursorD.getCount();
        cursorD.close();
        return cursorDCount > 0;

    }

    //*****************************************************************
    // VISITS FILTERES METHODS
    //****************
    // Clinics
    boolean hasDBClinic(String text){
        Cursor cursor =  db.query(DB_TABLE_CLINICS, new String[] {TableClinics._ID, TableClinics.COLUMN_CLINICS_NAME, TableClinics.COLUMN_CLINICS_DESCR},
                                  "name = ?", new String[]{text}, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount != 0;
    }

    // для быстрой фильтрации выпадающего списка при наборе символов (авто подсказка)
    Cursor getFilteredClinics(String text) {
        if (text.length()==0){
            return db.query(DB_TABLE_CLINICS, new String[] {TableClinics._ID, TableClinics.COLUMN_CLINICS_NAME, TableClinics.COLUMN_CLINICS_DESCR},
                    null, null, null, null, "name ASC");
        }else {
            return db.query(DB_TABLE_CLINICS, new String[] {TableClinics._ID, TableClinics.COLUMN_CLINICS_NAME, TableClinics.COLUMN_CLINICS_DESCR},
                    "name Like ?", new String[]{"%"+text+"%"}, null, null, "name ASC");
        }
    }

    // Doctors
    boolean hasDBDoctor(String text){
        Cursor cursor = db.query(DB_TABLE_DOCTORS, new String[] {TableDoctors._ID, TableDoctors.COLUMN_DOCTORS_NAME},
                "name = ?", new String[]{text}, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount != 0;
    }

    // для быстрой фильтрации выпадающего списка при наборе символов (авто подсказка)
    Cursor getFilteredDoctors(String text) {
        if (text.length()==0){
            return db.query(DB_TABLE_DOCTORS, new String[] {TableDoctors._ID, TableDoctors.COLUMN_DOCTORS_NAME},
                    null, null, null, null, "name ASC");
        }else {
            return db.query(DB_TABLE_DOCTORS, new String[] {TableDoctors._ID, TableDoctors.COLUMN_DOCTORS_NAME},
                    "name Like ?", new String[]{"%"+text+"%"}, null, null, "name ASC");
        }
    }

    // Specializations
    boolean hasDBSpecialization(String text){

        Cursor cursor = db.query(DB_TABLE_SPECIALIZATIONS, new String[]{TableSpecializations._ID, TableSpecializations.COLUMN_SPECIALIZATIONS_NAME},
                "name = ?", new String[]{text}, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount != 0;
    }

    // для быстрой фильтрации выпадающего списка при наборе символов (авто подсказка)
    Cursor getFilteredSpecialisations(String text) {
        if (text.length()==0){
            return db.query(DB_TABLE_SPECIALIZATIONS,
                    new String[] {TableSpecializations._ID,
                            TableSpecializations.COLUMN_SPECIALIZATIONS_NAME},
                    null, null, null, null, "name ASC");
        }else {
            return db.query(DB_TABLE_SPECIALIZATIONS,
                    new String[] {TableSpecializations._ID,
                            TableSpecializations.COLUMN_SPECIALIZATIONS_NAME},
                    "name Like ?", new String[]{"%"+text+"%"}, null, null, "name ASC");
        }
    }

    // Cures visit additionally
    boolean hasDBCure(String text){

        Cursor cursor = db.query(DB_TABLE_CURES, new String[] {TableCures._ID, TableCures.COLUMN_CURES_NAME},
                "name = ?", new String[]{text}, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount != 0;
    }

    // для быстрой фильтрации выпадающего списка при наборе символов (авто подсказка)
    Cursor getFilteredCures(String text) {
        if (text.length()==0){
            return db.query(DB_TABLE_CURES, new String[] {TableCures._ID, TableCures.COLUMN_CURES_NAME},
                    null, null, null, null, "name ASC");
        }else {
            return db.query(DB_TABLE_CURES, new String[] {TableCures._ID, TableCures.COLUMN_CURES_NAME},
                    "name Like ?", new String[]{"%"+text+"%"}, null, null, "name ASC");
        }
    }

    // Diagnoses
    boolean hasDBDiagnoses(String text){
        Cursor cursor =  db.query(DB_TABLE_DIAGNOSES, new String[] {TableDiagnoses._ID, TableDiagnoses.COLUMN_DIAGNOSES_NAME},
                "name = ?", new String[]{text}, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount != 0;
    }

    // для быстрой фильтрации выпадающего списка при наборе символов (авто подсказка)
    Cursor getFilteredDiagnoses(String text) {
        if (text.length()==0){
            return db.query(DB_TABLE_DIAGNOSES, new String[] {TableDiagnoses._ID, TableDiagnoses.COLUMN_DIAGNOSES_NAME},
                    null, null, null, null, "name ASC");
        }else {
            return db.query(DB_TABLE_DIAGNOSES, new String[] {TableDiagnoses._ID, TableDiagnoses.COLUMN_DIAGNOSES_NAME},
                    "name Like ?", new String[]{"%"+text+"%"}, null, null, "name ASC");
        }
    }

    // AnalisesTypes
    public boolean hasDBAnalisesTypes (String text){
        Cursor cursor = db.query(DB_TABLE_ANALISES_TYPES, new String[] {TableAnalisesTypes._ID, TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME},
                "name = ?", new String[]{text}, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount != 0;
    }

    // для быстрой фильтрации выпадающего списка при наборе символов (авто подсказка)
    Cursor getFilteredAnalisesTypes(String text) {
        if (text.length()==0){
            return db.query(DB_TABLE_ANALISES_TYPES, new String[] {TableAnalisesTypes._ID, TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME},
                    null, null, null, null, "name ASC");
        }else {
            return db.query(DB_TABLE_ANALISES_TYPES, new String[] {TableAnalisesTypes._ID, TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME},
                    "name Like ?", new String[]{"%"+text+"%"}, null, null, "name ASC");

        }
    }

    // Filter
    boolean hasDBFilter(String text){
        Cursor cursor =  db.query(DB_TABLE_FILTERS, new String[] {TableFilters._ID, TableFilters.COLUMN_FILTERS_NAME},
                "name = ?", new String[]{text}, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount != 0;
    }

    //*****************************************************************
    // работа с визитом
    //*****************************************************************
    void deleteVisit(int choosenID) {
        db.delete(DB_TABLE_VISITS, "_id = ?", new String[]{String.valueOf(choosenID)});
    }

    long insertVisit(Bundle bundle){
        ContentValues newValue = new ContentValues();
        //newValue.put(TableVisits.COLUMN_VISITS_DATE, bundle.getLong("mVisitsDate"));  // long
        //newValue.put(TableVisits.COLUMN_VISITS_TIME, bundle.getLong("mVisitsTime"));  // long
        newValue.put(TableVisits.COLUMN_VISITS_DATE_TIME, bundle.getLong("mVisitsDateTime"));  // long
        newValue.put(TableVisits.COLUMN_VISITS_CLINIC_ID, bundle.getInt("clinicID")==0 ? null : bundle.getInt("clinicID"));  // integer
        newValue.put(TableVisits.COLUMN_VISITS_DOCTOR_ID, bundle.getInt("doctorID")==0 ? null : bundle.getInt("doctorID"));  // integer
        newValue.put(TableVisits.COLUMN_VISITS_SPECIALIZATION_ID, bundle.getInt("specializationID")==0 ? null : bundle.getInt("specializationID"));  // integer
        newValue.put(TableVisits.COLUMN_VISITS_COMMENT, bundle.getString("vComment"));  // text
        newValue.put(TableVisits.COLUMN_VISITS_DIAGNOSIS_ID, bundle.getInt("diagnosisID"));  // integer
        newValue.put(TableVisits.COLUMN_VISITS_REF_ID, bundle.getInt("referencesID") == 0 ? null : bundle.getInt("referencesID"));  // integer
        newValue.put(TableVisits.COLUMN_VISITS_PROFILE_ID, bundle.getInt("profileID") == 0 ? null : bundle.getInt("profileID"));  // integer

        return db.insert(DB_TABLE_VISITS, null, newValue);

    }

    void updateVisit(int choosenID, Bundle bundle){
        ContentValues newValue = new ContentValues();
        long mVisitsDateTime = bundle.getLong("mVisitsDateTime");
        int clinicID = bundle.getInt("clinicID");
        int doctorID = bundle.getInt("doctorID");
        int specializationID = bundle.getInt("specializationID");
        int diagnosisID = bundle.getInt("diagnosisID");
        String vComment = bundle.getString("vComment");
        int referencesID = bundle.getInt("referencesID");
        int profileID = bundle.getInt("profileID");

        newValue.put(TableVisits.COLUMN_VISITS_DATE_TIME, mVisitsDateTime);  // long
        newValue.put(TableVisits.COLUMN_VISITS_CLINIC_ID, clinicID);  // integer
        newValue.put(TableVisits.COLUMN_VISITS_DOCTOR_ID, doctorID);  // integer
        newValue.put(TableVisits.COLUMN_VISITS_SPECIALIZATION_ID, specializationID);  // integer
        newValue.put(TableVisits.COLUMN_VISITS_COMMENT, vComment);  // text
        newValue.put(TableVisits.COLUMN_VISITS_DIAGNOSIS_ID, diagnosisID);  // integer
        newValue.put(TableVisits.COLUMN_VISITS_REF_ID, referencesID);  // integer
        newValue.put(TableVisits.COLUMN_VISITS_PROFILE_ID, profileID);  // integer

        db.update(DB_TABLE_VISITS, newValue, "_id = ?", new String[]{String.valueOf(choosenID)});

    }

    // обновление в визите - направлении только ссылки на основание и диагноз
    void updateVisitReferenceAndDiagnosis(int choosenID, int referencesID, int diagnosisID){
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisits.COLUMN_VISITS_REF_ID, referencesID==0 ? null : referencesID);  // integer
        newValue.put(TableVisits.COLUMN_VISITS_DIAGNOSIS_ID, diagnosisID==0 ? null : diagnosisID);  // integer

        db.update(DB_TABLE_VISITS, newValue, "_id = ?", new String[]{String.valueOf(choosenID)});
    }

    // обновление в визите только ссылки на профиль
    void updateVisitsProfile(int choosenID, int profilisID){
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisits.COLUMN_VISITS_PROFILE_ID, profilisID==0 ? null : profilisID);  // integer

        db.update(DB_TABLE_VISITS, newValue, "_id = ?", new String[]{String.valueOf(choosenID)});
    }

    // возвращаем поля (реквизиты) визита по его ID
    Bundle getVisitsFildsByVisitsID(int visitsID) {
        Cursor cursor = db.query(DB_TABLE_VISITS,
                                new String[] {TableVisits._ID,
                                              TableVisits.COLUMN_VISITS_DATE_TIME,
                                              TableVisits.COLUMN_VISITS_CLINIC_ID,
                                              TableVisits.COLUMN_VISITS_DOCTOR_ID,
                                              TableVisits.COLUMN_VISITS_SPECIALIZATION_ID,
                                              TableVisits.COLUMN_VISITS_COMMENT,
                                              TableVisits.COLUMN_VISITS_DIAGNOSIS_ID,
                                              TableVisits.COLUMN_VISITS_REF_ID,
                                              TableVisits.COLUMN_VISITS_PROFILE_ID},
                "_id = ?", new String[]{String.valueOf(visitsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            //Long mVisitsDate = cursor.getLong(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_DATE));
            //bundle.putLong("mVisitsDate", mVisitsDate);
            //Long mVisitsTime = cursor.getLong(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_TIME));
            //bundle.putLong("mVisitsTime", mVisitsTime);
            Long mVisitsDateTime = cursor.getLong(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_DATE_TIME));
            bundle.putLong("mVisitsDateTime", mVisitsDateTime);
            int clinicsID = cursor.getInt(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_CLINIC_ID));
            bundle.putInt("clinicsID", clinicsID);
            int doctorsID = cursor.getInt(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_DOCTOR_ID));
            bundle.putInt("doctorsID", doctorsID);
            int specializationsID = cursor.getInt(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_SPECIALIZATION_ID));
            bundle.putInt("specializationsID", specializationsID);
            bundle.putString("vComment", cursor.getString(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_COMMENT)));

            int diagnosisID = cursor.getInt(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_DIAGNOSIS_ID));
            bundle.putInt("diagnosisID", diagnosisID);
            int referencesID = cursor.getInt(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_REF_ID));
            bundle.putInt("referencesID", referencesID);
            int profileID = cursor.getInt(cursor.getColumnIndex(TableVisits.COLUMN_VISITS_PROFILE_ID));
            bundle.putInt("profileID", profileID);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // вернуть строковое представление визита:
    String getVisitsViewByID(int visitsID){
        if (visitsID == 0)return "";
        else {
            Bundle bundle = getVisitsFildsByVisitsID(visitsID);
            if (bundle == null)return "";
            else {
            // дата
            Long mVisitsDate = bundle.getLong("mVisitsDateTime");
            String mVisitsDateText;
            if (mVisitsDate == 0L) {
                mVisitsDateText = "";
            } else {
                CharSequence date = DateFormat.format("dd.MM.yy", mVisitsDate);
                mVisitsDateText = String.valueOf(date);
            }
            // специализация
            int mSpecializationsID = bundle.getInt("specializationsID", 0);
            String specializationsText = getSpecializationsNameByID(mSpecializationsID);
            int mDoctorsID = bundle.getInt("doctorsID", 0);
            String doctorsText = getDoctorsNameByID(mDoctorsID);
            int mClinicsID = bundle.getInt("clinicsID", 0);
            String clinicsText = getClinicsNameByID(mClinicsID);

            StringBuffer sb = new StringBuffer("");
            sb.append(mVisitsDateText).append("; ").append(specializationsText).append("; ").append(doctorsText).append("; ").append(clinicsText);

            String visitsStringView = sb.toString();

            return visitsStringView;
            }
        }
    }

    //  проверка использования визита.
    // вернем истина, если визит где-нибудь используется
    String checkOfUsageVisit(Context context, int visitsID){

        final String visit_must_not_delete_type_recommendations = context.getResources().getString(R.string.visit_must_not_delete_type_recommendations);
        // сначала TableVisitsRecommendations
        Cursor cursor = db.query(DB_TABLE_VISITS_RECOMMENDATIONS, new String[]
                        {TableVisitsRecommendations._ID,
                                TableVisitsRecommendations.COLUMN_BASIC_VISITS_ID},
                "visits_id = ?", new String[]{Integer.toString(visitsID)}, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();
        if (cursorCount > 0){
            return visit_must_not_delete_type_recommendations;
        }

        final String visit_must_not_delete_type_cures = context.getResources().getString(R.string.visit_must_not_delete_type_cures);
        // предыдущей проверки вообще-то достаточно, но если мы дошли до сюда, то значит в TableVisitsRecommendations не нашли. Будем искать дальше:
        Cursor cursorVC = db.query(DB_TABLE_VISITS_CURES, new String[]
                        {TableVisitsCures._ID,
                                TableVisitsCures.COLUMN_BASIC_VISITS_ID},
                        "visits_id = ?",
                        new String[]{Integer.toString(visitsID)},
                        null, null, null);

        int cursorVCCount = cursorVC.getCount();
        cursorVC.close();
        if (cursorVCCount > 0){
            return visit_must_not_delete_type_cures;
        }

        final String visit_must_not_delete_type_directions = context.getResources().getString(R.string.visit_must_not_delete_type_directions);
        Cursor cursorVD = db.query(DB_TABLE_VISITS_DIRECTIONS, new String[]
                        {TableVisitsDirections._ID,
                                TableVisitsDirections.COLUMN_BASIC_VISITS_ID},
                "basic_visits_id = ?",
                new String[]{Integer.toString(visitsID)},
                null, null, null);

        int cursorVDCount = cursorVD.getCount();
        cursorVD.close();
        if (cursorVDCount > 0){
            return visit_must_not_delete_type_directions;
        }

        final String visit_must_not_delete_type_analises_list = context.getResources().getString(R.string.visit_must_not_delete_type_analises_list);
        Cursor cursorAVL = db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST, new String[]
                        {TableAnalisisVisitsAnalisesList._ID,
                                TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID},
                "references_visits_id = ?",
                new String[]{Integer.toString(visitsID)},
                null, null, null);

        int cursorAVLCount = cursorAVL.getCount();
        cursorAVL.close();
        if (cursorAVLCount > 0){
            return visit_must_not_delete_type_analises_list;
        }

        final String visit_must_not_delete_type_photos = context.getResources().getString(R.string.visit_must_not_delete_type_photos);
        Cursor cursorPh = db.query(DB_TABLE_VISITS_PHOTOS, new String[]
                        {TableVisitsPhotos._ID,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_ID},
                        "basic_visits_type = ? and basic_visits_id = ?",
                        new String[]{Integer.toString(Constants.DIRECTIONS_VISITS_TYPE_0), Integer.toString(visitsID)},
                        null, null, null);

        int cursorPhCount = cursorPh.getCount();
        cursorPh.close();
        if (cursorPhCount > 0){
            return visit_must_not_delete_type_photos;
        }

        return "";
    }

    void deleteVisitWithDependensesTablesFinely(int choosenVisitsID){

        // сначала почистим таблицу направлений
        Cursor cursorD = getAllVisitsDirectionsList(choosenVisitsID);
        if (cursorD.getCount()>0){
            cursorD.moveToFirst();
            do {
                int recordID = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                int directionsVisitsType = cursorD.getInt(cursorD.getColumnIndex(TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE));
                int directionsVisitsID = cursorD.getInt(cursorD.getColumnIndex(TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID));

                // если есть направление, то почистим в нем ссылки на удаляемый визит (сами направления не удаляем):
                if (directionsVisitsID > 0){
                    switch (directionsVisitsType){
                        case Constants.DIRECTIONS_VISITS_TYPE_0:
                            Bundle bundle = getVisitsFildsByVisitsID(directionsVisitsID);
                            bundle.putInt("referencesID", 0);
                            updateVisit(directionsVisitsID, bundle); // проверить !!!!!!!!!!!!!
                            break;
                        case Constants.DIRECTIONS_ANALISIS_TYPE_1:
                            // получаем все строки таблицы "AnalisesList" со ссылкой на наш визит
                            Cursor cursorDAVL = getAllAnalisisVisitsAnalisesListByReferenceID(choosenVisitsID);
                            if (cursorDAVL.getCount()>0){
                                cursorDAVL.moveToFirst();
                                do {
                                    int avlRecordID = cursorDAVL.getInt(cursorDAVL.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList._ID));
                                    Bundle bundle1 = getAnalisisVisitsAnalisesListFildsByAnalisisVisitsAnalisesListID(avlRecordID);
                                    updateAnalisisVisitsAnalisesList(avlRecordID,
                                            bundle1.getInt("basicAnalisisVisitsID"),
                                            bundle1.getInt("analisisVisitsTypesID"),
                                            0); // <- очистили ссылку
                                }while (cursorDAVL.moveToNext());
                            }
                            break;
                    }
                }
                // теперь почистим таблицу
                deleteVisitsDirection(recordID);
            } while (cursorD.moveToNext());
        }
        cursorD.close();

        // теперь почистим таблицу назначений
        Cursor cursorVC = db.query(DB_TABLE_VISITS_CURES,
                new String[] {TableVisitsCures._ID,
                              TableVisitsCures.COLUMN_BASIC_VISITS_ID},
                "visits_id = ?",
                new String[]{Integer.toString(choosenVisitsID)},
                null, null, null);
        if (cursorVC.getCount() > 0){
            cursorVC.moveToFirst();
            do {
                int recordID = cursorVC.getInt(cursorVC.getColumnIndex(TableVisitsCures._ID));
                deleteVisitsCure(recordID);
            } while (cursorVC.moveToNext());
        }
        cursorVC.close();

        // теперь почистим таблицу рекомендаций
        Cursor cursorR = db.query(DB_TABLE_VISITS_RECOMMENDATIONS, new String[]
                        {TableVisitsRecommendations._ID,
                                TableVisitsRecommendations.COLUMN_BASIC_VISITS_ID},
                "visits_id = ?", new String[]{Integer.toString(choosenVisitsID)}, null, null, null);
        if (cursorR.getCount() > 0){
            cursorR.moveToFirst();
            do {
                int recordID = cursorR.getInt(cursorR.getColumnIndex(DBMethods.TableVisitsRecommendations._ID));
                deleteVisitsRecommendations(recordID);
            } while (cursorR.moveToNext());
        }
        cursorR.close();

        // теперь почистим таблицу фотографий и сами фотографии
        Cursor cursorPh = db.query(DB_TABLE_VISITS_PHOTOS, new String[]
                        {TableVisitsPhotos._ID,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE,
                                TableVisitsPhotos.COLUMN_PHOTOS_TYPE,
                                TableVisitsPhotos.COLUMN_PHOTOS_URI,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_ID},
                "basic_visits_type = ? and basic_visits_id = ?",
                new String[]{Integer.toString(Constants.DIRECTIONS_VISITS_TYPE_0), Integer.toString(choosenVisitsID)},
                null, null, null);
        if (cursorPh.getCount() > 0){
            cursorPh.moveToFirst();
            do {
                int recordID = cursorPh.getInt(cursorPh.getColumnIndex(DBMethods.TableVisitsPhotos._ID));
                int photosType = cursorPh.getInt(cursorPh.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_TYPE));
                String filePath = cursorPh.getString(cursorPh.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_URI));
                // сначала сам файл, но файл будем удалять только если он в приложении:
                if (photosType == Constants.PHOTOS_TYPE_APP){
                    File currentFile = new File(filePath);
                    currentFile.delete();
                }
                // затем таблицу
                deleteVisitsPhotoByID(recordID);
            } while (cursorPh.moveToNext());
        }
        cursorPh.close();

        // также нужно почистить ссылку на наш визит в визите-основании, если наш является направлением:
        Cursor cursorVD = db.query(DB_TABLE_VISITS_DIRECTIONS, new String[]
                        {TableVisitsDirections._ID,
                                TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE,
                                TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID},
                "direction_visits_type = ? and direction_visits_id = ?",
                new String[]{Integer.toString(Constants.DIRECTIONS_VISITS_TYPE_0), Integer.toString(choosenVisitsID)},
                null, null, null);
        if (cursorVD.getCount() > 0){
            cursorVD.moveToFirst();
            do {
                int recordID = cursorVD.getInt(cursorVD.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                int basicVisitsID = cursorVD.getInt(cursorVD.getColumnIndex(TableVisitsDirections.COLUMN_BASIC_VISITS_ID));
                int anTypesSpecID = cursorVD.getInt(cursorVD.getColumnIndex(TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID));
                // нашли визит-основание
                updateRowVisitsDirections(recordID, basicVisitsID, Constants.DIRECTIONS_VISITS_TYPE_0, anTypesSpecID, 0);
            } while (cursorVD.moveToNext());
        }
        cursorVD.close();

        // окончательно удаляем сам визит
        deleteVisit(choosenVisitsID);
    }

    //*****************************************************************
    // VISITS_CURES
    //****************
    // возвращаем курсор со списком всех visits_cures назначений выбранного визита
    Cursor getAllVisitsCures(int visitsID) {
        return db.query(DB_TABLE_VISITS_CURES,
                new String[]{TableVisitsCures._ID,
                        TableVisitsCures.COLUMN_CURES_ID,
                        TableVisitsCures.COLUMN_CURES_DESCR},
                "visits_id = ?", new String[]{String.valueOf(visitsID)}, null, null, null);
    }

    // возвращаем поля (реквизиты) visits_cures по его ID
    public Bundle getVisitsCuresFildsByVisitsCuresID(int visitsCuresID) {
        Cursor cursor = db.query(DB_TABLE_VISITS_CURES,
                new String[] {TableVisitsCures._ID,
                        TableVisitsCures.COLUMN_BASIC_VISITS_ID,
                        TableVisitsCures.COLUMN_CURES_ID,
                        TableVisitsCures.COLUMN_CURES_DESCR},
                "_id = ?", new String[]{String.valueOf(visitsCuresID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            int visitsID = cursor.getInt(cursor.getColumnIndex(TableVisitsCures.COLUMN_BASIC_VISITS_ID));
            bundle.putInt("visitsID", visitsID);
            int curesID = cursor.getInt(cursor.getColumnIndex(TableVisitsCures.COLUMN_CURES_ID));
            bundle.putInt("curesID", curesID);
            String visitsCuresDescr = cursor.getString(cursor.getColumnIndex(TableVisitsCures.COLUMN_CURES_DESCR));
            bundle.putString("visitsCuresDescr", visitsCuresDescr);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // insert new visits cures
    private void insertVisitsCure(int visitsID, ArrayList<ModelVisitsCures> vcArrayList) {
        int curesID;
        String curesDescr;
        for (int i = 0; i < vcArrayList.size(); i++) {
            HashMap itemMap = vcArrayList.get(i);
            String curesName = String.valueOf(itemMap.get("curesName"));
            curesID = getIDCuresByName(curesName);
            curesDescr = (String) itemMap.get("curesDescr");


            ContentValues newValue = new ContentValues();
            newValue.put(TableVisitsCures.COLUMN_BASIC_VISITS_ID, visitsID);
            newValue.put(TableVisitsCures.COLUMN_CURES_ID, curesID);
            newValue.put(TableVisitsCures.COLUMN_CURES_DESCR, curesDescr);
            db.insert(DB_TABLE_VISITS_CURES, null, newValue);
        }
    }

    // uptade visits cures
    public void updateVisitsCure(int visitsID, ArrayList<ModelVisitsCures> vcArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllVisitsCures(visitsID);
        if (cursor.getCount()>0){
            cursor.moveToFirst();
            do {
                int currrentID = cursor.getInt(cursor.getColumnIndex(TableVisitsCures._ID));
                deleteVisitsCure(currrentID);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (vcArrayList.size()>0) {
            insertVisitsCure(visitsID, vcArrayList);
        }
    }

    // delete visits cures
    public void deleteVisitsCure(int recordsID) {
        db.delete(DB_TABLE_VISITS_CURES, "_id = ?", new String[]{Integer.toString(recordsID)});
    }

    // insert new record (одна строка) в visits_cures_list
    public long insertRecordVisitsCuresList(int basicVisitsID, int visitsCuresID, String visitsCuresDescr, int curesExecutionsListsID) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisitsCures.COLUMN_BASIC_VISITS_ID, basicVisitsID);      // визит - основание, на котором выписали назначения
        newValue.put(TableVisitsCures.COLUMN_CURES_ID, visitsCuresID);      // тип назначения
        newValue.put(TableVisitsCures.COLUMN_CURES_DESCR, visitsCuresDescr);  // описание назначения
        //newValue.put(TableVisitsCures.COLUMN_DIRECTION_VISITS_ID, curesExecutionsListsID);  // график приемки назначений или список-график визитов на процедуры
        return db.insert(DB_TABLE_VISITS_CURES, null, newValue);

    }

    // insert new visits_cures_list
    public void insertVisitsCuresList(int basicVisitsID, ArrayList<MVisitCuresFragment.VisitsCuresModel> vcArrayList) {

        int visitsCuresID;
        String visitsCuresDescr;
        int curesExecutionsListsID;

        for (int i = 0; i < vcArrayList.size(); i++) {
            MVisitCuresFragment.VisitsCuresModel curesModel = vcArrayList.get(i);
            visitsCuresID = curesModel.visitsCuresID;
            visitsCuresDescr = curesModel.visitsCuresDescr;
            curesExecutionsListsID = curesModel.curesExecutionsListsID;

            insertRecordVisitsCuresList(basicVisitsID, visitsCuresID, visitsCuresDescr, curesExecutionsListsID);
        }
    }

    // uptade visits_cures_list
    void updateVisitsCuresList(int currentVisitsID, ArrayList<MVisitCuresFragment.VisitsCuresModel> vcArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllVisitsCures(currentVisitsID);
        if (cursor.getCount()>0){
            int currentID;
            cursor.moveToFirst();
            do {
                currentID = cursor.getInt(cursor.getColumnIndex(TableVisitsCures._ID));
                deleteVisitsCure(currentID);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (vcArrayList.size()>0){
            insertVisitsCuresList(currentVisitsID, vcArrayList);
        }
    }

    //*****************************************************************
    // VISITS_RECOMMENDATIONS
    //****************
    // возвращаем курсор со списком всех visits_recommendations назначений выбранного визита
    Cursor getAllVisitsRecommendations(int visitsID) {
        return db.query(DB_TABLE_VISITS_RECOMMENDATIONS,
                new String[] {TableVisitsRecommendations._ID,
                        TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID},
                "visits_id = ?", new String[]{String.valueOf(visitsID)}, null, null, null);
    }

    // возвращаем поля (реквизиты) visits_recommendations по его ID (все поля по одной рекомендации)
    public Bundle getVisitsRecommendationsFildsByVisitsRecommendationsID(int visitsRecommendationsID) {
        Cursor cursor = db.query(DB_TABLE_VISITS_RECOMMENDATIONS,
                new String[] {TableVisitsRecommendations._ID,
                        TableVisitsRecommendations.COLUMN_BASIC_VISITS_ID,
                        TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID},
                "_id = ?", new String[]{String.valueOf(visitsRecommendationsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            int visitsID = cursor.getInt(cursor.getColumnIndex(TableVisitsRecommendations.COLUMN_BASIC_VISITS_ID));
            bundle.putInt("visitsID", visitsID);
            int recommendationsID = cursor.getInt(cursor.getColumnIndex(TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID));
            bundle.putInt("recommendationsID", recommendationsID);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // insert new visits recommendations (по имени????????????)
    public void insertVisitsRecommendations(int visitsID, ArrayList<ModelVisitsCures> vrArrayList) {
        int recommendationsID;
        for (int i = 0; i < vrArrayList.size(); i++) {
            HashMap itemMap = vrArrayList.get(i);
            String recommendationsName = String.valueOf(itemMap.get("recommendationsName"));
            recommendationsID = getIDRecommendationsByName(recommendationsName);

            ContentValues newValue = new ContentValues();
            newValue.put(TableVisitsRecommendations.COLUMN_BASIC_VISITS_ID, visitsID);
            newValue.put(TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID, recommendationsID);
            db.insert(DB_TABLE_VISITS_RECOMMENDATIONS, null, newValue);
        }
    }

    // uptade visits recommendations
    public void updateVisitsRecommendations(int visitsID, ArrayList<ModelVisitsCures> vrArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllVisitsRecommendations(visitsID);
        if (cursor.getCount()>0){
            cursor.moveToFirst();
            do {
                int currentID = cursor.getInt(cursor.getColumnIndex(TableVisitsRecommendations._ID));
                deleteVisitsRecommendations(currentID);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (vrArrayList.size()>0) {
            insertVisitsRecommendations(visitsID, vrArrayList);
        }
    }

    // delete visits recommendations
    public void deleteVisitsRecommendations(int recordsID) {
        db.delete(DB_TABLE_VISITS_RECOMMENDATIONS, "_id = ?", new String[]{Integer.toString(recordsID)});
    }

    // insert new record (одна строка) в visits_cures_list
    public long insertRecordVisitsRecommendationsList(int basicVisitsID, int visitsRecommendationsID) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisitsRecommendations.COLUMN_BASIC_VISITS_ID, basicVisitsID);      // визит - основание, на котором выдали рекомендацию
        newValue.put(TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID, visitsRecommendationsID);      // id рекомендации
        return db.insert(DB_TABLE_VISITS_RECOMMENDATIONS, null, newValue);

    }

    // insert new visits_cures_list
    public void insertVisitsRecommendationsList(int basicVisitsID, ArrayList<MVisitRecommendationsFragment.VisitsRecommendationsModel> vrArrayList) {

        int visitsRecommendationsID;

        for (int i = 0; i < vrArrayList.size(); i++) {
            MVisitRecommendationsFragment.VisitsRecommendationsModel recommendationsModel = vrArrayList.get(i);
            visitsRecommendationsID = recommendationsModel.visitsRecommendationsID;

            insertRecordVisitsRecommendationsList(basicVisitsID, visitsRecommendationsID);
        }
    }

    // uptade visits_cures_list
    void updateVisitsRecommendationsList(int currentVisitsID, ArrayList<MVisitRecommendationsFragment.VisitsRecommendationsModel> vrArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllVisitsRecommendations(currentVisitsID);
        if (cursor.getCount()>0){
            int currentID;
            cursor.moveToFirst();
            do {
                currentID = cursor.getInt(cursor.getColumnIndex(TableVisitsRecommendations._ID));
                deleteVisitsRecommendations(currentID);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (vrArrayList.size()>0){
            insertVisitsRecommendationsList(currentVisitsID, vrArrayList);
        }
    }

    //  проверка использования рекомендации.  TableVisitsRecommendations
    // вернем истина, если рекомендация где-нибудь используется
    boolean checkOfUsageRecommendation(int recommendationsID){

        // Будем искать в визитах:
        Cursor cursor = db.query(DB_TABLE_VISITS_RECOMMENDATIONS, new String[]
                        {TableVisitsRecommendations._ID,
                                TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID,},
                "recommendations_id = ?", new String[]{Integer.toString(recommendationsID)}, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();
        if (cursorCount > 0){
            return true;
        }

        return false;
    }

    //*****************************************************************
    // VISITS_DIRECTIONS (направления визита)
    //****************
    // возвращаем курсор со списком всех visits_directions направлений выбранного визита (старая версия до 4)
    public Cursor getAllVisitsDirections(int visitsID) {
        return db.query(DB_TABLE_VISITS_DIRECTIONS,
                new String[] {TableVisitsDirections._ID,
                        TableVisitsDirections.COLUMN_BASIC_VISITS_ID,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID},
                "basic_visits_id = ?", new String[]{String.valueOf(visitsID)}, null, null, null);
    }

    // возвращаем курсор со списком всех направлений безотносительно визитов
    Cursor getAllDirectionsAllVisits() {
        return db.query(DB_TABLE_VISITS_DIRECTIONS,
                new String[] {TableVisitsDirections._ID,
                        TableVisitsDirections.COLUMN_BASIC_VISITS_ID,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE,
                        TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID},
                null, null, null, null, null);
    }

    // обновление в направлении только одной конкретной строки
    void updateRowVisitsDirections(int recordsID, int basicVisitsID, int directionsVisitsTypes, int anTypeOrSpecID, int directionsVisitsID){
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisitsDirections.COLUMN_BASIC_VISITS_ID, basicVisitsID);  // integer
        newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE, directionsVisitsTypes);  // integer
        newValue.put(TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID, anTypeOrSpecID);  // integer
        newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID, directionsVisitsID);  // integer

        db.update(DB_TABLE_VISITS_DIRECTIONS, newValue, "_id = ?", new String[]{String.valueOf(recordsID)});
    }

    // возвращаем поля (реквизиты) visits_directions по его ID (по индексу). Одна строка (текущий визит и направление, выданное на этом визите).
    Bundle getVisitsDirectionsFildsByVisitsDirectionsID(int directionsVisitsTypesID, int visitsDirectionsID) {
        Cursor cursor = db.query(DB_TABLE_VISITS_DIRECTIONS,
                new String[] {TableVisitsDirections._ID,
                        TableVisitsDirections.COLUMN_BASIC_VISITS_ID,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE,
                        TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID},
                "direction_visits_type = ? and _id = ?",
                new String[]{Integer.toString(directionsVisitsTypesID), Integer.toString(visitsDirectionsID)},
                null, null, null);

        Bundle bundle = new Bundle();
        if (cursor.getCount()>0) {
            cursor.moveToFirst();

            int basicVisitsID = cursor.getInt(cursor.getColumnIndex(TableVisitsDirections.COLUMN_BASIC_VISITS_ID));
            bundle.putInt("basicVisitsID", basicVisitsID);
            int directionVisitsTypesID = cursor.getInt(cursor.getColumnIndex(TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE));
            bundle.putInt("directionVisitsTypesID", directionVisitsTypesID);
            int directionATorSpecID = cursor.getInt(cursor.getColumnIndex(TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID));
            bundle.putInt("directionVisitsID", directionATorSpecID);
            int directionVisitsID = cursor.getInt(cursor.getColumnIndex(TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID));
            bundle.putInt("directionVisitsID", directionVisitsID);

            cursor.close();

            return bundle;
        }else {
            return bundle;
        }
    }

    // insert new visits directions
    public void insertVisitsDirection(int basicVisitsID, ArrayList<ModelVisitsCures> vdArrayList) {
        int directionVisitsID;
        int directionVisitsTypesID;
        for (int i = 0; i < vdArrayList.size(); i++) {
            HashMap itemMap = vdArrayList.get(i);
            directionVisitsTypesID = Integer.parseInt(String.valueOf(itemMap.get("directionVisitsTypesID")));
            directionVisitsID = Integer.parseInt(String.valueOf(itemMap.get("directionVisitsID")));

            ContentValues newValue = new ContentValues();
            newValue.put(TableVisitsDirections.COLUMN_BASIC_VISITS_ID, basicVisitsID);
            newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE, directionVisitsTypesID);
            newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID, directionVisitsID);
            db.insert(DB_TABLE_VISITS_DIRECTIONS, null, newValue);
        }
    }

    // insert new запись в visits directions
    //public void insertRecordVisitsDirection(int currentVisitsID, int directionATorSpecID, int directionVisitsTypesID, int directionVisitsID) {
    public void insertRecordVisitsDirection(int currentVisitsID, int directionVisitsTypesID, int directionVisitsID) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisitsDirections.COLUMN_BASIC_VISITS_ID, currentVisitsID);      // визит - основание
        newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE, directionVisitsTypesID);      // тип визита - направления
        //newValue.put(TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID, directionATorSpecID);  // тип исследования или специализация доктора
        newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID, directionVisitsID);  // визит - направление, выданное на currentVisitsID
        db.insert(DB_TABLE_VISITS_DIRECTIONS, null, newValue);

    }

    // uptade visits directions
    public void updateVisitsDirection(int currentVisitsID, ArrayList<ModelVisitsCures> vdArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllVisitsDirections(currentVisitsID);
        if (cursor.getCount()>0){
            int currentID;
            cursor.moveToFirst();
            do {
                currentID = cursor.getInt(cursor.getColumnIndex(TableVisitsDirections._ID));
                deleteVisitsDirection(currentID);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (vdArrayList.size()>0){
            insertVisitsDirection(currentVisitsID, vdArrayList);
        }
    }

    // delete visits directions (удалить конкретную строку строка таблицы - одно направление какого-либо визита)
    void deleteVisitsDirection(int recordsID) {
        db.delete(DB_TABLE_VISITS_DIRECTIONS, "_id = ?", new String[]{Integer.toString(recordsID)});
    }

    // возвращаем курсор со списком всех visits_directions направлений выбранного визита
    Cursor getAllVisitsDirectionsList(int visitsID) {
        return db.query(DB_TABLE_VISITS_DIRECTIONS,
                new String[] {TableVisitsDirections._ID,
                        TableVisitsDirections.COLUMN_BASIC_VISITS_ID,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE,
                        TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID,
                        TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID},
                "basic_visits_id = ?", new String[]{String.valueOf(visitsID)}, null, null, null);
    }

    // insert new record (одна строка) в visits_directions_list
    long insertRecordVisitsDirectionsList(int basicVisitsID, int directionsVisitsType, int anTypeOrSpecID, int directionsVisitsID) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisitsDirections.COLUMN_BASIC_VISITS_ID, basicVisitsID);      // визит - основание, на котором выдали направление
        newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE, directionsVisitsType);      // тип визита исследования (Constants.DIRECTIONS_ANALISIS_TYPE_1/Constants.DIRECTIONS_VISITS_TYPE_0)
        newValue.put(TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID, anTypeOrSpecID);  // тип исследования или специализация доктора
        newValue.put(TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID, directionsVisitsID);  // визит, на котором будет это исследование или консультация доктора
        return db.insert(DB_TABLE_VISITS_DIRECTIONS, null, newValue);

    }

    // insert new visits_directions_list
    public void insertVisitsDirectionsList(int basicVisitsID, ArrayList<MVisitDirectionsFragment.VisitsDirectionsModel> vdArrayList) {

        int directionsVisitsType;
        int anTypeOrSpecID;
        int directionsVisitsID;

        for (int i = 0; i < vdArrayList.size(); i++) {
            MVisitDirectionsFragment.VisitsDirectionsModel directionsModel = vdArrayList.get(i);
            directionsVisitsType = directionsModel.directionsVisitsType;
            anTypeOrSpecID = directionsModel.anTypeOrSpecID;
            directionsVisitsID = directionsModel.directionsVisitsID;

            insertRecordVisitsDirectionsList(basicVisitsID, directionsVisitsType, anTypeOrSpecID, directionsVisitsID);
        }
    }

    // uptade visits_directions_list
    void updateVisitsDirectionsList(int currentVisitsID, ArrayList<MVisitDirectionsFragment.VisitsDirectionsModel> vdArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllVisitsDirectionsList(currentVisitsID);
        if (cursor.getCount()>0){
            int currentID;
            cursor.moveToFirst();
            do {
                currentID = cursor.getInt(cursor.getColumnIndex(TableVisitsDirections._ID));
                deleteVisitsDirection(currentID);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (vdArrayList.size()>0){
            insertVisitsDirectionsList(currentVisitsID, vdArrayList);
        }
    }

    //*****************************************************************
    // работа с визитом - исследованием
    //*****************************************************************
    // возвращаем курсор со списком всех визмтов-исследований
    Cursor getAllAnalisisVisits() {
        return db.query(DB_TABLE_ANALISES_VISITS,
                new String[] {TableAnalisesVisits._ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_COMMENT,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID},
                null, null, null, null, "analisis_visits_date_time ASC");
    }

    void deleteAnalisisVisit(int chosenID) {
        db.delete(DB_TABLE_ANALISES_VISITS, "_id = ?", new String[]{String.valueOf(chosenID)});
    }

    long insertAnalisisVisit(Bundle bundle){
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME, bundle.getLong("mAnalisisVisitsMDateTime"));  // long
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID, bundle.getInt("mClinicsID")==0 ? null : bundle.getInt("mClinicsID"));  // integer
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID, bundle.getInt("mAnalisisTypesID")==0 ? null : bundle.getInt("mAnalisisTypesID"));  // integer
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_COMMENT, bundle.getString("mComment"));  // text
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID, bundle.getInt("mReferencesID") == 0 ? null : bundle.getInt("mReferencesID"));  // integer
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID, bundle.getInt("mProfilisID") == 0 ? null : bundle.getInt("mProfilisID"));  // integer

        return db.insert(DB_TABLE_ANALISES_VISITS, null, newValue);
    }

    void updateAnalisisVisit(int chosenID, Bundle bundle){
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME, bundle.getLong("mAnalisisVisitsMDateTime"));  // long
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID, bundle.getInt("mClinicsID")==0 ? null : bundle.getInt("mClinicsID"));  // integer
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID, bundle.getInt("mAnalisisTypesID")==0 ? null : bundle.getInt("mAnalisisTypesID"));  // integer
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_COMMENT, bundle.getString("mComment"));  // text
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID, bundle.getInt("mReferencesID")==0 ? null : bundle.getInt("mReferencesID"));  // integer
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID, bundle.getInt("mProfilisID")==-1 ? null : bundle.getInt("mProfilisID"));  // integer

        db.update(DB_TABLE_ANALISES_VISITS, newValue, "_id = ?", new String[]{String.valueOf(chosenID)});
    }

    // обновление в визите - исследовании только ссылки на основание
    public void updateAnalisisVisitReference(int chosenID, int referencesID){
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID, referencesID==0 ? null : referencesID);  // integer

        db.update(DB_TABLE_ANALISES_VISITS, newValue, "_id = ?", new String[]{String.valueOf(chosenID)});
    }

    // обновление в визите - исследовании только ссылки на профиль
    void updateAnalisisVisitsProfile(int choosenID, int profilisID){
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID, profilisID==0 ? null : profilisID);  // integer

        db.update(DB_TABLE_ANALISES_VISITS, newValue, "_id = ?", new String[]{String.valueOf(choosenID)});
    }

    // возвращаем поля (реквизиты) визита-исследования по его ID
    Bundle getAnalisisVisitsFildsByAnalisisVisitsID(int analisisVisitsID) {
        Cursor cursor = db.query(DB_TABLE_ANALISES_VISITS,
                new String[] {TableVisits._ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_COMMENT,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID},
                "_id = ?", new String[]{String.valueOf(analisisVisitsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            Long mAnalisisVisitsDateTime = cursor.getLong(cursor.getColumnIndex(TableAnalisesVisits.COLUMN_ANALISES_VISITS_DATE_TIME));
            bundle.putLong("mAnalisisVisitsMDateTime", mAnalisisVisitsDateTime);
            int mClinicsID = cursor.getInt(cursor.getColumnIndex(TableAnalisesVisits.COLUMN_ANALISES_VISITS_CLINIC_ID));
            bundle.putInt("mClinicsID", mClinicsID);
            int mAnalisisTypesID = cursor.getInt(cursor.getColumnIndex(TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID));
            bundle.putInt("mAnalisisTypesID", mAnalisisTypesID);
            bundle.putString("mComment", cursor.getString(cursor.getColumnIndex(TableAnalisesVisits.COLUMN_ANALISES_VISITS_COMMENT)));
            int mReferencesID = cursor.getInt(cursor.getColumnIndex(TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID));
            bundle.putInt("mReferencesID", mReferencesID);
            int mProfilesID = cursor.getInt(cursor.getColumnIndex(TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID));
            bundle.putInt("mProfilisID", mProfilesID);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // вернуть строковое представление визита-исследования:
    String getAnalisisVisitsViewByID(int visitsID){
        if (visitsID == 0)return "";
        else {
            Bundle bundle = getAnalisisVisitsFildsByAnalisisVisitsID(visitsID);
            if (bundle == null)return "";
            else {
                // дата
                Long mAnalisisVisitsDate = bundle.getLong("mAnalisisVisitsMDateTime");
                String mAnalisisVisitsDateText;
                if (mAnalisisVisitsDate == 0L) {
                    mAnalisisVisitsDateText = "";
                } else {
                    CharSequence date = DateFormat.format("dd.MM.yy", mAnalisisVisitsDate);
                    mAnalisisVisitsDateText = String.valueOf(date);
                }
                // вид исследования
                int mAnalisisTypesID = bundle.getInt("mAnalisisTypesID", 0);
                String analisisTypesText = getAnalisesTypesNameByID(mAnalisisTypesID);
                // клиника
                int mClinicsID = bundle.getInt("mClinicsID", 0);
                String clinicsText = getClinicsNameByID(mClinicsID);

                StringBuffer sb = new StringBuffer("");
                sb.append(mAnalisisVisitsDateText).append("; ").append(analisisTypesText).append("; ").append(clinicsText);

                if (DATABASE_VERSION > 3){
                    // добавим данные листа исследований
                    Cursor cursor = getAllAnalisisVisitsAnalisesList(visitsID);
                    if (cursor.getCount()>0){
                        cursor.moveToFirst();
                        do {
                            int rowID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList._ID));
                            int analisisTypesID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
                            String analisisTypesName = getAnalisesTypesNameByID(analisisTypesID);
                            sb.append("; ").append(analisisTypesName);
                        }while (cursor.moveToNext());
                    }
                }

                String analisisVisitsStringView = sb.toString();

                return analisisVisitsStringView;
            }
        }
    }

    // возвращаем строковое представление всех исследований выбранного визита-исследования
    String getAllAnalisisOfAnalisesVisit(int visitsID) {
        StringBuffer sb = new StringBuffer("");
        Cursor cursor = getAllAnalisisVisitsAnalisesList(visitsID);
        if (cursor.getCount()>0){
            cursor.moveToFirst();
            do {
                int rowID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList._ID));
                int analisisTypesID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
                String analisisTypesName = getAnalisesTypesNameByID(analisisTypesID);
                if (!(sb.toString().isEmpty())){
                    sb.append("; ");
                }
                sb.append(analisisTypesName);
            }while (cursor.moveToNext());
        }

        String analisisVisitsStringView = sb.toString();
        return analisisVisitsStringView;
    }

    //  проверка использования визита-исследования.
    // вернем истина, если визит где-нибудь используется
    String checkOfUsageAnalisisVisit(Context context, int analisisVisitsID){

        // сначала TableAnalisisVisitsAnalisesList
        final String analisis_visit_must_not_delete_type_analises_list = context.getResources().getString(R.string.analisis_visit_must_not_delete_type_analises_list);
        Cursor cursorAVL = db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST, new String[]
                        {TableAnalisisVisitsAnalisesList._ID,
                                TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID},
                "basic_analisis_visits_id = ?",
                new String[]{Integer.toString(analisisVisitsID)},
                null, null, null);

        int cursorAVLCount = cursorAVL.getCount();
        cursorAVL.close();
        if (cursorAVLCount > 0){
            return analisis_visit_must_not_delete_type_analises_list;
        }

        // предыдущей проверки вообще-то достаточно, но если мы дошли до сюда, то значит в TableAnalisisVisitsAnalisesList не нашли. Будем искать дальше:
        final String visit_must_not_delete_type_photos = context.getResources().getString(R.string.visit_must_not_delete_type_photos);
        Cursor cursorPh = db.query(DB_TABLE_VISITS_PHOTOS, new String[]
                        {TableVisitsPhotos._ID,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_ID},
                "basic_visits_type = ? and basic_visits_id = ?",
                new String[]{Integer.toString(Constants.DIRECTIONS_ANALISIS_TYPE_1), Integer.toString(analisisVisitsID)},
                null, null, null);

        int cursorPhCount = cursorPh.getCount();
        cursorPh.close();
        if (cursorPhCount > 0){
            return visit_must_not_delete_type_photos;
        }

        return "";
    }

    void deleteAnalisisVisitWithDependensesTablesFinely(int choosenAnalisisVisitsID){

        // сначала почистим таблицу TableAnalisisVisitsAnalisesList
        Cursor cursorAVL = db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST, new String[]
                        {TableAnalisisVisitsAnalisesList._ID,
                                TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID},
                "basic_analisis_visits_id = ?",
                new String[]{Integer.toString(choosenAnalisisVisitsID)},
                null, null, null);

        if (cursorAVL.getCount() > 0){
            cursorAVL.moveToFirst();
            do {
                int recordID = cursorAVL.getInt(cursorAVL.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList._ID));
                deleteAnalisisVisitsAnalisesList(recordID);
            }while (cursorAVL.moveToNext());
        }
        cursorAVL.close();

        // теперь почистим таблицу фотографий и сами фотографии
        Cursor cursorPh = db.query(DB_TABLE_VISITS_PHOTOS, new String[]
                        {TableVisitsPhotos._ID,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE,
                                TableVisitsPhotos.COLUMN_PHOTOS_TYPE,
                                TableVisitsPhotos.COLUMN_PHOTOS_URI,
                                TableVisitsPhotos.COLUMN_BASIC_VISITS_ID},
                "basic_visits_type = ? and basic_visits_id = ?",
                new String[]{Integer.toString(Constants.DIRECTIONS_ANALISIS_TYPE_1), Integer.toString(choosenAnalisisVisitsID)},
                null, null, null);
        if (cursorPh.getCount() > 0){
            cursorPh.moveToFirst();
            do {
                int recordID = cursorPh.getInt(cursorPh.getColumnIndex(DBMethods.TableVisitsPhotos._ID));
                int photosType = cursorPh.getInt(cursorPh.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_TYPE));
                String filePath = cursorPh.getString(cursorPh.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_URI));
                // сначала сам файл, но файл будем удалять только если он в приложении:
                if (photosType == Constants.PHOTOS_TYPE_APP){
                    File currentFile = new File(filePath);
                    currentFile.delete();
                }
                // затем таблицу
                deleteVisitsPhotoByID(recordID);
            }while (cursorPh.moveToNext());
        }
        cursorPh.close();

        // также нужно почистить ссылку на наш визит в визите-основании, если наш является направлением:
        Cursor cursorVD = db.query(DB_TABLE_VISITS_DIRECTIONS, new String[]
                        {TableVisitsDirections._ID,
                                TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE,
                                TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID},
                "direction_visits_type = ? and direction_visits_id = ?",
                new String[]{Integer.toString(Constants.DIRECTIONS_ANALISIS_TYPE_1), Integer.toString(choosenAnalisisVisitsID)},
                null, null, null);
        if (cursorVD.getCount() > 0){
            cursorVD.moveToFirst();
            do {
                int recordID = cursorVD.getInt(cursorVD.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                int basicVisitsID = cursorVD.getInt(cursorVD.getColumnIndex(TableVisitsDirections.COLUMN_BASIC_VISITS_ID));
                int anTypesSpecID = cursorVD.getInt(cursorVD.getColumnIndex(TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID));
                // нашли визит-основание
                updateRowVisitsDirections(recordID, basicVisitsID, Constants.DIRECTIONS_ANALISIS_TYPE_1, anTypesSpecID, 0);
            }while (cursorVD.moveToNext());
        }
        cursorVD.close();

        // окончательно удаляем сам визит
        deleteAnalisisVisit(choosenAnalisisVisitsID);

    }

    //*****************************************************************
    // TABLE_ANALISES_VISITS_ANALISES_LIST (список исследований, который берутся на визите-исследовании) - табличная часть документа визит-исследование
    //****************
    // возвращаем курсор со списком всех analises_visits_analises_list - для проверки, есть ли что-нибудь в таблице (это нужно для первоначального заполнения):
    Cursor getAllAnalisisVisitsAnalisesList() {
        return db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST,
                new String[] {TableAnalisisVisitsAnalisesList._ID}, null, null, null, null, null);
    }

    // возвращаем курсор со списком всех analises_visits_analises_list - листа исследований выбранного визита-исследования
    Cursor getAllAnalisisVisitsAnalisesList(int visitsID) {
        return db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST,
                new String[] {TableAnalisisVisitsAnalisesList._ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID},
                "basic_analisis_visits_id = ?", new String[]{String.valueOf(visitsID)}, null, null, null);
    }

    // возвращаем курсор со списком всех analises_visits_analises_list - листа исследований выбранного визита-основания
    public Cursor getAllAnalisisVisitsAnalisesListByReferenceID(int referenceID) {
        return db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST,
                new String[] {TableAnalisisVisitsAnalisesList._ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID},
                "references_visits_id = ?", new String[]{String.valueOf(referenceID)}, null, null, null);
    }

    // возвращаем поля (реквизиты) analises_visits_analises_list по его ID (по индексу). Одна строка.
    public Bundle getAnalisisVisitsAnalisesListFildsByAnalisisVisitsAnalisesListID(int recordsID) {
        Cursor cursor = db.query(DB_TABLE_ANALISES_VISITS_ANALISES_LIST,
                new String[] {TableAnalisisVisitsAnalisesList._ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID,
                        TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID},
                "_id = ?", new String[]{String.valueOf(recordsID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            int basicAnalisisVisitsID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID));
            bundle.putInt("basicAnalisisVisitsID", basicAnalisisVisitsID);
            int analisisTypesID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
            bundle.putInt("analisisVisitsTypesID", analisisTypesID);
            int referencesVisitsID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID));
            bundle.putInt("directionsVisitsID", referencesVisitsID);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // insert new запись (одна строка) в visits analises_visits_analises_list
    long insertRecordAnalisisVisitsAnalisesList(int basicAnalisisVisitsID, int analisisTypesID, int referencesVisitsID) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID, basicAnalisisVisitsID);      // визит - основание
        newValue.put(TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID, analisisTypesID);      // тип исследования
        newValue.put(TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID, referencesVisitsID);  // визит, на котором направили на исследование
        return db.insert(DB_TABLE_ANALISES_VISITS_ANALISES_LIST, null, newValue);

    }

    // insert new analises_visits_analises_list
    public void insertAnalisisVisitsAnalisesList(int basicAnalisisVisitsID, ArrayList<MAnalisisVisitAnaliseslistFragment.analisesTypesListModel> vdArrayList) {

        int analisisTypesID;
        int referencesVisitsID;
        for (int i = 0; i < vdArrayList.size(); i++) {
            MAnalisisVisitAnaliseslistFragment.analisesTypesListModel itemModel = vdArrayList.get(i);
            analisisTypesID = itemModel.analisisVisitsTypesID;
            referencesVisitsID = itemModel.referencesVisitsID;

            insertRecordAnalisisVisitsAnalisesList(basicAnalisisVisitsID, analisisTypesID, referencesVisitsID);
        }
    }

    // uptade analises_visits_analises_list
    void updateAnalisisVisitsAnalisesList(int currentAnalisisVisitsID, ArrayList<MAnalisisVisitAnaliseslistFragment.analisesTypesListModel> vdArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllAnalisisVisitsAnalisesList(currentAnalisisVisitsID);
        if (cursor.getCount()>0){
            int currentID;
            cursor.moveToFirst();
            do {
                currentID = cursor.getInt(cursor.getColumnIndex(TableAnalisisVisitsAnalisesList._ID));
                deleteAnalisisVisitsAnalisesList(currentID);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (vdArrayList.size()>0){
            insertAnalisisVisitsAnalisesList(currentAnalisisVisitsID, vdArrayList);
        }
    }

    public void updateAnalisisVisitsAnalisesList(int recordID, int basicAnalisisVisitsID, int analisisTypesID, int referencesVisitsID){
        ContentValues newValue = new ContentValues();
        newValue.put(TableAnalisisVisitsAnalisesList.COLUMN_BASIC_ANALISES_VISITS_ID, basicAnalisisVisitsID);
        newValue.put(TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID, analisisTypesID);
        newValue.put(TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID, referencesVisitsID);

        db.update(DB_TABLE_ANALISES_VISITS_ANALISES_LIST, newValue, "_id = ?", new String[]{String.valueOf(recordID)});
    }

    // delete analises_visits_analises_list (все строки одного визита-исследования)
    void deleteAnalisisVisitsAnalisesList(int choosenID) {
        db.delete(DB_TABLE_ANALISES_VISITS_ANALISES_LIST, "_id = ?", new String[]{Integer.toString(choosenID)});
    }


    //*****************************************************************
    // VISITS_PHOTOS (фоторесурсы визита)
    //****************
    // возвращаем курсор со списком всех visits_photos направлений выбранного визита
    Cursor getAllVisitsPhotos(int basicVisitsType, int basicVisitsID) {
        return db.query(DB_TABLE_VISITS_PHOTOS,
                new String[] {TableVisitsPhotos._ID,
                        TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE,
                        TableVisitsPhotos.COLUMN_BASIC_VISITS_ID,
                        TableVisitsPhotos.COLUMN_PHOTOS_TYPE, // из камеры (0) или из галереи (1)
                        TableVisitsPhotos.COLUMN_PHOTOS_NAME,
                        TableVisitsPhotos.COLUMN_PHOTOS_URI},
                "basic_visits_type = ? and basic_visits_id = ?",
                new String[]{Integer.toString(basicVisitsType), Integer.toString(basicVisitsID)},
                null, null, null);
    }

    // возвращаем курсор со списком всех visits_photos направлений выбранного визита
    Cursor getAllPhotos(int photosType) {
        return db.query(DB_TABLE_VISITS_PHOTOS,
                new String[] {TableVisitsPhotos._ID,
                        TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE,
                        TableVisitsPhotos.COLUMN_BASIC_VISITS_ID,
                        TableVisitsPhotos.COLUMN_PHOTOS_TYPE, // из камеры (0) или из галереи (1)
                        TableVisitsPhotos.COLUMN_PHOTOS_NAME,
                        TableVisitsPhotos.COLUMN_PHOTOS_URI},
/*
                "photos_type = ?",
                new String[]{Integer.toString(photosType)},
*/
                null, null,
                null, null, null);
    }

    // возвращаем поля (реквизиты) visits_photos по его ID (по индексу). Одна строка (текущий визит и фоторесурс, прикрепленный к этому визиту).
    public Bundle getVisitsPhotosFildsByVisitsPhotosID(int visitsPhotosID) {
        Cursor cursor = db.query(DB_TABLE_VISITS_PHOTOS,
                new String[] {TableVisitsPhotos._ID,
                        TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE,
                        TableVisitsPhotos.COLUMN_BASIC_VISITS_ID,
                        TableVisitsPhotos.COLUMN_PHOTOS_TYPE,
                        TableVisitsPhotos.COLUMN_PHOTOS_NAME,
                        TableVisitsPhotos.COLUMN_PHOTOS_URI},
                "_id = ?", new String[]{String.valueOf(visitsPhotosID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            int basicVisitsType = cursor.getInt(cursor.getColumnIndex(TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE));
            bundle.putInt("basicVisitsType", basicVisitsType);
            int basicVisitsID = cursor.getInt(cursor.getColumnIndex(TableVisitsPhotos.COLUMN_BASIC_VISITS_ID));
            bundle.putInt("basicVisitsID", basicVisitsID);
            int visitsPhotosType = cursor.getInt(cursor.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_TYPE));
            bundle.putInt("visitsPhotosType", visitsPhotosType);
            String visitsPhotosName = cursor.getString(cursor.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_NAME));
            bundle.putString("visitsPhotosName", visitsPhotosName);
            String visitsPhotosURI = cursor.getString(cursor.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_URI));
            bundle.putString("visitsPhotosURI", visitsPhotosURI);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // insert new visits_photos сохранение списка фотографий (устаревший метод)
    public void insertVisitsPhotos(int basicVisitsType, int basicVisitsID, ArrayList<ModelVisitsCures> vphArrayList) {
        String visitsPhotosType;
        String visitsPhotosName;
        String visitsPhotosURI;
        for (int i = 0; i < vphArrayList.size(); i++) {
            HashMap itemMap = vphArrayList.get(i);
            visitsPhotosType = String.valueOf(itemMap.get("visitsPhotosType"));
            visitsPhotosName = String.valueOf(itemMap.get("visitsPhotosName"));
            visitsPhotosURI = String.valueOf(itemMap.get("visitsPhotosURI"));

            ContentValues newValue = new ContentValues();
            newValue.put(TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE, basicVisitsType);
            newValue.put(TableVisitsPhotos.COLUMN_BASIC_VISITS_ID, basicVisitsID);
            newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_TYPE, visitsPhotosType);
            newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_NAME, visitsPhotosName);
            newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_URI, visitsPhotosURI);
            db.insert(DB_TABLE_VISITS_PHOTOS, null, newValue);
        }
    }

    // insert new visits_photos не списком
    long insertVisitsPhotosNew(Bundle bundle) {

        int basicVisitsType = bundle.getInt("basicVisitsType");
        int basicVisitsID = bundle.getInt("basicVisitsID");
        int visitsPhotosType = bundle.getInt("visitsPhotosType");
        String visitsPhotosName = bundle.getString("visitsPhotosName");
        String visitsPhotosURI = bundle.getString("visitsPhotosURI");

        ContentValues newValue = new ContentValues();
        newValue.put(TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE, basicVisitsType);
        newValue.put(TableVisitsPhotos.COLUMN_BASIC_VISITS_ID, basicVisitsID);
        newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_TYPE, visitsPhotosType); // из камеры (0) или из галереи (1)
        newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_NAME, visitsPhotosName);
        newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_URI, visitsPhotosURI);

        return db.insert(DB_TABLE_VISITS_PHOTOS, null, newValue);
    }


    // insert new запись в visits_photos
    void insertRecordVisitsPhotos(int basicVisitsType, int basicVisitsID, int visitsPhotosType, String visitsPhotosName, String visitsPhotosURI) {
        ContentValues newValue = new ContentValues();
        newValue.put(TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE, basicVisitsType);      // визит - основание
        newValue.put(TableVisitsPhotos.COLUMN_BASIC_VISITS_ID, basicVisitsID);      // визит - основание
        newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_TYPE, visitsPhotosType);      // тип фоторесурса визита из камеры (0) или из галереи (1)
        newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_NAME, visitsPhotosName);      // имя фоторесурса визита
        newValue.put(TableVisitsPhotos.COLUMN_PHOTOS_URI, visitsPhotosURI);      // путь к фоторесурсу
        db.insert(DB_TABLE_VISITS_PHOTOS, null, newValue);

    }

    // uptade vvisits_photos
    public void updateVisitsPhotos(int basicVisitsType, int basicVisitsID, ArrayList<ModelVisitsCures> vphArrayList) {
        // удалим старые и вставим заново
        Cursor cursor = getAllVisitsPhotos(basicVisitsType, basicVisitsID);
        if (cursor.getCount()>0){
            int currentID;
            String currentURI;
            cursor.moveToFirst();
            do {
                currentID = cursor.getInt(cursor.getColumnIndex(TableVisitsPhotos._ID));
                currentURI = cursor.getString(cursor.getColumnIndex(TableVisitsPhotos.COLUMN_PHOTOS_URI));
                deleteVisitsPhotoByID(currentID);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (vphArrayList.size()>0){
            insertVisitsPhotos(basicVisitsType, basicVisitsID, vphArrayList);
        }
    }

    // delete visits_photos все фотки выбранного визита
    public void deleteVisitsPhotos(int basicVisitsType, int basicVisitsID) {
        db.delete(DB_TABLE_VISITS_PHOTOS, "basic_visits_type = ? and basic_visits_id = ?", new String[]{Integer.toString(basicVisitsType), Integer.toString(basicVisitsID)});
    }

    // delete visits_photo - одну фотографию по id в таблице DB_TABLE_VISITS_PHOTOS
    void deleteVisitsPhotoByID(int recordsID) {
        db.delete(DB_TABLE_VISITS_PHOTOS, "_id = ?", new String[]{Integer.toString(recordsID)});
    }

    //*****************************************************************
    // filters
    //*****************************************************************
    // возвращаем курсор со списком всех фильтров
    Cursor getAllFilters() {
        return db.query(DB_TABLE_FILTERS,
                new String[] {TableFilters._ID,
                        TableFilters.COLUMN_FILTERS_NAME,
                        TableFilters.COLUMN_FILTERS_ONLY_WHITH_DATE,
                        TableFilters.COLUMN_FILTERS_FIRST_DATE,
                        TableFilters.COLUMN_FILTERS_LAST_DATE,
                        TableFilters.COLUMN_FILTERS_CLINICS_ID,
                        TableFilters.COLUMN_FILTERS_DOCTORS_ID,
                        TableFilters.COLUMN_FILTERS_SPECIALIZATIONS_ID,
                        TableFilters.COLUMN_FILTERS_CURES_ID,
                        TableFilters.COLUMN_FILTERS_DIAGNOSIS_ID,
                        TableFilters.COLUMN_FILTERS_ANALISES_TYPES_ID,
                        TableFilters.COLUMN_FILTERS_RECOMMENDATIONS_ID},
                null, null, null, null, null);
                //null, null, null, null, "name ASC");
    }

    // возвращаем поля (реквизиты) фильтра по его ID
    Bundle getFiltersFildsByFiltersID(int filtersID) {
        Cursor cursor = db.query(DB_TABLE_FILTERS,
                new String[] {TableFilters._ID,
                        TableFilters.COLUMN_FILTERS_NAME,
                        TableFilters.COLUMN_FILTERS_ONLY_WHITH_DATE,
                        TableFilters.COLUMN_FILTERS_FIRST_DATE,
                        TableFilters.COLUMN_FILTERS_LAST_DATE,
                        TableFilters.COLUMN_FILTERS_CLINICS_ID,
                        TableFilters.COLUMN_FILTERS_DOCTORS_ID,
                        TableFilters.COLUMN_FILTERS_SPECIALIZATIONS_ID,
                        TableFilters.COLUMN_FILTERS_CURES_ID,
                        TableFilters.COLUMN_FILTERS_DIAGNOSIS_ID,
                        TableFilters.COLUMN_FILTERS_ANALISES_TYPES_ID,
                        TableFilters.COLUMN_FILTERS_RECOMMENDATIONS_ID},
                "_id = ?", new String[]{String.valueOf(filtersID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            bundle.putString("filterName", cursor.getString(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_NAME)));
            int onlyWithDate = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_ONLY_WHITH_DATE));
            bundle.putBoolean("onlyWithDate", (onlyWithDate == 1));
            Long firstDate = cursor.getLong(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_FIRST_DATE));
            bundle.putLong("firstDate", firstDate);
            Long lastDate = cursor.getLong(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_LAST_DATE));
            bundle.putLong("lastDate", lastDate);
            int clinicsID = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_CLINICS_ID));
            bundle.putInt("clinicsID", clinicsID);
            int doctorsID = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_DOCTORS_ID));
            bundle.putInt("doctorsID", doctorsID);
            int specializationsID = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_SPECIALIZATIONS_ID));
            bundle.putInt("specializationsID", specializationsID);
            int mCuresID = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_CURES_ID));
            bundle.putInt("curesID", mCuresID);
            int mDiagnosisID = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_DIAGNOSIS_ID));
            bundle.putInt("diagnosisID", mDiagnosisID);
            int mAnalisesTypesID = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_ANALISES_TYPES_ID));
            bundle.putInt("analisesTypesID", mAnalisesTypesID);
            int mRecommendationsID = cursor.getInt(cursor.getColumnIndex(TableFilters.COLUMN_FILTERS_RECOMMENDATIONS_ID));
            bundle.putInt("recommendationsID", mRecommendationsID);

            cursor.close();

            return bundle;
        }else {
            cursor.close();
            return null;
        }
    }

    // возвращаем ID filter
    int getIDFiltersByName(String filterName) {
        int filterID;
        Cursor cursor = db.query(DB_TABLE_FILTERS, new String[] {TableFilters._ID},
                "name = ?", new String[]{filterName}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            filterID = cursor.getInt(cursor.getColumnIndex(TableFilters._ID));
        }else {filterID = 0;}
        cursor.close();

        return filterID;
    }

    int insertFilter(Bundle bundle){
        ContentValues newValue = new ContentValues();
        newValue.put(TableFilters.COLUMN_FILTERS_NAME, bundle.getString("filterName"));  // text
        newValue.put(TableFilters.COLUMN_FILTERS_ONLY_WHITH_DATE, bundle.getBoolean("onlyWithDate") ? 1 : 0);  // integer (boolen) 0 - 1
        newValue.put(TableFilters.COLUMN_FILTERS_FIRST_DATE, bundle.getLong("firstDate"));  // long
        newValue.put(TableFilters.COLUMN_FILTERS_LAST_DATE, bundle.getLong("lastDate"));  // long
        newValue.put(TableFilters.COLUMN_FILTERS_CLINICS_ID, bundle.getInt("clinicsID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_DOCTORS_ID, bundle.getInt("doctorsID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_SPECIALIZATIONS_ID, bundle.getInt("specializationsID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_CURES_ID, bundle.getInt("curesID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_DIAGNOSIS_ID, bundle.getInt("diagnosisID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_ANALISES_TYPES_ID, bundle.getInt("analisesTypesID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_RECOMMENDATIONS_ID, bundle.getInt("recommendationsID"));  // integer

        return (int) db.insert(DB_TABLE_FILTERS, null, newValue);
    }

    void updateFilter(int chosenID, Bundle bundle){
        ContentValues newValue = new ContentValues();
        newValue.put(TableFilters.COLUMN_FILTERS_NAME, bundle.getString("filterName"));  // text
        newValue.put(TableFilters.COLUMN_FILTERS_ONLY_WHITH_DATE, bundle.getBoolean("onlyWithDate") ? 1 : 0);  // integer (boolen) 0 - 1
        newValue.put(TableFilters.COLUMN_FILTERS_FIRST_DATE, bundle.getLong("firstDate"));  // long
        newValue.put(TableFilters.COLUMN_FILTERS_LAST_DATE, bundle.getLong("lastDate"));  // long
        newValue.put(TableFilters.COLUMN_FILTERS_CLINICS_ID, bundle.getInt("clinicsID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_DOCTORS_ID, bundle.getInt("doctorsID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_SPECIALIZATIONS_ID, bundle.getInt("specializationsID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_CURES_ID, bundle.getInt("curesID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_DIAGNOSIS_ID, bundle.getInt("diagnosisID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_ANALISES_TYPES_ID, bundle.getInt("analisesTypesID"));  // integer
        newValue.put(TableFilters.COLUMN_FILTERS_RECOMMENDATIONS_ID, bundle.getInt("recommendationsID"));  // integer

        db.update(DB_TABLE_FILTERS, newValue, "_id = ?", new String[]{String.valueOf(chosenID)});
    }

    void deleteFilter(int chosenID) {
        db.delete(DB_TABLE_FILTERS, "_id = ?", new String[]{String.valueOf(chosenID)});
    }

    //*****************************************************************
    // PROFILES
    //****************
    // возвращаем курсор со списком всех профилей
    Cursor getAllProfiles() {
        return db.query(DB_TABLE_PROFILES,
                new String[] {TableProfiles._ID,
                        TableProfiles.COLUMN_PROFILES_NAME,
                        TableProfiles.COLUMN_PROFILES_SEX,
                        TableProfiles.COLUMN_PROFILES_BIRTHDAY,
                        TableProfiles.COLUMN_PROFILES_COMMENT},
                null, null, null, null, "name ASC");
    }

    // возвращаем курсор со списком всех визитов выбранного профиля
    Cursor getAllVisitsByProfilesID(int profilesID) {
        return db.query(DB_TABLE_VISITS,
                new String[] {TableVisits._ID,
                        TableVisits.COLUMN_VISITS_PROFILE_ID},
                TableVisits.COLUMN_VISITS_PROFILE_ID + " = ?", new String[]{String.valueOf(profilesID)}, null, null, null);
    }

    // возвращаем курсор со списком всех визитов-исследований выбранного профиля
    Cursor getAllAnalisesVisitsByProfilesID(int profilesID) {
        return db.query(DB_TABLE_ANALISES_VISITS,
                new String[] {TableAnalisesVisits._ID,
                        TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID},
                TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID + " = ?", new String[]{String.valueOf(profilesID)}, null, null, null);
    }

    // возвращаем ID первого профиля
    int getFirstProfilesID() {
        int firstProfilesID;
        Cursor cursor = db.query(DB_TABLE_PROFILES, new String[] {TableProfiles._ID},
                null, null, null, null, "_id ASC");

        cursor.moveToFirst();
        firstProfilesID = cursor.getInt(cursor.getColumnIndex(TableProfiles._ID));

        cursor.close();

        return firstProfilesID;
    }

    // возвращаем поля (реквизиты) профиля по его ID (по индексу).
    Bundle getProfilesFildsByProfilesID(int profilesID) {
        Cursor cursor = db.query(DB_TABLE_PROFILES,
                new String[] {TableProfiles._ID,
                        TableProfiles.COLUMN_PROFILES_NAME,
                        TableProfiles.COLUMN_PROFILES_SEX,
                        TableProfiles.COLUMN_PROFILES_BIRTHDAY,
                        TableProfiles.COLUMN_PROFILES_COMMENT},
                "_id = ?", new String[]{String.valueOf(profilesID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            Bundle bundle = new Bundle();
            String profilesName = cursor.getString(cursor.getColumnIndex(TableProfiles.COLUMN_PROFILES_NAME));
            bundle.putString("profilesName", profilesName);
            String profilesSex = cursor.getString(cursor.getColumnIndex(TableProfiles.COLUMN_PROFILES_SEX));
            bundle.putString("profilesSex", profilesSex);
            Long profilesBirthday = cursor.getLong(cursor.getColumnIndex(TableProfiles.COLUMN_PROFILES_BIRTHDAY));
            bundle.putLong("profilesBirthday", profilesBirthday);
            String profilesComment = cursor.getString(cursor.getColumnIndex(TableProfiles.COLUMN_PROFILES_COMMENT));
            bundle.putString("profilesComment", profilesComment);

            cursor.close();

            return bundle;
        }else {
            return null;
        }
    }

    // возвращаем имя профиля по его ID
    public String getProfilesNameByID(int profilesID) {
        String profilesName;
        Cursor cursor = db.query(DB_TABLE_PROFILES, new String[] {TableProfiles._ID, TableProfiles.COLUMN_PROFILES_NAME},
                "_id = ?", new String[]{String.valueOf(profilesID)}, null, null, null);

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            profilesName = cursor.getString(cursor.getColumnIndex(TableProfiles.COLUMN_PROFILES_NAME));
        }else {
            profilesName = "";
        }
        cursor.close();
        return profilesName;
    }

    // возвращаем ID профиля
    public int getIDProfilesByName(String profilesName) {
        int profilesID;
        Cursor cursor = db.query(DB_TABLE_PROFILES, new String[] {TableProfiles._ID},
                "name = ?", new String[]{profilesName}, null, null, null);

        if (cursor.getCount()>0){
            cursor.moveToFirst();
            profilesID = cursor.getInt(cursor.getColumnIndex(TableProfiles._ID));
        }else profilesID = 0;
        cursor.close();

        return profilesID;
    }

    // insert профиль
    int insertProfiles(Bundle bundle) {
        String profilesName = bundle.getString("profilesName");
        String profilesSex = bundle.getString("profilesSex");
        Long profilesBirthday = bundle.getLong("profilesBirthday");
        String profilesComment = bundle.getString("profilesComment");

        ContentValues newValue = new ContentValues();
        newValue.put(TableProfiles.COLUMN_PROFILES_NAME, profilesName);
        newValue.put(TableProfiles.COLUMN_PROFILES_SEX, profilesSex);
        newValue.put(TableProfiles.COLUMN_PROFILES_BIRTHDAY, profilesBirthday);
        newValue.put(TableProfiles.COLUMN_PROFILES_COMMENT, profilesComment);
        long curProfileID = db.insert(DB_TABLE_PROFILES, null, newValue);

        return (int)curProfileID;
    }

    // uptade профиль
    void updateProfiles(int choosenID, Bundle bundle) {
        String profilesName = bundle.getString("profilesName");
        String profilesSex = bundle.getString("profilesSex");
        Long profilesBirthday = bundle.getLong("profilesBirthday");
        String profilesComment = bundle.getString("profilesComment");

        ContentValues newValue = new ContentValues();
        newValue.put(TableProfiles.COLUMN_PROFILES_NAME, profilesName);
        newValue.put(TableProfiles.COLUMN_PROFILES_SEX, profilesSex);
        newValue.put(TableProfiles.COLUMN_PROFILES_BIRTHDAY, profilesBirthday);
        newValue.put(TableProfiles.COLUMN_PROFILES_COMMENT, profilesComment);
        db.update(DB_TABLE_PROFILES, newValue, "_id = ?", new String[]{Integer.toString(choosenID)});
    }

    // delete профиль
    void deleteProfiles(int choosenRecordID) {
        db.delete(DB_TABLE_PROFILES, "_id = ?", new String[]{String.valueOf(choosenRecordID)});
    }




    private void updateDB(SQLiteDatabase db, int updateNumber){

        switch (updateNumber){
            case 1:

                db.execSQL(CREATE_DB_TABLE_CLINIKS);
                db.execSQL(CREATE_DB_TABLE_CURES);
                db.execSQL(CREATE_DB_TABLE_RECOMMENDATIONS);
                db.execSQL(CREATE_DB_TABLE_SPECIALIZATIONS);
                db.execSQL(CREATE_DB_TABLE_DOCTORS);
                db.execSQL(CREATE_DB_TABLE_VISITS);
                db.execSQL(CREATE_DB_TABLE_DIAGNOSES);
                db.execSQL(CREATE_DB_TABLE_ANALISES_TYPES);
                db.execSQL(CREATE_DB_TABLE_VISITS_CURES);
                db.execSQL(CREATE_DB_TABLE_VISITS_RECOMMENDATIONS);
                db.execSQL(CREATE_DB_TABLE_VISITS_DIRECTIONS);
                db.execSQL(CREATE_DB_TABLE_VISITS_PHOTOS);
                db.execSQL(CREATE_DB_TABLE_ANALISES_VISITS);


                // Первоначальное заполнение.
                // Добавляем записи в таблицу "Специализации докторов"
                ContentValues values = new ContentValues();
                // Получим массив строк из ресурсов
                Resources res = fContext.getResources();
                String[] specializations_records = res.getStringArray(R.array.specializations_init);
                // проходим через массив и вставляем записи в таблицу
                int length = specializations_records.length;
                for (String specializations_record : specializations_records) {
                    values.put(TableSpecializations.COLUMN_SPECIALIZATIONS_NAME, specializations_record);
                    db.insert(DB_TABLE_SPECIALIZATIONS, null, values);
                }
                // Добавляем записи в таблицу "Назначения"
                ContentValues valuesCu = new ContentValues();
                // Получим массив строк из ресурсов
                Resources resCu = fContext.getResources();
                String[] specializations_recordsCu = resCu.getStringArray(R.array.cures_init);
                // проходим через массив и вставляем записи в таблицу
                int lengthCu = specializations_recordsCu.length;
                for (String aSpecializations_recordsCu : specializations_recordsCu) {
                    valuesCu.put(TableCures.COLUMN_CURES_NAME, aSpecializations_recordsCu);
                    db.insert(DB_TABLE_CURES, null, valuesCu);
                }
                // Добавляем записи в таблицу "Диагнозы"
                ContentValues valuesDi = new ContentValues();
                // Получим массив строк из ресурсов
                Resources resDi = fContext.getResources();
                String[] diagnoses_recordsDi = resDi.getStringArray(R.array.diagnoses_init);
                // проходим через массив и вставляем записи в таблицу
                int lengthDi = diagnoses_recordsDi.length;
                for (int i = 0; i < lengthDi; i++) {
                    valuesDi.put(TableDiagnoses.COLUMN_DIAGNOSES_NAME, diagnoses_recordsDi[i]);
                    db.insert(DB_TABLE_DIAGNOSES, null, valuesDi);
                }
                // Добавляем записи в таблицу "Исследования"
                ContentValues valuesAnT = new ContentValues();
                // Получим массив строк из ресурсов
                Resources resAnT = fContext.getResources();
                String[] diagnoses_recordsAnT = resAnT.getStringArray(R.array.analises_types_init);
                // проходим через массив и вставляем записи в таблицу
                int lengthAnT = diagnoses_recordsAnT.length;
                for (int i = 0; i < lengthAnT; i++) {
                    valuesAnT.put(TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME, diagnoses_recordsAnT[i]);
                    db.insert(DB_TABLE_ANALISES_TYPES, null, valuesAnT);
                }
                // Добавляем записи в таблицу "Рекомендации"
                ContentValues valuesRecT = new ContentValues();
                // Получим массив строк из ресурсов
                Resources resRecT = fContext.getResources();
                String[] recommendations_recordsRecT = resRecT.getStringArray(R.array.recommendations_init);
                // проходим через массив и вставляем записи в таблицу
                int lengthRecT = recommendations_recordsRecT.length;
                for (int i = 0; i < lengthRecT; i++) {
                    valuesRecT.put(TableRecommendations.COLUMN_RECOMMENDATIONS_NAME, recommendations_recordsRecT[i]);
                    db.insert(DB_TABLE_RECOMMENDATIONS, null, valuesRecT);
                }

                break;

            case 2:
                // добавление новой таблицы "Фильтры"
                db.execSQL(CREATE_DB_TABLE_FILTERS);

                break;

            case 3:

                //Код для добавления новой таблицы "Профили" и поля "Профиль" для таблиц визитов:
                db.execSQL(CREATE_DB_TABLE_PROFILES);

                // теперь добавим поля, указывающие на профиль в таблицах визитов:
                db.execSQL("alter table " + DB_TABLE_VISITS + " add column " + TableVisits.COLUMN_VISITS_PROFILE_ID + " integer;");
                db.execSQL("alter table " + DB_TABLE_ANALISES_VISITS + " add column " + TableAnalisesVisits.COLUMN_ANALISES_VISITS_PROFILE_ID + " integer;");
                // заполнение первой строки таблицы профилем по умолчанию произведем в onCreate()  z_old_MainActivity1, т.к. в одной транзакции не получилось (видимо, создание таблицы еще не зафиксировалось)

                break;

            case 4:
                // добавление новой таблицы "Список исследований" для визита-исследования
                db.execSQL(CREATE_DB_TABLE_ANALISES_VISITS_ANALISES_LIST);
                // заполнение таблицы произведем в onCreate()  z_old_MainActivity1, т.к. в одной транзакции не получилось (видимо, создание таблицы еще не зафиксировалось)

                // также добавим поле в таблицу направлений:
                db.execSQL("alter table " + DB_TABLE_VISITS_DIRECTIONS + " add column " + TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID + " integer;");
                // заполнение таблицы также произведем в onCreate()  z_old_MainActivity1

                // и еще добавим поле в таблицу назначений:
                //db.execSQL("alter table " + DB_TABLE_VISITS_CURES + " add column " + TableVisitsCures.COLUMN_PROCEDURES_VISITS_ID + " integer;");
                // заполнять не будем готовим поле для новой таблицы

                break;

        }

    }

    private class AsyncUpdateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //int myProgress = 0;
            // [... Выполните задачу в фоновом режиме, обновите переменную myProgress...]
            //publishProgress(myProgress);
            // [... Продолжение выполнения фоновой задачи ...]


            // создадим первый профиль и заполним ссылкой на него нужные таблицы
            Bundle bundle = new Bundle();
            bundle.putString("profilesName", "My profile");
            bundle.putString("profilesSex", "");
            bundle.putLong("profilesBirthday", 315578470046L);
            bundle.putString("profilesComment", "");
            insertProfiles(bundle);



            // Верните значение, ранее переданное в метод onPostExecute
            //int result = 0;

            return null;
        }
    }


}
