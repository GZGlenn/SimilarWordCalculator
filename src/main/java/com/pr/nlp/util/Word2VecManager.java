package com.pr.nlp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Word2VecManager {

    public static HashMap<String, ArrayList<Float>> getWord2Vec(String path) {

        HashMap<String, ArrayList<Float>> semanticSimilarWordList = new HashMap<>();

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(path);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                String[] wordAndVec = line.split(" ");
                String word = wordAndVec[0];
                ArrayList<Float> feat = new ArrayList<>();
                double norm = 0;
                for (int i = 1 ; i < wordAndVec.length; i++) {
                    float df = Float.valueOf(wordAndVec[i]);
                    feat.add(df);
                    norm = df * df;
                }
                norm = Math.sqrt(norm);
                for (int i = 0; i < feat.size(); i++) {
                    if (norm == 0) continue;
                    feat.set(i, feat.get(i) / Float.valueOf(norm + ""));
                }

                semanticSimilarWordList.put(word, feat);
            }
        } catch(Exception e) {
            System.out.println("create statistic info error : " + e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
                }
            }
            if (fileReader != null)  {
                try {
                    fileReader.close();
                } catch (Exception e) {
                    LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
                }
            }
        }

        return semanticSimilarWordList;

    }


}
