package ru.kai.mcard.di.pack.PerActivity;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import ru.kai.mcard.di.pack.PerFragments.PerListFragment;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsCreate;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsDelete;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsGetList;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsGetSingle;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsUpdate;
import ru.kai.mcard.domain.repository.ISpecializationRepository;
import ru.kai.mcard.presentation.presentations_model.mapper.SpecializationPMapper;
import ru.kai.mcard.presentation.presenter.ActivityListDetailPresenter;
import ru.kai.mcard.presentation.presenter.SpecializationsListPresenter;

/**
 * Created by akabanov on 15.09.2017.
 */
@Module
public class ListDetailActivityModule {
    private Context context;

    public ListDetailActivityModule(Context context){
        this.context = context;
    }

    @PerListDetailActivity
    @Provides
    Context getActivityContext(){
        return this.context;
    }

    @PerListDetailActivity
    @Provides
    ActivityListDetailPresenter provideSpecializationPresenter(UseCaseSpecializationsGetSingle useCaseSpecGetSingle,
                                                               UseCaseSpecializationsCreate useCaseSpecCreate,
                                                               UseCaseSpecializationsUpdate useCaseSpecUpdate,
                                                               SpecializationPMapper pMapper){
        return new ActivityListDetailPresenter(
                useCaseSpecGetSingle,
                useCaseSpecCreate,
                useCaseSpecUpdate,
                pMapper);
    }

    @PerListDetailActivity
    @Provides
    UseCaseSpecializationsCreate provideUseCaseSpecializationsCreate(ISpecializationRepository specializationRepository){
        return new UseCaseSpecializationsCreate(specializationRepository);
    }

    @PerListDetailActivity
    @Provides
    UseCaseSpecializationsUpdate provideUseCaseSpecializationsUpdate(ISpecializationRepository specializationRepository){
        return new UseCaseSpecializationsUpdate(specializationRepository);
    }

}
