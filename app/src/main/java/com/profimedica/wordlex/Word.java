package com.profimedica.wordlex;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by Cumpanasu on 11/23/2015.
 */

public class Word implements Comparable<Word>, Parcelable {
    @Override
    public int compareTo(Word o2) {
        return this.Bad - o2.Bad;
    }
    public int compareToForeign(Word o2) {

        return this.FBad - o2.FBad;
    }

    public Word(Parcel source) {
        this.Id = source.readLong();
        this.Native = source.readString();
        this.Foreign = source.readString();
        try {
            this.Bad = source.readInt();
            this.Good = source.readInt();
            this.FBad = source.readInt();
            this.FGood = source.readInt();
            this.TimeSpend = source.readLong();
            this.FSpend = source.readLong();
        }catch(Exception e){}
        this.Dictionary = source.readString();
        Unsaved = false;
    }

    public Word(Long Id, String Native, String Foreign, Integer Bad, Integer Good, Integer FBad, Integer FGood, Long TimeSpend, Long FSpend, String Dictionary){
        this.Id = Id;
        this.Native = Native;
        this.Foreign = Foreign;
        try {
            this.Bad = Integer.valueOf(Bad);
            this.Good = Integer.valueOf(Good);
            this.FBad = Integer.valueOf(FBad);
            this.FGood = Integer.valueOf(FGood);
            this.TimeSpend = Long.valueOf(TimeSpend);
            this.FSpend = Long.valueOf(FSpend);
        }catch(Exception e){}
        this.Dictionary = Dictionary;
        Unsaved = false;
    }
    public Long Id;
    public String Native;
    public String Foreign;
    public int Bad;
    public int Good;
    public int FBad;
    public int FGood;
    public long TimeSpend;
    public long FSpend;
    public String Dictionary;
    public Boolean Unsaved;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.Id);
        dest.writeString(this.Native);
        dest.writeString(this.Foreign);
        try {
            dest.writeInt(this.Bad);
            dest.writeInt(this.Good);
            dest.writeInt(this.FBad);
            dest.writeInt(this.FGood);
            dest.writeLong(this.TimeSpend);
            dest.writeLong(this.FSpend);
        }catch(Exception e){

        }
        dest.writeString(this.Dictionary);
        Unsaved = false;
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }

        @Override
        public Word createFromParcel(Parcel source) {
            return new Word(source);
        }
    };
}
