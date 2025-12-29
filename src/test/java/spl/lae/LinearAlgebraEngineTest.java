package spl.lae;

import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.InputParser;

import static org.junit.jupiter.api.Assertions.*;


class LinearAlgebraEngineTest {

    @Test
    void testMultiplyRectangularFromJson() throws Exception {
        String filePath = "src/test/java/spl/lae/input.json";
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse(filePath);

        // 0 threads => should still run (inline fallback)
        LinearAlgebraEngine engine = new LinearAlgebraEngine(0);
        ComputationNode resultNode = engine.run(root);
        double[][] m = resultNode.getMatrix();

        assertNotNull(m);
        assertEquals(3, m.length);
        assertEquals(2, m[0].length);

        // Full expected:
        // [[20, 15],
        //  [48, 31],
        //  [76, 47]]
        assertEquals(20.0, m[0][0]);
        assertEquals(15.0, m[0][1]);
        assertEquals(48.0, m[1][0]);
        assertEquals(31.0, m[1][1]);
        assertEquals(76.0, m[2][0]);
        assertEquals(47.0, m[2][1]);
    }

    @Test
    void testLargeNAryAdditionAssociativity() throws Exception {
        String filePath = "src/test/java/spl/lae/input2.json";
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse(filePath);

        LinearAlgebraEngine engine = new LinearAlgebraEngine(1);
        ComputationNode resultNode = engine.run(root);
        double[][] m = resultNode.getMatrix();

        assertNotNull(m);
        assertEquals(6, m.length);
        assertEquals(6, m[0].length);

        // Check a few strategic cells (not only corners)
        assertEquals(11.0, m[0][0]); // 1 -2 +5 +0 +7
        assertEquals(14.0, m[0][5]); // 6 -2 +0 +3 +7
        assertEquals(24.0, m[2][3]); // 16 -2 +0 +0 +7
        assertEquals(46.0, m[5][5]); // 36 -2 +5 +0 +7
    }

    @Test
    void testComplexNestedOpsTransposeNegateAdd() throws Exception {
        String filePath = "src/test/java/spl/lae/input3.json";
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse(filePath);

        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        ComputationNode resultNode = engine.run(root);
        double[][] m = resultNode.getMatrix();

        assertNotNull(m);
        assertEquals(2, m.length);
        assertEquals(2, m[0].length);

        // Expected result:
        // -( (A*B)^T + (-C) + D^T )
        // = [[ -57, -137],
        //    [ -67, -156]]
        assertEquals(-57.0, m[0][0]);
        assertEquals(-137.0, m[0][1]);
        assertEquals(-67.0, m[1][0]);
        assertEquals(-156.0, m[1][1]);
    }
}
