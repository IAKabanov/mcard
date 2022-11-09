package ru.kai.mcard.presentation.view.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.view.ICommonLoadDataToView;

/**
 * Created by akabanov on 07.09.2017.
 *
 * Интерфейс-контракт. Имплементирован в SpecializationActivity.
 * Используется как поле в ActivityListDetailPresenter, чтобы из презентера дергать методы SpecializationActivity
 */

public interface IListDetailActivityView extends ICommonLoadDataToView {

    //***************************************************************************
    // for Presenter
    /**
     *  Метод для того, чтобы дать команду отобразить полученную (обновленную модель).
     *  В презентере создали новую или update. Через этот метод из презентера передаем полученную модель в Activity.
     *  Там уже передадим модель и команду отобразить ее в DetailsFragment.
    */
    void renderSingleDetailsFragment(@NonNull BasePModel pModel);

    //***************************************************************************
    // for Fragments

    void onFabClickListener();

    void onItemBrowsingListener(BasePModel pModel);

    void setCurPModel(@NonNull BasePModel pModel);

    void setCurPModelInitially(@NonNull BasePModel pModel);

    void onRVAdapterItemClick(BasePModel pModel, int position);

    @NonNull
    BasePModel getCurrentPModel();

}
