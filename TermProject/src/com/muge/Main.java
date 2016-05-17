package com.muge;


import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static HashMap<String,HashMap<String,Double>> bigramSarcasm = new HashMap<>();
    static HashMap<String,HashMap<String,Double>> bigramNonsarcasm = new HashMap<>();
    static HashMap<String,Double> unigramSarcasm = new HashMap<>();
    static HashMap<String,Double> unigramNonsarcasm = new HashMap<>();
    static double totalSarcasmVoc = 0.0;
    static double totalNonSarcasmVoc = 0.0;

    static double posNounMeanSarcastic = 0.0;
    static double posNounVarSarcastic = 0.0;
    static double posVerbMeanSarcastic = 0.0;
    static double posVerbVarSarcastic = 0.0;
    static double posAdjMeanSarcastic = 0.0;
    static double posAdjVarSarcastic = 0.0;
    static double posAdvMeanSarcastic = 0.0;
    static double posAdvVarSarcastic = 0.0;

    static double posNounMeanNonSarcastic = 0.0;
    static double posNounVarNonSarcastic = 0.0;
    static double posVerbMeanNonSarcastic = 0.0;
    static double posVerbVarNonSarcastic = 0.0;
    static double posAdjMeanNonSarcastic = 0.0;
    static double posAdjVarNonSarcastic = 0.0;
    static double posAdvMeanNonSarcastic = 0.0;
    static double posAdvVarNonSarcastic = 0.0;


    static double posNounToAdjMeanSarcastic = 0.0;
    static double posNounToAdjVarSarcastic = 0.0;
    static double posNounToAdjMeanNonSarcastic = 0.0;
    static double posNounToAdjVarNonSarcastic = 0.0;

    static double posVerbToAdvMeanSarcastic = 0.0;
    static double posVerbToAdvVarSarcastic = 0.0;
    static double posVerbToAdvMeanNonSarcastic = 0.0;
    static double posVerbToAdvVarNonSarcastic = 0.0;


    static double puncCountMeanSarcastic = 0.0;
    static double puncCountVarSarcastic = 0.0;
    static double wordCaseMeanNonSarcastic = 0.0;
    static double wordCaseVarNonSarcastic = 0.0;

    static double puncCountMeanNonSarcastic = 0.0;
    static double puncCountVarNonSarcastic = 0.0;
    static double wordCaseMeanSarcastic = 0.0;
    static double wordCaseVarSarcastic = 0.0;

    static int truePositiveX = 0;
    static int trueNegativeX = 0;
    static int falsePositiveX = 0;
    static int falseNegativeX = 0;

    public static void main(String[] args) throws IOException{

    //   splitTestTraining("/Users/mugekurtipek/Desktop/sarcasmPosTag.txt","/Users/mugekurtipek/Desktop/sarcasmTraining.txt","/Users/mugekurtipek/Desktop/sarcasmTest.txt");
    //    splitTestTraining("/Users/mugekurtipek/Desktop/nonsarcasmPosTag.txt","/Users/mugekurtipek/Desktop/nonsarcasmTraining.txt","/Users/mugekurtipek/Desktop/nonsarcasmTest.txt");

        posTagFeature("sarcasm");
        posTagFeature("nonsarcasm");

        otherFeatures("sarcasm");
        otherFeatures("nonsarcasm");

        readTrainingSarcasm();
        readTrainingNonsarcasm();

        System.out.println("Sarcasm");
        test("/Users/mugekurtipek/Desktop/sarcasmTest.txt");
        System.out.println("Non-Sarcasm");
        test("/Users/mugekurtipek/Desktop/nonsarcasmTest.txt");

        double precision = ((double)truePositiveX/100.0+(double)trueNegativeX/100.0)/2;
        double recall = (((double) truePositiveX/ (double)(truePositiveX+falsePositiveX))+((double)trueNegativeX/(double)(trueNegativeX+falseNegativeX)))/2;
        double fMeasure = 2 * precision * recall /(precision+recall);

        System.out.println("F meaure: "+fMeasure);
       // stemmer();
    }

    public static void splitTestTraining(String main, String trainingPath, String testPath)throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(main));
        String line;
        String previousLemma = "";
        ArrayList<String> trainingArray = new ArrayList<>();
        ArrayList<String> testArray = new ArrayList<>();
        while ((line = br.readLine()) != null) {

            if (!line.equals("")) {
                trainingArray.add(line);
            }
        }
        int k = trainingArray.size()* 20 / 100;
        for(int i = 0; i<k; i++){

            Random r = new Random();
            int rand = r.nextInt(trainingArray.size());

            testArray.add(trainingArray.get(rand));
            trainingArray.remove(rand);
        }
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(trainingPath));
        for(int i = 0; i<trainingArray.size();i++){
            bw1.write(trainingArray.get(i)+"\n");
        }
        bw1.close();
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(testPath));
        for(int i = 0; i<testArray.size();i++){
            bw2.write(testArray.get(i)+"\n");
        }
        bw2.close();

    }

    public static void stemmer()throws IOException{
        Stemmer s = new Stemmer();
        BufferedReader br = new BufferedReader(new FileReader("/Users/mugekurtipek/Desktop/sarcasmTokenized.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/mugekurtipek/Desktop/sarcasmStemmed.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] temp = line.split(" ");
            for(int i = 0; i<temp.length;i++){
                if (temp[i].equals("SENTENCESTARTSHERE")){
                    bw.write("\n");
                    bw.write("SENTENCESTARTSHERE ");

                }else{
                    char[] charArray = temp[i].toCharArray();
                    s.add(charArray,charArray.length);
                    s.stem();
                    bw.write(s.toString()+" ");
                }

            }
        }
        br.close();
        bw.close();





    }
    public static void readTrainingSarcasm() throws IOException { //To read the data.txt and create the P matrix

        BufferedReader br = new BufferedReader(new FileReader("/Users/mugekurtipek/Desktop/sarcasmTraining.txt"));
        String line;
        String previousLemma = "";
        while ((line = br.readLine()) != null) {

            if (!line.equals("")) {
                String[] temp = line.split(" ");
                for(int i = 0 ; i<temp.length;i++){
                    String lemma = temp[i];
                    lemma = lemma.substring(0,lemma.indexOf('|'));
                    if(unigramSarcasm.containsKey(lemma)){
                        unigramSarcasm.put(lemma,unigramSarcasm.get(lemma)+1);
                    }else{
                        unigramSarcasm.put(lemma,1.0);
                    }
                    if(i == 0) {
                        previousLemma = temp[i];
                    } else {

                        HashMap<String,Double> tempMap = new HashMap<>();
                        if(bigramSarcasm.containsKey(previousLemma)) {
                            tempMap = bigramSarcasm.get(previousLemma);
                            if(bigramSarcasm.get(previousLemma).containsKey(lemma)){
                                tempMap.put(lemma,tempMap.get(lemma)+1);
                                bigramSarcasm.put(previousLemma,tempMap);
                            }else{
                                tempMap.put(lemma,1.0);
                                bigramSarcasm.put(previousLemma,tempMap);
                            }
                        }else {
                            tempMap.put(lemma,1.0);
                            bigramSarcasm.put(previousLemma,tempMap);
                        }
                        previousLemma = lemma;
                    }

                }

                }
            }

        for(String key: unigramSarcasm.keySet()){
            totalSarcasmVoc += unigramSarcasm.get(key);
        }


        }

    public static void readTrainingNonsarcasm() throws IOException { //To read the data.txt and create the P matrix

        BufferedReader br = new BufferedReader(new FileReader("/Users/mugekurtipek/Desktop/nonsarcasmTraining.txt"));
        String line;
        String previousLemma = "";
        while ((line = br.readLine()) != null) {

            if (!line.equals("")) {
                String[] temp = line.split(" ");
                for(int i = 0 ; i<temp.length;i++){
                    String lemma = temp[i];
                    lemma = lemma.substring(0,lemma.indexOf('|'));
                    if(unigramNonsarcasm.containsKey(lemma)){
                        unigramNonsarcasm.put(lemma,unigramNonsarcasm.get(lemma)+1);
                    }else{
                        unigramNonsarcasm.put(lemma,1.0);
                    }
                    if(i == 0) {
                        previousLemma = temp[i];
                    } else {

                        HashMap<String,Double> tempMap = new HashMap<>();
                        if(bigramNonsarcasm.containsKey(previousLemma)) {
                            tempMap = bigramNonsarcasm.get(previousLemma);
                            if(bigramNonsarcasm.get(previousLemma).containsKey(lemma)){
                                tempMap.put(lemma,tempMap.get(lemma)+1);
                                bigramNonsarcasm.put(previousLemma,tempMap);
                            }else{
                                tempMap.put(lemma,1.0);
                                bigramNonsarcasm.put(previousLemma,tempMap);
                            }
                        }else {
                            tempMap.put(lemma,1.0);
                            bigramNonsarcasm.put(previousLemma,tempMap);
                        }
                        previousLemma = lemma;
                    }

                }

            }
        }
        for(String key: unigramNonsarcasm.keySet()){
            totalNonSarcasmVoc += unigramNonsarcasm.get(key);
        }


    }

    public static void test(String filePath) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        String previousLemma = "";
        int truePositive = 0;
        int falsePositive = 0;


        while ((line = br.readLine()) != null) {
            int totalNoun = 0;
            int totalVerb = 0;
            int totalAdj = 0;
            int totalAdv = 0;
            int puncCount = 0;
            int wordCaseCount = 0;
            double sarcasmScore = 0.0;
            double nonSarcasmScore = 0.0;
            if (!line.equals("")) {
                String[] temp = line.split(" ");
                for (int i = 0; i < temp.length; i++) {
                    String lemma = temp[i];
                    if(lemma.contains("|NN")){
                        totalNoun++;
                    }else if(lemma.contains("|RB")){
                        totalAdv++;
                    }else if(lemma.contains("|JJ")){
                        totalAdj++;
                    }else if(lemma.contains("|VB")){
                        totalVerb++;
                    }else{
                        //Nothing
                    }
                    lemma = lemma.substring(0,lemma.indexOf('|'));
                    Matcher m = Pattern.compile("[^a-zA-Z0-9]").matcher(lemma);
                    if(m.find()){
                        puncCount++;
                    }
                    Matcher m2 = Pattern.compile("[A-Z]").matcher(lemma);
                    while (m2.find()) {
                        wordCaseCount++;
                    }

                    if(i == 0){
                        previousLemma = lemma;
                    }else{
                        if(bigramSarcasm.containsKey(previousLemma)){
                            if(bigramSarcasm.get(previousLemma).containsKey(lemma)){
                                sarcasmScore += Math.log10(bigramSarcasm.get(previousLemma).get(lemma)/unigramSarcasm.get(previousLemma));
                            }
                        }else{
                            if(unigramSarcasm.containsKey(lemma)){
                                sarcasmScore += Math.log10(unigramSarcasm.get(lemma)/totalSarcasmVoc);
                            }else{
                                sarcasmScore += Math.log10(1/totalSarcasmVoc);
                            }
                        }

                        if(bigramNonsarcasm.containsKey(previousLemma)){
                            if(bigramNonsarcasm.get(previousLemma).containsKey(lemma)){
                                nonSarcasmScore += Math.log10(bigramNonsarcasm.get(previousLemma).get(lemma)/unigramNonsarcasm.get(previousLemma));
                            }
                        }else{
                            if(unigramNonsarcasm.containsKey(lemma)){
                                nonSarcasmScore += Math.log10(unigramNonsarcasm.get(lemma)/totalNonSarcasmVoc);
                            }else{
                                nonSarcasmScore += Math.log10(1/totalNonSarcasmVoc);
                            }
                        }

                    }
                }
            }

            double base = 0.0;
            double pow = 0.0;
           /* base = 1/Math.sqrt(2*Math.PI*posAdjMeanSarcastic);
            pow = -(Math.pow(((double)totalAdj-posAdjMeanSarcastic), 2)/(2*posAdjVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posAdjMeanNonSarcastic);
            pow = -(Math.pow(((double)totalAdj-posAdjMeanNonSarcastic), 2)/(2*posAdjVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));*/

            /*base = 1/Math.sqrt(2*Math.PI*posNounMeanSarcastic);
            pow = -(Math.pow(((double)totalNoun-posNounMeanSarcastic), 2)/(2*posNounVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posNounMeanNonSarcastic);
            pow = -(Math.pow(((double)totalNoun-posNounMeanNonSarcastic), 2)/(2*posNounVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));*/

            /*base = 1/Math.sqrt(2*Math.PI*posAdvMeanSarcastic);
            pow = -(Math.pow(((double)totalAdv-posAdvMeanSarcastic), 2)/(2*posAdvVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posAdvMeanNonSarcastic);
            pow = -(Math.pow(((double)totalAdv-posAdvMeanNonSarcastic), 2)/(2*posAdvVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));*/

            /*base = 1/Math.sqrt(2*Math.PI*posVerbMeanSarcastic);
            pow = -(Math.pow(((double)totalVerb-posVerbMeanSarcastic), 2)/(2*posVerbVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posVerbMeanNonSarcastic);
            pow = -(Math.pow(((double)totalVerb-posVerbMeanNonSarcastic), 2)/(2*posVerbVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));*/

            /*base = 1/Math.sqrt(2*Math.PI*posVerbToAdvMeanSarcastic);
            pow = -(Math.pow(((double)totalVerb/(double)totalAdv-posVerbToAdvMeanSarcastic), 2)/(2*posVerbToAdvVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posVerbToAdvMeanNonSarcastic);
            pow = -(Math.pow(((double)totalVerb/(double)totalAdv-posVerbToAdvMeanNonSarcastic), 2)/(2*posVerbToAdvVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));*/


            base = 1/Math.sqrt(2*Math.PI*posNounToAdjMeanSarcastic);
            pow = -(Math.pow(((double)totalNoun/(double)totalAdj-posNounToAdjMeanSarcastic), 2)/(2*posNounToAdjVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posNounToAdjMeanNonSarcastic);
            pow = -(Math.pow(((double)totalNoun/(double)totalAdj-posNounToAdjMeanNonSarcastic), 2)/(2*posNounToAdjVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));

           /*base = 1/Math.sqrt(2*Math.PI*puncCountMeanSarcastic);
            pow = -(Math.pow(((double)puncCount-puncCountMeanSarcastic), 2)/(2*puncCountVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*puncCountMeanNonSarcastic);
            pow = -(Math.pow(((double)puncCount-puncCountMeanNonSarcastic), 2)/(2*puncCountVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));*/

          /* base = 1/Math.sqrt(2*Math.PI*wordCaseMeanSarcastic);
            pow = -(Math.pow(((double)wordCaseCount-wordCaseMeanSarcastic), 2)/(2*wordCaseVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*wordCaseMeanNonSarcastic);
            pow = -(Math.pow(((double)wordCaseCount-wordCaseMeanNonSarcastic), 2)/(2*wordCaseVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));*/


            if(sarcasmScore > nonSarcasmScore){
                truePositive++;
            }else{
                falsePositive++;
            }
        }

        if(filePath.contains("non")){
            trueNegativeX = falsePositive;
            falsePositiveX = truePositive;
        }else{
            truePositiveX = truePositive;
            falseNegativeX = falsePositive;
        }
        System.out.println("True Positive: "+ truePositive);
        System.out.println("False Positive: "+ falsePositive);
        System.out.println("Accuracy: "+ (double)truePositive/(double)(truePositive+falsePositive));

    }

    public static void otherFeatures(String type)throws IOException{
        String filePath = "";
        if(type.equals("nonsarcasm")){
            filePath = "/Users/mugekurtipek/Desktop/nonsarcasmTraining.txt";
        }else{
            filePath = "/Users/mugekurtipek/Desktop/sarcasmTraining.txt";
        }

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;

        ArrayList<Integer> puncCount = new ArrayList<>();
        ArrayList<Integer> wordCaseCount = new ArrayList<>();
        for(int i = 0; i<400; i++){
            puncCount.add(0);
            wordCaseCount.add(0);
        }

        int cursor = -1;
        while ((line = br.readLine()) != null) {
            if(!line.equals("")){
                cursor++;
                String[] temp = line.split(" ");
                for (int i = 0; i < temp.length; i++) {

                    String token = temp[i].substring(0,temp[i].indexOf('|'));
                    Matcher m = Pattern.compile("[^a-zA-Z0-9]").matcher(token);
                    if(m.find()){
                        puncCount.set(cursor,puncCount.get(cursor)+1);
                    }
                    Matcher m2 = Pattern.compile("[A-Z]").matcher(token);
                    while (m2.find()) {
                        wordCaseCount.set(cursor,wordCaseCount.get(cursor)+1);
                    }
                }
            }
        }

        int totalPuncCount = 0;
        int totalWordCaseCount = 0;
        int totalPuncCountS = 0;
        int totalWordCaseCountS = 0;

        for(int i = 0; i<400; i++){
            totalPuncCount += puncCount.get(i);
            totalWordCaseCount += wordCaseCount.get(i);

            totalPuncCountS += puncCount.get(i) * puncCount.get(i);
            totalWordCaseCountS += wordCaseCount.get(i) * wordCaseCount.get(i);
        }

        if(type.equals("nonsarcasm")){
            puncCountMeanNonSarcastic = (double)totalPuncCount / 400.0;
            wordCaseMeanNonSarcastic = (double) totalWordCaseCount / 400.0;

            puncCountVarNonSarcastic = (double)totalPuncCountS/400.0 - (puncCountMeanNonSarcastic * puncCountMeanNonSarcastic);
            wordCaseVarNonSarcastic = (double)totalWordCaseCountS/400.0 - (wordCaseMeanNonSarcastic * wordCaseMeanNonSarcastic);
        }else{
            puncCountMeanSarcastic = (double)totalPuncCount / 400.0;
            wordCaseMeanSarcastic = (double) totalWordCaseCount / 400.0;

            puncCountVarSarcastic = (double)totalPuncCountS/400.0 - (puncCountMeanSarcastic * puncCountMeanSarcastic);
            wordCaseVarSarcastic = (double)totalWordCaseCountS/400.0 - (wordCaseMeanSarcastic * wordCaseMeanSarcastic);
        }

    }

    public static void posTagFeature(String type)throws IOException{

        String filePath = "";
        if(type.equals("nonsarcasm")){
             filePath = "/Users/mugekurtipek/Desktop/nonsarcasmTraining.txt";
        }else{
            filePath = "/Users/mugekurtipek/Desktop/sarcasmTraining.txt";
        }

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;

        ArrayList<Integer> nounCount = new ArrayList<>();
        ArrayList<Integer> verbCount = new ArrayList<>();
        ArrayList<Integer> adjCount = new ArrayList<>();
        ArrayList<Integer> advCount = new ArrayList<>();
        for(int i = 0; i<400; i++){
            nounCount.add(0);
            verbCount.add(0);
            adjCount.add(0);
            advCount.add(0);

        }

        int cursor = -1;
        while ((line = br.readLine()) != null) {
            if(!line.equals("")){
                cursor++;
                String[] temp = line.split(" ");
                for (int i = 0; i < temp.length; i++) {
                    if(temp[i].contains("|NN")){
                        nounCount.set(cursor,nounCount.get(cursor)+1);
                    }else if(temp[i].contains("|RB")){
                        advCount.set(cursor,advCount.get(cursor)+1);
                    }else if(temp[i].contains("|VB")){
                        verbCount.set(cursor,verbCount.get(cursor)+1);
                    }else if(temp[i].contains("|JJ")){
                        adjCount.set(cursor,adjCount.get(cursor)+1);
                    }else{
                        //DO NOTHING
                    }
                }
            }
        }
        int totalNoun = 0;
        int totalVerb = 0;
        int totalAdj = 0;
        int totalAdv = 0;
        int totalNounS = 0;
        int totalVerbS = 0;
        int totalAdjS = 0;
        int totalAdvS = 0;

        int totalNounToAdj = 0;
        int totalNounToAdjS = 0;

        int totalVerbToAdv = 0;
        int totalVerbToAdvS = 0;


        for(int i = 0; i<400; i++){
            totalNoun += nounCount.get(i);
            totalVerb += verbCount.get(i);
            totalAdj += adjCount.get(i);
            totalAdv += advCount.get(i);

            totalNounS += nounCount.get(i) * nounCount.get(i);
            totalVerbS += verbCount.get(i) * verbCount.get(i);
            totalAdjS += adjCount.get(i) * adjCount.get(i);
            totalAdvS += advCount.get(i) * advCount.get(i);

            totalNounToAdj += (double)nounCount.get(i)/(double)adjCount.get(i);
            totalNounToAdjS += (double)nounCount.get(i)/(double)adjCount.get(i) * (double)nounCount.get(i)/(double)adjCount.get(i);

            totalVerbToAdv += (double)verbCount.get(i)/(double)advCount.get(i);
            totalVerbToAdvS += (double)verbCount.get(i)/(double)advCount.get(i) * (double)verbCount.get(i)/(double)advCount.get(i);
        }

        if(type.equals("nonsarcasm")){
            posAdjMeanNonSarcastic = (double)totalAdj/400.0;
            posAdvMeanNonSarcastic = (double)totalAdv/400.0;
            posVerbMeanNonSarcastic = (double)totalVerb/400.0;
            posNounMeanNonSarcastic = (double)totalNoun/400.0;

            posAdjVarNonSarcastic = (double)totalAdjS/400.0 - (posAdjMeanNonSarcastic * posAdjMeanNonSarcastic);
            posAdvVarNonSarcastic = (double)totalAdvS/400.0 - (posAdvMeanNonSarcastic * posAdvMeanNonSarcastic);
            posNounVarNonSarcastic = (double)totalNounS/400.0 - (posNounMeanNonSarcastic * posNounMeanNonSarcastic);
            posVerbVarNonSarcastic = (double)totalVerbS/400.0 - (posVerbMeanNonSarcastic * posVerbMeanNonSarcastic);

            posNounToAdjMeanNonSarcastic = (double)totalNounToAdj/400.0;
            posNounToAdjVarNonSarcastic = (double)totalNounToAdjS/400.0 - (posNounToAdjMeanNonSarcastic * posNounToAdjMeanNonSarcastic);

            posVerbToAdvMeanNonSarcastic = (double)totalVerbToAdv/400.0;
            posVerbToAdvVarNonSarcastic = (double)totalVerbToAdvS/400.0 - (posVerbToAdvMeanNonSarcastic * posVerbToAdvMeanNonSarcastic);

        }else{
            posAdjMeanSarcastic = (double)totalAdj/400.0;
            posAdvMeanSarcastic = (double)totalAdv/400.0;
            posVerbMeanSarcastic = (double)totalVerb/400.0;
            posNounMeanSarcastic = (double)totalNoun/400.0;

            posAdjVarSarcastic = (double)totalAdjS/400.0 - (posAdjMeanSarcastic * posAdjMeanSarcastic);
            posAdvVarSarcastic = (double)totalAdvS/400.0 - (posAdvMeanSarcastic * posAdvMeanSarcastic);
            posNounVarSarcastic = (double)totalNounS/400.0 - (posNounMeanSarcastic * posNounMeanSarcastic);
            posVerbVarSarcastic = (double)totalVerbS/400.0 - (posVerbMeanSarcastic * posVerbMeanSarcastic);

            posNounToAdjMeanSarcastic = (double)totalNounToAdj/400.0;
            posNounToAdjVarSarcastic = (double)totalNounToAdjS/400.0 - (posNounToAdjMeanSarcastic * posNounToAdjMeanSarcastic);

            posVerbToAdvMeanSarcastic = (double)totalVerbToAdv/400.0;
            posVerbToAdvVarSarcastic = (double)totalVerbToAdvS/400.0 - (posVerbToAdvMeanSarcastic * posVerbToAdvMeanSarcastic);

        }


    }
}
