import jade.core.Agent; // Importation de la classe Agent de JADE
import javax.swing.*; // Importation de Swing pour les icônes
import java.awt.*; // Importation des classes AWT pour la gestion des images

public class MotherShip extends Agent {
    private int x; // Position X du vaisseau mère
    private int y; // Position Y du vaisseau mère
    private int collectedStones; // Nombre de pierres collectées
    private Image icon; // Icône du vaisseau mère

    // Constructeur par défaut
    public MotherShip() {
        this.x = 0; // Position par défaut
        this.y = 0; // Position par défaut
        this.collectedStones = 0; // Initialisation du nombre de pierres collectées
        // Vous pouvez initialiser l'icône ici si nécessaire
    }

    // Constructeur supplémentaire pour initialiser avec des valeurs
    public MotherShip(int x, int y, String iconPath) {
        this.x = x; // Initialisation de la position X
        this.y = y; // Initialisation de la position Y
        this.collectedStones = 0; // Initialisation du nombre de pierres collectées

        // Charger l'icône du vaisseau mère
        this.icon = new ImageIcon(getClass().getResource(iconPath)).getImage()
                .getScaledInstance(50, 50, Image.SCALE_SMOOTH);
    }

    // Méthode pour déposer les pierres
    public void depositStones(int stones) {
        this.collectedStones += stones; // Augmenter le nombre de pierres collectées
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
