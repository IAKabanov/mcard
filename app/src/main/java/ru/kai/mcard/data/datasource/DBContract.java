package ru.kai.mcard.data.datasource;

import android.provider.BaseColumns;

/**
 * Created by akabanov on 09.01.2018.
 */

public final class DBContract {

    DBContract(){}

    static final String DB_NAME = "mCard.db";
    static final String DB_TABLE_CLINICS = "clinics";
    static final String DB_TABLE_CURES = "cures";  // назначения (препараты, уколы, процедуры)
    static final String DB_TABLE_RECOMMENDATIONS = "recommendations";  // рекомендации по режиму
    static final String DB_TABLE_SPECIALIZATIONS = "specializations"; // специализации докторов
    static final String DB_TABLE_DOCTORS = "doctors";  // доктора - реальные люди
    static final String DB_TABLE_VISITS = "visits";
    static final String DB_TABLE_DIAGNOSES = "diagnoses";
    static final String DB_TABLE_ANALISES_TYPES = "analises_types";  // виды исследований (анализы, УЗИ, рентгенография и т.д.)
    static final String DB_TABLE_VISITS_CURES = "visits_cures";
    static final String DB_TABLE_VISITS_RECOMMENDATIONS = "visits_recommendations";
    static final String DB_TABLE_VISITS_DIRECTIONS = "visits_directions";
    static final String DB_TABLE_VISITS_PHOTOS = "visits_photos";
    static final String DB_TABLE_ANALISES_VISITS = "analises_visits";
    static final String DB_TABLE_ANALISES_VISITS_ANALISES_LIST = "analises_visits_analises_list"; // таблица анализов (исследований) для одного визита-исследования  - табличная часть документа визит-исследование
    static final String DB_TABLE_FILTERS = "filters";
    static final String DB_TABLE_PROFILES = "profiles";

    // поля таблицы "clinics"
    class TableClinics implements BaseColumns {
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


}
