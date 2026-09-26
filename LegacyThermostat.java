public class LegacyThermostat {

    private String dialPosition = "IDLE";

    public void rotateDial(String position) {
        this.dialPosition = position;
    }

    public String checkDial() {
        return this.dialPosition;
    }
}