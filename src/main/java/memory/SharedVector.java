package memory;
import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector=vector;
        this.orientation=orientation;
    }

    public double get(int index) {
        readLock();
        double output=0;

        try{
            output=vector[index];                                                                    
        }
        finally{
            readUnlock();
        }
        return output;
    }

    public int length() {
        readLock();
        int output=0;
        try{
            output=vector.length;                                                                    
        }
        finally{
            readUnlock();
        }
        return output;
    }

    public VectorOrientation getOrientation() {
        readLock();
        VectorOrientation output=null;
        try{
            output=this.orientation;                                                                    
        }
        finally{
            readUnlock();
        }
        return output;
    }

    public void writeLock() {
        lock.writeLock().lock();         
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        try{
        this.orientation = (this.orientation == VectorOrientation.ROW_MAJOR) ? 
                                VectorOrientation.COLUMN_MAJOR : VectorOrientation.ROW_MAJOR;
        }
        finally{
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        other.readLock();
        try{
            writeLock();
            try{
                if(this.vector.length!=other.vector.length || !this.orientation.equals(other.getOrientation()))
                    throw new RuntimeException("Ilegel");

                for(int i=0;i<this.vector.length;i++){
                    vector[i]+=other.vector[i];
                }
            }   
        
            finally{
                writeUnlock();
            }
        }
        finally{
            other.readUnlock();
        }
        
    }

    public void negate() {
        writeLock();
        try{
            for(int i=0;i<vector.length;i++){
                vector[i]= -vector[i];
            }
        }
        finally{
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        if((this.vector.length != other.vector.length))
            throw new RuntimeException("Illegal action- vectors should be in the same size");
        this.readLock();
        other.readLock();
        double output=0;
        try{
            if (this.vector.length != other.vector.length)
                throw new RuntimeException("Illegal action- vectors should be in the same size");

            if (this.orientation == other.orientation)
                throw new RuntimeException("Illegal action- vectors should be in different orientation");
            for(int i=0;i<vector.length;i++){
                output+=this.vector[i]*other.vector[i];
            }
        }
        finally{
            other.readUnlock();
            this.readUnlock();
        }
        return output;
    }
        
    public void vecMatMul(SharedMatrix matrix) {
        if (matrix == null || matrix.get(0) == null)
            throw new RuntimeException("Not computable");
        if (this.orientation != VectorOrientation.ROW_MAJOR)
            throw new RuntimeException("Not computable");
        if (this.vector.length != matrix.get(0).length())
            throw new RuntimeException("Not computable");

        double[] newVector= new double[matrix.length()];
        writeLock();
        try{
            for(int i=0;i<matrix.length();i++){
                newVector[i]=this.dot(matrix.get(i));
            }
            this.vector=newVector;
        }
        finally{
            writeUnlock();
        }        
    }
}
