package com.profimedica.wordlex;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CumpanasuF on 26.01.2016.
 */
public class Utils {
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static List<Word> ConsumeString(String string)
    {
        String TAG = "Utils.ConsumeString";
        List<Word> wordsToBeDiscovered = new ArrayList<>();
        String[] inputLines = string.split("\\r?\\n");
        for(int i=1; i<inputLines.length; i++){
            String[] splitedLine = inputLines[i].split("\\ = ");
            if(splitedLine.length > 1) {
                Word word = null;
                if(splitedLine.length > 8) {
                    word = new Word(Long.valueOf(splitedLine[0]), splitedLine[1], splitedLine[2], Integer.valueOf(splitedLine[3]), Integer.valueOf(splitedLine[4]), Integer.valueOf(splitedLine[5]), Integer.valueOf(splitedLine[6]), Long.valueOf(splitedLine[7]), Long.valueOf(splitedLine[8]), "DeEn");
                }
                else
                {
                    word = new Word(null, splitedLine[0], splitedLine[1], 0, 0, 0, 0, Long.valueOf(0), Long.valueOf(0), "DeEn");
                }
                word.Unsaved = true;
                wordsToBeDiscovered.add(word);
            }
        }
        return wordsToBeDiscovered;
    }

    public static String PrepareString(List<Word> words)
    {
        String TAG = "Utils.PrepareString";
        StringBuffer sb = new StringBuffer();
        String delimiter = " = ";
        for(Word word : words) {
            sb.append(
                    String.valueOf(word.Id) + delimiter +
                            word.Native + delimiter +
                            word.Foreign + delimiter +
                            String.valueOf(word.Bad) + delimiter +
                            String.valueOf(word.Good) + delimiter +
                            String.valueOf(word.FBad) + delimiter +
                            String.valueOf(word.FGood) + delimiter +
                            String.valueOf(word.TimeSpend) + delimiter +
                            String.valueOf(word.FSpend) + delimiter +
                            word.Dictionary + delimiter
            );
        }
        return sb.toString();
    }
}
