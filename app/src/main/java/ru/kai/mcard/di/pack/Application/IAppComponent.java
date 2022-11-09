package ru.kai.mcard.di.pack.Application;

import javax.inject.Singleton;

import dagger.Component;
import ru.kai.mcard.domain.repository.ISpecializationRepository;

/**
 * Created by akabanov on 12.09.2017.
 */

@Singleton
@Component(modules = AppModule.class)
public interface IAppComponent {

    ISpecializationRepository providesSpecializationRepository(); // явное объявление зависимости родительского компонента
                                                                  // (чтобы она была доступна в дочернем),
                                                                  // при dependencies = IAppComponent.class в ISpecializationsListComponent
}
