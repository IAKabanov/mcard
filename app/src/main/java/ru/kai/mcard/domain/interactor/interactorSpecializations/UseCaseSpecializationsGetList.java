package ru.kai.mcard.domain.interactor.interactorSpecializations;

import javax.inject.Inject;

import io.reactivex.Flowable;
import ru.kai.mcard.domain.interactor.BaseUseCase;
import ru.kai.mcard.domain.repository.ISpecializationRepository;

/**
 * Created by akabanov on 21.10.2017.
 * Из презентера будем вызывать этот класс, чтобы дергать методы репозитория из data слоя
 */

public class UseCaseSpecializationsGetList extends BaseUseCase {
    private final ISpecializationRepository repository;

    @Inject
    public UseCaseSpecializationsGetList(ISpecializationRepository repository){
        this.repository = repository;
    }

    @Override
    public Flowable buildUseCaseFlowable(Object o) {
        return this.repository.getAllSpecializations();
    }


}
