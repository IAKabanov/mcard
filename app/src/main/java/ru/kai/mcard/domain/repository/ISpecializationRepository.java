package ru.kai.mcard.domain.repository;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import ru.kai.mcard.domain.model.SingleDModel;


/**
 * Created by akabanov on 30.08.2017.
 * Реализация этих методов находится в data слое.
 * За эти методы мы будем в domain слое в интеракторах дергать методы, реализованные в data слое в репозиториях и изменять там данные.
 */

public interface ISpecializationRepository {

    //**********************
    Flowable<Integer> createSpecialization(final SingleDModel dModel);
    Flowable<Integer> updateSpecialization(final SingleDModel model);
    Flowable<Integer> deleteSpecialization(final SingleDModel model);

    Flowable<SingleDModel> getSpecialization(final int specID);
    @NonNull
    Flowable<List<SingleDModel>> getAllSpecializations();
    //Observable<List<SingleDModel>> getFilteredSpecializations(FilterModel);

}
