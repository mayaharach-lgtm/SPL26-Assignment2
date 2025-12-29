package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedVectorTest {

    @Test
    void testDotRowWithColumn() {
        SharedVector row = new SharedVector(new double[]{2, -1, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{4, 5, -2}, VectorOrientation.COLUMN_MAJOR);

        // 2*4 + (-1)*5 + 3*(-2) = 8 -5 -6 = -3
        assertEquals(-3.0, row.dot(col));
    }

    @Test
    void testVecMatMulRectangular() {
        // row vector length 4
        SharedVector row = new SharedVector(new double[]{1, 2, 3, 4}, VectorOrientation.ROW_MAJOR);

        // matrix 4x2, loaded column-major
        double[][] data = {
                {2, 0},
                {1, -1},
                {0, 3},
                {4, 2}
        };
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        row.vecMatMul(m);

        // expected: [20, 15]
        assertEquals(20.0, row.get(0));
        assertEquals(15.0, row.get(1));
        assertEquals(2, row.length());
        assertEquals(VectorOrientation.ROW_MAJOR, row.getOrientation());
    }

    @Test
    void testAddThrowsOnLengthMismatch() {
        SharedVector a = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        assertThrows(RuntimeException.class, () -> a.add(b));
    }

    @Test
    void testDotThrowsOnSameOrientation() {
        SharedVector a = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.ROW_MAJOR);
        assertThrows(RuntimeException.class, () -> a.dot(b));
    }
}
