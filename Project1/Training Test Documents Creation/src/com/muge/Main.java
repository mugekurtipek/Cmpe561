package com.muge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    static String dirPath = "";
    static String trainingPath = "";
    static String testPath = "";
    public static void main(String[] args) throws IOException {
        dirPath = args[0];
        trainingPath = args[1];
        testPath = args[2];
        File fileTraining = new File(trainingPath);
        if (!fileTraining.exists()) {
            if (fileTraining.mkdir()) {

            } else {
                System.out.println("Failed to create training directory!");
            }
        }

        File fileTest = new File(testPath);
        if (!fileTest.exists()) {
            if (fileTest.mkdir()) {

            } else {
                System.out.println("Failed to create test directory!");
            }
        }

        File folder = new File(dirPath);
        for(int i= 0; i<folder.list().length;i++){
            if (folder.list()[i].contains(".DS_Store")) {
                continue;
            }
            String filePath = dirPath+"/"+folder.list()[i];
            String newTrainingPath = trainingPath +"/"+folder.list()[i];
            String newTestPath = testPath +"/"+folder.list()[i];
            fileTraining = new File(newTrainingPath);
            if (!fileTraining.exists()) {
                if (fileTraining.mkdir()) {

                } else {
                    System.out.println("Failed to create "+folder.list()[i]+" directory!");
                }
            }
            fileTest = new File(newTestPath);
            if (!fileTest.exists()) {
                if (fileTest.mkdir()) {

                } else {
                    System.out.println("Failed to create "+folder.list()[i]+" directory!");
                }
            }

            File file = new File(filePath);
            ArrayList<String> allWritingsOfAWriter = new ArrayList<>();
            ArrayList<String> allWritingsOfAWriterNames = new ArrayList<>();
            for(int j= 0; j<file.list().length; j++){
                if (file.list()[j].contains(".DS_Store")) {
                    continue;
                }
                allWritingsOfAWriter.add(filePath+"/"+file.list()[j]);
                allWritingsOfAWriterNames.add(file.list()[j]);
            }

            int trainingNumber = allWritingsOfAWriter.size() * 60 / 100;
            int checkCount = 0;
            while(checkCount != trainingNumber){
                Random random = new Random();
                int rnd = random.nextInt(allWritingsOfAWriter.size());
                File tempFile = new File(allWritingsOfAWriter.get(rnd));
                tempFile.renameTo(new File(newTrainingPath+"/"+allWritingsOfAWriterNames.get(rnd)));
                //Files.move(Paths.get(allWritingsOfAWriter.get(rnd)),Paths.get(newTrainingPath));
                allWritingsOfAWriter.remove(rnd);
                allWritingsOfAWriterNames.remove(rnd);
                checkCount++;
            }
            for(int j = 0; j<allWritingsOfAWriter.size();j++){
                File tempFile = new File(allWritingsOfAWriter.get(j));
                tempFile.renameTo(new File(newTestPath+"/"+allWritingsOfAWriterNames.get(j)));
               // Files.move(Paths.get(allWritingsOfAWriter.get(j)),Paths.get(newTestPath));
            }

        }

    }

}
