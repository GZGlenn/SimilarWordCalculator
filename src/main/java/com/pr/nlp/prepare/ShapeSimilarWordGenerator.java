package com.pr.nlp.prepare;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.pr.nlp.util.FileUtil;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class ShapeSimilarWordGenerator {

    public ArrayList<String> inputPathList;
    public String outputPath;

    public ArrayList<HashSet<Character>> getShapeSimilarWord(String input_path) {
        ArrayList<HashSet<Character>> result = new ArrayList<>();
        ArrayList<String> lines = FileUtil.readFileByLine(input_path);
        for (String line : lines) {
            String normalLine = line.replace(",", "").trim();
            HashSet<Character> singleResult = new HashSet<>();
            for (int i = 0 ; i < normalLine.length() ; i++) {
                singleResult.add(ZhConverterUtil.convertToSimple(normalLine.charAt(i) + "").charAt(0));
            }
            result.add(singleResult);
        }
        return result;
    }

    public HashSet<String> mergeInputData(ArrayList<HashSet<Character>> input) {
        HashSet<String> result = new HashSet<>();

        for (HashSet<Character> hashSet : input) {
            String line = "";
            for (Character str : hashSet) {
                line += str + ",";
            }
            result.add(line.substring(0, line.length() - 1));
        }

        return result;
    }

    public void saveResult(HashSet<String> shapeSimilarWordSet, String path) {
        FileUtil.deleteFile(path);
        FileWriter fw = FileUtil.createFileWriter(path);
        for (String string : shapeSimilarWordSet) {
            FileUtil.append(fw, string + "\n");
        }

        FileUtil.close(fw);
    }

    public void run() {
        outputPath = "./data/shape_similar.txt";
        inputPathList = new ArrayList<>();
        inputPathList.add("./source/SimilarShape_modified.txt");
        inputPathList.add("./source/sims.txt");
        inputPathList.add("./source/xingjinzi.txt");
        inputPathList.add("./source/xjz.txt");

        ArrayList<HashSet<Character>> inputDataList = new ArrayList<>();
        for (String path : inputPathList) {
            inputDataList.addAll(getShapeSimilarWord(path));
        }

        HashSet<String> mergeData = mergeInputData(inputDataList);
        saveResult(mergeData, outputPath);
    }

    public static void main(String[] args) {
        ShapeSimilarWordGenerator generator = new ShapeSimilarWordGenerator();
        generator.run();
    }
}
