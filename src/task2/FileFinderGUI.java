package task2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.*;

class FileFinderGUI {
    private JFrame frame;
    private JTextField sizeTextField;
    private JComboBox<String> unitComboBox;
    private JLabel resultLabel;
    private JLabel directoryLabel;
    private String directoryPath;

    public void createAndShowGUI() {
        frame = new JFrame("Пошук великих файлів в директорії.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        // Поля для вводу розміру та одиниці
        JLabel directoryTextLabel = new JLabel("Вибрана директорія:", SwingConstants.LEFT);
        directoryLabel = new JLabel("Не вибрано", SwingConstants.LEFT);
        JButton selectDirectoryButton = new JButton("Вибрати директорію");
        JLabel sizeLabel = new JLabel("Введіть розмір файлів:", SwingConstants.LEFT);
        sizeTextField = new JTextField();
        JLabel unitLabel = new JLabel("Одиниця:", SwingConstants.LEFT);

        unitComboBox = new JComboBox<>(new String[]{"KB", "MB"});

        // Додаю елементи до панелі: лейбл ліворуч, компонент праворуч
        panel.add(directoryTextLabel);
        panel.add(directoryLabel);
        panel.add(selectDirectoryButton);
        panel.add(new JPanel());

        // Лейбл для розміру
        panel.add(sizeLabel);
        panel.add(sizeTextField);

        // Лейбл для одиниць
        panel.add(unitLabel);
        panel.add(unitComboBox);

        resultLabel = new JLabel("", SwingConstants.CENTER);

        // Кнопка для запуску пошуку
        JButton startButton = new JButton("Пошук");
        startButton.addActionListener(e -> startSearch());

        frame.add(panel, BorderLayout.CENTER);
        frame.add(startButton, BorderLayout.SOUTH);
        frame.add(resultLabel, BorderLayout.NORTH);

        selectDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    directoryPath = selectedDirectory.getPath();
                    directoryLabel.setText(directoryPath);
                }
            }
        });


        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void startSearch() {
        String sizeText = sizeTextField.getText();
        String selectedUnit = (String) unitComboBox.getSelectedItem();

        if (directoryPath == null || directoryPath.isEmpty() || sizeText.isEmpty()) {
            resultLabel.setText("Будь ласка, виберіть директорію та введіть розмір.");
            return;
        }

        long sizeThreshold;
        try {
            double sizeValue = Double.parseDouble(sizeText);
            sizeThreshold = (long) (sizeValue * (selectedUnit.equals("MB") ? 1024 * 1024 : 1024));
        } catch (NumberFormatException e) {
            resultLabel.setText("Невірно введено розмір файлів.");
            return;
        }

        File directory = new File(directoryPath);

        ExecutorService es = Executors.newFixedThreadPool(2);
        ArrayList<Future<Integer>> futures = new ArrayList<>();

        long start = System.nanoTime();
        // Заповнюю список з задачами для перевірки кожного файлу
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    futures.add(es.submit(new FileCheckTask(file, sizeThreshold)));
                }
            }
        }

        int totalLargeFiles = 0;
        for (Future<Integer> future : futures) {
            try {
                totalLargeFiles += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        long elapsedTime = System.nanoTime() - start;

        System.out.printf("Час виконання пошуку: %f секунд\n", elapsedTime/1_000_000_000.0);
        resultLabel.setText(String.format("Кількість файлів, більших за %.1f кілобайт: %d", sizeThreshold/1024., totalLargeFiles));
        es.shutdown();
    }

    // Задача для перевірки файлу на потрібний розмір
    static class FileCheckTask implements Callable<Integer> {
        private final File file;
        private final long sizeThreshold;

        public FileCheckTask(File file, long sizeThreshold) {
            this.file = file;
            this.sizeThreshold = sizeThreshold;
        }

        @Override
        public Integer call() {
            if (file.length() > sizeThreshold){
                return 1;
            } else return 0;
        }
    }
}
