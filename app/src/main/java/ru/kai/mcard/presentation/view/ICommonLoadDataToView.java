package ru.kai.mcard.presentation.view;

import android.content.Context;
import android.support.annotation.Nullable;

import ru.kai.mcard.presentation.presentations_model.BasePModel;

/**
 * Created by akabanov on 07.09.2017.
 * Родительский интерфейс для интерфейсов взаимодействия презентеров с фрагментами
 */

public interface ICommonLoadDataToView {
    /**
     * Show a view with a progress bar indicating a loading process.
     */
    void showLoading();

    /**
     * Hide a loading view.
     */
    void hideLoading();

    /**
     * Show an error message
     *
     * @param message A string representing an error.
     */
    void showError(String message);

    /**
     * Get a {@link android.content.Context}.
     */
    //Context context();

}
