package ru.kai.mcard.data.repository;

import android.support.annotation.NonNull;

import java.sql.SQLException;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import ru.kai.mcard.data.datasource.DBMethodsS;
import ru.kai.mcard.data.entity.SpecializationEntity;
import ru.kai.mcard.data.entity.mapper.SpecializationDataMapper;
import ru.kai.mcard.domain.model.SingleDModel;
import ru.kai.mcard.domain.repository.ISpecializationRepository;

/**
 * Created by akabanov on 30.08.2017.
 * Работа с БД и entity специализации. Методы будут дергать в domain слое, передавая model и получая model.
 */

public class SpecializationRepositoryImpl implements ISpecializationRepository {

    private DBMethodsS dbMethodsS;
    private SpecializationDataMapper dataMapper;

    public SpecializationRepositoryImpl(DBMethodsS dbMethodsS, SpecializationDataMapper dataMapper){
        this.dbMethodsS = dbMethodsS;
        this.dataMapper = dataMapper;
    }

    @Override
    public Flowable<Integer> createSpecialization(@NonNull final SingleDModel dModel) {
        return Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> emitter) throws Exception {
                try{
                    SpecializationEntity entity = dataMapper.transformFromDomainModel(dModel);
                    emitter.onNext(dbMethodsS.insertSpecialization(entity));
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(new SQLException());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<Integer> updateSpecialization(@NonNull final SingleDModel model) {
        return Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> emitter) throws Exception {
                try {
                    SpecializationEntity entity = dataMapper.transformFromDomainModel(model);
                    emitter.onNext(dbMethodsS.updateSpecialization(entity));
                    emitter.onComplete();
                }catch (Exception e){
                    emitter.onError(new SQLException());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<Integer> deleteSpecialization(@NonNull final SingleDModel model) {
        return Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> emitter) throws Exception {
                try {
                    SpecializationEntity entity = dataMapper.transformFromDomainModel(model);
                    emitter.onNext(dbMethodsS.deleteSpecialization(entity));
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(new SQLException());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    @NonNull
    public Flowable<SingleDModel> getSpecialization(final int specID) {
        return Flowable.create(new FlowableOnSubscribe<SingleDModel>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<SingleDModel> emitter) throws Exception {
                try{
                    SpecializationEntity entity = dbMethodsS.getSpecializationByID(specID);
                    emitter.onNext(dataMapper.transformFromDataEntityToDomainModel(entity));
                    emitter.onComplete();
                }catch (Exception e){
                    emitter.onError(new SQLException());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    @NonNull
    public Flowable<List<SingleDModel>> getAllSpecializations() {
        return Flowable.create(new FlowableOnSubscribe<List<SingleDModel>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<List<SingleDModel>> emitter) throws Exception {
                try {
                    List<SpecializationEntity> entityList = dbMethodsS.getAllSpecialisations();
                    emitter.onNext(dataMapper.transformFromDataEntityList(entityList));
                    emitter.onComplete();
                }catch (Exception e){
                    emitter.onError(new SQLException());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

}

/*
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<SingleDModel>> emitter) throws Exception {
                try {
                    List<SpecializationEntity> entityList = dbMethodsS.getAllSpecialisations();
                    emitter.onNext(dataMapper.transformFromDataEntityList(entityList));
                    emitter.onComplete();
                }catch (Exception e){
                    emitter.onError(new SQLException());
                }
            }
*/
