package task3;

interface DispatchStrategy {
    boolean shouldDispatch(int severityLevel, int currentHour);
    String getStrategyName();
}

class ImmediateStrategy implements DispatchStrategy {

    @Override
    public boolean shouldDispatch(int severityLevel, int currentHour) {
        return true;
    }

    @Override
    public String getStrategyName() {
        return "IMMEDIATE_DISPATCH";
    }
}
class BusinessHoursStrategy implements DispatchStrategy {

    @Override
    public boolean shouldDispatch(int severityLevel, int currentHour) {
        boolean emergency = severityLevel >= 5;
        boolean workingTime = currentHour >= 9 && currentHour <= 17;

        return emergency || workingTime;
    }

    @Override
    public String getStrategyName() {
        return "BUSINESS_HOURS_ONLY (09:00 - 17:00)";
    }
}

interface MessageChannel {
    void transmit(String recipient, String message);
}

class EmailChannel implements MessageChannel {

    @Override
    public void transmit(String recipient, String message) {
        System.out.println("[EMAIL PROTOCOL -> " + recipient + "]");
        System.out.println(message);
    }
}

class SmsChannel implements MessageChannel {

    @Override
    public void transmit(String recipient, String message) {
        String smsText = message.replace("\n", " -- ");

        System.out.println(
                "[SMS PROTOCOL -> " + recipient + "] " + smsText
        );
    }
}
abstract class Alert {
    protected final MessageChannel channel;
    protected final DispatchStrategy strategy;

    protected Alert(MessageChannel channel, DispatchStrategy strategy) {
        if (channel == null || strategy == null) {
            throw new IllegalArgumentException(
                    "Channel and strategy must be provided"
            );
        }

        this.channel = channel;
        this.strategy = strategy;
    }
    public void processAlert(String recipient, String content, int hour) {
        int level = getSeverityLevel();

        if (!strategy.shouldDispatch(level, hour)) {
            System.out.printf(
                    "[DISPATCH SUPPRESSED] Alert blocked by policy '%s' at hour %02d:00 (Severity: %d)%n",
                    strategy.getStrategyName(), hour, level
            );
            return;
        }

        String preparedMessage = formatAlertBody(content);
        channel.transmit(recipient, preparedMessage);
    }

    public abstract int getSeverityLevel();
    public abstract String formatAlertBody(String raw);
}

class CriticalSystemAlert extends Alert {

    CriticalSystemAlert(MessageChannel channel, DispatchStrategy strategy) {
        super(channel, strategy);
    }

    @Override
    public int getSeverityLevel() {
        return 5;
    }
    @Override
    public String formatAlertBody(String raw) {
        StringBuilder warning = new StringBuilder();

        warning.append("!!! CRITICAL PRODUCTION ALERT !!!\n");
        warning.append("Incident Report: ").append(raw).append("\n");
        warning.append(
                "ACTION REQUIRED: SRE on-call immediate intervention."
        );

        return warning.toString();
    }
}

class DailySummaryAlert extends Alert {

    DailySummaryAlert(MessageChannel channel, DispatchStrategy strategy) {
        super(channel, strategy);
    }
    @Override
    public int getSeverityLevel() {
        return 1;
    }

    @Override
    public String formatAlertBody(String raw) {
        return "Daily Digest:\n" + raw;
    }
}

public class Task3Runner {
    public static void main(String[] args) {
        MessageChannel email = new EmailChannel();
        MessageChannel sms = new SmsChannel();

        DispatchStrategy immediate = new ImmediateStrategy();
        DispatchStrategy businessHours = new BusinessHoursStrategy();

        System.out.println(
                ">>> 1. Critical Alert via SMS (Night time: 02:00, Emergency Bypass) <<<"
        );
        Alert emergency = new CriticalSystemAlert(sms, businessHours);
        emergency.processAlert(
                "+1555019283",
                "Database primary node kernel panic",
                2
        );

        System.out.println(
                "\n>>> 2. Daily Summary via Email at Night (Hour: 23:00 - Should Suppress) <<<"
        );

        Alert summaryNight = new DailySummaryAlert(email, businessHours);
        summaryNight.processAlert(
                "dev-team@company.internal",
                "99.98% uptime achieved today.",
                23
        );
        System.out.println(
                "\n>>> 3. Daily Summary via Email during Workday (Hour: 11:00 - Should Deliver) <<<"
        );

        Alert summaryDay = new DailySummaryAlert(email, businessHours);
        summaryDay.processAlert(
                "dev-team@company.internal",
                "99.98% uptime achieved today.",
                11
        );
    }
}
