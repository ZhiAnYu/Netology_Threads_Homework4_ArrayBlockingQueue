import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class Main {
    static BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    static BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    static BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);
    static final int amountText = 10_000;
    static final int amountSymbol = 100_000;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Random random = new Random();

        Thread generateThread = new Thread(() -> {
            String[] texts = new String[amountText];
            for (int i = 0; i < texts.length; i++) {
                texts[i] = generateText("abc", amountSymbol);
                try {
                    queueA.put(texts[i]);
                    queueB.put(texts[i]);
                    queueC.put(texts[i]);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        generateThread.start();

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Callable<Map<Integer, String>> logicA = () -> {
            try {
                int maxCount = -1;
                String bestText = "";
                for (int i = 0; i < amountText; i++) {
                    String text = queueA.take();
                    int count = calculateFreq(text, 'a');
                    if (count > maxCount) {
                        maxCount = count;
                        bestText = text;
                    }
                }
                Map<Integer, String> maxA = new HashMap<>();
                maxA.put(maxCount, bestText);
                return maxA;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        Callable<Map<Integer, String>> logicB = () -> {
            int maxCount = -1;
            String bestText = "";
            for (int i = 0; i < amountText; i++) {
                String text = queueB.take();
                int count = calculateFreq(text, 'b');
                if (count > maxCount) {
                    maxCount = count;
                    bestText = text;
                }
            }
            Map<Integer, String> maxB = new HashMap<>();
            maxB.put(maxCount, bestText);
            return maxB;
        };

        Callable<Map<Integer, String>> logicC = () -> {
            int maxCount = -1;
            String bestText = "";
            for (int i = 0; i < amountText; i++) {
                String text = queueC.take();
                int count = calculateFreq(text, 'c');
                if (count > maxCount) {
                    maxCount = count;
                    bestText = text;
                }
            }
            Map<Integer, String> maxC = new HashMap<>();
            maxC.put(maxCount, bestText);
            return maxC;
        };

        Future<Map<Integer, String>> futureA = executor.submit(logicA);
        Future<Map<Integer, String>> futureB = executor.submit(logicB);
        Future<Map<Integer, String>> futureC = executor.submit(logicC);

        // Получаем результаты (блокирующие вызовы)
        Map<Integer, String> resultA = futureA.get();
        Map<Integer, String> resultB = futureB.get();
        Map<Integer, String> resultC = futureC.get();

        // Выводим результаты
        System.out.println("A: " + resultA);
        System.out.println("B: " + resultB);
        System.out.println("C: " + resultC);

        // Завершаем executor
        executor.shutdown();

        generateThread.join();
    }

    public static int calculateFreq(String text, char s) {
        int counter = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == s) {
                counter++;
            }
        }
        return counter;
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }


}