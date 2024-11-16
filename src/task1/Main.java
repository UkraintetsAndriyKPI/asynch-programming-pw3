package task1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    public static final int MAX_RANDOM = 100;

    public static int[][] createArray(int sizeN, int sizeM) {
        Random rand = new Random();
        int[][] array = new int[sizeN][sizeM];
        for (int i = 0; i < sizeN; i++) {
            for (int j = 0; j < sizeM; j++) {
                // random int in range [0, MAX_RANDOM]
                array[i][j] = rand.nextInt(MAX_RANDOM+1);
            }
        }
        return array;
    }

    public static void printArray(int[][] array){
        for (int[] intArr : array) {
            System.out.println(Arrays.toString(intArr));
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        // Введення кількості рядків (N) з перевіркою
        int sizeN = 0;
        while (sizeN <= 0) {
            System.out.print("Введіть к-сть рядків матриці N (позитивне ціле число): ");
            if (scanner.hasNextInt()) {
                sizeN = scanner.nextInt();
                if (sizeN <= 0) {
                    System.out.println("ERROR Помилка: к-сть рядків повинна бути більше нуля.");
                }
            } else {
                scanner.next();
                System.out.println("ERROR Помилка: введіть ціле число.");
            }
        }

        // Введення кількості стовпців (M) з перевіркою
        int sizeM = 0;
        while (sizeM <= 0) {
            System.out.print("Введіть к-сть стовпців матриці M (позитивне ціле число): ");
            if (scanner.hasNextInt()) {
                sizeM = scanner.nextInt();
                if (sizeM <= 0) {
                    System.out.println("ERROR Помилка: к-сть стовпців повинна бути більше нуля.");
                }
            } else {
                scanner.next();
                System.out.println("ERROR Помилка: введіть ціле число.");
            }
        }
        scanner.close();

        int[][] inputArray = createArray(sizeN, sizeM);
        System.out.println("Згенерований випадковим чином масив:");
        printArray(inputArray);


        // Work Stealing method
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        long start = System.nanoTime();
        int[] colSums = forkJoinPool.invoke(new ColumnSumStealingTask(inputArray, 0, sizeM));
        long elapsedTime = System.nanoTime() - start;

        System.out.println(
                "\n(Work Stealing method) Результуючий масив усіх сум стовпців:\n"+Arrays.toString(colSums));
        System.out.printf(
                "(Work Stealing method) Час затрачений на виконання: %f секунд\n", elapsedTime / 1_000_000_000.0);

        // Work Dealing method
        ExecutorService es = Executors.newFixedThreadPool(2);

        int[] columnSums = new int[sizeM];
        ArrayList<Future<Integer>> futures = new ArrayList<>();
        start = System.nanoTime();
        for (int column = 0; column < sizeM; column++) {
            final int finalColumn = column;
            futures.add(es.submit(() -> {
                int columnSum = 0;
                for (int[] ints : inputArray) {
                    columnSum += ints[finalColumn];
                }
                return columnSum;
            }));
        }

        for (int i = 0; i < futures.size(); i++) {
            columnSums[i] = futures.get(i).get();
        }
        elapsedTime = System.nanoTime() - start;

        System.out.println(
                "\n(Work Dealing method) Результуючий масив усіх сум стовпців:\n"+Arrays.toString(columnSums));
        System.out.printf(
                "(Work Dealing method) Час затрачений на виконання: %f секунд\n", elapsedTime / 1_000_000_000.0);


        es.shutdown();
    }
}