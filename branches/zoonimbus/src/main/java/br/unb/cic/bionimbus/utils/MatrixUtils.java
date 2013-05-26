package br.unb.cic.bionimbus.utils;

import Jama.Matrix;

public class MatrixUtils {
    public static String printMatrix(Matrix matrix) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < matrix.getColumnDimension(); ++i) {
            for (int j = 0; j < matrix.getRowDimension(); j++) {
                sb.append(matrix.get(i, j) + " ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
