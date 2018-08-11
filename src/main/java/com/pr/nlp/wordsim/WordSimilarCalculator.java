package com.pr.nlp.wordsim;

import com.pr.nlp.util.FileUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WordSimilarCalculator {

    private ArrayList<ArrayList<String>> shapeSimilarWordList;
    private ArrayList<ArrayList<String>> pronunSimialrWordList;
    private ArrayList<ArrayList<String>> pinyinSimialrWordList;
    private HashMap<String, ArrayList<Double>> semanticSimilarWordList;

    public WordSimilarCalculator() {
        initShapeSimilarWordList();
        initPronunSimilarWordList();
        initPinyinSimilarWordList();
        initSemanticSimilarWordList();
    }

    private void initShapeSimilarWordList() {
        InputStream inputStream = this.getClass().getResourceAsStream("shape_similar.txt");

    }

    private void initPronunSimilarWordList() {

    }

    private void initPinyinSimilarWordList() {

    }

    private void initSemanticSimilarWordList() {

    }

    public HashSet<String> getSimilarWord(String word) {
        HashSet<String> result = new HashSet<>();
        result.addAll(getPronunSimilarWord(word));
        result.addAll(getShapeSimilarWord(word));
        result.addAll(getPinyinSimilarWord(word));
        return result;
    }


    public ArrayList<String> getPronunSimilarWord(String word) {
        return getSpecialSimilarWord(word, pronunSimialrWordList);
    }


    public ArrayList<String> getPinyinSimilarWord(String word) {
        return getSpecialSimilarWord(word, pinyinSimialrWordList);
    }

    public ArrayList<String> getShapeSimilarWord(String word) {
        return getSpecialSimilarWord(word, shapeSimilarWordList);
    }

    private ArrayList<String> getSpecialSimilarWord(String word, ArrayList<ArrayList<String>> data) {
        ArrayList<String> result = new ArrayList<>();
        for (ArrayList<String> singleLine : data) {
            if (singleLine.contains(word)) result.addAll(singleLine);
        }
        return result;
    }
}
