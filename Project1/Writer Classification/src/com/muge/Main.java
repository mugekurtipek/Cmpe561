package com.muge;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

public class Main {

    static String trainingPath = "";
    static String testPath = "";

    static ArrayList<String> trainingPaths = new ArrayList<>(); //Keeps training document's paths
    static ArrayList<Integer> numberOfTrainingPathsInClass = new ArrayList<>(); //Keeps how many training document exist for each class
    static ArrayList<Integer> numberOfTestPathsInClass = new ArrayList<>(); //Keeps how many test document exist for each class
    static ArrayList<String> testPaths = new ArrayList<>(); //Keeps the test document's paths
    static ArrayList<Integer> classWordSize = new ArrayList<>(); //Keeps how many words used in each class. This will be used in calculatin naive bayes
    static ArrayList<String> namesOfClasses = new ArrayList<>(); //Keeps the name of each class

    static ArrayList<Double> feature1Set = new ArrayList<>(); //Feature1 is average number of comma usage
    static ArrayList<Double> feature1SetMean = new ArrayList<>();
    static ArrayList<Double> feature1SetVariance = new ArrayList<>();
    static ArrayList<Double> feature1TestSet = new ArrayList<>();

    static ArrayList<Double> feature2Set = new ArrayList<>(); //Feature2 is number of word usage in the document
    static ArrayList<Double> feature2SetMean = new ArrayList<>();
    static ArrayList<Double> feature2SetVariance = new ArrayList<>();
    static ArrayList<Double> feature2TestSet = new ArrayList<>();

    static ArrayList<Integer> truePositive = new ArrayList<>();
    static ArrayList<Integer> falsePositive = new ArrayList<>();

    static ArrayList<Integer> truePositiveFeature = new ArrayList<>();
    static ArrayList<Integer> falsePositiveFeature = new ArrayList<>();


    static HashMap<String, ArrayList<Integer>> matrix = new HashMap<>(); //The main matrix. As key values it keeps the words in the vocabulary.
    // As values it keeps an arraylist. Each index in the arraylist represents a document and value of that index represent
    // the number of occurances of that word in the document.

    static HashMap<String, ArrayList<Integer>> matrixClassSum = new HashMap<>(); //The main matrix. As key values it keeps the words in the vocabulary.
    // As values it keeps an arraylist. Each index in the arraylist represents a class and value of that index represent
    // the number of occurances of that word in the class.





    public static void main(String[] args) throws IOException {
        trainingPath = args[0];
        testPath = args[1];
        createTrainingTest();
        tokenization();
        calculateFeatureSet();
        createSumMatrix();
        calculateNaiveBayes();

    }

    public static void createTrainingTest() throws IOException {
        File folder = new File(trainingPath);

        for(int i = 0; i<folder.list().length;i++){
            if(folder.list()[i].contains(".DS_Store")){
                continue;
            }
            String filePath = trainingPath+"/"+folder.list()[i];
            namesOfClasses.add(folder.list()[i]);
            File file = new File(filePath);
            int check = 0;
            for(int j= 0; j<file.list().length;j++){
                if(file.list()[j].contains(".DS_Store")){
                    check = 1;
                    continue;
                }
                trainingPaths.add(filePath+"/"+file.list()[j]);
                feature1Set.add(0.0);
                feature2Set.add(0.0);
            }
            numberOfTrainingPathsInClass.add(file.list().length - check);

        }

        folder = new File(testPath);
        for(int i = 0; i<folder.list().length;i++){
            if(folder.list()[i].contains(".DS_Store")){
                continue;
            }
            String filePath = testPath+"/"+folder.list()[i];
            File file = new File(filePath);
            int check = 0;
            for(int j= 0; j<file.list().length;j++){
                if(file.list()[j].contains(".DS_Store")){
                    check++;
                    continue;
                }
                testPaths.add(filePath+"/"+file.list()[j]);
                feature1TestSet.add(0.0);
                feature2TestSet.add(0.0);
            }
            numberOfTestPathsInClass.add(file.list().length - check);

        }

    }

    public static void tokenization() throws IOException {
        for(int i = 0; i<trainingPaths.size();i++){ //Tokenize every training set and create the dictionary
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(trainingPaths.get(i)), "cp1254"));
            String line = "";
            while ((line = br.readLine()) != null) { //Reading the doc line by line
                String[] elements = line.split("\\s+");

                for(int z = 0; z<elements.length;z++){
                    String element = elements[z];

                    element = element.toLowerCase();
                    if(element.contains(",")){  //I'm checking the average comma usage as a feature set
                        feature1Set.set(i,feature1Set.get(i) + 1);
                    }
                    element = element.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", ""); //to remove the punctiations at the beginning and end of the words
                    if(element.contains("’")){   //to remove the characters after ’
                        int index = element.indexOf("’");
                        element = element.substring(0,index);
                    }
                    if(element.contains("'")){   //to remove the characters after '
                        int index = element.indexOf("'");
                        element = element.substring(0,index);
                    }
                    if(element.matches("\\s+") || element.equals("")){ //removing space and empty tokens
                        continue;
                    }
                    feature2Set.set(i,feature2Set.get(i) + 1); //Counting number of words in a document as a feature set

                    if(!matrix.containsKey(element)){
                        ArrayList<Integer> newRow = new ArrayList<>();
                        for(int j = 0; j<trainingPaths.size();j++){
                            if(j==i){
                                newRow.add(1);
                            }else{
                                newRow.add(0);
                            }
                        }
                        matrix.put(element,newRow);
                    }else{
                        ArrayList<Integer> newRow = matrix.get(element);
                        newRow.set(i, newRow.get(i) + 1);
                        matrix.put(element,newRow);
                    }

                }
            }
            br.close();
        }

        for (String key : matrix.keySet()) { //To add the test documents to the dictionary
            ArrayList<Integer> row = matrix.get(key);
            for(int j = 0; j<testPaths.size(); j++){
                row.add(0);
            }
        }//Now our matrix's column number is 910


        for(int i = 0; i<testPaths.size();i++){ //Tokenize every test set and add them to the dictionary
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(testPaths.get(i)), "cp1254"));
            String line = "";
            while ((line = br.readLine()) != null) { //Reading the doc line by line
                String[] elements = line.split("\\s+");

                for(int z = 0; z<elements.length;z++){
                    String element = elements[z];
                    element = element.toLowerCase();
                    if(element.contains(",")){  //I'm checking the average comma usage as a feature set
                        feature1TestSet.set(i,feature1TestSet.get(i) + 1);
                    }
                    element = element.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", ""); //to remove the punctiations at the beginning and end of the words
                    if(element.contains("’")){   //to remove the characters after ’
                        int index = element.indexOf("’");
                        element = element.substring(0,index);
                    }
                    if(element.contains("'")){   //to remove the characters after '
                        int index = element.indexOf("'");
                        element = element.substring(0,index);
                    }
                    if(element.matches("\\s+") || element.equals("")){ //removing space and empty tokens
                        continue;
                    }
                    feature2TestSet.set(i,feature2TestSet.get(i)+1);
                    if(!matrix.containsKey(element)){
                        //Do nothing
                    }else{

                        ArrayList<Integer> newRow = matrix.get(element);
                        newRow.set(trainingPaths.size() + i, newRow.get(trainingPaths.size() + i) + 1);
                        matrix.put(element,newRow);
                    }

                }
            }
            br.close();
        }

    }




    public static void createSumMatrix(){ //To change the matrix
        for(int j = 0; j< numberOfTrainingPathsInClass.size(); j++){
            classWordSize.add(0);
        }
        for(String key: matrix.keySet()){
            ArrayList<Integer> row = new ArrayList<>();
            int index = 0;
            for(int j = 0; j< numberOfTrainingPathsInClass.size(); j++){
                int classScore = 0;
                for(int k= 0; k<numberOfTrainingPathsInClass.get(j);k++){
                    classScore += matrix.get(key).get(index + k);
                }
                classWordSize.set(j,classWordSize.get(j) + classScore);

                index += numberOfTrainingPathsInClass.get(j);
                row.add(classScore);
            }

            matrixClassSum.put(key,row);
        }
    }

    public static void calculateNaiveBayes(){

        initializeArrays();
        int numberOfClasses = numberOfTrainingPathsInClass.size();

        int mainScore = 0;
        int mainScoreWithFeature = 0;
        for(int i = trainingPaths.size(); i< trainingPaths.size() + testPaths.size() ; i++){
            double[] classCount = new double[numberOfClasses];
            double[] classCountWithFeature = new double[numberOfClasses];
            for(int k= 0; k<numberOfClasses;k++){
                classCount[k] = 1.0;
            }
            for (String key : matrix.keySet()) {
                if(matrix.get(key).get(i) != 0){
                   // System.out.println("AAA");
                    for(int j= 0; j<numberOfClasses; j++){
                        classCount[j] += Math.log10( (double) (matrixClassSum.get(key).get(j) + 0.05 ) / (double) (classWordSize.get(j) + 0.05 * matrixClassSum.size()));

                    }

                }

            }

            for(int j= 0; j<numberOfClasses; j++){
                classCount[j] += Math.log10((double)(numberOfTrainingPathsInClass.get(j))/(double) (trainingPaths.size()));

            }


            for(int m = 0; m<namesOfClasses.size();m++){
                double variance = feature1SetVariance.get(m);
                double mean = feature1SetMean.get(m);
                double x = feature1TestSet.get(i - trainingPaths.size());
                double base = 1/Math.sqrt(2*Math.PI*variance);
                double pow = -(Math.pow((x-mean), 2)/(2*variance));

                double variance2 = feature2SetVariance.get(m);
                double mean2 = feature2SetMean.get(m);
                double x2 = feature2TestSet.get(i - trainingPaths.size());
                double base2 = 1/Math.sqrt(2*Math.PI*variance2);
                double pow2 = -(Math.pow((x2-mean2), 2)/(2*variance2));
                classCountWithFeature[m] = classCount[m] + Math.log10(base * Math.exp(pow))+ Math.log10(base2 * Math.exp(pow2));

            }

            double maxProbability =  classCount[0];
            int maxProbabilityIndex = 0;
            for(int j= 0; j<numberOfClasses; j++){

                if(classCount[j] > maxProbability){
                    maxProbability = classCount[j];
                    maxProbabilityIndex = j;
                }

            }
            if(testPaths.get(i - trainingPaths.size()).contains(namesOfClasses.get(maxProbabilityIndex))){
                mainScore++;
                truePositive.set(maxProbabilityIndex, truePositive.get(maxProbabilityIndex) + 1);

            }else{
                falsePositive.set(maxProbabilityIndex,falsePositive.get(maxProbabilityIndex) + 1);
            }


            maxProbability =  classCountWithFeature[0];
            maxProbabilityIndex = 0;
            for(int j= 0; j<numberOfClasses; j++){

                if(classCountWithFeature[j] > maxProbability){
                    maxProbability = classCountWithFeature[j];
                    maxProbabilityIndex = j;
                }

            }
            if(testPaths.get(i - trainingPaths.size()).contains(namesOfClasses.get(maxProbabilityIndex))){
                mainScoreWithFeature++;
                truePositiveFeature.set(maxProbabilityIndex, truePositiveFeature.get(maxProbabilityIndex) + 1);
            }
            else{
                falsePositiveFeature.set(maxProbabilityIndex,falsePositiveFeature.get(maxProbabilityIndex) + 1);
            }
        }
        calculateOutputs(true,truePositive,falsePositive);
        calculateOutputs(false,truePositiveFeature,falsePositiveFeature);


    }

    public static void calculateFeatureSet(){
        int index = 0;
        for(int j= 0; j<numberOfTrainingPathsInClass.size();j++){
            double mean = 0.0;
            double var = 0.0;
            double mean2 = 0.0;
            double var2 = 0.0;
            for(int i = 0; i<numberOfTrainingPathsInClass.get(j);i++){
                mean += feature1Set.get(index + i);
                mean2 += feature2Set.get(index+i);
            }
            mean = mean / numberOfTrainingPathsInClass.get(j);
            mean2 = mean2 / numberOfTrainingPathsInClass.get(j);
            for(int i = 0; i<numberOfTrainingPathsInClass.get(j);i++){
                var += (feature1Set.get(index + i) - mean) * (feature1Set.get(index + i) - mean) ;
                var2 += (feature2Set.get(index + i) - mean2) * (feature2Set.get(index + i) - mean2) ;
            }
            var = var / numberOfTrainingPathsInClass.get(j);
            var2 = var2 / numberOfTrainingPathsInClass.get(j);
            feature1SetVariance.add(var);
            feature1SetMean.add(mean);
            feature2SetMean.add(mean2);
            feature2SetVariance.add(var2);
            index += numberOfTrainingPathsInClass.get(j);

        }

    }

    public static void calculateOutputs(boolean isBW,ArrayList<Integer> truePositive, ArrayList<Integer> falsePositive){ //To print the outputs
        double macroAverageRecall = 0.0;
        double macroAveragePrecision = 0.0;
        double macroAverageFMeasure = 0.0;
        double microAverageTP = 0.0;
        double microAverageFN = 0.0;
        double microAverageFP = 0.0;

        for(int i = 0; i<namesOfClasses.size();i++){
            if(truePositive.get(i) + falsePositive.get(i) != 0){
                macroAveragePrecision += (double)truePositive.get(i) / ((double) truePositive.get(i) + (double) falsePositive.get(i));
            }
            macroAverageRecall += (double)truePositive.get(i) / (double)numberOfTestPathsInClass.get(i);
            microAverageTP += (double) truePositive.get(i);
            microAverageFP += (double) falsePositive.get(i);
            microAverageFN += (double) numberOfTestPathsInClass.get(i) - (double)truePositive.get(i);
        }
        macroAveragePrecision = macroAveragePrecision / namesOfClasses.size();
        macroAverageRecall = macroAverageRecall / namesOfClasses.size();
        macroAverageFMeasure = 2 * macroAveragePrecision * macroAverageRecall / (macroAverageRecall + macroAveragePrecision);
        if(isBW){
            System.out.println("\nMacro-Average: (BoW only)");
        }
        else{
            System.out.println("\nMacro-Average: (BoW + FeatureSet)");
        }
        System.out.println("Precision: "+ macroAveragePrecision);
        System.out.println("Recall: "+ macroAverageRecall);
        System.out.println("F Measure: "+ macroAverageFMeasure);

        double microAverageRecall = microAverageTP / (microAverageTP + microAverageFN);
        double microAveragePrecision = microAverageTP / (microAverageTP + microAverageFP);
        double microAverageFMeasure = 2 * microAveragePrecision * microAverageRecall / (microAverageRecall + microAveragePrecision);
        if(isBW){
            System.out.println("\nMicro-Average: (BoW only)");
        }
        else{
            System.out.println("\nMicro-Average: (BoW + FeatureSet)");
        }
        System.out.println("Precision: "+ microAveragePrecision);
        System.out.println("Recall: "+ microAverageRecall);
        System.out.println("F Measure: "+ microAverageFMeasure);


    }
    public static void initializeArrays(){ //To fill the related arrays with 0
        for(int i = 0; i<namesOfClasses.size();i++){
            truePositive.add(0);

            falsePositive.add(0);

            truePositiveFeature.add(0);

            falsePositiveFeature.add(0);
        }
    }

}
