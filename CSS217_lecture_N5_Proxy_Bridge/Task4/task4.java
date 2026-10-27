
interface Device {
    void turnOn();
    void turnOff();
    void setVolume(int level);
}

class SmartLight implements Device {

    @Override
    public void turnOn() {
        System.out.println("Smart Light glows bright white.");
    }

    @Override
    public void turnOff() {
        System.out.println("Smart Light shuts off.");
    }

    @Override
    public void setVolume(int level) {
        System.out.println("Smart Light brightness set to: " + level + "%");
    }
}

class BasicRemote {
    protected Device device;

    public BasicRemote(Device device) {
        this.device = device;
    }

    public void powerOn() {
        device.turnOn();
    }

    public void powerOff() {
        device.turnOff();
    }
}

class AdvancedRemote extends BasicRemote {

    public AdvancedRemote(Device device) {
        super(device);
    }

    public void mute() {
        System.out.println("[Remote] Mute button pressed!");
        device.setVolume(0);
    }
}

public class Task4 {
    public static void main(String[] args) {
        Device light = new SmartLight();

        AdvancedRemote remote = new AdvancedRemote(light);

        remote.powerOn();
        remote.mute();
    }
}
