package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.LinkedList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // create executor with given thread count
        this.executor= new TiredExecutor(numThreads);

    }

    public ComputationNode run(ComputationNode computationRoot) {
        // resolve computation tree step by step until final matrix is produced
         try{   
            if(computationRoot!=null){
                computationRoot.associativeNesting();
                ComputationNode next=computationRoot.findResolvable();
                while(next!=null){
                    loadAndCompute(next);
                    next=computationRoot.findResolvable();
                }
            }
        }
        catch(Exception e){
            throw new IllegalArgumentException(e.getMessage());
        }
        finally{
            try{
                executor.shutdown();
            }
            catch(InterruptedException e){
                System.err.println("Shutdown was interrupted");
            }
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // load operand matrices
        // create compute tasks & submit tasks to executor
        ComputationNodeType type = node.getNodeType();
        List<ComputationNode> ch = node.getChildren();
        if (ch == null || ch.isEmpty())
            throw new IllegalArgumentException("Node has no operands");
        ComputationNode left = ch.get(0);
        leftMatrix = new SharedMatrix(left.getMatrix());
        rightMatrix = null;
        if (type == ComputationNodeType.ADD || type == ComputationNodeType.MULTIPLY) {
            if (ch.size() != 2) throw new IllegalArgumentException("Binary op requires 2 operands");
                ComputationNode right = ch.get(1);
            rightMatrix = new SharedMatrix(right.getMatrix());
        }

        if(type==ComputationNodeType.ADD){
            this.executor.submitAll(createAddTasks());
        }
        else if(type==ComputationNodeType.MULTIPLY){
            rightMatrix.loadColumnMajor(rightMatrix.readRowMajor());
            this.executor.submitAll(createMultiplyTasks());
        }
        else if(type==ComputationNodeType.NEGATE){
            this.executor.submitAll(createNegateTasks());
        }
        else if(type==ComputationNodeType.TRANSPOSE){
            this.executor.submitAll(createTransposeTasks());
        }
        else{
            return;
        }
        node.resolve(this.leftMatrix.readRowMajor());
    }

    public List<Runnable> createAddTasks() {
        // return tasks that perform row-wise addition
        List <Runnable> output=new LinkedList<Runnable>();
        if((this.leftMatrix.length()==this.rightMatrix.length())&&(this.leftMatrix.get(0).length()==this.rightMatrix.get(0).length())){
            for(int i=0;i<this.leftMatrix.length();i++){
                final int rowIndex = i;
                Runnable task= ()->{
                    this.leftMatrix.get(rowIndex).add(this.rightMatrix.get(rowIndex));
                };
                output.add(task);
            }
        }
        else {
            throw new IllegalArgumentException("Matrix dimensions do not match for addition");
        }
        return output;
    }

    public List<Runnable> createMultiplyTasks() {
        // return tasks that perform row × matrix multiplication
        List <Runnable> output=new LinkedList<Runnable>();
        if (leftMatrix == null || rightMatrix == null) {
            throw new IllegalArgumentException("null matrix");
        }
        if (leftMatrix.get(0) == null || rightMatrix.get(0) == null) {
            throw new IllegalArgumentException("empty matrix");
        }
        if(leftMatrix.get(0).length() == rightMatrix.get(0).length()){
            for(int i=0;i<this.leftMatrix.length();i++){
                final int rowIndex = i;
                Runnable task= ()->{
                    this.leftMatrix.get(rowIndex).vecMatMul(this.rightMatrix);
                };
                output.add(task);
            }
        }
        else {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiply");
        }
        return output;
    }

    public List<Runnable> createNegateTasks() {
        // return tasks that negate rows
        List <Runnable> output=new LinkedList<Runnable>();
        if(leftMatrix!=null){
            for(int i=0;i<this.leftMatrix.length();i++){
                final int rowIndex = i;
                Runnable task= ()->{
                    this.leftMatrix.get(rowIndex).negate();
                };
                output.add(task);
            }
        }
        else {
            throw new IllegalArgumentException("Matrix is null");
        }
        return output;
    }

    public List<Runnable> createTransposeTasks() {
        // return tasks that transpose rows
        List <Runnable> output=new LinkedList<Runnable>();
        if(leftMatrix!=null){
            for(int i=0;i<this.leftMatrix.length();i++){
                final int rowIndex = i;
                Runnable task= ()->{
                    this.leftMatrix.get(rowIndex).transpose();
                };
                output.add(task);
            }
        }
        else {
            throw new IllegalArgumentException("Matrix is null");
        }
        return output;
    }

    public String getWorkerReport() {
        // return summary of worker activity
        return this.executor.getWorkerReport();
    }

    
}
