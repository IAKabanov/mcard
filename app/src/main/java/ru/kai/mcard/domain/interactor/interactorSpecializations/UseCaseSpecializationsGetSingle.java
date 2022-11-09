package ru.kai.mcard.domain.interactor.interactorSpecializations;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Flowable;
import ru.kai.mcard.domain.interactor.BaseUseCase;
import ru.kai.mcard.domain.model.SingleDModel;
import ru.kai.mcard.domain.repository.ISpecializationRepository;

/**
 * Created by akabanov on 04.09.2017.
 * Из презентера будем вызывать этот класс, чтобы дергать методы репозитория из data слоя
 */

public class UseCaseSpecializationsGetSingle extends BaseUseCase<SingleDModel, ParamsSpecialization> {
    private final ISpecializationRepository repository;

    @Inject
    public UseCaseSpecializationsGetSingle(ISpecializationRepository repository){
        this.repository = repository;
    }

    @Nullable
    public Flowable buildUseCaseFlowable(@Nullable ParamsSpecialization params) {
        return this.repository.getSpecialization(params.getSpecID());
    }

}
