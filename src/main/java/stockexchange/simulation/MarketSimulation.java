package stockexchange.simulation;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import stockexchange.observer.sources.*;

public final class MarketSimulation {
    public record Result(int producers, int published, boolean terminated) { }
    private final StockPriceSource prices;
    private final TradingVolumeSource volumes;
    public MarketSimulation(StockPriceSource prices, TradingVolumeSource volumes) {
        this.prices = prices;
        this.volumes = volumes;
    }
    public Result run(int producerCount, int eventsPerProducer) throws InterruptedException, ExecutionException {
        if (producerCount < 3 || eventsPerProducer < 0) throw new IllegalArgumentException("Use at least three producers and nonnegative event count");
        int total = Math.multiplyExact(producerCount, eventsPerProducer);
        var executor = Executors.newFixedThreadPool(producerCount);
        try {
            var futures = new ArrayList<Future<?>>();
            for (int producer = 0; producer < producerCount; producer++) {
                final int id = producer;
                futures.add(executor.submit(() -> produce(id, eventsPerProducer)));
            }
            for (var future : futures) future.get();
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            awaitCancellation(executor);
            Thread.currentThread().interrupt();
            throw exception;
        } finally {
            executor.shutdown();
            if (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        if (!executor.awaitTermination(30, TimeUnit.SECONDS))
                            throw new IllegalStateException("Producer termination failed");
                    }
                } catch (InterruptedException exception) {
                    executor.shutdownNow();
                    awaitCancellation(executor);
                    Thread.currentThread().interrupt();
                    throw exception;
                }
            }
        }
        return new Result(producerCount, total, executor.isTerminated());
    }
    private void produce(int producer, int count) {
        for (int i = 0; i < count && !Thread.currentThread().isInterrupted(); i++) {
            if (producer % 2 == 0) prices.update("AAPL", new BigDecimal("100"), BigDecimal.valueOf(100 + i % 10));
            else volumes.update("AAPL", 1000, 1000 + i);
        }
    }
    private void awaitCancellation(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS))
                throw new IllegalStateException("Interrupted producers did not stop");
        } catch (InterruptedException repeated) {
            Thread.currentThread().interrupt();
        }
    }
}
