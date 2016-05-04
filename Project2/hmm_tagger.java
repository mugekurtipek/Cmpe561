package com.muge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static ArrayList<String> tags = new ArrayList<>();
    static int [] tagCounts;
    static HashMap<String,double[]> observationLikelihoods = new HashMap<>();
    static double [][] transitionMatrix;
    static String mostCommmonTag = "";

    static String testFileName = "";
    static String outputFileName = "";

    public static void main(String[] args) throws IOException{
        testFileName = args[0];
        outputFileName = args[1];
        createObservationLikelihoodMatrix();
        createTransitionMatrix();
        test();

    }

    public static void createObservationLikelihoodMatrix() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("observationLikelihood.txt"));
        String line;
        line = br.readLine();
        int numberOfTags = Integer.parseInt(line);
        while ((line = br.readLine()) != null) {
                String[] temp = line.split("\t");
                String token = temp[0];
                double[] tempArray = new double[numberOfTags];
                for(int i = 0; i<numberOfTags;i++){
                    tempArray[i] = Double.parseDouble(temp[i+1]);
                }
                observationLikelihoods.put(token,tempArray);

        }


    }



    public static void createTransitionMatrix() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("transitionMatrix.txt"));
        String line;
        line = br.readLine();
        int numberOfTags = Integer.parseInt(line);
        transitionMatrix = new double[numberOfTags+1][numberOfTags];
        line = br.readLine();
        String[] temp = line.split("\t");
        for(int i = 0; i<numberOfTags;i++){
            tags.add(temp[i]);
        }
        line = br.readLine();
        mostCommmonTag = line;
        for(int i = 0; i<numberOfTags+1;i++){
            line = br.readLine();
            temp = line.split("\t");
            for(int j = 0; j<numberOfTags; j++){
                transitionMatrix[i][j] = Double.parseDouble(temp[j]);
            }
        }
    }




    public static void test() throws IOException{
        PrintWriter writer = new PrintWriter(outputFileName, "UTF-8");
        BufferedReader br = new BufferedReader(new FileReader(testFileName));
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
                        writer.println(form+"|"+tag);


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
                        writer.println(form+"|"+tag);
                    }
                    previousTagIndex = tags.indexOf(tag);
                }else{
                    tag = mostCommmonTag;
                    writer.println(form+"|"+tag);
                    previousTagIndex = tags.indexOf(mostCommmonTag);
                }


            }
        }
        writer.close();
    }


}
