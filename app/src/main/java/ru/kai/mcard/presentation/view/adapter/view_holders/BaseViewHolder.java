package ru.kai.mcard.presentation.view.adapter.view_holders;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.kai.mcard.presentation.presentations_model.BasePModel;

/**
 * Created by akabanov on 08.02.2018.
 */

public abstract class BaseViewHolder extends RecyclerView.ViewHolder{
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void setPModel(BasePModel pModel);

    public abstract void setCVBackgroundColor(int backgroundColor);

}
