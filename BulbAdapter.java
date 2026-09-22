public class BulbAdapter implements SmartDevice {

    private final LegacyBulb bulb;

    private static final int K = 5;

    public BulbAdapter(LegacyBulb bulb) {
        if (bulb == null) {
            throw new IllegalArgumentException("LegacyBulb cannot be null");
        }

        this.bulb = bulb;
    }

    @Override
    public void turnOn() {
        bulb.setBrightness(255);
    }

    @Override
    public void turnOff() {
        bulb.setBrightness(0);
    }

    @Override
    public boolean isOn() {
        return bulb.hasPower() && bulb.readBrightness() > 0;
    }

    @Override
    public int getPowerPercent() {

        if (!bulb.hasPower()) {
            return 0;
        }

        int rawBrightness = bulb.readBrightness();

        if (rawBrightness == 0) {
            return 0;
        }

        int rawPercent = (rawBrightness * 100) / 255;

        int calibratedPercent = rawPercent + K;

        if (calibratedPercent > 100) {
            return 100;
        }

        return calibratedPercent;
    }
}