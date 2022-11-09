package ru.kai.mcard.presentation.presenter;

import android.support.annotation.NonNull;

import ru.kai.mcard.Constants;
import ru.kai.mcard.domain.interactor.CustDefaultSubscriber;
import ru.kai.mcard.domain.interactor.interactorSpecializations.ParamsSpecialization;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsCreate;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsGetSingle;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsUpdate;
import ru.kai.mcard.domain.model.SingleDModel;
import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.presentations_model.mapper.SpecializationPMapper;
import ru.kai.mcard.presentation.view.activity.IListDetailActivityView;

/**
 * Created by akabanov on 07.09.2017.
 *
 * Presenter must be independent on state of screen
 * before turning all filds must safe their statements? and
 * after turning restore them.
 * Как вариант: при первом создании активити она создаёт presenter через dagger, а при пересозданиях этой активити — берёт его уже из presenterManager.
 */

public class ActivityListDetailPresenter implements IPresenter {

    // интерфейс с методами фрагмента (View). Через это поле вызываем методы фрагмента
    private IListDetailActivityView iListDetailActivityView;

    private UseCaseSpecializationsGetSingle useCaseSpecGetSingle;
    private UseCaseSpecializationsCreate useCaseSpecCreate;
    private UseCaseSpecializationsUpdate useCaseSpecUpdate;
    private final SpecializationPMapper pMapper;

    public ActivityListDetailPresenter(UseCaseSpecializationsGetSingle useCaseSpecGetSingle,
                                       UseCaseSpecializationsCreate useCaseSpecCreate,
                                       UseCaseSpecializationsUpdate useCaseSpecUpdate,
                                       SpecializationPMapper pMapper){
        this.useCaseSpecGetSingle = useCaseSpecGetSingle;
        this.useCaseSpecCreate = useCaseSpecCreate;
        this.useCaseSpecUpdate = useCaseSpecUpdate;
        this.pMapper = pMapper;
    }

    public void setView(@NonNull IListDetailActivityView view){
        this.iListDetailActivityView = view;
    }

    @Override
    public void resume() {}

    @Override
    public void pause() {}

    @Override
    public void destroy() {
        this.iListDetailActivityView = null;
        useCaseSpecGetSingle.dispose();
        useCaseSpecCreate.dispose();
        useCaseSpecUpdate.dispose();
        //this.useCaseSpecGetList = null;
        //this.pMapper = null;
    }

    /**
     * Initializes the presenter by start retrieving the specialization list.
     */
    public void initialize() {
    }

    public void createSpecialisation(){
//        this.iListDetailActivityView.createNewSpecialisation();
    }

    private void showViewLoading(){
        this.iListDetailActivityView.showLoading();
    }

    private void hideViewLoading() {
        this.iListDetailActivityView.hideLoading();
    }

    private void showErrorMessage(String errorMessage) {
        this.iListDetailActivityView.showError(errorMessage);
    }


    // даем команду на отображение одной единицы модели (сущности) в DetailsFragment через Activity
    private void showSingleDetailInView(@NonNull BasePModel pModel){
        this.iListDetailActivityView.renderSingleDetailsFragment(pModel);
    }

    //******************************************************************************
    /**
     * Load single detail. Show it we will be by  SingleDetailLoaderSubscriber -> onNext
     */
/*
    private void loadSingleDetails() {  // ?????????????????????????????  где используется??????????
        this.showViewLoading();
        this.useCaseSpecGetSingle.execute(new SingleDetailLoaderSubscriber(),
                ParamsSpecialization.forSpecialization(pMapper.transformToDomainModel(curPModel)));
    }
*/

    // Subscriber для получения одной единицы модели (сущности)
    private final class SingleDetailLoaderSubscriber extends CustDefaultSubscriber<SingleDModel> {

        @Override
        public void onNext(@NonNull SingleDModel dModel) {
            BasePModel pModel = pMapper.transformFromDomainModel(dModel);
            ActivityListDetailPresenter.this.showSingleDetailInView(pModel); // показать полученную модель in View
        }

        @Override
        public void onComplete() {
            ActivityListDetailPresenter.this.hideViewLoading();
        }

        @Override
        public void onError(Throwable e) {
            ActivityListDetailPresenter.this.hideViewLoading();
            ActivityListDetailPresenter.this.showErrorMessage("ActivityListDetailPresenter: Load: " + e.toString());
        }
    }

    //******************************************************************************
    /**
     * Create single detail.
     */
    private final class SingleDetailCreateSubscriber extends CustDefaultSubscriber<Integer> {

        @Override public void onNext(@NonNull Integer curModelsID) {
            // при создании получили новый ID модели, обновим везде модель:
            BasePModel curPModel = iListDetailActivityView.getCurrentPModel();
            curPModel.setModelID(curModelsID);
            iListDetailActivityView.setCurPModel(curPModel);

            ActivityListDetailPresenter.this.showSingleDetailInView(curPModel);
        }

        @Override public void onComplete() {
            ActivityListDetailPresenter.this.hideViewLoading();
        }

        @Override public void onError(Throwable e) {
            ActivityListDetailPresenter.this.hideViewLoading();
            ActivityListDetailPresenter.this.showErrorMessage("ActivityListDetailPresenter: Create: " + e.toString());
        }
    }

    //******************************************************************************
    /**
     * Update single detail.
     */
    private final class SingleDetailUpdateSubscriber extends CustDefaultSubscriber<Integer> {

        @Override public void onNext(@NonNull Integer curModelsID) {
            //ActivityListDetailPresenter.this.curPModel.setModelID(curModelsID);
            //ActivityListDetailPresenter.this.showSingleDetailInView(curPModel);
        }

        @Override public void onComplete() {
            ActivityListDetailPresenter.this.hideViewLoading();
        }

        @Override public void onError(Throwable e) {
            ActivityListDetailPresenter.this.hideViewLoading();
            ActivityListDetailPresenter.this.showErrorMessage("ActivityListDetailPresenter: Update: " + e.toString());
        }
    }

    public void saveItem(BasePModel pModel){
        if (pModel.getModelID() == -1){
            // записываем элемент в БД.
            this.showViewLoading();
            this.useCaseSpecCreate.execute(new SingleDetailCreateSubscriber(),
                    ParamsSpecialization.forSpecialization(pMapper.transformToDomainModel(pModel)));
        }else {
            this.showViewLoading();
            this.useCaseSpecUpdate.execute(new SingleDetailUpdateSubscriber(),
                    ParamsSpecialization.forSpecialization(pMapper.transformToDomainModel(pModel)));

        }
/*
        if (this.iListDetailActivityView.isThisOrientationPort()) {
            // Если портрет, то закрываем details fragment, а в списке подсвечиваем соответствующую строку
            this.iListDetailActivityView.closeThisFragment();
        }else {
            // Если ландшафт, то подсвечиваем в списке строку, а вместо details fragment прорисовываем заглушку.
        }
*/
    }



}
