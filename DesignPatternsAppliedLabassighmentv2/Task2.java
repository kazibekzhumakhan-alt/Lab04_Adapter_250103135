
package task2;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class LegacyStockFeed {
    public String fetchRawTickerPacket(String ticker) {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if ("AAPL".equalsIgnoreCase(ticker)) {
            return "TICKER=AAPL;BID=182.50;ASK=183.50;STATUS=OPEN;TIMESTAMP=17300000";
        }

        if ("GOOGL".equalsIgnoreCase(ticker)) {
            return "TICKER=GOOGL;BID=164.20;ASK=164.80;STATUS=OPEN;TIMESTAMP=17300000";
        }

        return "TICKER=" + ticker
                + ";BID=0.00;ASK=0.00;STATUS=UNKNOWN;TIMESTAMP=0";
    }
}

interface StockProvider {
    double getMarketPrice(String ticker);
    String getStatus(String ticker);
}

class StockFeedAdapter implements StockProvider {
    private final LegacyStockFeed source;

    StockFeedAdapter(LegacyStockFeed source) {
        if (source == null) {
            throw new IllegalArgumentException("Stock feed is missing");
        }
        this.source = source;
    }

    private Map<String, String> readPacket(String ticker) {
        String packet = source.fetchRawTickerPacket(ticker);
        Map<String, String> fields = new HashMap<>();

        for (String part : packet.split(";")) {
            int separator = part.indexOf('=');

            if (separator < 0) continue;

            String name = part.substring(0, separator).trim();
            String value = part.substring(separator + 1).trim();

            fields.put(name, value);
        }

        return fields;
    }
    @Override
    public double getMarketPrice(String ticker) {
        Map<String, String> values = readPacket(ticker);

        double buyingPrice = Double.parseDouble(
                values.getOrDefault("BID", "0")
        );

        double sellingPrice = Double.parseDouble(
                values.getOrDefault("ASK", "0")
        );

        return (buyingPrice + sellingPrice) / 2;
    }

    @Override
    public String getStatus(String ticker) {
        return readPacket(ticker)
                .getOrDefault("STATUS", "UNAVAILABLE");
    }
}

class CachedStockProxy implements StockProvider {

    private static class SavedPrice {
        final double amount;
        final long savedAt;

        SavedPrice(double amount, long savedAt) {
            this.amount = amount;
            this.savedAt = savedAt;
        }

        boolean stillValid(long currentTime, long lifetime) {
            return currentTime - savedAt < lifetime;
        }
    }

    private final StockProvider provider;
    private final long lifetime;
    private final Map<String, SavedPrice> prices = new HashMap<>();

    CachedStockProxy(StockProvider provider, long lifetime) {
        if (provider == null || lifetime <= 0) {
            throw new IllegalArgumentException("Invalid cache configuration");
        }

        this.provider = provider;
        this.lifetime = lifetime;
    }
    @Override
    public synchronized double getMarketPrice(String ticker) {
        String symbol = ticker.toUpperCase(Locale.ROOT);
        long currentTime = System.currentTimeMillis();

        SavedPrice saved = prices.get(symbol);

        if (saved != null && saved.stillValid(currentTime, lifetime)) {
            System.out.printf(
                    "[CACHE HIT] Returning cached price for %s: $%.2f (Age: %d ms)%n",
                    symbol, saved.amount, currentTime - saved.savedAt
            );
            return saved.amount;
        }
        System.out.printf(
                "[NETWORK FETCH] Cache miss or expired for %s. Calling upstream adapter...%n",
                symbol
        );

        double latest = provider.getMarketPrice(symbol);
        prices.put(symbol, new SavedPrice(
                latest, System.currentTimeMillis()
        ));

        return latest;
    }

    @Override
    public String getStatus(String ticker) {
        return provider.getStatus(ticker);
    }
}

public class Task2Runner {
    public static void main(String[] args) throws InterruptedException {
        LegacyStockFeed oldFeed = new LegacyStockFeed();

        StockProvider adapter = new StockFeedAdapter(oldFeed);
        StockProvider market = new CachedStockProxy(adapter, 1500);

        System.out.println(">>> 1. Initial Request (Cold Cache) <<<");
        System.out.printf(
                "AAPL Price: $%.2f%n",
                market.getMarketPrice("AAPL")
        );

        System.out.println("\n>>> 2. Immediate Repeat Request (Hot Cache) <<<");
        System.out.printf(
                "AAPL Price: $%.2f%n",
                market.getMarketPrice("AAPL")
        );

        System.out.println("\n>>> 3. Sleeping 1600ms to invalidate cache... <<<");
        Thread.sleep(1600);

        System.out.println(">>> 4. Subsequent Request (Expired Cache) <<<");
        System.out.printf(
                "AAPL Price: $%.2f%n",
                market.getMarketPrice("AAPL")
        );
    }
}
