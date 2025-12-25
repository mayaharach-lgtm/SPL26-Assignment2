package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      if (args.length < 3){
          System.out.println("not enough variables");
          return;
      } 
      int numThreads = Integer.parseInt(args[0]);
      String inputPath = args[1];
      String outputPath = args[2];
      LinearAlgebraEngine engine=null;
      try{
          //parse input
          InputParser parser = new InputParser();
          ComputationNode root = parser.parse(inputPath);
          //run LEA
          engine=new LinearAlgebraEngine(numThreads);
          ComputationNode result=engine.run(root);
          //parse output
          OutputWriter outparser = new OutputWriter();
          outparser.write(result.getMatrix(), outputPath);
          
      }
      catch(Exception e){
         OutputWriter.write(e.getMessage(), outputPath);
      }
      finally{
        if(engine!=null){
          engine.shutdown();
        }
      }
    }
}