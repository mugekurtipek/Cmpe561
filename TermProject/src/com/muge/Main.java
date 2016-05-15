package com.muge;


import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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

    public static void main(String[] args) throws IOException{

      // splitTestTraining("/Users/mugekurtipek/Desktop/sarcasmPosTag.txt","/Users/mugekurtipek/Desktop/sarcasmTraining.txt","/Users/mugekurtipek/Desktop/sarcasmTest.txt");
      //  splitTestTraining("/Users/mugekurtipek/Desktop/nonsarcasmPosTag.txt","/Users/mugekurtipek/Desktop/nonsarcasmTraining.txt","/Users/mugekurtipek/Desktop/nonsarcasmTest.txt");

         posTagFeature("sarcasm");
        posTagFeature("nonsarcasm");

        readTrainingSarcasm();
        readTrainingNonsarcasm();

        System.out.println("Sarcasm");
        test("/Users/mugekurtipek/Desktop/sarcasmTest.txt");
        System.out.println("Non-Sarcasm");
        test("/Users/mugekurtipek/Desktop/nonsarcasmTest.txt");


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

            double base = 1/Math.sqrt(2*Math.PI*posAdjMeanSarcastic);
            double pow = -(Math.pow(((double)totalAdj-posAdjMeanSarcastic), 2)/(2*posAdjVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posAdjMeanNonSarcastic);
            pow = -(Math.pow(((double)totalAdj-posAdjMeanNonSarcastic), 2)/(2*posAdjVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posNounMeanSarcastic);
            pow = -(Math.pow(((double)totalAdj-posNounMeanSarcastic), 2)/(2*posNounVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posNounMeanNonSarcastic);
            pow = -(Math.pow(((double)totalAdj-posNounMeanNonSarcastic), 2)/(2*posNounVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posAdvMeanSarcastic);
            pow = -(Math.pow(((double)totalAdj-posAdvMeanSarcastic), 2)/(2*posAdvVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posAdvMeanNonSarcastic);
            pow = -(Math.pow(((double)totalAdj-posAdvMeanNonSarcastic), 2)/(2*posAdvVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posVerbMeanSarcastic);
            pow = -(Math.pow(((double)totalAdj-posVerbMeanSarcastic), 2)/(2*posVerbVarSarcastic));
            sarcasmScore += Math.log10(base * Math.exp(pow));

            base = 1/Math.sqrt(2*Math.PI*posVerbMeanNonSarcastic);
            pow = -(Math.pow(((double)totalAdj-posVerbMeanNonSarcastic), 2)/(2*posVerbVarNonSarcastic));
            nonSarcasmScore += Math.log10(base * Math.exp(pow));


            if(sarcasmScore > nonSarcasmScore){
                truePositive++;
            }else{
                falsePositive++;
            }
        }

        System.out.println("True Positive: "+ truePositive);
        System.out.println("False Positive: "+ falsePositive);
        System.out.println("Accuracy: "+ (double)truePositive/(double)(truePositive+falsePositive));

    }

    public static void posTagFeature(String type)throws IOException{

        String filePath = "";
        if(type.equals("nonsarcasm")){
             filePath = "/Users/mugekurtipek/Desktop/nonsarcasmPosTag.txt";
        }else{
            filePath = "/Users/mugekurtipek/Desktop/sarcasmPosTag.txt";
        }

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;

        ArrayList<Integer> nounCount = new ArrayList<>();
        ArrayList<Integer> verbCount = new ArrayList<>();
        ArrayList<Integer> adjCount = new ArrayList<>();
        ArrayList<Integer> advCount = new ArrayList<>();
        for(int i = 0; i<500; i++){
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
        for(int i = 0; i<500; i++){
            totalNoun += nounCount.get(i);
            totalVerb += verbCount.get(i);
            totalAdj += adjCount.get(i);
            totalAdv += advCount.get(i);

            totalNounS += nounCount.get(i) * nounCount.get(i);
            totalVerbS += verbCount.get(i) * verbCount.get(i);
            totalAdjS += adjCount.get(i) * adjCount.get(i);
            totalAdvS += advCount.get(i) * advCount.get(i);
        }

        if(type.equals("nonsarcasm")){
            posAdjMeanNonSarcastic = (double)totalAdj/500.0;
            posAdvMeanNonSarcastic = (double)totalAdv/500.0;
            posVerbMeanNonSarcastic = (double)totalVerb/500.0;
            posNounMeanNonSarcastic = (double)totalNoun/500.0;

            posAdjVarNonSarcastic = (double)totalAdjS/500.0 - (posAdjMeanNonSarcastic * posAdjMeanNonSarcastic);
            posAdvVarNonSarcastic = (double)totalAdvS/500.0 - (posAdvMeanNonSarcastic * posAdvMeanNonSarcastic);
            posNounVarNonSarcastic = (double)totalNounS/500.0 - (posNounMeanNonSarcastic * posNounMeanNonSarcastic);
            posVerbVarNonSarcastic = (double)totalVerbS/500.0 - (posVerbMeanNonSarcastic * posVerbMeanNonSarcastic);
        }else{
            posAdjMeanSarcastic = (double)totalAdj/500.0;
            posAdvMeanSarcastic = (double)totalAdv/500.0;
            posVerbMeanSarcastic = (double)totalVerb/500.0;
            posNounMeanSarcastic = (double)totalNoun/500.0;

            posAdjVarSarcastic = (double)totalAdjS/500.0 - (posAdjMeanSarcastic * posAdjMeanSarcastic);
            posAdvVarSarcastic = (double)totalAdvS/500.0 - (posAdvMeanSarcastic * posAdvMeanSarcastic);
            posNounVarSarcastic = (double)totalNounS/500.0 - (posNounMeanSarcastic * posNounMeanSarcastic);
            posVerbVarSarcastic = (double)totalVerbS/500.0 - (posVerbMeanSarcastic * posVerbMeanSarcastic);
        }



    }
}
