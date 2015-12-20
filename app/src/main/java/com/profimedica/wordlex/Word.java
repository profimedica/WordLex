package com.profimedica.wordlex;

import java.util.Comparator;

/**
 * Created by Cumpanasu on 11/23/2015.
 */

public class Word implements Comparable<Word> {
    @Override
    public int compareTo(Word o2) {
        return this.Bad - o2.Bad;
    }
    public int compareToForeign(Word o2) {

        return this.FBad - o2.FBad;
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
}
