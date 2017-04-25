
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Code2Uml {

    static CompilationUnit cu;
    static ArrayList<String> javaFiles = new ArrayList();
    static ArrayList<String> javaClassFiles = new ArrayList<>();
    static ArrayList<String> interfaceNames = new ArrayList<>();
    static String[] allFiles;
    static boolean isInterface;
    static String currentClass;
    static ArrayList<String> test = new ArrayList<>();
    //variables used in fetching all the fields
    static ArrayList<String> allVariables = new ArrayList<String>();
    static ArrayList<String> isAssosiatedTo = new ArrayList<String>();
    static ArrayList<String> assosiation = new ArrayList<>();
    static ArrayList<String> repAssociation1 = new ArrayList<String>();
    static ArrayList<String> repAssociation2 = new ArrayList<String>();
    static ArrayList<String> finalOp = new ArrayList<>();
    static ArrayList<String> umlGeneratorIp = new ArrayList<>();
    static ArrayList<String> varNames = new ArrayList<>();
    //variables to collect Constructors
    static ArrayList<String> allConstructors = new ArrayList<>();
    //variables used in finding all the methods
    static ArrayList<String> allMethods = new ArrayList<String>();
    static ArrayList<String> methodNames = new ArrayList<String>();
    static ArrayList<String> repDependency = new ArrayList<String>();
    static ArrayList<String> isDependentTo = new ArrayList<String>();
    //variable to get interfaces and child classes
    static ArrayList<String> allInterfaceNClasses = new ArrayList<String>();
    //variables to identify getterSetter
    static ArrayList<String> getterSetter = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        FileInputStream finStream = null;
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
                //get all interfaces name
                String fname = inputDirName + "/" + classNames;
                finStream = new FileInputStream(fname);
                try {
                    cu = JavaParser.parse(finStream);
                    if (cu.toString().contains(" interface ")) {
                        interfaceNames.add(allFiles[0]);
                    } else {
                        javaClassFiles.add(allFiles[0]);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Code2Uml.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        umlGeneratorIp.add("@startuml \n");
        for (String f : javaFiles) {
            String fileName = inputDirName + "/" + f + ".java";
            try {
                currentClass = f;
                finStream = new FileInputStream(fileName);
                cu = JavaParser.parse(finStream);
                isInterface = cu.toString().contains(" interface ");
                //calling methods to find variables
                GetVariables getVar = new GetVariables();
                getVar.visit(cu, null);
                //calling methods to fetch constructors
                GetConstructors gC = new GetConstructors();
                gC.visit(cu, null);
                //calling methods to get all methods in the test cases
                GetMethods getMet = new GetMethods();
                getMet.visit(cu, null);
                //calling methods to find what classes being extended or interfaces being implemented
                GetClassesOrInterfaces getCls = new GetClassesOrInterfaces();
                getCls.visit(cu, 0);
                createUMLInput();
                //System.out.println(cu+"\n\n");
            } catch (ParseException | FileNotFoundException e) {
            } finally {
                try {
                    finStream.close();
                    //break;
                } catch (IOException e) {
                }
            }
        }
        umlGeneratorIp.add("@enduml");
        int i = 0;
        String umlGeneratorIpStr = "";
        for (String s : umlGeneratorIp) {
            if (i == 0) {
                umlGeneratorIpStr = s;
                i++;
            } else {
                umlGeneratorIpStr = umlGeneratorIpStr + s;
            }
        }
        System.out.println(umlGeneratorIpStr);
        // final call to plantUmlGenerator
        new ClassDiagramGenerator().createClassDiagram(umlGeneratorIpStr);
    }

    private static void createUMLInput() {
        if (isInterface) {
            finalOp.add("interface " + currentClass);
        } else {
            finalOp.add("class " + currentClass);
        }
        finalOp.add("{\n");
        allVariables.forEach((var) -> {
            finalOp.add(var);
            finalOp.add("\n");
        });
        for (String a : allConstructors) {
            finalOp.add(a);
            //finalOp.add("\n");
        }
        for (String a : allMethods) {
            finalOp.add(a);
        }
        finalOp.add(
                "\n}\n\n");
        finalOp.forEach(
                (s) -> {
                    umlGeneratorIp.add(s);
                    //System.out.print(s);
                }
        );
        finalOp.clear();
        allVariables.clear();
        isAssosiatedTo.clear();
        allConstructors.clear();
        allMethods.clear();
        varNames.clear();
    }

    //Class for fetching variables in the test classes
    private static class GetVariables extends VoidVisitorAdapter {

        @Override
        public void visit(FieldDeclaration fd, Object obj) {
            String classVariables;
            String variables = fd.getVariables().toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("^\\s+", "").replaceAll("\\s+$", "");
            String types = fd.getType().toString();
            boolean isAssociated = false;
            //Association Logic goes here..if there is double association then omit it 
            for (String className : javaFiles) {
                if (isAssosiatedTo.contains(types)) {
                    break;
                }
                if (className.equals(types)) {
                    isAssosiatedTo.add(types);
                    assosiation.add(currentClass + "--" + types);
                    assosiation.add("\n");
                    repAssociation1.add(types + "--" + currentClass);
                } else if (types.contains("<" + className + ">")) {
                    isAssociated = true;
                    int beginIndex = types.indexOf("<");
                    int endIndex = types.indexOf(">");
                    isAssosiatedTo.add(types.substring(beginIndex + 1, endIndex));
                    assosiation.add(currentClass + "--*" + types.substring(beginIndex + 1, endIndex));
                    assosiation.add("\n");
                    repAssociation2.add(types.substring(beginIndex + 1, endIndex) + "--*" + currentClass);
                }
            }
            String b = null;
            Iterator<String> iter1 = assosiation.iterator();
            while (iter1.hasNext()) {
                b = iter1.next();
                for (String a : repAssociation1) {
                    if (b.equals(a.replace("*", ""))) {
                        iter1.remove();
                    }
                }
            }
            String c = null;
            Iterator<String> iter2 = assosiation.iterator();
            while (iter2.hasNext()) {
                c = iter2.next();
                for (String a : repAssociation2) {
                    if (c.equals(a.replace("*", ""))) {
                        iter2.remove();
                    }
                }
            }
            for (String s : assosiation) {
                finalOp.add(s);
            }
            assosiation.clear();
            varNames.add(fd.getVariables().toString().replaceAll("\\[", "").replaceAll("]", "").trim());
            String[] v;
            if (variables.contains("=")) {
                if (!isAssociated) {
                    v = variables.split("=");
                    if (fd.getModifiers() == 1) {
                        classVariables = "+ " + v[0].trim() + " : " + fd.getType();
                        allVariables.add(classVariables);
                    } else if (fd.getModifiers() == 2) {
                        classVariables = "- " + v[0].trim() + " : " + fd.getType();
                        allVariables.add(classVariables);
                    }
                }
            } else {
                if (fd.getModifiers() == 1) {
                    classVariables = "+ " + variables + " : " + fd.getType();
                    allVariables.add(classVariables);
                } else if (fd.getModifiers() == 2) {
                    classVariables = "- " + variables + " : " + fd.getType();
                    allVariables.add(classVariables);
                }
            }
        }
    }
//Class for extracting all methods in the test codes

    private static class GetMethods extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(MethodDeclaration md, Object o) {
            String methods;
            if (md.getBody() != null) {
                if (md.getBody().getStmts() != null) {
                    for (int i = 0; i < md.getBody().getStmts().size(); i++) {
                        String statement = md.getBody().getStmts().get(i).toString();
                        if (statement.contains(" = new") || statement.contains(" =new")) {
                            String[] reference = statement.split("=");
                            for (String classname : interfaceNames) {
                                if (reference[0].toString().contains(classname) && isDependentTo.contains(currentClass + " ..> " + classname) == false) {
                                    isDependentTo.add(currentClass + " ..> " + classname);
                                    finalOp.add(currentClass + "..>" + classname);
                                    finalOp.add("\n");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (varNames.isEmpty()) {
                if (md.getParameters() == null) {
                    if (md.getModifiers() == 1) {
                        methods = "+ " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    } else if (md.getModifiers() == 0) {
                        methods = "- " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    } else if (md.getModifiers() == 1025) {
                        methods = "+ " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    } else if (md.getModifiers() == 9) {
                        methods = "+ {static} " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    }
                } else {
                    String[] param = md.getParameters().toString().replace("]", "").replace("[", "").split(",");
                    String[] singleParam;
                    int numOfPrm = md.getParameters().size();
                    if (numOfPrm == 1) {
                        singleParam = param[0].split(" ");
                        for (String a : javaFiles) {
                            if (isDependentTo.contains(currentClass + "..>" + a)) {
                                break;
                            } else {
                                if (a.equals(singleParam[0]) && interfaceNames.toString().contains(singleParam[0]) && isInterface == false) {
                                    isDependentTo.add(currentClass + "..>" + a);
                                    finalOp.add(currentClass + "..>" + a);
                                    finalOp.add("\n");
                                    break;
                                    //repDependency.add(a+ "<..")
                                }
                            }
                        }
                        if (md.getModifiers() == 1) {
                            methods = "+ " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 0) {
                            methods = "- " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 1025) {
                            methods = "+ " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 9) {
                            if (md.getName().equals("main")) {
                                methods = "+ {static} " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + "[]) : " + md.getType();
                                allMethods.add(methods);
                                allMethods.add("\n");
                            } else {
                                methods = "+ {static} " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                                allMethods.add(methods);
                                allMethods.add("\n");
                            }
                        }
                    } else {
                        if (md.getModifiers() == 1) {
                            methods = "+ " + md.getName() + "(";
                            allMethods.add(methods);
                        } else if (md.getModifiers() == 0) {
                            methods = "- " + md.getName() + "(";
                            allMethods.add(methods);
                        } else if (md.getModifiers() == 1025) {// add something here which is missing 
                            methods = "+ " + md.getName() + "( ";
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 9) {
                            methods = "+ {static} " + md.getName() + "( ";
                            allMethods.add(methods);
                            allMethods.add("\n");
                        }
                        for (String prm : param) {
                            String[] parName = prm.replaceAll("^\\s+", "").replaceAll("\\s+$", "").split(" ");
                            for (String a : javaFiles) {
                                if (a.equals(parName[0]) && isInterface == false && interfaceNames.toString().contains(parName[0])) {
                                    if (isDependentTo.contains(currentClass + "..>" + a)) {
                                        break;
                                    } else {
                                        isDependentTo.add(currentClass + "..>" + a);
                                        finalOp.add(currentClass + "..>" + a);
                                        finalOp.add("\n");
                                    }
                                }
                            }
                            methods = parName[1] + ":" + parName[0];
                            allMethods.add(methods);
                            allMethods.add(",");
                        }
                        allMethods.add("):");
                        allMethods.add(md.getType().toString());
                    }
                    if (numOfPrm > 1) {
                        allMethods.remove(allMethods.lastIndexOf(","));
                    }
                }
            } else {
                for (String s : varNames) {
                    if (md.getName().toLowerCase().equals("get" + s) || md.getName().toLowerCase().equals("set" + s)) {
                        Iterator<String> iter = allVariables.iterator();
                        String newVar = new String();
                        while (iter.hasNext()) {
                            String str = iter.next();
                            String[] a = str.split(" ");
                            if (a[1].equals(s)) {
                                iter.remove();
                                newVar = str.replace("-", "+");
                            }
                        }
                        allVariables.add(newVar);
                        break;
                    }
                }
                if (md.getParameters() == null) {
                    if (md.getModifiers() == 1) {
                        methods = "+ " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    } else if (md.getModifiers() == 0) {
                        methods = "- " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    } else if (md.getModifiers() == 1025) {
                        methods = "+ " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    } else if (md.getModifiers() == 9) {
                        methods = "+ {static} " + md.getName() + "() : " + md.getType();
                        allMethods.add(methods);
                        allMethods.add("\n");
                    }
                } else {
                    String[] param = md.getParameters().toString().replace("]", "").replace("[", "").split(",");
                    String[] singleParam;
                    int numOfPrm = md.getParameters().size();
                    if (numOfPrm == 1) {
                        singleParam = param[0].split(" ");
                        for (String a : javaFiles) {
                            if (a.equals(singleParam[0]) && interfaceNames.toString().contains(singleParam[0]) && isInterface == false) {
                                //System.out.println("code running "+parName[0]+"..>"+a);
                                if (isDependentTo.contains(currentClass + "..>" + a)) {
                                    break;
                                } else {
                                    isDependentTo.add(currentClass + "..>" + a);
                                    finalOp.add(currentClass + "..>" + a);
                                    finalOp.add("\n");
                                }
                            }
                        }
                        if (md.getModifiers() == 1) {
                            methods = "+ " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 0) {
                            methods = "- " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 1025) {
                            methods = "+ " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 9) {
                            if (md.getName().equals("main")) {
                                methods = "+ {static} " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + "[]) : " + md.getType();
                                allMethods.add(methods);
                                allMethods.add("\n");
                            } else {
                                methods = "+ {static} " + md.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ") : " + md.getType();
                                allMethods.add(methods);
                                allMethods.add("\n");
                            }
                        }
                    } else {
                        if (md.getModifiers() == 1) {
                            methods = "+ " + md.getName() + "(";
                            allMethods.add(methods);
                        } else if (md.getModifiers() == 0) {
                            methods = "- " + md.getName() + "(";
                            allMethods.add(methods);
                        } else if (md.getModifiers() == 1025) {
                            methods = "+ " + md.getName() + "( ";
                            allMethods.add(methods);
                            allMethods.add("\n");
                        } else if (md.getModifiers() == 9) {
                            methods = "+ {static} " + md.getName() + "( ";
                            allMethods.add(methods);
                            allMethods.add("\n");
                        }
                        for (String prm : param) {
                            String[] parName = prm.replaceAll("^\\s+", "").replaceAll("\\s+$", "").split(" ");
                            for (String a : javaFiles) {
                                if (a.equals(parName[0]) && interfaceNames.toString().contains(parName[0]) && isInterface == false) {
                                    if (isDependentTo.contains(currentClass + "..>" + a)) {
                                        break;
                                    } else {
                                        isDependentTo.add(currentClass + "..>" + a);
                                        finalOp.add(currentClass + "..>" + a);
                                        finalOp.add("\n");
                                    }
                                }
                            }
                            methods = parName[1] + ":" + parName[0];
                            allMethods.add(methods);
                            allMethods.add(",");
                        }
                        allMethods.add("):");
                        allMethods.add(md.getType().toString());
                    }
                    if (numOfPrm > 1) {
                        allMethods.remove(allMethods.lastIndexOf(","));
                    }
                }
            }
        }
    }
//Class for Extracting all the Constructors

    private static class GetConstructors extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(ConstructorDeclaration consDec, Object obj) {
            String consName = (consDec.getName());
            int modifier = consDec.getModifiers();
            if (consDec.getParameters() == null) {
                if (modifier == 1) {
                    allConstructors.add("+" + consName + "()");
                    allConstructors.add("\n");
                } else if (modifier == 2) {
                    allConstructors.add("-" + consName + "()");
                    allConstructors.add("\n");
                }
            } else {
                String[] param = consDec.getParameters().toString().replace("]", "").replace("[", "").split(",");
                String[] singleParam;
                int numOfPrm = consDec.getParameters().size();
                if (numOfPrm == 1) {
                    singleParam = param[0].split(" ");
                    for (String a : javaFiles) {
                        if (isDependentTo.contains(currentClass + "..>" + a)) {
                            break;
                        } else {
                            if (a.equals(singleParam[0]) && interfaceNames.toString().contains(singleParam[0]) && isInterface == false) {
                                isDependentTo.add(currentClass + "..>" + a);
                                finalOp.add(currentClass + "..>" + a);
                                finalOp.add("\n");
                                break;
                                //repDependency.add(a+ "<..")
                            }
                        }
                    }
                    if (consDec.getModifiers() == 1) {
                        consName = "+ " + consDec.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ")";
                        allConstructors.add(consName);
                        allConstructors.add("\n");
                    } else if (consDec.getModifiers() == 0) {
                        consName = "- " + consDec.getName() + "( " + singleParam[1] + ": " + singleParam[0] + ")";
                        allConstructors.add(consName);
                        allConstructors.add("\n");
                    }
                } else {
                    if (consDec.getModifiers() == 1) {
                        consName = "+ " + consDec.getName() + "(";
                        allConstructors.add(consName);
                    } else if (consDec.getModifiers() == 0) {
                        consName = "- " + consDec.getName() + "(";
                        allConstructors.add(consName);
                    }
                    for (String prm : param) {
                        String[] parName = prm.replaceAll("^\\s+", "").replaceAll("\\s+$", "").split(" ");
                        for (String a : javaFiles) {
                            if (a.equals(parName[0]) && interfaceNames.toString().contains(parName[0]) && isInterface == false) {
                                if (isDependentTo.contains(currentClass + "..>" + a)) {
                                    break;
                                } else {
                                    isDependentTo.add(currentClass + "..>" + a);
                                    finalOp.add(currentClass + "..>" + a);
                                    finalOp.add("\n");
                                }
                            }
                        }
                        consName = parName[1] + ":" + parName[0];
                        allConstructors.add(consName);
                        allConstructors.add(",");
                    }
                    allConstructors.add(")");
                    allConstructors.add("\n");
                }
                if (numOfPrm > 1) {
                    allConstructors.remove(allConstructors.lastIndexOf(","));
                }
            }
        }
    }
//Class for Extracting all the classes

    private static class GetClassesOrInterfaces extends VoidVisitorAdapter {

        @Override
        public void visit(ClassOrInterfaceDeclaration cid, Object obj) {
            ArrayList<String> classesExtended = new ArrayList<>();
            if (cid.getExtends() != null) {
                String c1 = currentClass + " --|> " + cid.getExtends().toString().replace("[", "").replace("]", "");
                finalOp.add(c1 + "\n");
            }
            if (cid.getImplements() != null) {
                int num = cid.getImplements().size();
                if (num == 1) {
                    String c2 = currentClass + " ..|> " + cid.getImplements().toString().replace("[", "").replace("]", "");
                    finalOp.add(c2 + "\n");
                } else {
                    String[] a = cid.getImplements().toString().replace("[", "").replace("]", "").replaceAll(" ", "").split(",");
                    for (String s : a) {
                        String c2 = currentClass + " ..|> " + s;
                        finalOp.add(c2 + "\n");
                    }
                }
            }
        }
    }
}

