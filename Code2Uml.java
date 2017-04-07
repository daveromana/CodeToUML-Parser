package code2uml;

import java.io.File;

public class Code2Uml {

    public static void main(String[] args) {
        
        String inputDirName = "C:\\Users\\Karan\\Downloads\\202 downloads\\cmpe202-master\\cmpe202-master\\umlparser\\uml-parser-test-1";
        File inputFile = new File(inputDirName);
        File[] inputFileList = inputFile.listFiles();
        
        String classNames = null;
        String[] allFiles = null;
        String[] javaFiles = new String[inputFileList.length];
        int i = 0;
        for(File f : inputFileList)
        {
            classNames = f.getName();
            allFiles = classNames.split("\\.");
            if("java".equals(allFiles[1].toLowerCase())){
                javaFiles[i] = allFiles[0];
                i++;
            }
        }
        for(String s : javaFiles){
            System.out.println(s);
        }
    }   
}
