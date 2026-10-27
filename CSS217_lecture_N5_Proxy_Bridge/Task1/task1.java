interface Image {
    void show();
}

class RealImage implements Image {
    private String fileName;

    public RealImage(String fileName) {
        this.fileName = fileName;
        System.out.println("--> [SLOW] Reading " + fileName
                + " from hard drive...");
    }

    @Override
    public void show() {
        System.out.println("Displaying " + fileName + " on screen!");
    }
}

class ProxyImage implements Image {
    private String fileName;
    private String userRole;
    private RealImage realImage;

    public ProxyImage(String fileName, String userRole) {
        this.fileName = fileName;
        this.userRole = userRole;
    }

    @Override
    public void show() {
        if (!userRole.equals("ADMIN")) {
            System.out.println("[ACCESS DENIED] You lack permission to view: "
                    + fileName);
            return;
        }

        if (realImage == null) {
            realImage = new RealImage(fileName);
        }

        realImage.show();
    }
}

public class Task1 {
    public static void main(String[] args) {
        Image guest = new ProxyImage("salary_sheet.png", "GUEST");
        Image admin = new ProxyImage("salary_sheet.png", "ADMIN");

        guest.show();
        admin.show();
    }
}
