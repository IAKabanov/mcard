package ru.kai.mcard.domain.interactor.interactorSpecializations;

import javax.inject.Inject;

import io.reactivex.Flowable;
import ru.kai.mcard.domain.interactor.BaseUseCase;
import ru.kai.mcard.domain.repository.ISpecializationRepository;

/**
 * Created by akabanov on 04.09.2017.
 * Из презентера будем вызывать этот класс, чтобы дергать методы репозитория из data слоя
 */

public class UseCaseSpecializationsCreate extends BaseUseCase<Integer, ParamsSpecialization> {
    private final ISpecializationRepository repository;

    @Inject
    public UseCaseSpecializationsCreate(ISpecializationRepository repository){
        this.repository = repository;
    }

    @Override
    public Flowable buildUseCaseFlowable(ParamsSpecialization paramsSpecialization) {
        return this.repository.createSpecialization(paramsSpecialization.getDModel());
    }


}
