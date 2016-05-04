package com.muge;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    static int tagIndex = 3;
    static ArrayList<String> tokens = new ArrayList<>();
    static ArrayList<String> tags = new ArrayList<>();

    static ArrayList<String> goldenTokens = new ArrayList<>();
    static ArrayList<String> goldenTags = new ArrayList<>();

    static ArrayList<String> allTags = new ArrayList<>();

    static String outputFileName = "";
    static String goldenFileName = "";


    public static void main(String[] args) throws IOException{
	// write your code here
        outputFileName = args[0];
        goldenFileName = args[1];
        if(args[2].equals("cpostag")) {
            tagIndex = 3;
        }
        if(args[2].equals("postag")) {
            tagIndex = 4;
        }

        readOutput();
        validation();
    }


    public static void readOutput() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(outputFileName));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.equals("")) {
                String token = line.substring(0,line.indexOf('|'));
                String tag = line.substring(line.indexOf('|') + 1);
                tokens.add(token);
                tags.add(tag);
                if(!allTags.contains(tag)){
                    allTags.add(tag);
                }
            }
        }

    }
    public static void validation() throws IOException {



        int truePositive = 0;
        int falsePositive = 0;
        BufferedReader br = new BufferedReader(new FileReader(goldenFileName));
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
                goldenTokens.add(form);
                goldenTags.add(realTag);
                if(!allTags.contains(realTag)){
                    allTags.add(realTag);
                }
            }
        }

        double confusionMatrix [][] = new double[allTags.size()][allTags.size()];
        double total = 0;
        for( int i = 0; i<tokens.size();i++){
            if(tokens.get(i).equals(goldenTokens.get(i))){
                if(tags.get(i).equals(goldenTags.get(i))){
                    truePositive++;
                }else{
                    falsePositive++;
                    confusionMatrix[allTags.indexOf(goldenTags.get(i))][allTags.indexOf(tags.get(i))] += 1;
                    total ++;
                }
            }
        }
        //System.out.println("TRUE: "+truePositive);
        //System.out.println("FALSE: "+falsePositive);
        System.out.print("Overall accuracy = % ");

        System.out.printf("%.2f" , (double)truePositive/(double)(truePositive+falsePositive)* 100);
        System.out.println();
        System.out.println();
        System.out.println("Confusion Matrix:");
        System.out.println("(The intersection of Adv and Noun [C(Adv, Noun)] indicates the number of times (in percentage) an item with actual tag Adv has been assign the tag Noun by the model with respect to the total number of errors.)");
        System.out.println();
        for(int j = 0; j<allTags.size();j++){
            System.out.print(allTags.get(j) + "\t");
        }

        for(int i = 0; i<allTags.size();i++){
            System.out.println();
            for(int j = 0; j<allTags.size();j++){
                if(j==0){
                    //System.out.print(allTags.get(i)+"\t");
                }
                if(i==j){
                    System.out.print(" -   \t");
                }else{
                    System.out.printf("%.2f",confusionMatrix[i][j]/total*100);
                    System.out.print("\t");
                }

            }

        }
        System.out.println();

    }
}
