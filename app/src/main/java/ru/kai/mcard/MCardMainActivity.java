package ru.kai.mcard;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.kai.mcard.presentation.view.activity.SpecializationActivity;
import ru.kai.mcard.utility.Common;

public class MCardMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private DBMethods fDBMethods;

    Boolean drawerOpend = true;

    // настройки
    private SharedPreferences mSettings;
    int currentTheme;

    private int mCurrentProfile_ID = -1;

    boolean mFilterOnOff = false; // для оповещения о том, включен фильтр или нет
    int mCurrentFilterID = -1; // для фильтрации списков

    int mCurrentItemID = -1;  // id визита (консультации или исследования). Для определения позиции всегда идет в паре с типом визита
    int mCurrentItemType = -1;  // тип визита. Для однозначного определения позиции.
    int mCurrentItemPosition = -1;

    Boolean forDirectionsChoose;
    int directionsListsPosition;

    private Paint p = new Paint();

    DrawerLayout drawer;
    //FloatingActionButton fabMainMCardList;
    LinearLayoutManager llManager;
    RecyclerView rv;
    MainMCardListRVAdapter rvMainMCardListRVAdapter;
    AppBarLayout mMainMCardListAppbarLayout;
    CollapsingToolbarLayout mMainMCardListCollapsingToolbarLayout;
    RelativeLayout rl_mcard_main_list;
    NavigationView nav_view;
    CoordinatorLayout mainCoordinatorLayout;
    Toolbar toolbar;
    FloatingActionButton fabMainMCardList;
    NavigationView navigationView;

    TextView tvMainMCardListEmpty;
    TextView tvMainMCardListFiltredEmpty;

    private int counter; // счетчик для показа параметров экрана

    private boolean order_down = true; // сортировка визитов; если true, новые в конце.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // настройки
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        // установка темы
        setTheme(Common.getCurrentTheme(this));
/*
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME)) {
            // Получаем число из настроек
            currentTheme = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_THEME, R.style.Theme_StandartGreen);
            setTheme(currentTheme);
        }
*/
        setContentView(R.layout.mcard_main_activity);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        inizialiseActivity();

        //if (DBMethods.DATABASE_VERSION < 5) { // 2.4.25 (87) - (88)
            reparePhotos();
        //}

        // первоначальное заполнение таблиц анализов в визитах-исследованиях (обновление 4 SQL-базы) (июль 2016)
        loadAnalisesList();

        // создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);


        // политика конфиденциальности
        Boolean currentPP = true;
        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY)) {
            // Получаем значение из настроек
            currentPP = mSettings.getBoolean(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY, true);
        }
        if (currentPP){
            Intent intentPP = new Intent(MCardMainActivity.this, PryvacyPolicyActivityAccept.class);
            startActivityForResult(intentPP, Constants.PRIVACY_POLICY_ACCEPT);
            //overridePendingTransition(R.anim.popup_anim_enter,R.anim.pp_accept_alfa);
        }


    }

    private void inizialiseActivity(){

        rl_mcard_main_list = (RelativeLayout)findViewById(R.id.rl_mcard_main_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar_mcard_main_list);
        fabMainMCardList = (FloatingActionButton) findViewById(R.id.fab_mcard_main_list);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawer = (DrawerLayout) findViewById(R.id.mcard_drawer_layout);
        rv = (RecyclerView)findViewById(R.id.mMainMCardListRecyclerView);

        if (mSettings.contains(Constants.APP_PREFERENCES_VISIT_ORDER_DOWN)) {
            // Получаем значение из настроек
            order_down = mSettings.getBoolean(Constants.APP_PREFERENCES_VISIT_ORDER_DOWN, true);
        }


        toolbar.setTitle(getResources().getString(R.string.title_activity_mcard));
        setSupportActionBar(toolbar);

        fabMainMCardList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getSupportFragmentManager();
                DialogFragment myDialogFragment = VisitsChoiseDialogFragment.newInstance(mCurrentProfile_ID); //  <-- внутренний класс
                myDialogFragment.show(manager, "dialog");
            }
        });


        tvMainMCardListEmpty = (TextView) findViewById(R.id.tvMainMCardListEmpty);
        tvMainMCardListFiltredEmpty = (TextView) findViewById(R.id.tvMainMCardListFiltredEmpty);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_DRAWER)) {
            drawerOpend = mSettings.getBoolean(Constants.APP_PREFERENCES_SHOW_DRAWER, true);
        }
        //drawer.openDrawer(GravityCompat.START);

        // для понимания, открыта шторка или нет -->
        //drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerOpend = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                drawerOpend = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        //   <--

        loadProfile();

        rv.setHasFixedSize(true);

        // разделитель списка, установим в адаптере

        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                //outRect.left = (int) getResources().getDimension(R.dimen.devider_margin);
                //outRect.right = (int) getResources().getDimension(R.dimen.devider_margin);
                outRect.bottom = (int) getResources().getDimension(R.dimen.mcard_main_list_devider_height);

                // Add top margin only for the first item to avoid double space between items
                //if(parent.getChildPosition(view) == 0)
                //outRect.top = (int) getResources().getDimension(R.dimen.filds_padding);

            }
        });

        llManager = new LinearLayoutManager(this);
        rv.setLayoutManager(llManager);

        rvMainMCardListRVAdapter = new MainMCardListRVAdapter(null);
        try {
            rv.setAdapter(rvMainMCardListRVAdapter);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        // проверим, не открыта ли активность для выбора reference:
        forDirectionsChoose = getIntent().getBooleanExtra("getExistsDirection", false);
        directionsListsPosition = getIntent().getIntExtra("directionsListsPosition", 0);
        if (forDirectionsChoose){
            toolbar.setTitle(getResources().getString(R.string.app_title_for_directions_choose));
            fabMainMCardList.setVisibility(View.GONE);
        }

        initSwipe();

        // нужно пользователю сообщить, что у него включен фильтр. Он наверняка забыл...
        mainCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.mainCoordinatorLayout);
        readFiltersParameters();

    }

    // переделка фотографий после версии 88
    private void reparePhotos() {
        final String secondPart = "/Android/data/ru.kai.mcard/files/Pictures/";
        Cursor cursor = fDBMethods.getAllPhotos(Constants.PHOTOS_TYPE_APP);
        if (cursor.getCount()>0){
            cursor.moveToFirst();
            int photosID;
            do {
                photosID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsPhotos._ID));
                int basicVisitsType = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_BASIC_VISITS_TYPE));
                int basicVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_BASIC_VISITS_ID));
                int visitsPhotosType = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_PHOTOS_TYPE));
                String visitsPhotosName = cursor.getString(cursor.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_PHOTOS_NAME));
                String visitsPhotosURI = cursor.getString(cursor.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_PHOTOS_URI));

                if ((visitsPhotosType == Constants.PHOTOS_TYPE_APP)|(visitsPhotosType == Constants.PHOTO_CAMERA)){
                    int indexOfBeginSecondPart = visitsPhotosURI.indexOf(secondPart);

                    if (indexOfBeginSecondPart > 0) {
                        String firstPart = visitsPhotosURI.substring(0, indexOfBeginSecondPart);
                        visitsPhotosURI = visitsPhotosURI.replace(firstPart, "");
                        visitsPhotosURI = visitsPhotosURI.replace(secondPart, "");

                        fDBMethods.deleteVisitsPhotoByID(photosID);
                        fDBMethods.insertRecordVisitsPhotos(basicVisitsType, basicVisitsID, visitsPhotosType, visitsPhotosName, visitsPhotosURI);
                    }
                }

            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem orderItem = menu.getItem(0);
        if(order_down){ // старые визиты вверху, новые в конце списка
            orderItem.setIcon(R.drawable.ic_arrow_drop_down_white_24dp);
        }else {  // старые визиты внизу, новые в начале списка
            orderItem.setIcon(R.drawable.ic_arrow_drop_up_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.main_img_order:
                if(order_down){ // старые визиты вверху, новые в конце списка
                    order_down = false;
                    item.setIcon(R.drawable.ic_arrow_drop_up_white_24dp);
                }else {  // старые визиты внизу, новые в начале списка
                    order_down = true;
                    item.setIcon(R.drawable.ic_arrow_drop_down_white_24dp);
                }
                // Запоминаем текущее значение
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean(Constants.APP_PREFERENCES_VISIT_ORDER_DOWN, order_down);
                editor.apply();
                getLoaderManager().restartLoader(0, null, this);
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerOpend){
            drawer.openDrawer(GravityCompat.START);
        }else {
            drawer.closeDrawer(GravityCompat.START);
        }

        readFiltersParameters();

        // обнуляем счетчик для 8-кратного нажатия с цлью показа параметров экрана
        counter = 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PRIVACY_POLICY_ACCEPT){
            // если политику конфиденциальности не приняли, то закрываем приложение
            if (resultCode != RESULT_OK){
                Boolean currentPP = true;
                if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY)) {
                    // Получаем число из настроек
                    currentPP = mSettings.getBoolean(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY, true);
                }
                if (currentPP){
                    System.exit(0);
                }
            }
        }
        if (data != null){
            mCurrentItemID = data.getIntExtra("currentItemID", -1);
            mCurrentItemType = data.getIntExtra("currentItemType", -1);
        }
        if (resultCode == RESULT_OK){
            switch (requestCode) {
                case Constants.VISIT_DATA:
                    mCurrentItemType = Constants.DIRECTIONS_VISITS_TYPE_0;
                    //getLoaderManager().restartLoader(0, null, this);
                    drawerOpend = false;
                    break;
                case Constants.ANALISIS_VISIT_DATA:
                    mCurrentItemType = Constants.DIRECTIONS_ANALISIS_TYPE_1;
                    //getLoaderManager().restartLoader(0, null, this);
                    drawerOpend = false;
                    break;
                case Constants.FILTER_CHOICE:
                    if (data != null) {
                        mCurrentFilterID = data.getIntExtra("chosenFiltersID", -1);

                        // обновим настройки
                        mFilterOnOff = mCurrentFilterID != -1;

                        saveFiltersParameters();
                    }
                    //getLoaderManager().restartLoader(0, null, this);
                    drawerOpend = false;
                    break;
                case Constants.THEMES_CHOICE:
                    if (data != null) {
                        currentTheme = data.getIntExtra("currentTheme", R.style.Theme_StandartGreen);

                        setTheme(currentTheme);
                        setContentView(R.layout.mcard_main_activity);
                        // почему-то цвет StatusBar-а не устанавливается при смене темы. Установим его при помощи этой процедуры
                        Common.setStatusBarColor(this, currentTheme);
                        inizialiseActivity();
                    }
                    //getLoaderManager().restartLoader(0, null, this);
                    drawerOpend = true;
                    break;
                case Constants.EDIT_PROFILES:
                    if (data != null) {
                        mCurrentProfile_ID = data.getIntExtra("mCurrentProfileID", -1);

                        // Запоминаем текущий профиль
                        SharedPreferences.Editor editor1 = mSettings.edit();
                        editor1.putInt(Constants.APP_PREFERENCES_CURRENT_PROFILE_ID, mCurrentProfile_ID);  // id примененного фильтра
                        editor1.apply();
                    }
                    //getLoaderManager().restartLoader(0, null, this);
                    drawerOpend = false;
                    break;
            }
        }

        if (drawerOpend){
            drawer.openDrawer(GravityCompat.START);
        }else {
            drawer.closeDrawer(GravityCompat.START);
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Constants.APP_PREFERENCES_SHOW_DRAWER, drawerOpend);
        editor.apply();

        //mCardBackupAgent.requestBackup(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null){
            fDBMethods.close();
        }
    }

    private void readFiltersParameters(){
        if (mSettings.contains(Constants.APP_PREFERENCES_FILTER_OFF)) {
            // для оповещения о том, включен фильтр или нет
            mFilterOnOff = mSettings.getBoolean(Constants.APP_PREFERENCES_FILTER_OFF, false);
        }

        if (mFilterOnOff){
            if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_FILTER_ID)) {
                mCurrentFilterID = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_FILTER_ID, -1);
            }
        }else {
            mCurrentFilterID = -1;
        }

        if(mFilterOnOff){
            Snackbar snackbar = Common.getCustomSnackbar(MCardMainActivity.this, mainCoordinatorLayout, getString(R.string.main_filter_is_on));
            snackbar.show();
        }
    }

    private void saveFiltersParameters(){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Constants.APP_PREFERENCES_FILTER_OFF, mFilterOnOff);
        editor.putInt(Constants.APP_PREFERENCES_CURRENT_FILTER_ID, mCurrentFilterID);  // id примененного фильтра
        editor.apply();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_clinics:
                drawerOpend = true;
                Intent intentC = new Intent(MCardMainActivity.this, MClinicsActivity.class);
                intentC.putExtra("can_choosen_items", false);
                startActivity(intentC);
                break;
            case R.id.nav_doctors:
                drawerOpend = true;
                Intent intent = new Intent(MCardMainActivity.this, MDoctorsActivity.class);
                intent.putExtra("can_choosen_items", false);
                startActivity(intent);
                break;
            case R.id.nav_specialisations:
                drawerOpend = true;
                Intent intentS = new Intent(MCardMainActivity.this, SpecializationActivity.class); // new
                //Intent intentS = new Intent(MCardMainActivity.this, MSpecializationsActivity.class);
                intentS.putExtra("can_choosen_items", false);
                startActivity(intentS);
                break;
            case R.id.nav_analises_types:
                drawerOpend = true;
                Intent intentAT = new Intent(MCardMainActivity.this, MAnalisesTypesActivity.class);
                intentAT.putExtra("can_choosen_items", false);
                startActivity(intentAT);
                break;
            case R.id.nav_diagnoses:
                drawerOpend = true;
                Intent intentD = new Intent(MCardMainActivity.this, MDiagnosesActivity.class);
                intentD.putExtra("can_choosen_items", false);
                startActivity(intentD);
                break;
            case R.id.nav_cures:
                drawerOpend = true;
                Intent intentCu = new Intent(MCardMainActivity.this, MCuresActivity.class);
                intentCu.putExtra("can_choosen_items", false);
                startActivity(intentCu);
                break;
            case R.id.nav_recommendations:
                drawerOpend = true;
                Intent intentR = new Intent(MCardMainActivity.this, MRecommendationsActivity.class);
                intentR.putExtra("can_choosen_items", false);
                startActivity(intentR);
                break;
            case R.id.nav_fast_filters:
                drawerOpend = true;
                Intent intentF = new Intent(MCardMainActivity.this, MFilterListActivity.class);
                intentF.putExtra("current_filter_id", mCurrentFilterID);
                startActivityForResult(intentF, Constants.FILTER_CHOICE);
                break;
            case R.id.nav_filter_ext:
                drawerOpend = true;
                Intent intentFx = new Intent(MCardMainActivity.this, MFilterExtActivity.class);
                intentFx.putExtra("current_filter_id", mCurrentFilterID);
                startActivityForResult(intentFx, Constants.FILTER_CHOICE);
                break;
            case R.id.nav_filter_off:
                mCurrentFilterID = -1;
                mFilterOnOff = false;
                saveFiltersParameters();
                drawerOpend = false;
                drawer.closeDrawer(GravityCompat.START);
                getLoaderManager().restartLoader(0, null, this);
                break;
            case R.id.nav_themes_choice:
                drawerOpend = true;
                Intent intentT = new Intent(MCardMainActivity.this, MThemesChoiseActivity.class);
                intentT.putExtra("choosen_themes", 0);
                startActivityForResult(intentT, Constants.THEMES_CHOICE);
                break;
            case R.id.nav_profiles:
                drawerOpend = false;
                // получим из настроек id профиля
                if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_PROFILE_ID)) {
                    mCurrentProfile_ID = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_PROFILE_ID, -1);
                }

                Intent intentP = new Intent(MCardMainActivity.this, MProfilesListActivity.class);
                intentP.putExtra("mCurrentProfileID", mCurrentProfile_ID);
                startActivityForResult(intentP, Constants.EDIT_PROFILES);
                break;
            case R.id.nav_about:
                drawerOpend = true;
                Intent intentA = new Intent(MCardMainActivity.this, AboutActivity.class);
                startActivity(intentA);
                break;
            case R.id.nav_backups:
                drawerOpend = true;
                Intent intentB = new Intent(MCardMainActivity.this, MBackupActivity.class);
                startActivity(intentB);
                break;
        }

        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadProfile(){

        // произведем первоначальное заполнение таблицы "Профили", если таблица существует:
        Boolean mustCreateFirstProfile = false;
        try {
            Cursor cursor = fDBMethods.getAllProfiles();
            if (cursor.getCount()==0)mustCreateFirstProfile = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mustCreateFirstProfile) {
            // создадим первый профиль и заполним ссылкой на него нужные таблицы
            Bundle bundle = new Bundle();
            bundle.putString("profilesName", getString(R.string.first_profile_name));
            bundle.putString("profilesSex", "");
            bundle.putLong("profilesBirthday", 329920870046L);
            bundle.putString("profilesComment", "");
            mCurrentProfile_ID = fDBMethods.insertProfiles(bundle);

            // Запоминаем текущий профиль в настройках
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(Constants.APP_PREFERENCES_CURRENT_PROFILE_ID, mCurrentProfile_ID);  // id текущего профиля
            editor.apply();

            // теперь заполним таблицы визитов ссылками на созданный профиль
            Cursor cursorV = fDBMethods.getAllVisits();
            if (cursorV.getCount() > 0) {
                int visitsID;
                cursorV.moveToFirst();
                do {
                    visitsID = cursorV.getInt(cursorV.getColumnIndex(DBMethods.TableVisits._ID));
                    // обновим данные визита
                    fDBMethods.updateVisitsProfile(visitsID, mCurrentProfile_ID);

                }while (cursorV.moveToNext());
            }
            cursorV.close();

            // то же самое у визитов-исследований
            Cursor cursorAV = fDBMethods.getAllAnalisisVisits();
            if (cursorAV.getCount() > 0) {
                int analisesVisitsID;
                cursorAV.moveToFirst();
                do {
                    analisesVisitsID = cursorAV.getInt(cursorAV.getColumnIndex(DBMethods.TableAnalisesVisits._ID));
                    // обновим данные визита-исследования
                    fDBMethods.updateAnalisisVisitsProfile(analisesVisitsID, mCurrentProfile_ID);

                }while (cursorAV.moveToNext());
            }
            cursorAV.close();
        }

        // получим из настроек id профиля
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_PROFILE_ID)) {
            mCurrentProfile_ID = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_PROFILE_ID, -1);
        }

    }

    public class MainMCardListRVAdapter extends RecyclerView.Adapter<MainMCardListRVAdapter.MainMCardListViewHolders> {

        private final int TYPE_VISIT = Constants.DIRECTIONS_VISITS_TYPE_0;
        private final int TYPE_ANALISES_VISIT = Constants.DIRECTIONS_ANALISIS_TYPE_1;
        Cursor dataCursor;

        LinearLayout ll_cv_visits_mcard_main_list; // для выделения цветом
        LinearLayout ll_cv_an_visits_mcard_main_list; // для выделения цветом

        MainMCardListRVAdapter(Cursor cursor) {
            this.dataCursor = cursor;
        }

        @Override
        public MainMCardListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = null;

                switch (viewType) {
                    case TYPE_VISIT:
                        View vVisit = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_mcard_main_list_visits, null);

                        ll_cv_visits_mcard_main_list = (LinearLayout) vVisit.findViewById(R.id.ll_cv_visits_mcard_main_list);
                        ll_cv_visits_mcard_main_list.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentItemType = Constants.DIRECTIONS_VISITS_TYPE_0;
                                //itemView.setSelected(true);
                                // отправляем ID выбранной позиции visit
                                TextView tv_visits_mcard_main_list_date_visits_id = (TextView) v.findViewById(R.id.tv_visits_mcard_main_list_visits_id);
                                int choosenID = Integer.parseInt(tv_visits_mcard_main_list_date_visits_id.getText().toString());

                                if (forDirectionsChoose) {
                                    Intent intent = new Intent();
                                    intent.putExtra("choosen_directions_id", choosenID);
                                    intent.putExtra("directionVisitsTypesID", Constants.DIRECTIONS_VISITS_TYPE_0);
                                    intent.putExtra("directionsListsPosition", directionsListsPosition);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    return;
                                }

                                Intent intent = new Intent(MCardMainActivity.this, MVisitActivity.class);
                                intent.putExtra("choosen_visits_id", choosenID);
                                intent.putExtra("currentProfileID", mCurrentProfile_ID);
                                startActivityForResult(intent, Constants.VISIT_DATA);
                            }
                        });
                        v = vVisit;
                        break;
                    case TYPE_ANALISES_VISIT:
                        View vAnVisit = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_mcard_main_list_analises_visits, null);

                        ll_cv_an_visits_mcard_main_list = (LinearLayout) vAnVisit.findViewById(R.id.ll_cv_an_visits_mcard_main_list);
                        ll_cv_an_visits_mcard_main_list.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentItemType = Constants.DIRECTIONS_ANALISIS_TYPE_1;
                                //itemView.setSelected(true);
                                // отправляем ID выбранной позиции analises visit
                                TextView tv_visits_mcard_main_list_date_visits_id = (TextView) v.findViewById(R.id.tv_an_visits_mcard_main_list_an_visits_id);
                                int choosenID = Integer.parseInt(tv_visits_mcard_main_list_date_visits_id.getText().toString());

                                // если нажали для выбора направления (не может быть направления с визита исследования)
                                if (forDirectionsChoose) {
                                    Intent intent = new Intent();
                                    intent.putExtra("choosen_directions_id", choosenID);
                                    intent.putExtra("directionVisitsTypesID", Constants.DIRECTIONS_ANALISIS_TYPE_1);
                                    intent.putExtra("directionsListsPosition", directionsListsPosition);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    return;
                                }

                                Intent intent = new Intent(MCardMainActivity.this, MAnalisisVisitActivity.class);
                                intent.putExtra("choosen_visits_id", choosenID);
                                intent.putExtra("currentProfileID", mCurrentProfile_ID);
                                startActivityForResult(intent, Constants.ANALISIS_VISIT_DATA);
                            }
                        });

                        v = vAnVisit;
                        break;
                }

                MainMCardListViewHolders analisesTypesListVH = new MainMCardListViewHolders(v);

                return analisesTypesListVH;
        }

        public Cursor swapCursor(Cursor cursor) {
            if (dataCursor == cursor) {
                return null;
            }
            Cursor oldCursor = dataCursor;
            this.dataCursor = cursor;
            if (cursor != null) {
                this.notifyDataSetChanged();
            }
            return oldCursor;
        }


        @Override
        public int getItemViewType(int position) {
            // условие для определения айтем какого типа выводить в конкретной позиции
            dataCursor.moveToPosition(position);
            int typeOfVisit = dataCursor.getInt(dataCursor.getColumnIndex("type_of_visit"));
            if(typeOfVisit == Constants.DIRECTIONS_VISITS_TYPE_0){
                return TYPE_VISIT;
            }
            if(typeOfVisit == Constants.DIRECTIONS_ANALISIS_TYPE_1){
                return TYPE_ANALISES_VISIT;
            }
            //if (position == <условие>) return TYPE_ITEM1;
            //return TYPE_ITEM2;
            return 0; // default - visit
        }


        @Override
        public void onBindViewHolder(MainMCardListViewHolders holder, int position) {

            int typeOfVisit = getItemViewType(position);

            int visitsID = dataCursor.getInt(dataCursor.getColumnIndex("id"));
            Long vDate = dataCursor.getLong(dataCursor.getColumnIndex("date"));
            int ClinicsID = dataCursor.getInt(dataCursor.getColumnIndex("clinicID"));
            int SpecializationsID = dataCursor.getInt(dataCursor.getColumnIndex("specializationID"));
            int DoctorsID = dataCursor.getInt(dataCursor.getColumnIndex("doctorID"));

            String clinicsName = fDBMethods.getClinicsNameByID(ClinicsID);
            String doctorsName;
            String specializationsName;
            String analisisTypesName;

            holder.typeOfVisit = typeOfVisit;

            switch (typeOfVisit) {
                case TYPE_VISIT:
                    holder.tv_visits_mcard_main_list_visits_id.setText(String.valueOf(visitsID));
                    holder.tv_visits_mcard_main_list_date.setText(setVisitsDate(vDate));
                    holder.tv_visits_mcard_main_list_clinic.setText(clinicsName);
                    doctorsName = fDBMethods.getDoctorsNameByID(DoctorsID);
                    holder.tv_visits_mcard_main_list_doctor.setText(doctorsName);
                    specializationsName = fDBMethods.getSpecializationsNameByID(SpecializationsID);
                    holder.tv_visits_mcard_main_list_specialisation.setText(specializationsName);
                    if ((typeOfVisit == mCurrentItemType) & (visitsID == mCurrentItemID)) {
                        holder.ll_cv_visits_mcard_main_list.setBackground(Common.getChoosenItemsDrawable(MCardMainActivity.this));
                    } else {
                        holder.ll_cv_visits_mcard_main_list.setBackground(Common.getOrdinaryVisitsItemsDawable(MCardMainActivity.this));
                    }
                    break;
                case TYPE_ANALISES_VISIT:
                    holder.tv_an_visits_mcard_main_list_an_visits_id.setText(String.valueOf(visitsID));
                    holder.tv_an_visits_mcard_main_list_date.setText(setVisitsDate(vDate));
                    holder.tv_an_visits_mcard_main_list_clinic.setText(clinicsName);
                    //analisisTypesName = fDBMethods.getAnalisesTypesNameByID(SpecializationsID);
                    analisisTypesName = fDBMethods.getAllAnalisisOfAnalisesVisit(visitsID);
                    holder.tv_an_visits_mcard_main_list_analisis_type.setText(analisisTypesName);
                    if ((typeOfVisit == mCurrentItemType) & (visitsID == mCurrentItemID)) {
                        holder.ll_cv_an_visits_mcard_main_list.setBackground(Common.getChoosenItemsDrawable(MCardMainActivity.this));
                    } else {
                        holder.ll_cv_an_visits_mcard_main_list.setBackground(Common.getOrdinaryItemsDawable(MCardMainActivity.this));
                    }
                    break;
            }
        }

        private String setVisitsDate(Long vDate){
            if (vDate == 0L) {
                return "";
            }
            else {
                CharSequence date = DateFormat.format("dd.MM.yy", vDate);
                return String.valueOf(date);
            }
        }

        @Override
        public int getItemCount() {
            return (dataCursor == null) ? 0 : dataCursor.getCount();
        }

        class MainMCardListViewHolders extends RecyclerView.ViewHolder {

            int typeOfVisit;

            LinearLayout ll_cv_visits_mcard_main_list;
            TextView tv_visits_mcard_main_list_visits_id;
            TextView tv_visits_mcard_main_list_date;
            TextView tv_visits_mcard_main_list_specialisation;
            TextView tv_visits_mcard_main_list_clinic;
            TextView tv_visits_mcard_main_list_doctor;

            LinearLayout ll_cv_an_visits_mcard_main_list;
            TextView tv_an_visits_mcard_main_list_an_visits_id;
            TextView tv_an_visits_mcard_main_list_date;
            TextView tv_an_visits_mcard_main_list_analisis_type;
            TextView tv_an_visits_mcard_main_list_clinic;

            MainMCardListViewHolders(final View itemView) {
                super(itemView);
                // visit
                ll_cv_visits_mcard_main_list = (LinearLayout) itemView.findViewById(R.id.ll_cv_visits_mcard_main_list);
                tv_visits_mcard_main_list_visits_id = (TextView) itemView.findViewById(R.id.tv_visits_mcard_main_list_visits_id);
                tv_visits_mcard_main_list_date = (TextView) itemView.findViewById(R.id.tv_visits_mcard_main_list_date);
                tv_visits_mcard_main_list_specialisation = (TextView) itemView.findViewById(R.id.tv_visits_mcard_main_list_specialisation);
                tv_visits_mcard_main_list_clinic = (TextView) itemView.findViewById(R.id.tv_visits_mcard_main_list_clinic);
                tv_visits_mcard_main_list_doctor = (TextView) itemView.findViewById(R.id.tv_visits_mcard_main_list_doctor);

                // analises visit
                ll_cv_an_visits_mcard_main_list = (LinearLayout) itemView.findViewById(R.id.ll_cv_an_visits_mcard_main_list);
                tv_an_visits_mcard_main_list_an_visits_id = (TextView) itemView.findViewById(R.id.tv_an_visits_mcard_main_list_an_visits_id);
                tv_an_visits_mcard_main_list_date = (TextView) itemView.findViewById(R.id.tv_an_visits_mcard_main_list_date);
                tv_an_visits_mcard_main_list_analisis_type = (TextView) itemView.findViewById(R.id.tv_an_visits_mcard_main_list_analisis_type);
                tv_an_visits_mcard_main_list_clinic = (TextView) itemView.findViewById(R.id.tv_an_visits_mcard_main_list_clinic);

            }

        }
    }

    private void initSwipe(){


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                //int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){
                    // удалить
                    final int currentTypeOfVisit = ((MainMCardListRVAdapter.MainMCardListViewHolders) viewHolder).typeOfVisit;
                    int currentVisitsID = -1;
                    switch (currentTypeOfVisit){
                        case Constants.DIRECTIONS_VISITS_TYPE_0:
                            currentVisitsID = Integer.parseInt(((MainMCardListRVAdapter.MainMCardListViewHolders) viewHolder).tv_visits_mcard_main_list_visits_id.getText().toString());
                            mCurrentItemType = Constants.DIRECTIONS_VISITS_TYPE_0;
                            break;
                        case Constants.DIRECTIONS_ANALISIS_TYPE_1:
                            mCurrentItemType = Constants.DIRECTIONS_ANALISIS_TYPE_1;
                            currentVisitsID = Integer.parseInt(((MainMCardListRVAdapter.MainMCardListViewHolders) viewHolder).tv_an_visits_mcard_main_list_an_visits_id.getText().toString());
                            break;
                    }
                    mCurrentItemID = currentVisitsID;
                    String title = getResources().getString(R.string.deleting_visit_title);
                    String message = getResources().getString(R.string.deleting_visit_question);
                    String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
                    String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

                    AlertDialog.Builder ad = new AlertDialog.Builder(MCardMainActivity.this);
                    ad.setTitle(title);  // заголовок
                    ad.setMessage(message); // сообщение
                    ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            int currVisitsID;
                            switch (currentTypeOfVisit){
                                case Constants.DIRECTIONS_VISITS_TYPE_0:
                                    currVisitsID = Integer.parseInt(((MainMCardListRVAdapter.MainMCardListViewHolders) viewHolder).tv_visits_mcard_main_list_visits_id.getText().toString());
                                    String answerCheck = fDBMethods.checkOfUsageVisit(MCardMainActivity.this, currVisitsID);
                                    if (!answerCheck.isEmpty()){
                                        // визит используется где-то. Сообщим об этом:
                                        Snackbar snackbar = Common.getCustomSnackbar(MCardMainActivity.this, mainCoordinatorLayout, answerCheck);
                                        snackbar.show();
                                    }else {
                                        fDBMethods.deleteVisit(currVisitsID);
                                    }
                                    break;
                                case Constants.DIRECTIONS_ANALISIS_TYPE_1:
                                    currVisitsID = Integer.parseInt(((MainMCardListRVAdapter.MainMCardListViewHolders) viewHolder).tv_an_visits_mcard_main_list_an_visits_id.getText().toString());
                                    String answerCheckAV = fDBMethods.checkOfUsageAnalisisVisit(MCardMainActivity.this, currVisitsID);
                                    if (!answerCheckAV.isEmpty()){
                                        // визит используется где-то. Сообщим об этом:
                                        Snackbar snackbar = Common.getCustomSnackbar(MCardMainActivity.this, mainCoordinatorLayout, answerCheckAV);
                                        snackbar.show();
                                    }else {
                                        fDBMethods.deleteAnalisisVisit(currVisitsID);
                                    }
                                    break;
                            }

                            getLoaderManager().restartLoader(0, null, MCardMainActivity.this);
                        }
                    });
                    ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            getLoaderManager().restartLoader(0, null, MCardMainActivity.this);
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();

                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                //int alternativeThemeColor = Common.getAlternativeThemeColor(getApplicationContext());

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 1;

                    if(dX > 0){
                        // edit
/*
                        p.setColor(Color.parseColor("#20ab1e"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_edit_black);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + 0*width ,(float) itemView.getBottom() - width,(float) itemView.getLeft()+ 1*width,(float)itemView.getTop() + width);
                        c.drawBitmap(icon,null,icon_dest,p);
*/
                        return;
                    } else {
                        // delete
                        p.setColor(Color.parseColor("#FF4081"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_delete_black);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 1*width,(float) itemView.getBottom() - width, (float) itemView.getRight() - 0*width,(float)itemView.getTop() + width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv);

    }

    public static class VisitsChoiseDialogFragment extends DialogFragment {

        public VisitsChoiseDialogFragment() {
            // Required empty public constructor
        }

        public static VisitsChoiseDialogFragment newInstance(int mCurrentProfile_ID) {
            VisitsChoiseDialogFragment frag = new VisitsChoiseDialogFragment();
            Bundle args = new Bundle();
            args.putInt("currentProfileID", mCurrentProfile_ID);
            frag.setArguments(args);

            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final String messageTitle = getActivity().getString(R.string.item_main_visit_choice_question);
            final String visit = getActivity().getString(R.string.item_main_visit_choice_visit);      // Визит
            final String anVisit = getActivity().getString(R.string.item_main_visit_choice_analisis_visit);  // Визит на исследование

            final String[] visitsArray = {visit, anVisit};

            final int currentProfileID = getArguments().getInt("currentProfileID");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            builder.setTitle(messageTitle)
                    .setItems(visitsArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which){
                                case 0:  // Визит
                                    ((MCardMainActivity)getActivity()).openActivityForResult(Constants.VISIT_DATA, currentProfileID);
                                    break;
                                case 1:  // Визит на исследование
                                    ((MCardMainActivity)getActivity()).openActivityForResult(Constants.ANALISIS_VISIT_DATA, currentProfileID);
                                    break;
                            }
                        }
                    });

            return builder.create();
        }
    }

    public void openActivityForResult(int typeOfVisit, int currentProfileID){
        switch (typeOfVisit){
            case Constants.VISIT_DATA:  // Визит
                Intent intent = new Intent(MCardMainActivity.this, MVisitActivity.class);
                intent.putExtra("choosen_visits_id", 0);
                intent.putExtra("currentProfileID", currentProfileID);
                startActivityForResult(intent, Constants.VISIT_DATA);
                break;
            case Constants.ANALISIS_VISIT_DATA:  // Визит на исследование
                Intent intent1 = new Intent(MCardMainActivity.this, MAnalisisVisitActivity.class);
                intent1.putExtra("choosen_visits_id", 0);
                intent1.putExtra("currentProfileID", currentProfileID);
                startActivityForResult(intent1, Constants.ANALISIS_VISIT_DATA);
                break;
        }

    }

    // процедура нужна для перехода версии БД с 3 на 4
    private void loadAnalisesList(){
        // проверим, нужно ли первоначальное заполнение (существует ли таблица)
        Boolean mustCreateFirstAnalisesRows = false;
        try {
            Cursor cursor = fDBMethods.getAllAnalisisVisitsAnalisesList();
            if (cursor.getCount()==0){
                mustCreateFirstAnalisesRows = true;
                cursor.close();
                // заполним первую строку. Она будет отвязана от всех визитов и нужна лишь для того, чтобы видеть в дальнейшем, что первоначальное заполнение уже производили.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mustCreateFirstAnalisesRows) {
            fDBMethods.insertRecordAnalisisVisitsAnalisesList(0, 0, 0);

            // теперь собственно первоначальное заполнение:
            // пробежимся по всем визитам-исследованиям, возьмем значение их единственного реквизита "Исследование" и заполним им первую строку вновь созданной таблицы:
            Cursor cursor = fDBMethods.getAllAnalisisVisits();

            long rowID;
            if (cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    int analisisVisitsID    = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisesVisits._ID));
                    int analisisTypesID     = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisesVisits.COLUMN_ANALISES_VISITS_ANALISES_TYPES_ID));
                    int analisisVisitsRefID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisesVisits.COLUMN_ANALISES_VISITS_REF_ID));

                    rowID = fDBMethods.insertRecordAnalisisVisitsAnalisesList(analisisVisitsID, analisisTypesID, analisisVisitsRefID);

                } while (cursor.moveToNext());
            }
            cursor.close();


            // также проведем дозаполнение таблицы DB_TABLE_VISITS_DIRECTIONS. Заполним новое поле TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID
            // пробежимся по всем направлениям. Проверим тип. Если направление - это исследование, то поле COLUMN_AN_TYPES_SPECIALISATIONS_ID заполним ID типа исследования,
            // иначе ID специализации доктора.
            Cursor cursorD = fDBMethods.getAllDirectionsAllVisits();

            if (cursorD.getCount()>0) {
                cursorD.moveToFirst();
                do {
                    int row_ID = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                    int basicVisitsID = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_BASIC_VISITS_ID));
                    int directionsVisitsTypes = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE));
                    //int anTypeOrSpecID = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID));
                    int directionsVisitsID = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID));

                    if (directionsVisitsTypes == Constants.DIRECTIONS_ANALISIS_TYPE_1){    // направление на исследование
                        Bundle bundle = fDBMethods.getAnalisisVisitsFildsByAnalisisVisitsID(directionsVisitsID);
                        int AnalisisTypesID = bundle.getInt("mAnalisisTypesID");
                        fDBMethods.updateRowVisitsDirections(row_ID, basicVisitsID, directionsVisitsTypes, AnalisisTypesID, directionsVisitsID);
                    }else{  // направление на консультацию
                        Bundle bundle = fDBMethods.getVisitsFildsByVisitsID(directionsVisitsID);
                        int specializationsID = bundle.getInt("specializationsID");
                        fDBMethods.updateRowVisitsDirections(row_ID, basicVisitsID, directionsVisitsTypes, specializationsID, directionsVisitsID);
                    }

                } while (cursorD.moveToNext());
            }
            cursorD.close();

        }

    }

    public void showScreenParameters(View view) {

        // если пользователь тапнет 8 раз, то откроем окно показа параметров экрана
        counter = counter + 1;

        if (counter == 8) {

/*
            // покажем строку с резервным копированием
            Menu menu = nav_view.getMenu();
            MenuItem menuItem = menu.findItem(R.id.nav_backups);
            menuItem.setVisible(true);
*/


            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            String strScreen = "";
            strScreen += "Width: " + String.valueOf(metrics.widthPixels) + " pixels"
                    + "\n";
            strScreen += "Height: " + String.valueOf(metrics.heightPixels) + " pixels"
                    + "\n";
            strScreen += "The Logical Density: " + String.valueOf(metrics.density)
                    + "\n";
            strScreen += "X Dimension: " + String.valueOf(metrics.xdpi) + " dot/inch"
                    + "\n";
            strScreen += "Y Dimension: " + String.valueOf(metrics.ydpi) + " dot/inch"
                    + "\n";
            strScreen += "The screen density expressed as dots-per-inch: "
                    + metrics.densityDpi + "\n";
            strScreen += "A scaling factor for fonts displayed on the display: "
                    + metrics.scaledDensity + "\n";

            AlertDialog.Builder builder = new AlertDialog.Builder(MCardMainActivity.this);
            builder.setTitle("Параметры экрана.")
                    .setMessage(strScreen)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();


        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MainMCardListCursorLoader(this, fDBMethods, mCurrentFilterID, mCurrentProfile_ID, order_down);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        // скрыть стартовый текст, если список не пустой, видимость надписи "Список визитов пуст"
        if (data == null){
            if (mCurrentFilterID == -1){
                tvMainMCardListEmpty.setVisibility(View.VISIBLE);
                tvMainMCardListFiltredEmpty.setVisibility(View.GONE);
            }else {
                tvMainMCardListEmpty.setVisibility(View.GONE);
                tvMainMCardListFiltredEmpty.setVisibility(View.VISIBLE);
            }
        }else {
            if (data.getCount() == 0) {
                if (mCurrentFilterID == -1) {
                    tvMainMCardListEmpty.setVisibility(View.VISIBLE);
                    tvMainMCardListFiltredEmpty.setVisibility(View.GONE);
                } else {
                    tvMainMCardListEmpty.setVisibility(View.GONE);
                    tvMainMCardListFiltredEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                tvMainMCardListEmpty.setVisibility(View.GONE);
                tvMainMCardListFiltredEmpty.setVisibility(View.GONE);
            }
        }


        if (data != null){
            // код для определения позиции адаптера, соответствующей текущему clinicsID (это нужно при добавлении новой позиции в список)
            if ((mCurrentItemID != -1)&(mCurrentItemType != -1)){
                if (data.getCount()>0){
                    data.moveToFirst();
                    do {
                        int visitsType = data.getInt(data.getColumnIndex("type_of_visit"));
                        int visitsID = data.getInt(data.getColumnIndex("id"));
                        if ((visitsType == mCurrentItemType)&(visitsID == mCurrentItemID)){
                            mCurrentItemPosition = data.getPosition();
                            break;
                        }
                    }while (data.moveToNext());
                }
            }
        }


        if (data != null) {
            try {
                rvMainMCardListRVAdapter.swapCursor(data);
            } catch (Exception e) {
                Toast.makeText(this, "onLoadFinished: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }


        try {
            if (mCurrentItemPosition != -1){
                //mClinicsAppbarLayout.setExpanded(false);
                rv.smoothScrollToPosition(mCurrentItemPosition);
            }else {
                rv.smoothScrollToPosition(0);
            }
        }catch (Exception e){}

        // проверять ПК будем только у новых юзеров, не будем надоедать старым
        // если есть хоть один визит, значит юзер не новый
        if (data != null){
            if (data.getCount() > 0){
                Boolean currentPP = true;
                if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY)) {
                    // Получаем значение из настроек
                    currentPP = mSettings.getBoolean(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY, true);
                }
                if (currentPP){
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY, false);
                    editor.apply();
                }

            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvMainMCardListRVAdapter.swapCursor(null);
    }

    static class MainMCardListCursorLoader extends CursorLoader {
        DBMethods scldbMethods;
        int currentFilterID;
        int currentProfileID;
        boolean order_down;

        MainMCardListCursorLoader(Context context, DBMethods dbMethods, int currentFilterID, int currentProfileID, boolean order_down) {
            super(context);
            this.scldbMethods = dbMethods;
            this.currentFilterID = currentFilterID;
            this.currentProfileID = currentProfileID;
            this.order_down = order_down;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = scldbMethods.getMainFiltratedVisitList(currentFilterID, currentProfileID, order_down);
            return cursor;
        }
    }



}
