package ru.kai.mcard.di.pack.PerFragments;

import dagger.Component;
import ru.kai.mcard.di.pack.Application.IAppComponent;
import ru.kai.mcard.presentation.view.fragment.SpecializationsListFragment;

/**
 * Created by akabanov on 15.09.2017.
 */
@PerListFragment
@Component(dependencies = IAppComponent.class, modules = {SpecializationsListModule.class})
public interface ISpecializationsListComponent {
    void inject (SpecializationsListFragment listFragment);
}
