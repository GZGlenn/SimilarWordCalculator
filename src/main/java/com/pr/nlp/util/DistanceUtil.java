package com.pr.nlp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistanceUtil {

    public static double calEditDistance(String strA, String strB) {
        int distance = -1;

        if(null == strA || null == strB || strA.isEmpty() || strB.isEmpty()) {
            return distance;
        }

        if (strA.equals(strB)) {
            return 0;
        }

        int length = Math.max(strA.length(),strB.length());

        int array[][] = new int[length + 1][length + 1];
        for(int i = 0; i <= length; i++){
            array[i][0] = i;

        }

        for(int j=0;j<=length;j++){
            array[0][j]=j;
        }

        for(int i = 1; i <= strA.length(); i++){
            for(int j = 1; j <= strB.length(); j++){
                array[i][j] = min(array[i-1][j] + 1,
                        array[i][j-1] + 1,
                        array[i-1][j-1] + (strA.charAt(i-1) == strB.charAt(j-1) ? 0:1));
            }
        }

        return array[strA.length()][strB.length()];
    }

    public static <T extends List<Double>> double calEuclideanDistance (T data1, T data2) {
        double differenceSum = 0;
        for (int i = 0 ; i < Math.min(data1.size(), data2.size()); i++) {
            if (i < data1.size() && i < data2.size()) {
                differenceSum += Math.pow(data1.get(i) - data2.get(i), 2);
            }
        }

        return Math.sqrt(differenceSum);
    }


    public static <T extends List<Float>> float calCosineSimilarityWithNorm (T data1, T data2) {
        float similar = 0;
        for (int i = 0 ; i < Math.min(data1.size(), data2.size()); i++) {
            if (i < data1.size() && i < data2.size()) {
                similar += data1.get(i) * data2.get(i);
            }
        }

        return similar;
    }

    public static int min(int a,int b, int c){
        return Math.min(Math.min(a,b),c);
    }

    public static void test() {
        String a=null;
        String b="abd";

        System.out.println("case 1：编辑距离为："+ DistanceUtil.calEditDistance(a,b));
        System.out.println();

        a="Program";
        b="P-r-o-g-r-a-m";
        System.out.println("case 2：编辑距离为"+ DistanceUtil.calEditDistance(a,b));
        System.out.println();

        a="2333";
        b="6666666";
        System.out.println("case 3：编辑距离为"+ DistanceUtil.calEditDistance(a,b));
        System.out.println();

        a="adbe";
        b="abc";
        System.out.println("case 4：编辑距离为"+ DistanceUtil.calEditDistance(a,b));
        System.out.println();

        a="hehe";
        b="hehe";
        System.out.println("case 5：编辑距离为"+ DistanceUtil.calEditDistance(a,b));
        System.out.println();
    }
}
