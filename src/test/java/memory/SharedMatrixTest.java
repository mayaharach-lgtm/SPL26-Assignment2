package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    @Test
    void testReadRowMajorAfterManualTransposeRectangular() {
        double[][] data = {
                {1, 2, 3},
                {4, 5, 6}
        }; // 2x3

        SharedMatrix m = new SharedMatrix(data);

        // "Transpose" by toggling each stored vector orientation (as in your engine)
        for (int i = 0; i < m.length(); i++) {
            m.get(i).transpose();
        }

        double[][] t = m.readRowMajor(); // should now be 3x2: data^T
        assertEquals(3, t.length);
        assertEquals(2, t[0].length);

        assertEquals(1.0, t[0][0]);
        assertEquals(4.0, t[0][1]);
        assertEquals(2.0, t[1][0]);
        assertEquals(5.0, t[1][1]);
        assertEquals(3.0, t[2][0]);
        assertEquals(6.0, t[2][1]);
    }

    @Test
    void testReadRowMajorWhenLoadedColumnMajorRectangular() {
        double[][] data = {
                {7, 8},
                {9, 10},
                {11, 12}
        }; // 3x2

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        double[][] back = m.readRowMajor();
        assertEquals(3, back.length);
        assertEquals(2, back[0].length);
        assertEquals(7.0, back[0][0]);
        assertEquals(12.0, back[2][1]);
    }

    @Test
    void testEmptyMatrixContract() {
        SharedMatrix m = new SharedMatrix();
        assertEquals(0, m.length());
        assertNull(m.get(0));
        assertThrows(IllegalArgumentException.class, m::readRowMajor);
    }
}
