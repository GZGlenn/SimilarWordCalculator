package com.pr.nlp.prepare;

import com.pr.nlp.util.DistanceUtil;
import com.pr.nlp.util.FileUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PinyinSimilarGenerator {

    String ngramPath = "";
    String pinyinPath = "./resources/word_pinyin.txt";
    String similarPath = "./resources/pinyin_similar.txt";
    final String seperator = ",";

    public boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    public HashMap<String, HashSet<String>> getWord2Pinyin() {
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        boolean isPolyphone = true;

        HashMap<String, HashSet<String>> word2Pinyin = new HashMap<>();
        ArrayList<String> lines = FileUtil.readFileByLine(ngramPath);
        for (String line: lines) {
            String words = line.split("\t")[0];
            char[] chars = words.toCharArray();
            for (char c : chars) {
                if (isChinese(c)) {
                    try {
                        String[] strs = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
                        HashSet<String> pinyins = new HashSet<String>();
                        if (isPolyphone) {
                            for (int i = 0; i < strs.length; i++) {
                                pinyins.add(strs[i]);
                            }
                        }
                        if (pinyins.size() != 0) word2Pinyin.put(c + "", pinyins);

                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }

        return word2Pinyin;
    }

    public void saveWord2PinyinData(HashMap<String, HashSet<String>> word2Pinyin) {
        FileUtil.deleteFile(pinyinPath);
        FileWriter fw = FileUtil.createFileWriter(pinyinPath);
        for (HashMap.Entry<String, HashSet<String>> entry : word2Pinyin.entrySet()) {
            String str = entry.getKey() + "\t";
            for (String pinyin : entry.getValue()) {
                str += pinyin + seperator;
            }
            str = str.substring(0, str.length() - 1);
            FileUtil.append(fw, str + "\n");
        }
        FileUtil.close(fw);
    }

    public ArrayList<HashSet<String>> getSimilarByPinyin(HashMap<String, HashSet<String>> word2Pinyin) {
        ArrayList<HashSet<String>> result = new ArrayList<>();
        for (HashMap.Entry<String, HashSet<String>> entry : word2Pinyin.entrySet()) {
            String[] pinyins = entry.getValue().toArray(new String[0]);
            for (int pyIdx = 0 ; pyIdx < pinyins.length; pyIdx++) {
                String mainPinyin = pinyins[pyIdx];
                HashSet<String> similarWords = new HashSet<>();
                similarWords.add(entry.getKey());
                for (HashMap.Entry<String, HashSet<String>> subEntry : word2Pinyin.entrySet()) {
                    for (String pinyin : subEntry.getValue()) {
                        double distance = DistanceUtil.calEditDistance(mainPinyin, pinyin);
                        if (distance <= 1 && distance >= 0) {
                            similarWords.add(subEntry.getKey());
                            break;
                        }
                    }
                }
                result.add(similarWords);
            }
        }

        // merge very similar line
        ArrayList<HashSet<String>> fresult = new ArrayList<>();
        ArrayList<Boolean> isMerge = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            isMerge.add(false);
        }

        for (int i = 0 ; i < result.size(); i++) {
            if (!isMerge.get(i)) {
                isMerge.set(i, true);
                HashSet<String> tmpSet = new HashSet<>();
                HashSet<String> hashSet = (HashSet<String>)result.get(i).clone();
                for (int j = i+1; j < result.size(); j++) {
                    HashSet<String> hashSet2 = result.get(j);
                    tmpSet.clear();
                    tmpSet.addAll(hashSet);
                    tmpSet.retainAll(hashSet2);
                    if (tmpSet.size() * 1.0 / hashSet.size() > 0.8) {
                        hashSet.addAll(hashSet2);
                        isMerge.set(j, true);
                    }
                }
                fresult.add(hashSet);
            }
        }

        return fresult;
    }

    public void saveSimilarWord(ArrayList<HashSet<String>> similarWordList) {
        FileUtil.deleteFile(similarPath);
        FileWriter fw = FileUtil.createFileWriter(similarPath);
        for (HashSet<String> similarWord : similarWordList) {
            String str = "";
            for (String word : similarWord) {
                str += word + seperator;
            }
            str = str.substring(0, str.length() - 1);
            FileUtil.append(fw, str + "\n");
        }
        FileUtil.close(fw);
    }

    public void run() {
        HashMap<String, HashSet<String>> word2Pinyin = getWord2Pinyin();
        saveWord2PinyinData(word2Pinyin);
        ArrayList<HashSet<String>> similarWord = getSimilarByPinyin(word2Pinyin);
        saveSimilarWord(similarWord);
    }

    public static void main(String[] args) {
        PinyinSimilarGenerator generator = new PinyinSimilarGenerator();
        generator.run();
    }

}
