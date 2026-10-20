interface DigitalGame {
    void playDigital();
}

class ModernConsole {
    public void runGame(DigitalGame game) {
        System.out.println("Modern Console starting...");
        game.playDigital();
    }
}

class RetroCartridge {
    public void insertCartridge() {
        System.out.println("Retro cartridge inserted!");
        System.out.println("Playing classic retro game!");
    }
}

class CartridgeAdapter implements DigitalGame {
    private RetroCartridge cartridge;

    public CartridgeAdapter(RetroCartridge cartridge) {
        this.cartridge = cartridge;
    }

    @Override
    public void playDigital() {
        cartridge.insertCartridge();
    }
}

public class Main {
    public static void main(String[] args) {

        ModernConsole console = new ModernConsole();

        RetroCartridge retroGame = new RetroCartridge();

        DigitalGame adapter =
                new CartridgeAdapter(retroGame);

        console.runGame(adapter);
    }
}
