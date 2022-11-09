package ru.kai.mcard;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import ru.kai.mcard.utility.Common;


public class MAnalisisVisitActivity extends AppCompatActivity implements MAnalisisVisitAnaliseslistFragment.OnGetSaveVisitCommand,
                                                                            MAnalisisVisitMainFragment.OnGetSaveVisitCommand {

    Toolbar mToolBar;
    TabLayout mTabLayout;
    private ViewPager mViewPager;
    VisitsViewPagerAdapter mVisitsViewPagerAdapter;

    // поля визита, которые будут сохранены в БД
    MAnalisisVisitMainFragment fragmentAVMain;
    MAnalisisVisitAnaliseslistFragment fragmentAVAnalisesList;
    //AutoCompleteTextView editAnalisisVisitsClinic;
    //EditText editAnalisisVisitsComment;
    //TextView tvAnalisisVisitsDate;
    //TextView tvAnalisisVisitsTime;

    private DBMethods fDBMethods;

    int gettingID; // для идентификации визита и update или delete


    int mCurrentProfileID; // нужно знать для какого профиля сохраняем данный визит

    // поля для записи (реквизиты)
    Long mAnalisisVisitsDateTime = 0L;
    int mClinicsID;
    String mComment;

    // для заполнения нового, если он открыт, как направление
    boolean thisIsANewDirectionsVisit = false; // для того, чтобы проидентифицировать вмзмт как вновь созданное направление
    int directionsListsPosition = 0; // для того, чтобы проидентифицировать вмзмт как вновь созданное направление
    int mReferencesID = 0;
    int directAnalisisTypeID = 0;

    public Boolean firstOpenDirection = false; // первое открытие нового направления

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));
/*
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        //int currentTheme = R.style.Theme_StandartGreen;
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME)) {
            // Получаем число из настроек
            int currentTheme = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_THEME, R.style.Theme_StandartGreen);
            setTheme(currentTheme);
        }
*/

        setContentView(R.layout.m_activity_analisis_visit);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        mToolBar = (Toolbar)findViewById(R.id.toolbar_analisis_visit_m);
        setSupportActionBar(mToolBar);

        mTabLayout = (TabLayout)findViewById(R.id.tabLayout_analisis_visit_m);
        mViewPager = (ViewPager) findViewById(R.id.viewPager_analisis_visit_m);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        mCurrentProfileID = getIntent().getIntExtra("currentProfileID", -1);  // нужно знать для какого профиля сохраняем данный визит

        gettingID = getIntent().getIntExtra("choosen_visits_id", 0);
        // установим значения, если визит открыт для изменения (откроем из БД по ID):
        Bundle bundleFilds = new Bundle();  // будем использовать для передачи в аргументы фрагмента
        bundleFilds.putInt("gettingID", gettingID);
        if (gettingID != 0) {
            bundleFilds = fDBMethods.getAnalisisVisitsFildsByAnalisisVisitsID(gettingID);
            mAnalisisVisitsDateTime = bundleFilds.getLong("mAnalisisVisitsMDateTime", 0L);
            mClinicsID = bundleFilds.getInt("mClinicsID", 0);
            mComment = bundleFilds.getString("mComment");
        }

        // визит может также быть открыт и для создания нового направления
        if (gettingID == 0){
            // визит новый, но открыт как новое направление, нужно его заполнить:
            mReferencesID = getIntent().getIntExtra("referenceID", 0);
            if (mReferencesID != 0){
                thisIsANewDirectionsVisit = true;
                directionsListsPosition = getIntent().getIntExtra("directionsListsPosition", 0);
                directAnalisisTypeID = getIntent().getIntExtra("directAnalisisTypeID", 0);
                bundleFilds.putInt("directAnalisisTypeID", directAnalisisTypeID);
                bundleFilds.putInt("mReferencesID", mReferencesID);
            }
        }
        //editAnalisisVisitsReference.setText(fDBMethods.getVisitsViewByID(mReferencesID));



        mVisitsViewPagerAdapter = new VisitsViewPagerAdapter(getSupportFragmentManager());

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        bundleFilds.putInt("gettingID", gettingID);
        bundleFilds.putInt("currentProfileID", mCurrentProfileID);

        if (getSupportFragmentManager().getFragments() == null) {
            fragmentAVMain = MAnalisisVisitMainFragment.newInstance(bundleFilds);
            fragmentAVAnalisesList = MAnalisisVisitAnaliseslistFragment.newInstance(bundleFilds);

            mVisitsViewPagerAdapter.addFragment(fragmentAVMain, getResources().getString(R.string.caption_basic));
            mVisitsViewPagerAdapter.addFragment(fragmentAVAnalisesList, getResources().getString(R.string.tab_caption_analisis_visit_analises));

        } else {
            fragmentAVMain = null;
            fragmentAVAnalisesList = null;

            try {
                fragmentAVMain = (MAnalisisVisitMainFragment) fragmentManager.getFragments().get(0);
            }catch (Exception e){}
            if (fragmentAVMain == null) {
                fragmentAVMain = MAnalisisVisitMainFragment.newInstance(bundleFilds);
            }
            mVisitsViewPagerAdapter.addFragment(fragmentAVMain, getResources().getString(R.string.caption_basic));

            try {
                fragmentAVAnalisesList = (MAnalisisVisitAnaliseslistFragment) fragmentManager.getFragments().get(1);
            }catch (Exception e){}
            if (fragmentAVAnalisesList == null) {
                fragmentAVAnalisesList = MAnalisisVisitAnaliseslistFragment.newInstance(bundleFilds);
            }
            mVisitsViewPagerAdapter.addFragment(fragmentAVAnalisesList, getResources().getString(R.string.tab_caption_analisis_visit_analises));

        }

        mViewPager.setAdapter(mVisitsViewPagerAdapter);
        mViewPager.setCurrentItem(0);
        mTabLayout.setupWithViewPager(mViewPager);

        //editAnalisisVisitsClinic = (AutoCompleteTextView) fragmentAVMain.getView().findViewById(R.id.editAnalisisVisitsMClinic);
        //editAnalisisVisitsClinic = fragmentAVMain.editAnalisisVisitsMClinic;


        //editAnalisisVisitsClinic = (AutoCompleteTextView) findViewById(R.id.editAnalisisVisitsMClinic);
        //editAnalisisVisitsComment = (EditText) findViewById(R.id.editAnalisisVisitsMComment);
        //tvAnalisisVisitsDate = (TextView) findViewById(R.id.tvAnalisisVisitsMDate);
        //tvAnalisisVisitsTime = (TextView) findViewById(R.id.tvAnalisisVisitsMTime);


        // скроем клавиатуру при листании
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                try {
                    //btnClose.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
                    //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                } catch (Throwable t) {}
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "onResume Визит-исследование (MAnalisisVisitActivity)");
        mTracker.setScreenName("onResume Визит-исследование (MAnalisisVisitActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_analisis_visit_m, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item == null){
            try {
                throw new NullPointerException("No menu item");
            } catch (NullPointerException e) {
                System.out.println(e.toString());
                e.printStackTrace();
                return false;
            }
        }

        int id = item.getItemId();

        switch (id){
            case R.id.av_action_img_OK:

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("OK AnalisisVisit.")
                        .build());

                analisisVisitMSave(true);
                return true;
            case R.id.av_action_img_Close:
                analisisVisitMClose();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void analisisVisitMClose() {
        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettingID); // для правильного позиционирования в main mcard списке
        intent.putExtra("currentItemType", Constants.DIRECTIONS_ANALISIS_TYPE_1); // для правильного позиционирования в main mcard списке
        intent.putExtra("thisIsANewDirectionsVisit", thisIsANewDirectionsVisit); // передаем параметр только для одного случая - если открыли новое
        // направление, но передумали и хотят закрыть без сохранения. Закроем в reference визите в onActivityResult
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public int analisisVisitMSave(boolean closeVisit) {

        // сначала запишем введенные символы в список "clinic" (если они еще не являются clinic - сделаем проверку по id)
        // проверим, содержится ли в БД
        // ведь user может и не нажать enter (тогда введенное слово не запишется в список), например рукой перевести курсор
        // в примечания. А теперь хочет записать.
        if (fragmentAVMain != null){
            String newClinic = fragmentAVMain.editAnalisisVisitsMClinic.getText().toString();
            if (!newClinic.isEmpty()){
                //String textClinic = newClinic.toLowerCase(Locale.getDefault());
                if (fDBMethods.hasDBClinic(newClinic) == false) {
                    // добавим введенное слово в БД
                    fragmentAVMain.mClinicsID = (int)fDBMethods.insertClinic(newClinic, "");
                }
            }else fragmentAVMain.mClinicsID = 0; // если стерли клинику, и хотят оставить ее пустой

            // сохраняем основную вкладку
            Bundle bundle = new Bundle();
            bundle.putLong("mAnalisisVisitsMDateTime", fragmentAVMain.mAnalisisVisitsMDateTime);
            bundle.putInt("mClinicsID", fragmentAVMain.mClinicsID);
//          bundle.putInt("mAnalisisTypesID", mAnalisisTypesID);
            bundle.putString("mComment", fragmentAVMain.editAnalisisVisitsMComment.getText().toString().trim());
//          bundle.putInt("mReferencesID", mReferencesID);
            bundle.putInt("mProfilisID", mCurrentProfileID);
            if (gettingID == 0) {
                gettingID = (int)fDBMethods.insertAnalisisVisit(bundle);
            } else {
                fDBMethods.updateAnalisisVisit(gettingID, bundle);
            }
        }

        // теперь фрагмент с листом исследований
        if (fragmentAVAnalisesList != null){
        ArrayList<MAnalisisVisitAnaliseslistFragment.analisesTypesListModel> analiseeTypesList = fragmentAVAnalisesList.analisesTypesList;
            if ((analiseeTypesList != null) && (analiseeTypesList.size() > 0)) {
                fDBMethods.updateAnalisisVisitsAnalisesList(gettingID, analiseeTypesList);
            }
        }

        Intent intent = new Intent();
        intent.putExtra("thisIsANewDirectionsVisit", thisIsANewDirectionsVisit);
        intent.putExtra("directionsListsPosition", directionsListsPosition);
        intent.putExtra("directionsVisitID", gettingID);
        intent.putExtra("currentItemID", gettingID); // для правильного позиционирования в main mcard списке
        setResult(RESULT_OK, intent);

        if (closeVisit == true){
            finish();
        }

        return gettingID;

    }

    // реализация интерфейсов
    @Override
    public int onGetSaveVisitCommand() {
        int visID = -1; // может быть ошибка при записи. Если так, то вернем "-1"
        try {
            visID = analisisVisitMSave(false);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return visID;
    }



}
