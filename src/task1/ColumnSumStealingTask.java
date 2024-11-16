package task1;

import java.util.concurrent.RecursiveTask;

class ColumnSumStealingTask extends RecursiveTask<int[]> {
    private static final int MAX_COLUMNS_IN_TASK = 2;
    private int[][] array;
    private int colStart;
    private int colEnd;

    public ColumnSumStealingTask(int[][] array, int colStart, int colEnd) {
        this.array = array;
        this.colStart = colStart;
        this.colEnd = colEnd;
    }

    @Override
    protected int[] compute() {
        if (colEnd - colStart <= MAX_COLUMNS_IN_TASK){
            return computeArray();
        } else {
            int midLen = (colStart + colEnd) / 2;
            ColumnSumStealingTask leftTask = new ColumnSumStealingTask(array, colStart, midLen);
            ColumnSumStealingTask rightTask = new ColumnSumStealingTask(array, midLen, colEnd);
            leftTask.fork();
            rightTask.fork();
            int[] leftResult = leftTask.join();
            int[] rightResult = rightTask.join();
            return combineResults(leftResult, rightResult);
        }
    }

    private int[] computeArray() {
        int[] sumsArray = new int[colEnd - colStart];
        for (int column = colStart; column < colEnd; column++) {
            int columnSum = 0;
            for (int row = 0; row < array.length; row++) {
                columnSum += array[row][column];
            }
            sumsArray[column - colStart] = columnSum;
        }
        return sumsArray;
    }

    private int[] combineResults(int[] left, int[] right) {
        int[] finalResult = new int[left.length + right.length];
        for (int i = 0; i < left.length; i++) {
            finalResult[i] = left[i];
        }
        for (int i = 0; i < right.length; i++) {
            finalResult[left.length + i] = right[i];
        }
        return finalResult;
    }
}
