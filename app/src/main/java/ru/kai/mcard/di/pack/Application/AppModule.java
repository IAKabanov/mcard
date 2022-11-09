package ru.kai.mcard.di.pack.Application;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kai.mcard.data.datasource.DBHelper;
import ru.kai.mcard.data.datasource.DBMethodsS;
import ru.kai.mcard.data.entity.mapper.SpecializationDataMapper;
import ru.kai.mcard.data.repository.SpecializationRepositoryImpl;

import ru.kai.mcard.domain.repository.ISpecializationRepository;

/**
 * Created by akabanov on 12.09.2017.
 */

@Module
public class AppModule {
    private Context context;

    public AppModule(Context context){
        this.context = context;
    }

/*
    @Singleton
    @Provides
    Context getApplicationContext(){
        return context;
    }

*/
    @Singleton
    @Provides
    DBHelper getDBHelper(){
        return new DBHelper(this.context);
    }

    @Singleton
    @Provides
    SQLiteDatabase getDatabase(DBHelper dbHelper){
        return dbHelper.open();
    }

    @Singleton
    @Provides
    DBMethodsS getDBMethodsS(SQLiteDatabase db){
        return new DBMethodsS(db);
    }

    @Singleton
    @Provides
    SpecializationDataMapper providesSpecDM(){
        return new SpecializationDataMapper();
    }

    @Singleton
    @Provides
    ISpecializationRepository providesSpecializationRepository(DBMethodsS dbMethodsS, SpecializationDataMapper specializationDataMapper){
        return new SpecializationRepositoryImpl(dbMethodsS, specializationDataMapper);
    }

}
