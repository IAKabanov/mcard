package ru.kai.mcard.presentation.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.kai.mcard.di.pack.PerFragments.PerListFragment;
import ru.kai.mcard.domain.interactor.CustDefaultSubscriber;
import ru.kai.mcard.domain.interactor.interactorSpecializations.ParamsSpecialization;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsDelete;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsGetList;
import ru.kai.mcard.domain.model.SingleDModel;
import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.presentations_model.mapper.SpecializationPMapper;
import ru.kai.mcard.presentation.view.fragment.ISpecializationsListFragment;

/**
 * Created by akabanov on 07.09.2017.
 *
 * Presenter must be independent on state of screen
 * before turning all filds must safe their statements? and
 * after turning restore them.
 * Как вариант: при первом создании активити она создаёт presenter через dagger, а при пересозданиях этой активити — берёт его уже из presenterManager.
 */

@PerListFragment
public class SpecializationsListPresenter implements IPresenter {

    // интерфейс с методами фрагмента (View). Через это поле вызываем методы фрагмента
    private ISpecializationsListFragment iSpecializationListView;

    private UseCaseSpecializationsGetList useCaseSpecGetList;
    private UseCaseSpecializationsDelete useCaseSpecDelete;
    private final SpecializationPMapper pMapper;

    public SpecializationsListPresenter(UseCaseSpecializationsGetList useCaseSpecGetList,
                                        UseCaseSpecializationsDelete useCaseSpecDelete,
                                        SpecializationPMapper pMapper){
        this.useCaseSpecGetList = useCaseSpecGetList;
        this.useCaseSpecDelete = useCaseSpecDelete;
        this.pMapper = pMapper;
    }

    public void setView(@NonNull ISpecializationsListFragment view){
        this.iSpecializationListView = view;
    }

    @Override
    public void resume() {}

    @Override
    public void pause() {}

    @Override
    public void destroy() {
        //this.useCaseSpecGetList = null;
        //this.pMapper = null;
        useCaseSpecGetList.dispose();
        useCaseSpecDelete.dispose();
        this.iSpecializationListView = null;
    }

    /**
     * Initializes the presenter by start retrieving the specialization list.
     */
    public void initialize() {
//        curPModel = new SinglePModel(Constants.CREATE_NEW_MODEL);
    }

    /**
     * Loads all specializations.
     */
    public void loadSpecializationsList() {
        this.showViewLoading();
        this.useCaseSpecGetList.execute(new SpecializationsListSelectAllSubscriber(), null);

    }

    public void createSpecialisation(){
        this.iSpecializationListView.createNewSpecialisation();
    }

    private void showViewLoading(){
        this.iSpecializationListView.showLoading();
    }

    private void hideViewLoading() {
        this.iSpecializationListView.hideLoading();
    }

    private void showErrorMessage(String errorMessage) {
        this.iSpecializationListView.showError(errorMessage);
    }

    private void showSpecializationsListInView(@Nullable List<BasePModel> pModelList){
        if (pModelList != null) {
            this.iSpecializationListView.renderSpecializationsList(pModelList);
        }
    }

    private final class SpecializationsListSelectAllSubscriber extends CustDefaultSubscriber<List<SingleDModel>> {

        @Override public void onNext(@NonNull List<SingleDModel> specializationsList) {
            List<BasePModel> pModelList = pMapper.transformFromDomainListModel(specializationsList);
            SpecializationsListPresenter.this.showSpecializationsListInView(pModelList);
        }

        @Override public void onComplete() {
            SpecializationsListPresenter.this.hideViewLoading();
        }

        @Override public void onError(Throwable e) {
            SpecializationsListPresenter.this.hideViewLoading();
            SpecializationsListPresenter.this.showErrorMessage(e.toString());
        }
    }

    public void deleteItem(@NonNull BasePModel pModel){
        this.showViewLoading();
        this.useCaseSpecDelete.execute(new SpecializationsListDeleteSubscriber(),
                ParamsSpecialization.forSpecialization(pMapper.transformToDomainModel(pModel)));

    }

    private final class SpecializationsListDeleteSubscriber extends CustDefaultSubscriber<Integer> {

        @Override public void onNext(@NonNull Integer specializationsList) {
            //List<SinglePModel> pModelList = pMapper.transformFromDomainListModel(specializationsList);
            //SpecializationsListPresenter.this.showSpecializationsListInView(pModelList);
        }

        @Override public void onComplete() {
            SpecializationsListPresenter.this.hideViewLoading();
        }

        @Override public void onError(Throwable e) {
            SpecializationsListPresenter.this.hideViewLoading();
            SpecializationsListPresenter.this.showErrorMessage("SpecializationsListPresenter: Delete: " + e.toString());
        }
    }


}
