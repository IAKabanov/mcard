package ru.kai.mcard;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by akabanov on 23.01.2016.
 */
public class ModelVisitsCures extends HashMap<String, String> implements Parcelable {

    private HashMap<String, String> map;

    public ModelVisitsCures(){
        super();
        map = new HashMap();
    }

    public ModelVisitsCures(Parcel in) {

        map = new HashMap();
        readFromParcel(in);    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeMap(map);
        dest.writeInt(map.size());
        for (String s: map.keySet()) {
            dest.writeString(s);
            dest.writeString(map.get(s));
        }
    }

    public static final Parcelable.Creator<ModelVisitsCures> CREATOR = new Parcelable.Creator<ModelVisitsCures>() {

        @Override
        public ModelVisitsCures createFromParcel(Parcel source) {
            return new ModelVisitsCures(source);
        }

        @Override
        public ModelVisitsCures[] newArray(int size) {
            return new ModelVisitsCures[size];
        }
    };

    public void readFromParcel(Parcel in) {
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            map.put(in.readString(), in.readString());
        }
    }

    @Override
    public String put(String key, String val){
        super.put(key, val);
        map.put(key, val);
        return val;
    }

    public String get(String key) {
        return map.get(key);
    }

}
