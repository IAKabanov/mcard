package ru.kai.mcard.presentation.presentations_model.mapper;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.kai.mcard.domain.model.SingleDModel;
import ru.kai.mcard.presentation.presentations_model.BasePModel;

/**
 * Created by akabanov on 06.09.2017.
 */

public class SpecializationPMapper {

    @Inject
    public SpecializationPMapper(){}

    public BasePModel transformFromDomainModel(SingleDModel dModel){
        BasePModel pModel = null;
        if (dModel != null){
            pModel = new BasePModel(dModel.getID());
            pModel.setName(dModel.getName());
        }
        return pModel;
    }

    public List<BasePModel> transformFromDomainListModel(@NonNull List<SingleDModel> dModelList){
        final List<BasePModel> pModelList = new ArrayList<>();

        for (SingleDModel dModel:dModelList) {
            final BasePModel currentPModel = transformFromDomainModel(dModel);
            if (currentPModel != null){
                pModelList.add(currentPModel);
            }
        }

        return pModelList;
    }

    public SingleDModel transformToDomainModel(BasePModel pModel){
        SingleDModel dModel = null;
        if (pModel != null){
            dModel = new SingleDModel(pModel.getModelID());
            dModel.setName(pModel.getName());
        }
        return dModel;
    }



}
