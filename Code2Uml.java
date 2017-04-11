package code2uml;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Code2Uml {

    public static void main(String[] args) {
        
        FileInputStream finStream = null ;
        CompilationUnit cu;
        String inputDirName = "C:\\Users\\Karan\\Downloads\\202 downloads\\cmpe202-master\\cmpe202-master\\umlparser\\uml-parser-test-1";
        File inputFile = new File(inputDirName);
        File[] inputFileList = inputFile.listFiles();
        
        String classNames ;
        String[] allFiles ;
        String[] javaFiles = new String[inputFileList.length];
        int i = 0;
        
        for(File f : inputFileList)
        {
            classNames = f.getName();
            allFiles = classNames.split("\\.");
            if("java".equals(allFiles[1].toLowerCase())){
                javaFiles[i] = allFiles[0];
                i++;
                String fileName = inputDirName + "/" + f.getName();
                try{
                    finStream = new FileInputStream(fileName);
                    cu = JavaParser.parse(finStream);
                    System.out.println(cu);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                    try{
                        finStream.close();    
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }   
}
