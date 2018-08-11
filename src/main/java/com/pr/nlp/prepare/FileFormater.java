package com.pr.nlp.prepare;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.pr.nlp.util.FileUtil;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FileFormater {

    private String spFilePath = "./source/SimilarPronunciation.txt";
    private String ssFilePath = "./source/SimilarShape.txt";

    public void dealSPFile() {
        String outputPath = "./source/SimilarPronunciation_modified.txt";
        ArrayList<HashSet<String>> result = new ArrayList<>();

        ArrayList<String> lines = FileUtil.readFileByLine(spFilePath);
        for (int i = 1 ; i < lines.size() ; i++) {
            HashSet<String> output = new HashSet<>();

            String line = lines.get(i);
            String[] spInfo = line.split("\t");

            if (spInfo.length == 1) continue;

//            System.out.println(spInfo.length + " => " + i + " : " + line);

            output.add(ZhConverterUtil.convertToSimple(spInfo[0].trim()));

            for (int k = 1; k < spInfo.length ; k++) {
                String item = spInfo[k].trim();
                for (int j = 0; j < item.length(); j++) {
                    output.add(ZhConverterUtil.convertToSimple(item.charAt(j) + ""));
                }
            }

            result.add(output);
        }

        FileUtil.deleteFile(outputPath);
        FileWriter fw = FileUtil.createFileWriter(outputPath);
        for (HashSet<String> strSet : result) {
            String target = "";
            for (String str : strSet) {
                target += str + ",";
            }
            FileUtil.append(fw, target.substring(0, target.length() - 1) + "\n");
        }

        FileUtil.close(fw);
    }

    public void dealSSFile() {
        String outputPath = "./source/SimilarShape_modified.txt";
        ArrayList<String> result = new ArrayList<>();

        ArrayList<String> lines = FileUtil.readFileByLine(ssFilePath);
        for (int i = 0 ; i < lines.size() ; i++) {
            String output = "";

            String line = lines.get(i);
            String[] spInfo = line.split(",");

            output = ZhConverterUtil.convertToSimple(spInfo[0].trim());

            String item = spInfo[1].trim();
            for (int j = 0 ; j < item.length() ; j++) {
                output += "," + ZhConverterUtil.convertToSimple(item.charAt(j) + "");
            }

            result.add(output);
        }

        FileUtil.deleteFile(outputPath);
        FileWriter fw = FileUtil.createFileWriter(outputPath);
        for (String str : result) {
            FileUtil.append(fw, str + "\n");
        }

        FileUtil.close(fw);
    }

    public void run() {
        dealSPFile();
        dealSSFile();
    }

    public static void main(String[] args) {
        FileFormater formater = new FileFormater();
        formater.run();
    }
}
