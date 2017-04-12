package code2uml;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Code2Uml {
    static ArrayList<String> allVariables = new ArrayList<String>();
    static ArrayList<String> javaFiles = new ArrayList();
    static String[] allFiles ;
     
    public static void main(String[] args) {
        
        FileInputStream finStream = null ;
        CompilationUnit cu;
        String inputDirName = "C:\\Users\\Karan\\Downloads\\202 downloads\\cmpe202-master\\cmpe202-master\\umlparser\\uml-parser-test-1";
        File inputFile = new File(inputDirName);
        File[] inputFileList = inputFile.listFiles();
        
        String classNames ;
       
        int i = 0;
        
        for(File f : inputFileList)
        {
            classNames = f.getName();
            allFiles = classNames.split("\\.");
            if("java".equals(allFiles[1].toLowerCase())){
                javaFiles.add(allFiles[0]);
                String fileName = inputDirName + "/" + f.getName();
                try{
                    finStream = new FileInputStream(fileName);
                    cu = JavaParser.parse(finStream);
                    
                    //calling methods to find variables
                    GetVariables getVar = new GetVariables();
                    getVar.visit(cu,null);
                    
                    //calling methods to find what classes being extended or interfaces being implemented
                    GetClassesOrInterfaces getCls = new GetClassesOrInterfaces();
                    getCls.visit(cu, 0);
                    
                    createUMLInput();
                    //System.out.println(cu+"\n\n");
                    
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

    private static void createUMLInput() {
        
        //System.out.println(javaFiles.size());
       ArrayList<String> finalOp = new ArrayList<String>() ;
           
           finalOp.add("Class "+allFiles[0] + "{\n");
           for(String var : allVariables ){
               finalOp.add(var+"\n");
           }
           finalOp.add("}\n\n");

       
       for(String s : finalOp)
       System.out.print(s);
       
       finalOp.clear();
       allVariables.clear();
    }

    //Class for fetching variables in the test classes
    private static class GetVariables extends VoidVisitorAdapter{
        
        public void visit(FieldDeclaration fd, Object obj){
            
            String classVariables;
            String variableWdBracs = fd.getVariables().toString();
           
            if(fd.getModifiers() == 2){
              classVariables = "- " + variableWdBracs.substring(1, variableWdBracs.length()-1) + " : " + fd.getType();
              allVariables.add(classVariables);
            }else if(fd.getModifiers() == 1){
              classVariables = "- " + variableWdBracs.substring(1, variableWdBracs.length()-1) + " : " + fd.getType();
              allVariables.add(classVariables);
            }       
        }
    }
    
    //Class for Extracting all the classes
    private static class GetClassesOrInterfaces extends VoidVisitorAdapter{
        
        public void visit(ClassOrInterfaceDeclaration cid, Object obj){
            
          
            
        }
        
    }

    
}
