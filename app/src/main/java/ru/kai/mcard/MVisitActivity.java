package ru.kai.mcard;


import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import ru.kai.mcard.utility.Common;

public class MVisitActivity extends AppCompatActivity implements MVisitDirectionsFragment.OnGetSaveVisitCommand, MVisitCuresFragment.OnGetSaveVisitCommand, MVisitRecommendationsFragment.OnGetSaveVisitCommand,
                                        MVisitMainFragment.OnGetSaveVisitCommand, MVisitMainFragment.OnGetVisitsID, MVisitDirectionsFragment.OnGetVisitsID, MVisitCuresFragment.OnGetVisitsID,
                                        MVisitRecommendationsFragment.OnGetVisitsID, MVisitDirectionsFragment.OnShowSnackbar{

    Toolbar mToolBar;
    TabLayout mTabLayout;
    private ViewPager mViewPager;
    VisitsViewPagerAdapter mVisitsViewPagerAdapter;

    // поля визита, которые будут сохранены в БД
    MVisitMainFragment fragmentVisitMmain;
    MVisitDirectionsFragment fragmentVisitMdirections;
    MVisitCuresFragment fragmentVisitMcures;
    MVisitRecommendationsFragment fragmentVisitMrecommendations;

    private DBMethods fDBMethods;

    int gettedID; // для идентификации визита и update или delete

    int mCurrentProfileID; // нужно знать для какого профиля сохраняем данный визит

    // поля для записи (реквизиты)
    Long mVisitsDateTime = 0L;
    int mClinicsID;
    int mDoctorsID;
    int mSpecializationsID;
    int mDiagnosisID;
    String mComment;
    int mReferencesID;

    Bundle bundleFilds;


    boolean thisIsANewDirectionsVisit = false; // для того, чтобы проидентифицировать вмзмт как вновь созданное направление
    int directionsListsPosition = 0; // для того, чтобы проидентифицировать вмзмт как вновь созданное направление
    Bundle visitsFragmentsBundle;

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_visit);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        mToolBar = (Toolbar) findViewById(R.id.toolbar_visit_m);
        setSupportActionBar(mToolBar);

        mTabLayout = (TabLayout) findViewById(R.id.tabLayout_visit_m);
        mViewPager = (ViewPager) findViewById(R.id.viewPager_visit_m);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        mCurrentProfileID = getIntent().getIntExtra("currentProfileID", -1);  // нужно знать для какого профиля сохраняем данный визит

        gettedID = getIntent().getIntExtra("choosen_visits_id", 0);
        // установим значения, если визит открыт для изменения (откроем из БД по ID):
        bundleFilds = new Bundle();  // будем использовать для передачи в аргументы фрагмента
        if (gettedID != 0) {
            bundleFilds = fDBMethods.getVisitsFildsByVisitsID(gettedID);
            mVisitsDateTime = bundleFilds.getLong("mVisitsDateTime", 0L);
            mClinicsID = bundleFilds.getInt("clinicsID", 0);
            mDoctorsID = bundleFilds.getInt("doctorsID", 0);
            mSpecializationsID = bundleFilds.getInt("specializationsID", 0);
            mDiagnosisID = bundleFilds.getInt("diagnosisID", 0);
            mComment = bundleFilds.getString("vComment");
            mReferencesID = bundleFilds.getInt("referencesID");
        } else {
            // визит новый, но открыт как новое направление, нужно его заполнить:
            mReferencesID = getIntent().getIntExtra("referenceID", 0);
            if (mReferencesID != 0) {
                thisIsANewDirectionsVisit = true;
                directionsListsPosition = getIntent().getIntExtra("directionsListsPosition", 0);
                mSpecializationsID = getIntent().getIntExtra("directSpecialisationsID", 0);
                mDiagnosisID = getIntent().getIntExtra("diagnosisID", 0);
                bundleFilds.putInt("diagnosisID", mDiagnosisID);
                bundleFilds.putInt("specializationsID", mSpecializationsID);
                bundleFilds.putInt("referencesID", mReferencesID);
            }
        }

        bundleFilds.putInt("gettedID", gettedID);
        bundleFilds.putInt("currentProfileID", mCurrentProfileID);

        //FragmentManager fragmentManager = getFragmentManager();
        //android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentManager fragmentManager = getSupportFragmentManager();

        mVisitsViewPagerAdapter = new VisitsViewPagerAdapter(getSupportFragmentManager());

        //fragmentManager.putFragment();

        if (getSupportFragmentManager().getFragments() == null) {
            // получаем экземпляр FragmentTransaction
            //android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //visitsFragmentsBundle = new Bundle();

            fragmentVisitMmain = MVisitMainFragment.newInstance(bundleFilds);
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMmain, getResources().getString(R.string.caption_basic));
            //fragmentTransaction.add(fragmentVisitMmain, "main");
            //fragmentManager.putFragment(visitsFragmentsBundle, "main", fragmentVisitMmain);

            fragmentVisitMdirections = MVisitDirectionsFragment.newInstance(bundleFilds);
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMdirections, getResources().getString(R.string.visit_but_directions));
            //fragmentTransaction.add(fragmentVisitMdirections, "directions");

            fragmentVisitMcures = MVisitCuresFragment.newInstance(bundleFilds);
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMcures, getResources().getString(R.string.visit_but_cures));
            //fragmentTransaction.add(fragmentVisitMcures, "cures");

            fragmentVisitMrecommendations = MVisitRecommendationsFragment.newInstance(bundleFilds);
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMrecommendations, getResources().getString(R.string.visit_tab_recommendations));
            //fragmentTransaction.add(fragmentVisitMrecommendations, "recommendations");

            //fragmentTransaction.commit();

        } else {

            fragmentVisitMmain = null;
            fragmentVisitMdirections = null;
            fragmentVisitMcures = null;
            fragmentVisitMrecommendations = null;
            //getFragmentManager().findFragmentById(R.layout.fragment_visit_m_main);
            //fragmentVisitMmain = (MVisitMainFragment) mVisitsViewPagerAdapter.getItem(0);
            //fragmentVisitMmain = (MVisitMainFragment) getSupportFragmentManager().getFragments().get(0);
            //fragmentVisitMmain = (MVisitMainFragment) fragmentManager.findFragmentByTag("main");
            //fragmentVisitMmain = (MVisitMainFragment) fragmentManager.getFragment(visitsFragmentsBundle, "main");
            try {
                fragmentVisitMmain = (MVisitMainFragment) fragmentManager.getFragments().get(0);
            }catch (Exception e){}
            if (fragmentVisitMmain == null) {
                fragmentVisitMmain = MVisitMainFragment.newInstance(bundleFilds);
            }
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMmain, getResources().getString(R.string.caption_basic));

            //fragmentVisitMdirections = (MVisitDirectionsFragment) getSupportFragmentManager().getFragments().get(1);
            //fragmentVisitMdirections = (MVisitDirectionsFragment) fragmentManager.findFragmentByTag("directions");
            try {
                fragmentVisitMdirections = (MVisitDirectionsFragment) fragmentManager.getFragments().get(1);
            }catch (Exception e){}
            if (fragmentVisitMdirections == null) {
                fragmentVisitMdirections = MVisitDirectionsFragment.newInstance(bundleFilds);
            }
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMdirections, getResources().getString(R.string.visit_but_directions));

            //fragmentVisitMcures = (MVisitCuresFragment) getSupportFragmentManager().getFragments().get(2);
            //fragmentVisitMcures = (MVisitCuresFragment) fragmentManager.findFragmentByTag("cures");
            try {
                fragmentVisitMcures = (MVisitCuresFragment) fragmentManager.getFragments().get(2);
            }catch (Exception e){}
            if (fragmentVisitMcures == null) {
                fragmentVisitMcures = MVisitCuresFragment.newInstance(bundleFilds);
            }
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMcures, getResources().getString(R.string.visit_but_cures));

            //fragmentVisitMrecommendations = (MVisitRecommendationsFragment) getSupportFragmentManager().getFragments().get(3);
            //fragmentVisitMrecommendations = (MVisitRecommendationsFragment) fragmentManager.findFragmentByTag("recommendations");
            try {
                fragmentVisitMrecommendations = (MVisitRecommendationsFragment) fragmentManager.getFragments().get(3);
            }catch (Exception e){}
            if (fragmentVisitMrecommendations == null) {
                fragmentVisitMrecommendations = MVisitRecommendationsFragment.newInstance(bundleFilds);
            }
            mVisitsViewPagerAdapter.addFragment(fragmentVisitMrecommendations, getResources().getString(R.string.visit_tab_recommendations));

        }

        mViewPager.setAdapter(mVisitsViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


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
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Визит (MVisitActivity) onResume");
        mTracker.setScreenName("Визит (MVisitActivity) onResume");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
/*
        if (cursorClinics != null)
            cursorClinics.close();
        if (cursorDoctors != null)
            cursorDoctors.close();
        if (cursorSpecializations != null)
            cursorSpecializations.close();
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_visit_m, menu);
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

        switch (id) {
            case R.id.action_img_OK:

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("OK Визит.")
                        .build());

                VisitMSave(true); // сохраним с закрытием
                return true;
            case R.id.action_img_Close:
                VisitMClose();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void VisitMClose() {
        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID); // для правильного позиционирования в main mcard списке
        intent.putExtra("currentItemType", Constants.DIRECTIONS_VISITS_TYPE_0); // для правильного позиционирования в main mcard списке
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    // для нового визита если в фрагментах потребуется сохранение, то сохраним и вернем во фрагмент id сохраненного нового визита
    public int VisitMSave(boolean closeVisit) {

        // сначала запишем введенные символы в список "clinic" (если они еще не являются clinic - сделаем проверку по id)
        // проверим, содержится ли в БД
        // ведь user может и не нажать enter (тогда введенное слово не запишется в список), например рукой перевести курсор
        // в примечания. А теперь хочет записать.

/*
        if ((fragmentVisitMmain.editVisitsClinic != null)&&
                (fragmentVisitMmain.editVisitsDoctor != null)&&
                (fragmentVisitMmain.editVisitsSpecialization != null)&&
                (fragmentVisitMmain.editVisitsDiagnosis != null)&&
                (fragmentVisitMmain.editVisitsComment != null)) {
*/

        if (fragmentVisitMmain != null){
            String newClinic = fragmentVisitMmain.editVisitsClinic.getText().toString();
            if (!newClinic.isEmpty()) {
                //String textClinic = newClinic.toLowerCase(Locale.getDefault());
                if (fDBMethods.hasDBClinic(newClinic) == false) {
                    // добавим введенное слово в БД
                    fragmentVisitMmain.mClinicsID = (int) fDBMethods.insertClinic(newClinic, "");
                }else {
                    fragmentVisitMmain.mClinicsID = fDBMethods.getIDClinicsByName(newClinic);
                }
            }else {
                fragmentVisitMmain.mClinicsID = 0;
            }

            String newDoctor = fragmentVisitMmain.editVisitsDoctor.getText().toString();
            if (!newDoctor.isEmpty()) {
                //String textClinic = newClinic.toLowerCase(Locale.getDefault());
                if (fDBMethods.hasDBDoctor(newDoctor) == false) {
                    // добавим введенное слово в БД
                    fragmentVisitMmain.mDoctorsID = (int) fDBMethods.insertDoctor(newDoctor, mSpecializationsID);
                }else {
                    fragmentVisitMmain.mDoctorsID = fDBMethods.getIDDoctorsByName(newDoctor);
                }
            }else {
                fragmentVisitMmain.mDoctorsID = 0;
            }

            String newSpecialization = fragmentVisitMmain.editVisitsSpecialization.getText().toString();
            if (!newSpecialization.isEmpty()) {
                //String textClinic = newClinic.toLowerCase(Locale.getDefault());
                if (fDBMethods.hasDBSpecialization(newSpecialization) == false) {
                    // добавим введенное слово в БД
                    fragmentVisitMmain.mSpecializationsID = (int) fDBMethods.insertSpecialization(newSpecialization);
                }else {
                    fragmentVisitMmain.mSpecializationsID = fDBMethods.getIDSpecializationsByName(newSpecialization);
                }
            }else {
                fragmentVisitMmain.mSpecializationsID = 0;
            }

            String newDiagnosis = fragmentVisitMmain.editVisitsDiagnosis.getText().toString();
            if (!newDiagnosis.isEmpty()) {
                //String textClinic = newClinic.toLowerCase(Locale.getDefault());
                if (fDBMethods.hasDBDiagnoses(newDiagnosis) == false) {
                    // добавим введенное слово в БД
                    fragmentVisitMmain.mDiagnosisID = (int) fDBMethods.insertDiagnoses(newDiagnosis);
                }else {
                    fragmentVisitMmain.mDiagnosisID = fDBMethods.getIDDiagnosesByName(newDiagnosis);
                }
            }else {
                fragmentVisitMmain.mDiagnosisID = 0;
            }

            // сохраняем основную вкладку
            Bundle bundle = new Bundle();
            bundle.putLong("mVisitsDateTime", fragmentVisitMmain.mVisitsDateTime);
            bundle.putInt("clinicID", fragmentVisitMmain.mClinicsID);
            bundle.putInt("doctorID", fragmentVisitMmain.mDoctorsID);
            bundle.putInt("specializationID", fragmentVisitMmain.mSpecializationsID);
            bundle.putInt("diagnosisID", fragmentVisitMmain.mDiagnosisID);
            bundle.putInt("referencesID", fragmentVisitMmain.mReferencesID);
            //bundle.putString("vComment", fragmentVisitMmain.mComment);
            bundle.putString("vComment", fragmentVisitMmain.editVisitsComment.getText().toString());

            bundle.putInt("profileID", mCurrentProfileID);
            if (gettedID == 0) {
                gettedID = (int) fDBMethods.insertVisit(bundle);
            } else {
                fDBMethods.updateVisit(gettedID, bundle);
            }
        }

        // теперь фрагмент с листом исследований
        if (fragmentVisitMdirections != null){
            ArrayList<MVisitDirectionsFragment.VisitsDirectionsModel> directionsList = fragmentVisitMdirections.directionsList;
            if ((directionsList != null) && (directionsList.size() > 0)) {
                fDBMethods.updateVisitsDirectionsList(gettedID, directionsList);
            }
        }

        // теперь фрагмент с листом назначений
        if (fragmentVisitMcures != null){
            ArrayList<MVisitCuresFragment.VisitsCuresModel> curesList = fragmentVisitMcures.curesList;
            if ((curesList != null) && (curesList.size() > 0)) {
                fDBMethods.updateVisitsCuresList(gettedID, curesList);
            }
        }

        // теперь фрагмент с листом рекомендаций
        if (fragmentVisitMrecommendations != null){
            ArrayList<MVisitRecommendationsFragment.VisitsRecommendationsModel> recommendationsList = fragmentVisitMrecommendations.recommendationsList;
            if ((recommendationsList != null) && (recommendationsList.size() > 0)) {
                fDBMethods.updateVisitsRecommendationsList(gettedID, recommendationsList);
            }
        }

        Intent intent = new Intent();
        intent.putExtra("thisIsANewDirectionsVisit", thisIsANewDirectionsVisit);
        intent.putExtra("directionsListsPosition", directionsListsPosition);
        intent.putExtra("directionsVisitID", gettedID);
        intent.putExtra("currentItemID", gettedID); // для правильного позиционирования в main mcard списке
        setResult(RESULT_OK, intent);

        if (closeVisit == true){
            finish();
        }

        return gettedID;
    }

    // реализация интерфейсов
    @Override
    public int onGetSaveVisitCommand() {
        int visID = VisitMSave(false);

        return visID;
    }

    @Override
    public int onGetVisitsID() {
        return gettedID;
    }

    @Override
    public int onShowSnackbar(String message) {

        Snackbar mSnackbar = Snackbar.make(mViewPager, message, Snackbar.LENGTH_LONG);
        View mSnackbarView = mSnackbar.getView();

        int alternativeThemeColor = Common.getAlternativeThemeColor(getApplicationContext());
        mSnackbarView.setBackgroundColor(alternativeThemeColor);
        mSnackbar.show();

        return 0;
    }
}