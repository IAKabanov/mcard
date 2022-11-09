package ru.kai.mcard.domain.interactor.interactorSpecializations;

import android.support.annotation.Nullable;

import ru.kai.mcard.Constants;
import ru.kai.mcard.domain.model.SingleDModel;

/**
 * Created by akabanov on 26.10.2017.
 */

public final class ParamsSpecialization {
    @Nullable
    private final SingleDModel dModel;

    private ParamsSpecialization(@Nullable SingleDModel dModel) {
        this.dModel = dModel;
    }

    public static ParamsSpecialization forSpecialization(@Nullable SingleDModel dModel) {
        return new ParamsSpecialization(dModel);
    }

    int getSpecID() {
        if (dModel == null) {
            //return Constants.MODEL_IS_NULL; //  -2. Было -1
            return Constants.CREATE_NEW_MODEL; //  -1
        } else {
            return this.dModel.getID();
        }
    }

    @Nullable
    SingleDModel getDModel(){
        return this.dModel;
    }
}
