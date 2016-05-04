package com.muge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static ArrayList<String> tags = new ArrayList<>();
    static int [] tagCounts;
    static HashMap<String,double[]> observationLikelihoods = new HashMap<>();
    static double [][] transitionMatrix;
    static String mostCommmonTag = "";

    static int tagIndex = 3;
    static String fileName = "";

    public static void main(String[] args) throws IOException{
        if (args[1].equals("cpostag")){
            tagIndex = 3;
        }
        if (args[1].equals("postag")){
            tagIndex = 4;
        }
        fileName = args[0];



        readTags();
        createObservationLikelihoodMatrix();
        writeObservationLikelihoodToFile();
        createTransitionMatrix();
        writeTransitionMatrixToFile();

        //test();
        //validation();
    }
    public static void readTags() throws IOException { //To read the data.txt and create the P matrix
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = br.readLine()) != null) {
            if(!line.equals("")){
                //System.out.println(line);
                String[] temp = line.split("\t");
                String tag = temp[tagIndex];
                if(!tags.contains(tag)){
                    tags.add(tag);
                }
            }

        }
        /*for(int i= 0; i<tags.size();i++){
            System.out.print(tags.get(i)+"\t");
        }
        System.out.println();*/
    }
    public static void createObservationLikelihoodMatrix() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("/Users/mugekurtipek/Desktop/turkish_metu_sabanci_train.conll"));
        String line;
        tagCounts = new int[tags.size()];
        while ((line = br.readLine()) != null) {
            if(!line.equals("")){
                String[] temp = line.split("\t");
                String form = temp[1];
                String lemma = temp[2];
                String tag = temp[tagIndex];
                ArrayList<String> multipleTags = new ArrayList<>();
                if(form.equals("_")){
                    multipleTags.add(tag);
                    boolean check = true;
                    while(check){
                        line = br.readLine();
                        temp = line.split("\t");
                        tag = temp[tagIndex];
                        multipleTags.add(tag);
                        if(temp[1].equals("_")){
                            check = true;
                        }else{
                            check = false;
                        }
                    }
                }
                if(observationLikelihoods.containsKey(lemma)){
                    double[] columns = observationLikelihoods.get(lemma);
                    if(multipleTags.size() != 0){
                        for(int i = 0; i< multipleTags.size(); i++){
                            columns[tags.indexOf(multipleTags.get(i))] += 1;
                            tagCounts[tags.indexOf(multipleTags.get(i))] += 1;
                        }
                    }else{
                        columns[tags.indexOf(tag)] += 1;
                        tagCounts[tags.indexOf(tag)] += 1;
                    }
                    observationLikelihoods.put(lemma,columns);
                }else{
                    double[] columns  = new double [tags.size()];
                    if(multipleTags.size() != 0){
                        for(int i = 0; i< multipleTags.size(); i++){
                            columns[tags.indexOf(multipleTags.get(i))] += 1;
                            tagCounts[tags.indexOf(multipleTags.get(i))] += 1;
                        }
                    }else{
                        columns[tags.indexOf(tag)] += 1;
                        tagCounts[tags.indexOf(tag)] += 1;
                    }
                    observationLikelihoods.put(lemma,columns);
                }
            }

        }
        /*for(String key: observationLikelihoods.keySet()){
            System.out.print(key+"\t");
            for(int i = 0 ; i<observationLikelihoods.get(key).length;i++){
                System.out.print(observationLikelihoods.get(key)[i] +"\t");
            }
            System.out.println();
        }*/


        //To change the number of occurances to the probabilities
        for(String key: observationLikelihoods.keySet()){
            for(int i = 0 ; i<observationLikelihoods.get(key).length;i++){
                observationLikelihoods.get(key)[i] = observationLikelihoods.get(key)[i] / tagCounts[i];
            }
        }


    }

    public static void writeObservationLikelihoodToFile() throws IOException{
        PrintWriter writer = new PrintWriter("observationLikelihood.txt", "UTF-8");
        writer.println(tags.size());
        for(String key:observationLikelihoods.keySet() ){
            writer.print(key+"\t");
            for(int i = 0; i<tags.size();i++){
                writer.print(observationLikelihoods.get(key)[i] + "\t");
            }
            writer.println();
        }
        writer.close();
    }

    public static void createTransitionMatrix() throws IOException{
        transitionMatrix = new double[tags.size() + 1][tags.size()];
        tagCounts = new int[tags.size() + 1];


        BufferedReader br = new BufferedReader(new FileReader("/Users/mugekurtipek/Desktop/turkish_metu_sabanci_train.conll"));
        String line;
        String previousTag = "";
        while ((line = br.readLine()) != null) {
            if (!line.equals("")) {
                String[] temp = line.split("\t");
                int id = Integer.parseInt(temp[0]);
                String form = temp[1];
                String lemma = temp[2];
                String tag = temp[tagIndex];
                if(form.equals("_")){
                    boolean check = true;
                    while(check){
                        line = br.readLine();
                        temp = line.split("\t");
                        tag = temp[tagIndex];
                        if(temp[1].equals("_")){
                            check = true;
                        }else{
                            check = false;
                        }
                    }
                }
                if(id == 1){
                    transitionMatrix[0][tags.indexOf(tag)] += 1;
                    tagCounts[0] += 1;
                    tagCounts[tags.indexOf(tag) + 1] += 1;
                }else{
                    transitionMatrix[tags.indexOf(previousTag)+1][tags.indexOf(tag)] += 1;
                    tagCounts[tags.indexOf(tag) + 1] += 1;
                }
                previousTag = tag;
            }
        }
       /* for(int i= 0; i<tags.size()+1;i++){
            if (i==0) {
                System.out.print("Start\t");
            }else{
                System.out.print(tags.get(i-1)+"\t");
            }

            for(int j = 0; j <tags.size();j++){
                System.out.print(transitionMatrix[i][j]+ "\t");
            }
            System.out.println();
        }


        for(int i= 0; i<tags.size() + 1;i++){
            System.out.print(tagCounts[i]+ "\t");
        }
        System.out.println();*/

        //To change the number of occurances to the probabilities
        for(int i= 0; i<tags.size() + 1;i++){
            for(int j = 0; j <tags.size();j++){
                transitionMatrix[i][j] = transitionMatrix[i][j] / tagCounts[i];
            }
        }

        //To find the most common tag
        int max = 0;
        int maxIndex = -1;
        for(int i = 1; i<tagCounts.length;i++){
            if(tagCounts[i]>max){
                max = tagCounts[i];
                maxIndex = i-1;
            }
        }
        mostCommmonTag = tags.get(maxIndex);

      /*   for(int i= 0; i<tags.size()+1;i++){
            if (i==0) {
                System.out.print("Start\t");
            }else{
                System.out.print(tags.get(i-1)+"\t");
            }

            for(int j = 0; j <tags.size();j++){
                System.out.print(transitionMatrix[i][j]+ "\t");
            }
            System.out.println();
        }*/
    }

    public static void writeTransitionMatrixToFile() throws IOException{
        PrintWriter writer = new PrintWriter("transitionMatrix.txt", "UTF-8");
        writer.println(tags.size());
        for(int j = 0; j<tags.size();j++){
            writer.print(tags.get(j) + "\t");
        }
        writer.println();
        writer.println(mostCommmonTag);
        for(int i = 0; i<tags.size()+1;i++){
            for(int j = 0; j<tags.size();j++){
                writer.print(transitionMatrix[i][j] + "\t");
            }
            writer.println();
        }
        writer.close();
    }


    public static void test() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("/Users/mugekurtipek/Desktop/turkish_metu_sabanci_test_blind_sample.conll.txt"));
        String line;
        int previousTagIndex = -1;
        while ((line = br.readLine()) != null) {
            if (!line.equals("")) {
                String[] temp = line.split("\t");
                int id = Integer.parseInt(temp[0]);
                String form = temp[1];
                String lemma = temp[2];
                String tag = "";
                if(form.equals("_")){
                    boolean check = true;
                    while(check){
                        line = br.readLine();
                        temp = line.split("\t");
                        form = temp[1];
                        if(temp[1].equals("_")){
                            check = true;
                        }else{
                            check = false;
                        }
                    }
                }
                if(observationLikelihoods.containsKey(lemma)){
                    if(id == 1){
                        double max = 0;
                        int maxIndex = -1;
                        for(int i = 0; i<tags.size(); i++){
                            double tempValue = transitionMatrix[0][i] *  observationLikelihoods.get(lemma)[i];
                            if(tempValue>max){
                                max = tempValue;
                                maxIndex = i;
                            }
                        }
                        tag = tags.get(maxIndex);
                        System.out.println(form+" | "+tag);


                    }else{
                        double max = 0;
                        int maxIndex = -1;
                        for(int i = 0; i<tags.size(); i++){
                            double tempValue = transitionMatrix[previousTagIndex][i] *  observationLikelihoods.get(lemma)[i];
                            if(tempValue>max){
                                max = tempValue;
                                maxIndex = i;
                            }
                        }
                        tag = tags.get(maxIndex);
                        System.out.println(form+" | "+tag);
                    }
                    previousTagIndex = tags.indexOf(tag);
                }else{
                    tag = mostCommmonTag;
                    System.out.println(form+" | "+tag +" UNKNOWN WORD");
                    previousTagIndex = tags.indexOf(mostCommmonTag);
                }


            }
        }
    }

    public static void validation() throws IOException{
        int confusionMatrix [][] = new int[tags.size()][tags.size()];
        int truePositive = 0;
        int falsePositive = 0;
        BufferedReader br = new BufferedReader(new FileReader("/Users/mugekurtipek/Desktop/turkish_metu_sabanci_val.conll"));
        String line;
        int previousTagIndex = -1;
        while ((line = br.readLine()) != null) {
            if (!line.equals("")) {
                String[] temp = line.split("\t");
                int id = Integer.parseInt(temp[0]);
                String form = temp[1];
                String lemma = temp[2];
                String realTag = temp[tagIndex];
                String tag = "";
                if(form.equals("_")){
                    boolean check = true;
                    while(check){
                        line = br.readLine();
                        temp = line.split("\t");
                        form = temp[1];
                        realTag = temp[tagIndex];
                        if(temp[1].equals("_")){
                            check = true;
                        }else{
                            check = false;
                        }
                    }
                }
                if(observationLikelihoods.containsKey(lemma)){
                    if(id == 1){
                        double max = 0;
                        int maxIndex = -1;
                        for(int i = 0; i<tags.size(); i++){
                            double tempValue = transitionMatrix[0][i] *  observationLikelihoods.get(lemma)[i];
                            if(tempValue>max){
                                max = tempValue;
                                maxIndex = i;
                            }
                        }
                        tag = tags.get(maxIndex);
                        if(tag.equals(realTag)){
                            truePositive ++;
                        }else{
                            falsePositive++;
                            confusionMatrix[tags.indexOf(realTag)][tags.indexOf(tag)] += 1;
                        }
                        //System.out.println(form+" | "+tag);


                    }else{
                        double max = 0;
                        int maxIndex = -1;
                        for(int i = 0; i<tags.size(); i++){
                            double tempValue = transitionMatrix[previousTagIndex][i] *  observationLikelihoods.get(lemma)[i];
                            if(tempValue>max){
                                max = tempValue;
                                maxIndex = i;
                            }
                        }
                        tag = tags.get(maxIndex);
                        if(tag.equals(realTag)){
                            truePositive ++;
                        }else{
                            falsePositive++;
                            confusionMatrix[tags.indexOf(realTag)][tags.indexOf(tag)] += 1;
                        }
                        //System.out.println(form+" | "+tag);
                    }
                    previousTagIndex = tags.indexOf(tag);
                }else{
                    tag = mostCommmonTag;
                    if(tag.equals(realTag)){
                        truePositive ++;
                    }else{
                        falsePositive++;
                        confusionMatrix[tags.indexOf(realTag)][tags.indexOf(tag)] += 1;
                    }
                    //System.out.println(form+" | "+tag +" UNKNOWN WORD");
                    previousTagIndex = tags.indexOf(mostCommmonTag);
                }


            }
        }
        System.out.println("TRUE: "+truePositive);
        System.out.println("FALSE: "+falsePositive);



        //To print the confusion matrix
        for(int i = 0; i<tags.size();i++){
            System.out.print(tags.get(i)+"\t");
        }
        double total = 0;
        for(int i = 0; i<tags.size();i++){

            for( int j= 0; j<tags.size();j++){
                total += confusionMatrix[i][j];
            }
        }
        for(int i = 0; i<tags.size();i++){
            System.out.println();
            for( int j= 0; j<tags.size();j++){
                if(i==j){
                    System.out.print("-\t");
                }else{
                    System.out.printf("%.2f",confusionMatrix[i][j]/total);
                    System.out.print("\t");
                }

            }
        }
    }

}
