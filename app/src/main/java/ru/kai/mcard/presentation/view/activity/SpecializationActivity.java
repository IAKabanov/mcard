package ru.kai.mcard.presentation.view.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.squareup.leakcanary.RefWatcher;

import java.util.Objects;

import javax.inject.Inject;

import ru.kai.mcard.Constants;
import ru.kai.mcard.MCardApplication;
import ru.kai.mcard.di.pack.Application.IAppComponent;
import ru.kai.mcard.di.pack.PerActivity.DaggerIListDetailActivityComponent;
import ru.kai.mcard.di.pack.PerActivity.IListDetailActivityComponent;
import ru.kai.mcard.di.pack.PerActivity.ListDetailActivityModule;
import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.presenter.ActivityListDetailPresenter;
import ru.kai.mcard.presentation.view.animation.IDismissible;
import ru.kai.mcard.presentation.view.fragment.ISpecializationsDetailsFragment;
import ru.kai.mcard.presentation.view.fragment.ISpecializationsListFragment;
import ru.kai.mcard.presentation.view.fragment.SpecializationsDetailsFragment;
import ru.kai.mcard.utility.Common;
import ru.kai.mcard.R;
import ru.kai.mcard.presentation.view.fragment.SpecializationsListFragment;
import ru.kai.mcard.utility.Utility;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SpecializationActivity extends AppCompatActivity implements IListDetailActivityView{

    private IListDetailActivityComponent component;
    @Inject
    ActivityListDetailPresenter presenter;

    private int receivedID = -1; // полученный ID - определяем, открыли активность для подбора (if receivedID != -1) или просто посмотреть (if receivedID == -1).
    Boolean forPick; // открыли активность для подбора или просто посмотреть.
                     // это нужно для правильной реакции на "тапы" списка.

    private FragmentManager fragmentManager; // = getFragmentManager();
    private final String TAGList = Constants.TAGList;
    private final String TAGDetails = Constants.TAGDetails;
    private String curFragmentTAG; // для определения текущего фрагмента в портретной ориентации

    private Toolbar toolbar;

    private ISpecializationsDetailsFragment iSpecializationsDetailsFragment;
    private ISpecializationsListFragment iSpecializationsListFragment;

    private final String TAG = SpecializationActivity.class.getSimpleName();

    private int curItemPosition = 0;
    private BasePModel curPModel;// = new SinglePModel(-2); // нужна, чтобы запомнить перед поворотом и затем отобразить правильно,
                                                                    // а еще, чтобы правильно отображать иконки меню.
                                                                    // Constants.MODEL_IS_NULL - модели просто нет. Для отображения "Выберите элемент" - depricated

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        IAppComponent appComponent = MCardApplication.getInstance().getAppComponent();
        component = DaggerIListDetailActivityComponent.builder()
                .listDetailActivityModule(new ListDetailActivityModule(SpecializationActivity.this))
                .iAppComponent(appComponent)
                .build();
        component.inject(this);
        this.presenter.initialize();
        this.presenter.setView(this);

        fragmentManager = getFragmentManager();

        setContentView(R.layout.specialization_activity);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.mSpecializationToolbar);
        toolbar = (Toolbar) findViewById(R.id.mSpecializationToolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Log.w(TAG, " onCreate = " + SpecializationActivity.this.toString());

        // fragments

        // сначала удаляем подвисшие (баг какой-то)
        Fragment lf = fragmentManager.findFragmentByTag("listFragment");
        if (lf != null){
            fragmentManager.beginTransaction().remove(lf).commit();
        }

        BasePModel gettedPModel = null;

        // восстановление после поворота
        if (savedInstanceState != null){
            if (savedInstanceState.containsKey("specializationPModel")) {
                gettedPModel = (BasePModel) savedInstanceState.getSerializable("specializationPModel");
            }
            if (savedInstanceState.containsKey("curItemPosition")) {
                curItemPosition = savedInstanceState.getInt("curItemPosition");
            }
        }

        BasePModel newPModel = new BasePModel(Constants.CREATE_NEW_MODEL); // просто новая модель для вновь открытой this для: если список не пустой, то в detail будет отображаться первый item, иначе - новая
        curPModel = gettedPModel == null ? newPModel : gettedPModel;
        if (Utility.isThisOrientationPort(SpecializationActivity.this)) {
            if (savedInstanceState == null) {
                initializeListFragment(R.id.SpecializationActivityContainerList, curItemPosition);
            }else {
                // возврат после поворота; отобразим DetailFragment, если модель не первоначально пустая:
                initializeListFragment(R.id.SpecializationActivityContainerList, curItemPosition);
                replaceFragmentsFromListToDetails(curPModel);
            }
        } else { // ландшафт
            initializeListFragment(R.id.SpecializationActivityContainerList, curItemPosition);
            initializeDetailsFragment(R.id.SpecializationActivityContainerDetail, curPModel);
        }

        // чтобы при появлении клавиатуры toolbar не "прыгал".
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
/*
        SpecializationsDetailsFragment detailsFragment = (SpecializationsDetailsFragment) fragmentManager.findFragmentByTag(TAGDetails);
        if (detailsFragment != null){
            outState.putSerializable("specializationPModel", detailsFragment.getCurModel());
        }
*/
        outState.putSerializable("specializationPModel", curPModel);
        if (iSpecializationsListFragment != null){
            outState.putInt("curItemPosition", iSpecializationsListFragment.getCurItemPosition());
        }else {
            outState.putInt("curItemPosition", 0);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (Utility.isThisOrientationPort(SpecializationActivity.this) && curFragmentTAG.equals(TAGList)){

        }else{
            getMenuInflater().inflate(R.menu.menu_general, menu);
        }
        return true;
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Utility.isThisOrientationPort(SpecializationActivity.this) && curFragmentTAG.equals(TAGList)){
            menu.clear();
        }
        if (Utility.isThisOrientationPort(SpecializationActivity.this)){
            if (curFragmentTAG.equals(TAGDetails)){
                menu.removeItem(R.id.general_img_choice);
                if (menu.size() == 1) {
                    menu.add(0, 1, 0, R.id.general_img_OK);
                }
            }
        }else{
            if (menu.size() == 1) {
                menu.add(0, 1, 0, R.id.general_img_OK);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.general_img_OK:
                saveItem(); // сохраним с закрытием
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0);
                }

                break;
            case R.id.general_img_Close:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    protected void onStop() {
        Log.w(TAG, " onStop = " + SpecializationActivity.this.toString());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, " onDestroy = " + SpecializationActivity.this.toString());
        this.presenter.destroy();
        this.component = null;
        this.iSpecializationsDetailsFragment = null;
        this.iSpecializationsListFragment = null;
        this.curPModel = null;
        this.fragmentManager = null;

        Runtime.getRuntime().gc();
        super.onDestroy();

    }

    // fonts - calligraphy library
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // аппаратная кнопка назад
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (!Utility.isThisOrientationPort(SpecializationActivity.this)){

            //fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            SpecializationActivity.this.finish();
        }else {
            if(curFragmentTAG == TAGList){ // если лист, то закроем активити совсем
                //fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                SpecializationActivity.this.finish();
            }else{

                // анимация из Detail в List
                //From the Activity call this in e.g. onBackPress
                SpecializationsDetailsFragment detailFragment = (SpecializationsDetailsFragment) fragmentManager.findFragmentByTag(TAGDetails);
                if (detailFragment != null){
                ((IDismissible) detailFragment).dismiss(new IDismissible.OnDismissedListener() {
                    @Override
                    public void onDismissed() {
                        //fragmentManager.popBackStack();
                        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.SpecializationActivityContainerDetail);
                        frameLayout.setVisibility(View.INVISIBLE);
                        SpecializationsDetailsFragment detailFragment = (SpecializationsDetailsFragment) fragmentManager.findFragmentByTag(TAGDetails);
                        if (detailFragment != null){
                            fragmentManager.beginTransaction().remove(detailFragment).commit();
                        }
                        frameLayout.setVisibility(View.VISIBLE);
                    }
                });}

                if (iSpecializationsListFragment != null) {
                    iSpecializationsListFragment.setCurItemPosition(curItemPosition);
                }
                curFragmentTAG = TAGList;
                iSpecializationsDetailsFragment = null;
                invalidateOptionsMenu();
            }
        }
        //super.onBackPressed();
    }

    private void initializeListFragment(int container, int curItemPosition){
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment listFragment = new SpecializationsListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("curItemPosition", curItemPosition);
        listFragment.setArguments(bundle);
        ft.add(container, listFragment, TAGList);
        //ft.addToBackStack(TAGList);
        this.iSpecializationsListFragment = (ISpecializationsListFragment) listFragment;
        ft.commit();
        curFragmentTAG = TAGList;
        /* Log.w(TAG, " initializeListFragment = " + listFragment.toString()); */
    }

    private void initializeDetailsFragment(int container, BasePModel pModel){
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment detailsFragment = new SpecializationsDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("specializationPModel", pModel);
        detailsFragment.setArguments(bundle);
        ft.add(container, detailsFragment, TAGDetails);
        //ft.addToBackStack(TAGDetails);
        ft.commit();
        curFragmentTAG = TAGDetails;
//        curPModel = pModel; // - ???????????????????
        this.iSpecializationsDetailsFragment = (ISpecializationsDetailsFragment) detailsFragment;
        //Log.w(TAG, " initializeDetailsFragment = " + detailsFragment.toString());
    }

    private void replaceFragmentsFromListToDetails(BasePModel pModel){
        if (Utility.isThisOrientationPort(SpecializationActivity.this)) {

            final FragmentTransaction ft = fragmentManager.beginTransaction();
            Fragment detailsFragment = new SpecializationsDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("specializationPModel", pModel);

            // animation
            if (pModel.getModelID() == Constants.CREATE_NEW_MODEL){
                if (this.iSpecializationsListFragment != null){
                    bundle.putParcelable("ARG_REVEAL_SETTINGS",this.iSpecializationsListFragment.getRevealSettings());
                }
            }

            detailsFragment.setArguments(bundle);
            ft.replace(R.id.SpecializationActivityContainerDetail, detailsFragment, TAGDetails);
/*
            ft.replace(R.id.SpecializationActivityContainerList, detailsFragment, TAGDetails);
            ft.addToBackStack(null);
*/
            ft.commit();
            curFragmentTAG = TAGDetails;
//            curPModel = pModel;
            this.iSpecializationsDetailsFragment = (ISpecializationsDetailsFragment) detailsFragment;
        }
    }

    private void showItemDetail(BasePModel pModel){
        curPModel = pModel;
        if (Utility.isThisOrientationPort(SpecializationActivity.this)) {
            replaceFragmentsFromListToDetails(pModel);
            invalidateOptionsMenu();
        } else {
            // ландшафт. Поэтому просто перерисуем фрагмент детализации
            if (this.iSpecializationsDetailsFragment != null){
                renderSingleDetailsFragment(pModel);
            }
            // old version
            //SpecializationsDetailsFragment detailsFragment = (SpecializationsDetailsFragment)fragmentManager.findFragmentByTag(TAGDetails);
            //if (detailsFragment != null) {
            //detailsFragment.presenter.showSpecializationsDetailsInView(pModel);
            //}
        }

    }

    // реализация интерфейса List фрагмента
    @Override
    public void onFabClickListener() {
        curPModel = new BasePModel(Constants.CREATE_NEW_MODEL);
        showItemDetail(curPModel);
        invalidateOptionsMenu();
        if(iSpecializationsListFragment != null){
            //if (iSpecializationsListFragment.getCollection().size() > 0) {
//                iSpecializationsListFragment.setPositionCurPModel(0);
            //}
        }
    }

    @Override
    public void onItemBrowsingListener(BasePModel pModel) {
        showItemDetail(pModel);
    }

    private void saveItem() {
        // записываем элемент в БД.
        if(iSpecializationsDetailsFragment != null){
            curPModel = iSpecializationsDetailsFragment.getCurrentPModelName();
        }
        if (curPModel != null) {
            presenter.saveItem(curPModel);
            // Если портрет, то закрываем details fragment, а в списке подсвечиваем соответствующую строку
            if (Utility.isThisOrientationPort(SpecializationActivity.this)){
                closeDetailFragment();
            }else { // Если ландшафт, то подсвечиваем в списке строку.
                iSpecializationsListFragment.notifyUpdatedItem(curPModel);
            }
        }
    }

    private void closeDetailFragment(){
        this.iSpecializationsDetailsFragment = null;
        //fragmentManager.popBackStack();
        final SpecializationsDetailsFragment detailFragment = (SpecializationsDetailsFragment) fragmentManager.findFragmentByTag(TAGDetails);
        if (detailFragment != null){
            fragmentManager.beginTransaction().remove(detailFragment).commit();
        }
    }

    // SingleRowRVAdapter.IOnRVAdapterItemClickListener. Need show detail data in DetailFragment
    @Override
    public void onRVAdapterItemClick(BasePModel pModel, int position) {
        //Toast.makeText(this, "ModelID = " + pModel.getModelID() + ", Position = " + position, Toast.LENGTH_SHORT).show();
        curPModel = pModel;
        if (iSpecializationsListFragment != null){
            iSpecializationsListFragment.setCurItemPosition(position);
        }
        if (receivedID == -1){ // activity открыли для просмотра
            if (!Utility.isThisOrientationPort(SpecializationActivity.this)){
                // открываем по "тапу" DetailFragment отлько из ландшафта; из портрета открываем по swipe из интерфейса фрагмента
                showItemDetail(pModel);
                startChoiceIconAnimation();
            }
        }else { // activity открыли для подбора.

        }
    }

    private void startChoiceIconAnimation(){
        final Animation shakeanimation = AnimationUtils.loadAnimation(this, R.anim.shake_choice_icon); // анимация иконке при тапе на листе в ландшафте
        ActionMenuItemView choiceIcon = (ActionMenuItemView) findViewById(R.id.general_img_choice); // for animation
        if (choiceIcon != null){
            choiceIcon.startAnimation(shakeanimation);
        }
    }

    @Override
    public void showLoading() {
        if (this.iSpecializationsDetailsFragment != null){
            this.iSpecializationsDetailsFragment.showLoading();
        }
    }

    @Override
    public void hideLoading() {
        if (this.iSpecializationsDetailsFragment != null){
            this.iSpecializationsDetailsFragment.hideLoading();
        }
    }

    @Override
    public void showError(String message) {
        if (this.iSpecializationsDetailsFragment != null){
            this.iSpecializationsDetailsFragment.showError(message);
        }
    }

    @Override
    public void renderSingleDetailsFragment(@NonNull BasePModel pModel) {
        if (this.iSpecializationsDetailsFragment != null){
            this.iSpecializationsDetailsFragment.renderSpecializationsDetails(pModel);
        }
    }

    @NonNull
    @Override
    public BasePModel getCurrentPModel() {
        return curPModel;
    }

    @Override  // первоначально при загрузке списка в адаптер
    public void setCurPModel(@NonNull BasePModel pModel) {
        this.curPModel = pModel;
    }

    @Override  // первоначально при загрузке списка в адаптер
    public void setCurPModelInitially(@NonNull BasePModel pModel) {
        if (curPModel.getModelID() < 0){
            this.curPModel = pModel;
            // обновим DetailView
            renderSingleDetailsFragment(pModel);
        }
    }
}
