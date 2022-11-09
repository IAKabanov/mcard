package ru.kai.mcard.presentation.presentations_model;

import java.io.Serializable;

/**
 * Created by akabanov on 06.09.2017.
 * класс для модели прост
 */

public class BasePModel implements Serializable {
    private int modelID;
    private String name;

    public BasePModel(int modelID){
        this.modelID = modelID;
    }

    public int getModelID() {
        return modelID;
    }

    public void setModelID(int curID){
        this.modelID = curID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
