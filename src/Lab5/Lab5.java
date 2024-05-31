package Lab5;

import java.io.*;
import java.util.*;

public class    Lab5 {


    static class Node {
        char ch;
        int freq;
        Node left, right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        String new_path_in = "";
        StringBuilder response = new StringBuilder();
        int ans;

        do {
            System.out.println("--Меню--");
            System.out.println("    1. Открыть текстовый файл");
            System.out.println("    2. Вывести содержимое текстового файла");
            System.out.println("    3. Вывести символы алфавита с указанием их частоты появления с сортировкой по частоте");
            System.out.println("    4. Сгенерировать коды для символов алфавита входного файла");
            System.out.println("    5. Сжать содержимое текстового файла с помощью кодов фиксированной длины с сохранением данных в файл");
            System.out.println("    6. Сжать содержимое текстового файла с помощью кодов Хаффмана с сохранением данных в файл");
            System.out.println("    7. Сравнить размеры файлов исходного текстового файла и двух зашифрованных");
            System.out.println("    9. Декодировать файл с кодами Хаффмана");
            System.out.println("    10. Выход");
            System.out.print("Выберите пункт меню: ");
            ans = in.nextInt();

            switch (ans) {
                case 1:
                    in.nextLine();
                    System.out.print("Укажите путь файла: ");
                    new_path_in = in.nextLine().trim();
                    if (new_path_in.startsWith("\"") && new_path_in.endsWith("\"")) {
                        new_path_in = new_path_in.substring(1, new_path_in.length() - 1);
                    }
                    File file = new File(new_path_in);

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        response.setLength(0);
                        while ((line = reader.readLine()) != null) {
                            response.append(line).append("\n");
                        }
                        System.out.println("Файл успешно прочитан.");
                    } catch (IOException e) {
                        System.out.println("Ошибка при чтении файла: " + e.getMessage());
                    }
                    break;
                case 2:
                    if (new_path_in.isEmpty()) {
                        System.out.println("Сначала откройте файл");
                    } else {
                        System.out.println("Содержимое файла:");
                        System.out.println(response.toString());
                    }
                    break;
                case 3:
                    if (new_path_in.isEmpty()) {
                        System.out.println("Сначала откройте файл");
                    } else {
                        Map<Character, Integer> frequencyMap = new HashMap<>();

                        for (int i = 0; i < response.length(); i++) {
                            char c = response.charAt(i);
                            if (Character.isLetter(c)) {
                                frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
                            }
                        }

                        List<Map.Entry<Character, Integer>> sortedList = new ArrayList<>(frequencyMap.entrySet());
                        sortedList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                        System.out.println("Символы алфавита с указанием их частоты появления:");
                        for (Map.Entry<Character, Integer> entry : sortedList) {
                            System.out.println(entry.getKey() + ": " + entry.getValue());
                        }
                    }
                    break;
                case 4:
                    if (new_path_in.isEmpty()) {
                        System.out.println("Сначала откройте файл");
                    } else {
                        // Частота появления каждого символа
                        Map<Character, Integer> frequencyMap = new HashMap<>();
                        for (int i = 0; i < response.length(); i++) {
                            char c = response.charAt(i);
                            if (Character.isLetter(c)) {
                                frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
                            }
                        }

                        // Построение дерева Хаффмана
                        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.freq));
                        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
                            pq.add(new Node(entry.getKey(), entry.getValue(), null, null));
                        }

                        while (pq.size() > 1) {
                            Node left = pq.poll();
                            Node right = pq.poll();
                            pq.add(new Node('\0', left.freq + right.freq, left, right));
                        }

                        Node root = pq.poll();
                        Map<Character, String> huffmanCode = new HashMap<>();
                        generateHuffmanCodes(root, "", huffmanCode);

                        // Вывод кодов фиксированной длины (7 бит для каждого символа)
                        System.out.println("Коды фиксированной длины для символов алфавита входного файла:");
                        Map<Character, String> fixedLengthCodes = generateFixedLengthCodes(frequencyMap.keySet());
                        for (Map.Entry<Character, String> entry : fixedLengthCodes.entrySet()) {
                            System.out.println("Символ: " + entry.getKey() + ", Код: " + entry.getValue());
                        }

                        System.out.println("\nКоды Хаффмана для символов алфавита входного файла:");
                        for (Map.Entry<Character, String> entry : huffmanCode.entrySet()) {
                            System.out.println("Символ: " + entry.getKey() + ", Код Хаффмана: " + entry.getValue());
                        }
                    }
                    break;
                case 5:
                    compressWithFixedLengthCodes(new_path_in, response.toString());
                    break;
                case 6:
                    compressWithHuffmanCodes(new_path_in, response.toString());
                    break;
                case 7:
                    compareFileSizes(new_path_in);
                    break;
                case 8:
                    decompressFixedLengthCodes(new_path_in);
                    break;
                case 9:
                    decompressHuffmanCodes(new_path_in);
                    break;
                case 10:
                    System.out.println("Выход из программы.");
                    break;
                default:
                    System.out.println("Неверный пункт меню. Пожалуйста, выберите снова.");
            }
        } while (ans != 10);

        in.close();
    }

    private static Map<Character, String> generateFixedLengthCodes(Set<Character> alphabet) {
        Map<Character, String> fixedLengthCodes = new HashMap<>();
        int codeLength = 7;
        int code = 0;
        for (char c : alphabet) {
            String binaryCode = String.format("%" + codeLength + "s", Integer.toBinaryString(code)).replace(' ', '0');
            fixedLengthCodes.put(c, binaryCode);
            code++;
        }
        return fixedLengthCodes;
    }

    private static void compressWithFixedLengthCodes(String filePath, String content) throws IOException {
        String compressedFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "_fixed_length_compressed.bin";
        Map<Character, String> fixedLengthCodes = generateFixedLengthCodes(new HashSet<>(content.chars().mapToObj(c -> (char)c).toList()));
        try (OutputStream os = new FileOutputStream(compressedFilePath)) {
            StringBuilder encodedContent = new StringBuilder();
            for (char c : content.toCharArray()) {
                encodedContent.append(fixedLengthCodes.get(c));
            }
            BitSet bitSet = new BitSet(encodedContent.length());
            int bitIndex = 0;
            for (char bit : encodedContent.toString().toCharArray()) {
                if (bit == '1') {
                    bitSet.set(bitIndex);
                }
                bitIndex++;
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
                oos.writeObject(bitSet);
            }
        }
        System.out.println("Файл сжат и сохранен как: " + compressedFilePath);
    }


    private static void compressWithHuffmanCodes(String filePath, String content) throws IOException {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : content.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.freq));
        for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue(), null, null));
        }

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            pq.add(new Node('\0', left.freq + right.freq, left, right));
        }

        Node root = pq.peek();
        Map<Character, String> huffmanCode = new HashMap<>();
        generateHuffmanCodes(root, "", huffmanCode);

        StringBuilder encodedContent = new StringBuilder();
        for (char c : content.toCharArray()) {
            encodedContent.append(huffmanCode.get(c));
        }

        String compressedFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "_huffman_compressed.bin";
        try (OutputStream os = new FileOutputStream(compressedFilePath)) {
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(huffmanCode);
            BitSet bitSet = new BitSet(encodedContent.length());
            int bitIndex = 0;
            for (char bit : encodedContent.toString().toCharArray()) {
                if (bit == '1') {
                    bitSet.set(bitIndex);
                }
                bitIndex++;
            }
            oos.writeObject(bitSet);
            oos.close();
        }
        System.out.println("Файл сжат и сохранен как: " + compressedFilePath);
    }

    private static void generateHuffmanCodes(Node root, String code, Map<Character, String> huffmanCode) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null) {
            huffmanCode.put(root.ch, code);
        }
        generateHuffmanCodes(root.left, code + '0', huffmanCode);
        generateHuffmanCodes(root.right, code + '1', huffmanCode);
    }

    private static void compareFileSizes(String filePath) {
        File originalFile = new File(filePath);
        File fixedLengthCompressedFile = new File(filePath.substring(0, filePath.lastIndexOf('.')) + "_fixed_length_compressed.bin");
        File huffmanCompressedFile = new File(filePath.substring(0, filePath.lastIndexOf('.')) + "_huffman_compressed.bin");

        if (originalFile.exists() && fixedLengthCompressedFile.exists() && huffmanCompressedFile.exists()) {
            long originalSize = originalFile.length();
            long fixedLengthSize = fixedLengthCompressedFile.length();
            long huffmanSize = huffmanCompressedFile.length();

            System.out.println("Размеры файлов:");
            System.out.println("Исходный файл: " + originalSize + " байт");
            System.out.println("Файл с кодами фиксированной длины: " + fixedLengthSize + " байт");
            System.out.println("Файл с кодами Хаффмана: " + huffmanSize + " байт");
        } else {
            System.out.println("Не удается найти один или несколько файлов для сравнения.");
        }
    }

    private static void decompressFixedLengthCodes(String filePath) throws IOException {
        String compressedFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "_fixed_length_compressed.bin";
        String decompressedFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "_fixed_length_decompressed.txt";
        try (InputStream is = new FileInputStream(compressedFilePath);
             OutputStream os = new FileOutputStream(decompressedFilePath)) {
            int data;
            while ((data = is.read()) != -1) {
                os.write((byte)data); // Read each byte and write it directly
            }
        }
        System.out.println("Файл декодирован и сохранен как: " + decompressedFilePath);
    }


    private static void decompressHuffmanCodes(String filePath) throws IOException {
        String compressedFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "_huffman_compressed.bin";
        String decompressedFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "_huffman_decompressed.txt";
        try (InputStream is = new FileInputStream(compressedFilePath);
             ObjectInputStream ois = new ObjectInputStream(is);
             OutputStream os = new FileOutputStream(decompressedFilePath)) {
            try {
                Map<Character, String> huffmanCode = (Map<Character, String>) ois.readObject();
                BitSet bitSet = (BitSet) ois.readObject();

                Map<String, Character> reverseHuffmanCode = new HashMap<>();
                for (Map.Entry<Character, String> entry : huffmanCode.entrySet()) {
                    reverseHuffmanCode.put(entry.getValue(), entry.getKey());
                }

                StringBuilder decodedContent = new StringBuilder();
                StringBuilder currentCode = new StringBuilder();
                for (int i = 0; i < bitSet.length(); i++) {
                    currentCode.append(bitSet.get(i) ? '1' : '0');
                    if (reverseHuffmanCode.containsKey(currentCode.toString())) {
                        decodedContent.append(reverseHuffmanCode.get(currentCode.toString()));
                        currentCode.setLength(0);
                    }
                }

                os.write(decodedContent.toString().getBytes());
                System.out.println("Файл декодирован и сохранен как: " + decompressedFilePath);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
