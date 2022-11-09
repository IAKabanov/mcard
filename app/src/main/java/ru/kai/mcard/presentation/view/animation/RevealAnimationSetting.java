package ru.kai.mcard.presentation.view.animation;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

// Parcelable RevealAnimationSetting with AutoValue
@AutoValue
public abstract class RevealAnimationSetting implements Parcelable {

    //public abstract class RevealAnimationSetting {
    public abstract int getCenterX();
    public abstract int getCenterY();
    public abstract int getWidth();
    public abstract int getHeight();

    public static RevealAnimationSetting with(int centerX, int centerY, int width, int height) {
        return new AutoValue_RevealAnimationSetting(centerX, centerY, width, height);
    }
}