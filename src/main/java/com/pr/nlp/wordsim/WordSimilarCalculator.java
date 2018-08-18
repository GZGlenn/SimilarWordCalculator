package com.pr.nlp.wordsim;

import com.hankcs.hanlp.HanLP;
import com.pr.nlp.util.DistanceUtil;
import com.pr.nlp.util.FileUtil;
import com.pr.nlp.util.Word2VecManager;
import jdk.nashorn.internal.runtime.arrays.ArrayIndex;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class WordSimilarCalculator {

    private String root_path = "/home/public/code/chinese_spelling/SimilarWordCalculator/src/main/resources/";
    private final String shape_similar_file_name = "shape_similar.txt";
    private final String pronounce_similar_file_name = "pronounce_similar.txt";
    private final String pinyin_similar_file_name = "pinyin_similar.txt";

    private String word2vecpath;

    private ArrayList<ArrayList<String>> shapeSimilarWordList;
    private ArrayList<ArrayList<String>> pronunSimialrWordList;
    private ArrayList<ArrayList<String>> pinyinSimialrWordList;
    private HashMap<String, ArrayList<Float>> semanticSimilarWordList;

    public WordSimilarCalculator() {
        initShapeSimilarWordList();
        initPronunSimilarWordList();
        initPinyinSimilarWordList();
    }

    public WordSimilarCalculator(String root_path) {
        this.root_path = root_path;
        initShapeSimilarWordList();
        initPronunSimilarWordList();
        initPinyinSimilarWordList();
    }

    public String getRoot_path() {
        return root_path;
    }

    public String getWord2vecpath() {
        return word2vecpath;
    }

    public void setRoot_path(String root_path) {
        this.root_path = root_path;
    }

    public void setWord2vecpath(String word2vecpath) {
        this.word2vecpath = word2vecpath;
        initSemanticSimilarWordList(word2vecpath);
    }

    private void initShapeSimilarWordList() {
        shapeSimilarWordList = new ArrayList<>();
        ArrayList<String> lines = FileUtil.readFileByLine(root_path + shape_similar_file_name);
        for (String line : lines) {
            shapeSimilarWordList.add(new ArrayList<String>(Arrays.asList(line.split(","))));
        }
    }

    private void initPronunSimilarWordList() {
        pronunSimialrWordList = new ArrayList<>();
        ArrayList<String> lines = FileUtil.readFileByLine(root_path + pronounce_similar_file_name);
        for (String line : lines) {
            pronunSimialrWordList.add(new ArrayList<String>(Arrays.asList(line.split(","))));
        }
    }

    private void initPinyinSimilarWordList() {
        pinyinSimialrWordList = new ArrayList<>();
        ArrayList<String> lines = FileUtil.readFileByLine(root_path + pinyin_similar_file_name);
        for (String line : lines) {
            pinyinSimialrWordList.add(new ArrayList<String>(Arrays.asList(line.split(","))));
        }
    }

    private void initSemanticSimilarWordList(String path) {
        semanticSimilarWordList = Word2VecManager.getWord2Vec(path);
    }

    public HashSet<String> getSimilarWord(String word) {
        int wordnum = HanLP.segment(word).size();
        HashSet<String> result = new HashSet<>();
        result.addAll(getPronunSimilarWord(word, wordnum));
        result.addAll(getShapeSimilarWord(word, wordnum));
        result.addAll(getPinyinSimilarWord(word, wordnum));
        result.addAll(getSemanticSimilarWord(word, 20));
        return result;
    }

    public ArrayList<String> getSemanticSimilarWord(String word, int limit) {
        ArrayList<ImmutablePair<String, Float>> result = new ArrayList<>();

        if (!semanticSimilarWordList.containsKey(word)) return new ArrayList<>();

        for (HashMap.Entry<String, ArrayList<Float>> entry : semanticSimilarWordList.entrySet()) {
            if (word == entry.getKey()) continue;
            float similarity = DistanceUtil.calCosineSimilarityWithNorm(semanticSimilarWordList.get(word), entry.getValue());
            if (result.size() < limit) {
                result.add(new ImmutablePair<>(entry.getKey(), similarity));
            }
            else if (result.get(result.size() - 1).getValue() > similarity) continue;
            else {
                result.set(result.size() - 1, new ImmutablePair<>(entry.getKey(), similarity));
            }

            result.sort(new Comparator<Pair<String, Float>>() {
                @Override
                public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
                    if (o1.getValue() > o2.getValue()) return -1;
                    else return 1;
                }
            });
        }

        ArrayList<String> words = new ArrayList<>();
        for (Pair<String, Float> pair : result) {
            words.add(pair.getKey());
        }

        return words;
    }

    public ArrayList<String> getPronunSimilarWord(String word, int wordnum) {
        return getSpecialSimilarWord(word, wordnum, pronunSimialrWordList);
    }


    public ArrayList<String> getPinyinSimilarWord(String word, int wordnum) {
        return getSpecialSimilarWord(word, wordnum, pinyinSimialrWordList);
    }

    public ArrayList<String> getShapeSimilarWord(String word, int wordnum) {
        return getSpecialSimilarWord(word, wordnum, shapeSimilarWordList);
    }

    private ArrayList<String> getSpecialSimilarWord(String word, int wordnum, ArrayList<ArrayList<String>> data) {
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0 ; i < word.length(); i++) {
            String singleWord = word.charAt(i) + "";
            ArrayList<String> simWordList = new ArrayList<>();
            for (ArrayList<String> singleLine : data) {
                if (singleLine.contains(singleWord)) {
                    simWordList.addAll(singleLine);
                }
            }
            for (String str: simWordList) {
                String modifiedWord = word.replace(word.charAt(i) + "", str);
                if (HanLP.segment(modifiedWord).size() <= wordnum) result.add(modifiedWord);
            }
        }


        return result;
    }

//    private ArrayList<String> getResourceData(String url) {
//        ArrayList<String> result = new ArrayList<>();
//        InputStream inputStream = this.getClass().getResourceAsStream(url);
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//        String line = "";
//        try {
//            while((line = bufferedReader.readLine()) != null) {
//                result.add(line.trim());
//            }
//        } catch (Exception e) {
//            System.out.println("error get Resource:" + url + " ==> " + e.getMessage());
//        }finally {
//            if (bufferedReader != null) {
//                try {
//                    bufferedReader.close();
//                } catch (Exception e) {
//                    System.out.println("error close bufferedReader:" + url + " ==> " + e.getMessage());
//                }
//            }
//
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (Exception e) {
//                    System.out.println("error close inputStream:" + url + " ==> " + e.getMessage());
//                }
//            }
//
//            return result;
//        }
//    }

}
