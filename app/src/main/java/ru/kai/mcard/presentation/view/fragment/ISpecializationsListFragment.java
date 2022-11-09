package ru.kai.mcard.presentation.view.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.view.ICommonLoadDataToView;
import ru.kai.mcard.presentation.view.animation.RevealAnimationSetting;

/**
 * Created by akabanov on 07.09.2017.
 *
 * Интерфейс-контракт. Имплементирован в лист-фрагменте.
 * Используется в лист-презентере, чтобы из презентера дергать методы фрагмента
 */

public interface ISpecializationsListFragment extends ICommonLoadDataToView {
    /**
     * Render a user list in the UI.
     *
     * @param pModels The collection of {@link BasePModel} that will be shown.
     */
    void renderSpecializationsList(@NonNull List<BasePModel> pModels); // загрузить из презентера

    void loadSpecializationsList(); // команда обновить список. При инициализации. Также для активити - обновить список после создания нового

    void notifyUpdatedItem(BasePModel pModel); // для активити - обновить текущий item списка после update-а

    void createNewSpecialisation();

    int getCurItemPosition();

    void setCurItemPosition(int curItemPosition);

    RevealAnimationSetting getRevealSettings(); // получить параметры для анимации нажатия на fab

    //void setPositionCurPModel(int curItemPosition);

}
