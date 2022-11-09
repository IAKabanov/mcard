package ru.kai.mcard.di.pack.PerFragments;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsCreate;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsDelete;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsGetList;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsGetSingle;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsUpdate;
import ru.kai.mcard.domain.repository.ISpecializationRepository;
import ru.kai.mcard.presentation.presentations_model.mapper.SpecializationPMapper;
import ru.kai.mcard.presentation.presenter.SpecializationsListPresenter;

/**
 * Created by akabanov on 15.09.2017.
 */
@Module
public class SpecializationsListModule {
    private Context context;

    public SpecializationsListModule(Context context){
        this.context = context;
    }

    @PerListFragment
    @Provides
    Context getActivityContext(){
        return this.context;
    }

    @PerListFragment
    @Provides
    SpecializationsListPresenter provideSpecializationPresenter(UseCaseSpecializationsGetList useCaseSpecGetList,
                                                                UseCaseSpecializationsDelete useCaseSpecDelete,
                                                                SpecializationPMapper pMapper){
        return new SpecializationsListPresenter(useCaseSpecGetList,
                useCaseSpecDelete,
                pMapper);
    }

    @PerListFragment
    @Provides
    UseCaseSpecializationsCreate provideUseCaseSpecializationsCreate(ISpecializationRepository specializationRepository){
        return new UseCaseSpecializationsCreate(specializationRepository);
    }

    @PerListFragment
    @Provides
    UseCaseSpecializationsUpdate provideUseCaseSpecializationsUpdate(ISpecializationRepository specializationRepository){
        return new UseCaseSpecializationsUpdate(specializationRepository);
    }

    @PerListFragment
    @Provides
    UseCaseSpecializationsDelete provideUseCaseSpecializationsDelete(ISpecializationRepository specializationRepository){
        return new UseCaseSpecializationsDelete(specializationRepository);
    }

    @PerListFragment
    @Provides
    UseCaseSpecializationsGetSingle provideUseCaseSpecializationsGetSingle(ISpecializationRepository specializationRepository){
        return new UseCaseSpecializationsGetSingle(specializationRepository);
    }

    @PerListFragment
    @Provides
    UseCaseSpecializationsGetList provideUseCaseSpecializationsGetList(ISpecializationRepository specializationRepository){
        return new UseCaseSpecializationsGetList(specializationRepository);
    }


}
