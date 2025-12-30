package memory;

public class SharedMatrix {
    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        this.vectors=null;
    }

    public SharedMatrix(double[][] matrix) {
        if(matrix==null || matrix.length==0 || matrix[0].length==0){
            throw new RuntimeException("Ilegal input");
        }
        vectors=new SharedVector[matrix.length];
        for(int i=0;i<matrix.length;i++){
            double[] vector=new double[matrix[i].length];
            for(int j=0;j<matrix[0].length;j++){
                vector[j]=matrix[i][j];
            }
            this.vectors[i]=new SharedVector(vector,VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadRowMajor(double[][] matrix) {
      if(matrix==null || matrix.length==0 || matrix[0].length==0){
            throw new RuntimeException("Ilegal input");
        }
        vectors=new SharedVector[matrix.length];
        for(int i=0;i<matrix.length;i++){
            double[] vector=new double[matrix[i].length];
            for(int j=0;j<matrix[0].length;j++){
                vector[j]=matrix[i][j];
            }
            this.vectors[i]=new SharedVector(vector,VectorOrientation.ROW_MAJOR);
        }
    }


    public void loadColumnMajor(double[][] matrix) {
        if(matrix==null || matrix.length==0 || matrix[0].length==0){
            throw new RuntimeException("Ilegal input");
        }
        int row = matrix.length;
        int col = matrix[0].length;
        vectors=new SharedVector[col];
        for(int j=0;j<col;j++){
            double[] vector=new double[row];
            for(int i=0;i<row;i++){
                vector[i]=matrix[i][j];
            }
            this.vectors[j]=new SharedVector(vector,VectorOrientation.COLUMN_MAJOR);
        }
    }

    public double[][] readRowMajor() {
        if (vectors == null || vectors.length == 0 || vectors[0] == null) {
            throw new IllegalArgumentException("Empty matrix");
        }
        VectorOrientation orientation = vectors[0].getOrientation();
        if (orientation == VectorOrientation.ROW_MAJOR) {
            int row=vectors.length;
            int col=vectors[0].length();
            double[][] output=new double[row][col];
            for(int i=0;i<row;i++){
                for(int j=0;j<col;j++){
                    output[i][j]=vectors[i].get(j);
                }
            }
            return output;
        }
        else { 
            int cols = vectors.length;
            int rows = vectors[0].length();
            double[][] output = new double[rows][cols];
            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < rows; i++) {
                    output[i][j] = vectors[j].get(i);
                }
            }
            return output;
        }
    }

    public SharedVector get(int index) {
        //return vector at index
        if (vectors == null)
             return null;
        return vectors[index];
    }

    public int length() {
        //return number of stored vectors
        if(vectors == null){
            return 0;
        }
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        //return orientation
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // acquire read lock for each vector
        // Utility to batch-acquire read locks for all vectors in the matrix,
        // ensuring a consistent snapshot during matrix-level read operations.
        for(int i=0;i<vectors.length;i++){
            vectors[i].readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
         // acquire read unlock for each vector
         // Utility to batch-acquire read locks for all vectors in the matrix,
         // ensuring a consistent snapshot during matrix-level read operations.
        for(int i=0;i<vectors.length;i++){
            vectors[i].readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // acquire write lock for each vector
        // Batch-acquires exclusive write locks for all vectors to safely update
        // or initialize the matrix contents.
        for(int i=0;i<vectors.length;i++){
            vectors[i].writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // release write locks
        // Batch-acquires exclusive write locks for all vectors to safely update
        // or initialize the matrix contents.
         for(int i=0;i<vectors.length;i++){
            vectors[i].writeUnlock();
        }
    }
}
