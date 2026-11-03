package task4;
import java.util.Locale;

class LegacyForeignOrder {
    private final String orderId;
    private final double gbpAmount;

    LegacyForeignOrder(String orderId, double gbpAmount) {
        this.orderId = orderId;
        this.gbpAmount = gbpAmount;
    }

    public String getOrderId() {
        return orderId;
    }
    public double getGbpAmount() {
        return gbpAmount;
    }
}

class LegacyCurrencyConverter {
    public double convert(double amount, String from, String to) {
        if (from.equalsIgnoreCase("GBP")
                && to.equalsIgnoreCase("USD")) {
            return amount * 1.25;
        }

        return amount;
    }
}
interface PricingService {
    double calculateTotal();
    String getReceiptBreakdown();
}

class CurrencyOrderAdapter implements PricingService {
    private final LegacyForeignOrder order;
    private final LegacyCurrencyConverter exchange;
    private final String targetCurrency;

    CurrencyOrderAdapter(
            LegacyForeignOrder order,
            LegacyCurrencyConverter exchange,
            String targetCurrency
    ) {
        if (order == null || exchange == null || targetCurrency == null) {
            throw new IllegalArgumentException("Invalid order configuration");
        }

        this.order = order;
        this.exchange = exchange;
        this.targetCurrency = targetCurrency;
    }

    @Override
    public double calculateTotal() {
        double pounds = order.getGbpAmount();
        return exchange.convert(pounds, "GBP", targetCurrency);
    }

    @Override
    public String getReceiptBreakdown() {
        return String.format(
                Locale.US,
                "Order [%s] Base Item: $%.2f %s (Converted from %.2f GBP)",
                order.getOrderId(),
                calculateTotal(),
                targetCurrency,
                order.getGbpAmount()
        );
    }
}
abstract class PricingDecorator implements PricingService {
    protected final PricingService wrappedService;

    protected PricingDecorator(PricingService service) {
        if (service == null) {
            throw new IllegalArgumentException("Pricing service is missing");
        }
        wrappedService = service;
    }

    @Override
    public double calculateTotal() {
        return wrappedService.calculateTotal();
    }

    @Override
    public String getReceiptBreakdown() {
        return wrappedService.getReceiptBreakdown();
    }
}

class GiftWrapDecorator extends PricingDecorator {
    private static final double GIFT_FEE = 5.00;

    GiftWrapDecorator(PricingService service) {
        super(service);
    }

    @Override
    public double calculateTotal() {
        return super.calculateTotal() + GIFT_FEE;
    }

    @Override
    public String getReceiptBreakdown() {
        return super.getReceiptBreakdown() + " + [Gift Wrap: $5.00]";
    }
}

class ExpressCourierDecorator extends PricingDecorator {
    private static final double DELIVERY_FEE = 15.00;

    ExpressCourierDecorator(PricingService service) {
        super(service);
    }
    @Override
    public double calculateTotal() {
        return super.calculateTotal() + DELIVERY_FEE;
    }

    @Override
    public String getReceiptBreakdown() {
        return super.getReceiptBreakdown()
                + " + [Express Courier: $15.00]";
    }
}

class CustomsDutyDecorator extends PricingDecorator {
    private static final double CUSTOMS_RATE = 0.085;

    CustomsDutyDecorator(PricingService service) {
        super(service);
    }
    @Override
    public double calculateTotal() {
        double amountBeforeTax = super.calculateTotal();
        double withCustoms = amountBeforeTax * (1 + CUSTOMS_RATE);

        return Math.round(withCustoms * 100.0) / 100.0;
    }

    @Override
    public String getReceiptBreakdown() {
        return super.getReceiptBreakdown()
                + " + [Customs Duty: 8.5%]";
    }
}

public class Task4Runner {
    public static void main(String[] args) {
        LegacyForeignOrder order =
                new LegacyForeignOrder("ORD-9821", 100.00);

        LegacyCurrencyConverter converter =
                new LegacyCurrencyConverter();

        PricingService baseOrder =
                new CurrencyOrderAdapter(order, converter, "USD");

        System.out.printf(
                "Base Converted: $%.2f%n",
                baseOrder.calculateTotal()
        );

        PricingService checkout = new CustomsDutyDecorator(
                new ExpressCourierDecorator(
                        new GiftWrapDecorator(baseOrder)
                )
        );

        System.out.println("\n>>> INVOICE BREAKDOWN <<<");
        System.out.println(checkout.getReceiptBreakdown());

        System.out.printf(
                "FINAL CHARGE TOTAL: $%.2f%n",
                checkout.calculateTotal()
        );
    }
}
