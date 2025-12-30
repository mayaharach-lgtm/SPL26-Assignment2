package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
        String outputPath;
        if (args.length >= 3) {
            outputPath = args[2];
        } 
        else {
            outputPath = "output.json";
        }
        outputPath = args[2];
        int numThreads = Integer.parseInt(args[0]);
        String inputPath = args[1];
        if (args.length < 3){
            OutputWriter.write("Invalid number of arguments", outputPath);
                return;
        } 
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
      System.out.println(engine.getWorkerReport());
    }

}