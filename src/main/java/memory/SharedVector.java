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
        // Acquires a read lock to ensure thread-safe access to vector
        // data/metadata while allowing multiple concurrent readers.
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
        // Acquires a read lock to ensure thread-safe access to vector
        // data/metadata while allowing multiple concurrent readers.
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
        // Acquires a read lock to ensure thread-safe access to vector
        // data/metadata while allowing multiple concurrent readers.
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
        // Acquires an exclusive write lock to prevent race conditions while modifying
        // the vector's underlying data or orientation.
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
        // Locks the current vector for writing and the other vector for reading
        //  to ensure consistency during the in-place addition.
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
        // Acquires an exclusive write lock to prevent race conditions while modifying
        // the vector's underlying data or orientation.
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
        // Acquires read locks on both vectors to perform a thread-safe
        // dot product calculation without blocking other readers.
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
        // Uses a write lock because this operation updates the vector's
        // internal array with the result of the matrix multiplication.
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
