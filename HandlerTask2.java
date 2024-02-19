import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HandlerTask2 implements Handler {
    private final Client client;

    public HandlerTask2(Client client) {
        this.client = client;
    }

    @Override
    public Duration timeout() {
        // Возвращаем задержку для повторной отправки данных после отклонения
        return Duration.ofSeconds(1);
    }

    @Override
    public void performOperation() {
        // Получаем порцию данных
        Event event = client.readData();
        
        // Создаем пул потоков для параллельной отправки
        ExecutorService executor = Executors.newFixedThreadPool(event.recipients().size());
        
        // Для каждого адресата отправляем данные параллельно
        for (Address recipient : event.recipients()) {
            executor.execute(() -> {
                Result result = Result.REJECTED;
                while (result == Result.REJECTED) {
                    result = client.sendData(recipient, event.payload());
                    if (result == Result.REJECTED) {
                        try {
                            Thread.sleep(timeout().toMillis());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Thread interrupted while sleeping: " + e.getMessage());
                        }
                    }
                }
            });
        }
        
        // Завершаем работу пула потоков
        executor.shutdown();
    }
}