package ru.kai.mcard.data.entity;

/**
 * Created by akabanov on 18.08.2017.
 */

public class SpecializationEntity {
    private int specID;
    private String name;

    public SpecializationEntity(int specID){
        this.specID = specID;
    }

    public int getSpecID(){
        return specID;
    }

    public String getName(){
        return name;
    }

    public void setName(String specName){
        this.name = specName;
    }
}
