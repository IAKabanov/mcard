package ru.kai.mcard;

public class Constants {
    public static final boolean DEBUG_MODE = true; // перед production-ом за false-ить

    public static final int DIRECTIONS_VISITS_TYPE_0 = 0;  // направление на консультацию
    public static final int DIRECTIONS_ANALISIS_TYPE_1 = 1;  // направление на исследование

    // db
    public static final String DB_NAME = "mCard.db";
    public static final int DATABASE_VERSION = 4;

    // main
    static final int VISIT_DATA = 2;
    static final int ANALISIS_VISIT_DATA = 3;
    static final int FILTER_CHOICE = 4;  // для запуска активности выбора фильтра


    // константы для открытия активностей
    static final int SPECIALIZATION_DATA = 11;       // "Специализация"
    static final int VISIT_DIRECTIONS_DATA = 12;     // "Визит (направления)"
    static final int VISIT_CURES_DATA = 13;          // "Визит (назначения)"
    static final int VISIT_ADDITIONALLY_DATA = 14;   // "Визит (дополнительно)"
    static final int VISIT_CLINICS_DATA = 15;        // "Клиники"
    static final int VISIT_DOCTORS_DATA = 16;        // "Доктора(ФИО)"
    static final int VISIT_SPECIALIZATIONS_DATA = 17;// "Специализации докторов"
    static final int CLINIC_DATA = 18;               // "Клиники"
    static final int CURES_DATA = 19;               // "назначения"



    static final int VISITS_CURE_DATA = 41;    // константа для открытия активности "назначения по визиту"
    static final int MAIN_ACTIVITY_FOR_REFERENCE = 42;   // открытие z_old_MainActivity1 для выбора reference
    static final int MAIN_ACTIVITY_FOR_DIRECTIONS = 43;   // открытие z_old_MainActivity1 для выбора directions
    static final int VISIT_DIRECTIONS_FOR_UPDATE = 44;   // открытие z_old_VisitActivity1 для выбора update
    static final int VISITS_DIAGNOSES_DATA = 45;   // константа для открытия активности "диагнозов"
    static final int VISIT_DIRECTIONS_ANALISIS_FOR_UPDATE = 46;   // открытие z_old_AnalisisVisitActivity1 для выбора update
    static final int NEW_DIRECTIONS_VISIT = 47;   // вмзмт открыт как вновь созданное направление. Это нужно для того, чтобы при сохранении такого визита в основании проставить на него ссылку
    static final int NEW_DIRECTIONS_ANALISIS_VISIT = 48;   // вмзмт-исследование открыт как вновь созданное направление. Это нужно для того, чтобы при сохранении такого визита в основании проставить на него ссылку
    static final int REFERENCE_BASIC_VISIT = 49; // открытие визита-основания (вдруг что-то в нем изменили...)

    // константы для открытия активностей
    static final int ANALISES_TYPES_DATA = 51; // константа для открытия активности "Вид исследования (редактирование)"
    static final int ANALISIS_VISIT_CLINICS_DATA = 61;    // "Клиники"
    static final int ANALISIS_VISIT_ANALISES_TYPES_DATA = 62;    // "Виды исследований"
    static final int ANALISIS_VISIT_MAIN_ACTIVITY_FOR_REFERENCE = 63;    // Выбор визита-основания
    static final int DIAGNOSIS_DATA = 71; // константа для открытия активности "Диагноз"
    static final int RECOMMENDATIONS_DATA = 81;
    static final int RECOMMENDATION_DATA = 82;
    static final int PRIVACY_POLICY_ACCEPT = 83;


    static final int PHOTO_ADD_DATA = 91;
    static final int CAMERA_RESULT = 92;
    static final int GALLERY_REQUEST = 93;
    static final int PHOTO_LIST_OPEN = 94;
    static final int PHOTO_VIEW_OPEN = 95;

    // для определения: мы хотим добавить фото или изменить его
    static final int PHOTO_ADD = 100;
    static final int PHOTO_CHANGE = 101;

    // для определения: из камеры фотография или из галереи
    static final int PHOTO_CAMERA = 110;    // раньше так
    static final int PHOTO_GALLERY = 111;

    public static final int PHOTOS_TYPE_APP = 0;       // из камеры  сейчас так
    static final int PHOTOS_TYPE_GALARY = 1;    // из галереи


    static final int FILTER_LIST_DATA = 120;  // константа для открытия активности "Список фильтров"
    static final int FILTER_EXT_DATA = 121;  // константа для открытия активности "Фильтр расширенный"

    // сохранение настроек
    public static final String APP_PREFERENCES = "mcard_settings";  // это будет именем файла настроек
    static final String APP_PREFERENCES_FILTER_OFF = "filter_off";
    static final String APP_PREFERENCES_CURRENT_FILTER_ID = "current_filter_id";
    public static final String APP_PREFERENCES_CURRENT_THEME = "current_theme";
    public static final String APP_PREFERENCES_CURRENT_THEME_S = "current_theme_s";
    static final String APP_PREFERENCES_CURRENT_PROFILE_ID = "current_profile_id";
    static final String APP_PREFERENCES_MUST_CHECK_PROFILE = "must_check_profile";  // purchases
    static final String APP_PREFERENCES_SHOW_VISITS_DIRECTIONS_HELP = "show_visits_directions_help";
    static final String APP_PREFERENCES_SHOW_VISITS_CURES_HELP = "show_visits_cures_help";
    static final String APP_PREFERENCES_SHOW_DOCTORS_HELP = "show_doctors_help";
    static final String APP_PREFERENCES_SHOW_CLINICS_HELP_B = "show_clinics_help_b";
    static final String APP_PREFERENCES_SHOW_DRAWER = "show_drawer";
    static final String APP_PREFERENCES_SHOW_FILTERS_HELP = "show_filters_help";
    static final String APP_PREFERENCES_SHOW_PROFILES_HELP = "show_profiles_help";
    static final String APP_PREFERENCES_SHOW_BACKUP_SD_CARD_BACKUP_HELP = "show_backup_sd_card_backup_help";
    static final String APP_PREFERENCES_SHOW_BACKUP_SD_CARD_RESTORE_HELP = "show_backup_sd_card_restore_help";

    static final String APP_PREFERENCES_SHOW_PRIVACY_POLICY = "show_privacy_policy";

    static final String APP_PREFERENCES_THEME_NEW_YEAR = "theme_new_year";
    static final String APP_PREFERENCES_THEME_GREEN_PEAS = "theme_green_peas";

    static final String APP_PREFERENCES_VISIT_ORDER_DOWN = "visit_order_down";  // сортировка списка визитов в main списке

    // themes
    static final int THEMES_CHOICE = 130;
    public static final String THEME_LITERA_G = "G"; // green
    public static final String THEME_LITERA_L = "L"; // lemon
    public static final String THEME_LITERA_P = "P"; // pink
    public static final String THEME_LITERA_C = "C"; // cornflower

    // profiles
    static final int EDIT_PROFILES = 140;
    static final int ADD_OR_EDIT_PROFILE = 141;


    static final int ABOUT = 142;

    static final String DEBUG_TAG = "ru.kai.mcard";

    static final int PROGRESS_DLG_ID = 150;
    static final int CHOOSE_DIR = 151;

    // backups
    static final String LAST_BACKUPS_BTN_PRESS = "last_backup_btn_press";
    static final String LAST_BACKUPS_IN_CLOUD_DONE = "last_backup_in_cloud";
    static final String LAST_BACKUPS_IN_CLOUD_YES_NO = "last_backup_in_cloud_yes_no";
    static final String LAST_BACKUPS_IN_CLOUD_LAST_DATE_TIME = "last_backup_in_cloud_last_date_time";
    static final String LAST_BACKUPS_OLD_STATE = "last_backup_old_state";
    static final String LAST_BACKUPS_NEW_STATE_BEFORE = "last_backup_new_state_before";
    static final String LAST_BACKUPS_NEW_STATE_AFTER = "last_backup_new_state_after";
    static final String LAST_BACKUPS_NEW_STATE_RESTORE = "last_backup_new_state_restore";
    static final String LAST_BACKUPS_ON_RESTORE_DONE = "last_on_restore_done";

    static final String BACKUPS_PATH_ON_SD_CARD = "backups_path_on_sd_card";
    static final String RESTORE_PATH_FROM_SD_CARD = "restore_path_from_sd_card";

    // models
    public static final int CREATE_NEW_MODEL = -1; // будет так: если список не пустой, то в detail будет отображаться первый item, иначе - новая
    public static final int MODEL_IS_NULL = -2; // модели просто нет. Для отображения "Выберите элемент"

    // повороты. Если мы поворачиваем device из портрета в ландшафт, то нужно зафиксировать TAG фрагмента,
    // из которого был поворот, чтобы при обратном повороте вернуться на экран именно с этим фрагментом.
    // Так как активность при повороте пересоздается, то сохранять имя активости и состояние фрагментов будем в SharedPreferences.
    public static final String TAGActivityName = "fragmentSavingActivityName";
    public static final String TAGFragmentTAG = "fragmentSavingFragmentTAG";

    public static final String TAGList = "listFragment";
    public static final String TAGDetails = "detailsFragment";

}
