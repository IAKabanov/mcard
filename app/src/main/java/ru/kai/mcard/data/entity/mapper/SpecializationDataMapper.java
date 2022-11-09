package ru.kai.mcard.data.entity.mapper;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.kai.mcard.data.entity.SpecializationEntity;
import ru.kai.mcard.domain.model.SingleDModel;

/**
 * Created by akabanov on 30.08.2017.
 */

public class SpecializationDataMapper {

    @Inject
    public SpecializationDataMapper(){}

    @Nullable
    public SingleDModel transformFromDataEntityToDomainModel(@Nullable SpecializationEntity entity){
        SingleDModel model = null;
        if(entity != null){
            model = new SingleDModel(entity.getSpecID());
            model.setName(entity.getName());
        }
        return model;
    }

    public List<SingleDModel> transformFromDataEntityList(List<SpecializationEntity> entityList){
        final List<SingleDModel> modelList = new ArrayList<>();
        for (SpecializationEntity entity : entityList) {
            final SingleDModel model = transformFromDataEntityToDomainModel(entity);
            if (model != null) {
                modelList.add(model);
            }
        }
        return modelList;
    }

    public SpecializationEntity transformFromDomainModel(SingleDModel model){
        SpecializationEntity entity = null;
        if (model != null){
            entity = new SpecializationEntity(model.getID());
            entity.setName(model.getName());
        }
        return entity;
    }
}
