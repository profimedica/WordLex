package com.profimedica.wordlex;

/**
 * Created by Cumpanasu on 11/23/2015.
 */

public class Word{
    public Word(String Id, String Native, String Foreign, Integer Bad, Integer Good, Long TimeSpend, String Dictionary){
        this.Id = Id;
        this.Native = Native;
        this.Foreign = Foreign;
        try {
            this.Bad = Integer.valueOf(Bad);
            this.Good = Integer.valueOf(Good);
            this.TimeSpend = Long.valueOf(TimeSpend);
        }catch(Exception e){}
        this.Dictionary = Dictionary;
    }
    public String Id;
    public String Native;
    public String Foreign;
    public int Bad;
    public int Good;
    public long TimeSpend;
    public String Dictionary;
}
