package com.example.owledcontroller;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Effect {
    private String effectName;
    private String effect;
    private int popularity;
    private String gif;

    public Effect() {

    }

    public Effect(String effectName, String effect, int popularity, String gif) {
        this.effectName = effectName;
        this.effect = effect;
        this.popularity = popularity;
        this.gif = gif;
    }

    public String getEffectName() {
        return effectName;
    }

    public String getEffect() {
        return effect;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getGif() {
        return gif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Effect effect = (Effect) o;

        return popularity == effect.popularity;
    }

    @Override
    public int hashCode() {
        return popularity;
    }

}