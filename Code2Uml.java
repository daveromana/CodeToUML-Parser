
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class Code2Uml {

    static ArrayList<String> allVariables = new ArrayList<String>();
    static ArrayList<String> javaFiles = new ArrayList();
    static String[] allFiles;
    static ArrayList<String> isAssosiatedTo = new ArrayList<String>();
    static ArrayList<String> repAssociation = new ArrayList<String>();
    static ArrayList<String> finalOp = new ArrayList<>();
    static ArrayList<String> umlGeneratorIp = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        FileInputStream finStream = null;
        CompilationUnit cu;
        String inputDirName = "C:\\Users\\Karan\\Downloads\\202 downloads\\cmpe202-master\\cmpe202-master\\umlparser\\uml-parser-test-1";
        File inputFile = new File(inputDirName);
        File[] inputFileList = inputFile.listFiles();
        String classNames;

        //fetch all the class names in a file
        for (File f : inputFileList) {
            classNames = f.getName();
            allFiles = classNames.split("\\.");
            if ("java".equals(allFiles[1].toLowerCase())) {
                javaFiles.add(allFiles[0]);
            }
        }

        umlGeneratorIp.add("@startuml \n");

        for (File f2 : inputFileList) {
            classNames = f2.getName();
            allFiles = classNames.split("\\.");
            if ("java".equals(allFiles[1].toLowerCase())) {
                //javaFiles.add(allFiles[0]);
                String fileName = inputDirName + "/" + f2.getName();
                try {
                    finStream = new FileInputStream(fileName);
                    cu = JavaParser.parse(finStream);

                    //calling methods to find variables
                    GetVariables getVar = new GetVariables();
                    getVar.visit(cu, null);

                    //calling methods to find what classes being extended or interfaces being implemented
                    GetClassesOrInterfaces getCls = new GetClassesOrInterfaces();
                    getCls.visit(cu, 0);

                    createUMLInput();

                    //System.out.println(cu+"\n\n");
                } catch (ParseException | FileNotFoundException e) {
                } finally {
                    try {
                        finStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        umlGeneratorIp.add("@enduml");

        /*System.out.println();
        umlGeneratorIp.forEach((s) -> {
            System.out.print(s);
        });*/
        String umlGeneratorIpStr = umlGeneratorIp.toString().replaceAll(",", " ").replaceFirst(Pattern.quote("["), " ");
        umlGeneratorIpStr = umlGeneratorIpStr.substring(0, umlGeneratorIpStr.length() - 1);
        System.out.println(umlGeneratorIpStr);

        // final call to plantUmlGenerator
        new ClassDiagramGenerator().createClassDiagram(umlGeneratorIpStr);
    }

    private static void createUMLInput() {

        //System.out.println(javaFiles.size());
        finalOp.add("Class " + allFiles[0]);
        finalOp.add("{\n");
        allVariables.forEach((var) -> {
            finalOp.add(var);
            finalOp.add("\n");
        });
        finalOp.add("}\n\n");

        Iterator<String> iter = finalOp.iterator();

        while (iter.hasNext()) {
            String b = iter.next();

            repAssociation.stream().filter((a) -> (a.equals(b))).forEachOrdered((_item) -> {
                iter.remove();
            });
        }

        System.out.println();
        finalOp.forEach((s) -> {
            umlGeneratorIp.add(s);
            //System.out.print(s);
        });

        //System.out.println();
        finalOp.clear();
        allVariables.clear();
        isAssosiatedTo.clear();
    }

    //Class for fetching variables in the test classes
    private static class GetVariables extends VoidVisitorAdapter {

        @Override
        public void visit(FieldDeclaration fd, Object obj) {

            String classVariables;
            String variableWdBracs = fd.getVariables().toString();
            ArrayList<String> types = new ArrayList<>();
            types.add(fd.getType().toString());

            if (fd.getModifiers() == 2) {
                classVariables = "- " + variableWdBracs.substring(1, variableWdBracs.length() - 1) + " : " + fd.getType();
                allVariables.add(classVariables);
            } else if (fd.getModifiers() == 1) {
                classVariables = "- " + variableWdBracs.substring(1, variableWdBracs.length() - 1) + " : " + fd.getType();
                allVariables.add(classVariables);
            }

            //Association Logic goes here..
            for (String s : types) {
                //System.out.print(s+" - ");
                for (String className : javaFiles) {

                    if (isAssosiatedTo.contains(s)) {
                        break;
                    }
                    if (className.equals(s)) {
                        isAssosiatedTo.add(s);
                        finalOp.add(allFiles[0] + "--" + s);
                        finalOp.add("\n");
                        repAssociation.add(s + "--" + allFiles[0]);
                    } else if (s.contains("<" + className + ">")) {
                        int beginIndex = s.indexOf("<");
                        int endIndex = s.indexOf(">");
                        //System.out.println(s.substring(beginIndex+1, endIndex));
                        isAssosiatedTo.add(s.substring(beginIndex + 1, endIndex));
                        finalOp.add(allFiles[0] + "--" + s.substring(beginIndex + 1, endIndex));
                        finalOp.add("\n");
                        repAssociation.add(s.substring(beginIndex + 1, endIndex) + "--" + allFiles[0]);
                    }
                }
            }
        }
    }

    //Class for Extracting all the classes
    private static class GetClassesOrInterfaces extends VoidVisitorAdapter {

        @Override
        public void visit(ClassOrInterfaceDeclaration cid, Object obj) {

        }

    }

}
