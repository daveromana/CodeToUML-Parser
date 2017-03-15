public Class JavaToUMLParser{
 public static void main(String args[]){
 
 CompilationUnit c = new CompilationUnit();
 FileInputStream f = new FileInputStream("A.java");

  String s = c.javaParser(f);
  System.out.println(s);
  
 }
}
