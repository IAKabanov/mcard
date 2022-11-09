package ru.kai.mcard.presentation.view.fragment;

import android.support.annotation.NonNull;

import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.view.ICommonLoadDataToView;

/**
 * Created by akabanov on 07.09.2017.
 */

public interface ISpecializationsDetailsFragment extends ICommonLoadDataToView {
    /**
     * Render a user list in the UI.
     *
     * @param pModel The collection of {@link BasePModel} that will be shown.
     */
    void renderSpecializationsDetails(@NonNull BasePModel pModel);

    @NonNull
    BasePModel getCurrentPModelName();

}
