package ru.kai.mcard.di.pack.PerActivity;

import dagger.Component;
import ru.kai.mcard.di.pack.Application.IAppComponent;
import ru.kai.mcard.di.pack.PerFragments.PerListFragment;
import ru.kai.mcard.di.pack.PerFragments.SpecializationsListModule;
import ru.kai.mcard.presentation.view.activity.SpecializationActivity;
import ru.kai.mcard.presentation.view.fragment.SpecializationsListFragment;

/**
 * Created by akabanov on 15.09.2017.
 */
@PerListDetailActivity
@Component(dependencies = IAppComponent.class, modules = {ListDetailActivityModule.class})
public interface IListDetailActivityComponent {
    void inject(SpecializationActivity listDetailActivity);
}
