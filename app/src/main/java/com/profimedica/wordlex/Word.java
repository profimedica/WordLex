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
    public Word(Long Id, String Native, String Foreign, Integer Bad, Integer Good, Long TimeSpend, String Dictionary){
        this.Id = Id;
        this.Native = Native;
        this.Foreign = Foreign;
        try {
            this.Bad = Integer.valueOf(Bad);
            this.Good = Integer.valueOf(Good);
            this.TimeSpend = Long.valueOf(TimeSpend);
        }catch(Exception e){}
        this.Dictionary = Dictionary;
        Unsaved = false;
    }
    public Long Id;
    public String Native;
    public String Foreign;
    public int Bad;
    public int Good;
    public long TimeSpend;
    public String Dictionary;
    public Boolean Unsaved;
}
