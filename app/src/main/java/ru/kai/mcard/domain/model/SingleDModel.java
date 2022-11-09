package ru.kai.mcard.domain.model;

/**
 * Created by akabanov on 30.08.2017.
 */

public class SingleDModel {
    private int specID;
    private String name;

    public SingleDModel(int specID){
        this.specID = specID;
    }

    public int getID(){
        return specID;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

}
