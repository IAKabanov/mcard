package ru.kai.mcard.domain.repository;

import java.util.List;

import ru.kai.mcard.domain.model.SingleDModel;


/**
 * Created by akabanov on 30.08.2017.
 * Реализация этих методов находится в data слое.
 * За эти методы мы будем в domain слое дергать методы, реализованные в data слое и изменять там данные.
 */

public interface CatalogRepository<M> {

    int createSpecialization(M model);  // insert
    int updateSpecialization(M model);
    int deleteSpecialization(M model);

    SingleDModel getSpecializationByID(int specID);
    List<SingleDModel> getAllSpecializations();
}
