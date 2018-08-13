package com.pr.nlp.wordsim;

import com.pr.nlp.util.DistanceUtil;
import com.pr.nlp.util.FileUtil;
import com.pr.nlp.util.LogUtil;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class WordSimilarCalculator {

    private ArrayList<ArrayList<String>> shapeSimilarWordList;
    private ArrayList<ArrayList<String>> pronunSimialrWordList;
    private ArrayList<ArrayList<String>> pinyinSimialrWordList;
    private HashMap<String, ArrayList<Double>> semanticSimilarWordList;

    public WordSimilarCalculator(String word2vecpath) {
        initShapeSimilarWordList();
        initPronunSimilarWordList();
        initPinyinSimilarWordList();
        initSemanticSimilarWordList(word2vecpath);
    }

    private void initShapeSimilarWordList() {
        shapeSimilarWordList = new ArrayList<>();
        ArrayList<String> lines = this.getResourceData("shape_similar.txt");
        for (String line : lines) {
            shapeSimilarWordList.add(new ArrayList<String>(Arrays.asList(line.split(","))));
        }
    }

    private void initPronunSimilarWordList() {
        pronunSimialrWordList = new ArrayList<>();
        ArrayList<String> lines = this.getResourceData("pronounce_similar.txt");
        for (String line : lines) {
            pronunSimialrWordList.add(new ArrayList<String>(Arrays.asList(line.split(","))));
        }
    }

    private void initPinyinSimilarWordList() {
        pinyinSimialrWordList = new ArrayList<>();
        ArrayList<String> lines = this.getResourceData("pinyin_similar.txt");
        for (String line : lines) {
            pinyinSimialrWordList.add(new ArrayList<String>(Arrays.asList(line.split(","))));
        }
    }

    private void initSemanticSimilarWordList(String path) {
        semanticSimilarWordList = new HashMap<>();
        ArrayList<String> lines = FileUtil.readFileByLine(path);
        for (String line : lines) {
            String[] wordAndVec = line.split("\t");
            String word = wordAndVec[0];
            ArrayList<Double> feat = new ArrayList<>();
            double norm = 0;
            for (String f : wordAndVec[1].split(",")) {
                double df = Double.valueOf(f);
                feat.add(df);
                norm = df * df;
            }
            norm = Math.sqrt(norm);
            for (int i = 0; i < feat.size(); i++) {
                feat.set(i, feat.get(i) / norm);
            }

            semanticSimilarWordList.put(word, feat);
        }
    }

    public HashSet<String> getSimilarWord(String word) {
        HashSet<String> result = new HashSet<>();
        result.addAll(getPronunSimilarWord(word));
        result.addAll(getShapeSimilarWord(word));
        result.addAll(getPinyinSimilarWord(word));
        result.addAll(getSemanticSimilarWord(word, 20));
        return result;
    }

    public ArrayList<String> getSemanticSimilarWord(String word, int limit) {
        ArrayList<Pair<String, Double>> result = new ArrayList<>();

        for (HashMap.Entry<String, ArrayList<Double>> entry : semanticSimilarWordList.entrySet()) {
            if (word == entry.getKey()) continue;
            double similarity = DistanceUtil.calCosineSimilarityWithNorm(semanticSimilarWordList.get(word), entry.getValue());
            if (result.size() < limit) {
                result.add(new Pair<>(entry.getKey(), similarity));
            }
            else if (result.get(result.size() - 1).getValue() > similarity) continue;
            else {
                result.set(result.size() - 1, new Pair<>(entry.getKey(), similarity));
            }

            result.sort(new Comparator<Pair<String, Double>>() {
                @Override
                public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                    if (o1.getValue() > o2.getValue()) return -1;
                    else return 1;
                }
            });
        }

        ArrayList<String> words = new ArrayList<>();
        for (Pair<String, Double> pair : result) {
            words.add(pair.getKey());
        }

        return words;
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

    private ArrayList<String> getResourceData(String url) {
        ArrayList<String> result = new ArrayList<>();
        InputStream inputStream = this.getClass().getResourceAsStream(url);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        try {
            while((line = bufferedReader.readLine()) != null) {
                result.add(line.trim());
            }
        } catch (Exception e) {
            System.out.println("error get Resource:" + url + " ==> " + e.getMessage());
        }finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    System.out.println("error close bufferedReader:" + url + " ==> " + e.getMessage());
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    System.out.println("error close inputStream:" + url + " ==> " + e.getMessage());
                }
            }

            return result;
        }
    }

}
