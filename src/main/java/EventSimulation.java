import java.util.ArrayList;
import java.util.concurrent.*;
import java.math.BigDecimal;

class EventSimulation {
    static void run(PriceUpdateSource prices, StockUpdateSource stock, int threads) throws InterruptedException, ExecutionException {
        if(threads<3) throw new IllegalArgumentException("At least 3 producer threads are required");
        var executor=Executors.newFixedThreadPool(threads);
        try {
            var futures=new ArrayList<Future<?>>();
            for(int t=0;t<threads;t++) futures.add(executor.submit(()->{
                for(int i=0;i<100;i++) {
                    prices.update("phone",new BigDecimal("100"),new BigDecimal("90"),"electronics");
                    stock.update("phone",10,9,"electronics");
                }
            }));
            for(var future:futures) future.get();
        } catch(InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw ex;
        } finally {
            executor.shutdown();
            if(!Thread.currentThread().isInterrupted() && !executor.awaitTermination(30,TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if(!executor.awaitTermination(30,TimeUnit.SECONDS)) throw new IllegalStateException("Producers did not stop");
            }
        }
    }
}
