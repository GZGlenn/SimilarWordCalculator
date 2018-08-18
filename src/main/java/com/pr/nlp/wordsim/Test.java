package com.pr.nlp.wordsim;

import com.pr.nlp.util.DistanceUtil;

import java.util.ArrayList;
import java.util.HashSet;

public class Test {

    public static String word2vecPath = "/home/glenn/IdeaProjects/wordEmbedding_model/20180803-002633_chinese_vectors.txt";

    public static void main(String[] args) {
        WordSimilarCalculator calculator = new WordSimilarCalculator();
        calculator.setWord2vecpath(word2vecPath);
        HashSet<String> words = calculator.getSimilarWord("平果");
        for (String str : words) System.out.println(str);

        System.out.println("---------------------------");

        HashSet<String> words2 = calculator.getSimilarWord("苹果");
        for (String str : words2) System.out.println(str);
    }
}
