import jade.core.Agent; // Importation de la classe Agent de JADE
import javax.swing.*; // Importation de Swing pour les icônes
import java.awt.*; // Importation des classes AWT pour la gestion des images

public class MotherShip extends Agent {
    private int x; // Position X du vaisseau mère
    private int y; // Position Y du vaisseau mère
    private int collectedStones; // Nombre de pierres collectées
    private int maxCapacity;
    private Image icon; // Icône du vaisseau mère

    // Constructeur par défaut
    public MotherShip() {
        this.x = 0; // Position par défaut
        this.y = 0; // Position par défaut
        this.collectedStones = 0; // Initialisation du nombre de pierres collectées
        // Vous pouvez initialiser l'icône ici si nécessaire
    }

    // Constructeur supplémentaire pour initialiser avec des valeurs
    public MotherShip(int x, int y, String iconPath, int maxCapacity) {
        this.x = x; // Initialisation de la position X
        this.y = y; // Initialisation de la position Y
        this.collectedStones = 0; // Initialisation du nombre de pierres collectées
        this.maxCapacity = maxCapacity; // Set maximum capacity for stones

        // Charger l'icône du vaisseau mère
        this.icon = new ImageIcon(getClass().getResource(iconPath)).getImage()
                .getScaledInstance(50, 50, Image.SCALE_SMOOTH);
    }

    // Méthode pour déposer les pierres
    // Method to deposit stones
    public boolean depositStones(int stones) {
        if (canAccommodate(stones)) { // Check if it can accommodate the stones
            this.collectedStones += stones; // Increase the number of collected stones
            System.out.println(stones + " stones deposited. Total stones now: " + collectedStones);
            return true; // Deposit was successful
        } else {
            System.out.println("Cannot deposit " + stones + " stones. Exceeds capacity.");
            return false; // Deposit failed
        }
    }

    // Check if the mother ship can accommodate more stones
    public boolean canAccommodate(int stones) {
        return (collectedStones + stones) <= maxCapacity; // Return true if within capacity
    }

    // Accesseurs
    public int getX() {
        return x; // Retourne la position X
    }

    public int getY() {
        return y; // Retourne la position Y
    }

    public int getCollectedStones() {
        return collectedStones; // Retourne le nombre de pierres collectées
    }

    public Image getIcon() {
        return icon; // Retourne l'icône du vaisseau mère
    }

    @Override
    protected void setup() {
//        System.out.println("Agent MotherShip " + getLocalName() + " prêt.");
        // Ici vous pouvez ajouter d'autres initialisations si nécessaire
    }
}
