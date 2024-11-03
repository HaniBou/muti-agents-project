import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jade.core.AID;

public class PlanetVisualizer extends JPanel {
    private int gridSize; // Taille de la grille
    private int cellSize = 50; // Taille de chaque cellule
    private List<SpaceAgent> agents; // Liste des agents actifs
    private int[][] stones; // Matrice pour les pierres
    private boolean[][] occupied; // Matrice pour vérifier les cases occupées
    private Image agentIcon, stoneIcon; // Icônes pour l'agent et la pierre
    private boolean isRunning; // État de la simulation
    private List<Point> agentPositions; // Liste pour stocker les positions des agents
    private int[] agentCapacities; // Capacité courante de chaque agent
    private int agentMaxCapacity = 3; // Capacité max de collecte pour chaque agent
    private MotherShip mothership; // Instance du vaisseau mère

    // Constructeur de la classe
    public PlanetVisualizer(int gridSize, int numStones) {
        this.gridSize = gridSize;
        this.agents = new ArrayList<>(); // Initialiser la liste des agents
        this.agentCapacities = new int[0]; // Initialiser la liste des capacités
        setPreferredSize(new Dimension(gridSize * 20, gridSize * 20));

        // Chargement et redimensionnement des icônes
        try {
            this.agentIcon = new ImageIcon(getClass().getResource("/resources/agentIcon.png"))
                    .getImage().getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH);
            this.stoneIcon = new ImageIcon(getClass().getResource("/resources/stoneIcon.png"))
                    .getImage().getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Erreur de chargement des icônes, vérifiez les chemins de fichiers.");
            this.agentIcon = null;
            this.stoneIcon = null;
        }

        stones = new int[gridSize][gridSize];
        occupied = new boolean[gridSize][gridSize]; // Initialisation de la matrice occupée
        initializeStones(numStones); // Initialiser les pierres
        isRunning = true; // Initialiser l'état de la simulation
        agentPositions = new ArrayList<>(); // Initialiser la liste des positions des agents

        // Initialiser le vaisseau mère à une position fixe (par exemple, au centre de la grille)
        this.mothership = new MotherShip(gridSize / 2, gridSize / 2, "/resources/mothershipIcon.png");
    }
    public Point getMotherShipPosition() {
        return new Point(mothership.getX(), mothership.getY());
    }
    // Méthode pour ajouter un agent
    public int addAgentPosition(int x, int y) {
        agentPositions.add(new Point(x, y));
        setOccupied(x, y, true);

        // Étendre le tableau des capacités pour inclure le nouvel agent
        int[] newAgentCapacities = new int[agentPositions.size()];
        System.arraycopy(agentCapacities, 0, newAgentCapacities, 0, agentCapacities.length);
        agentCapacities = newAgentCapacities;

        repaint();
        return agentPositions.size() - 1; // Retourne l'index de l'agent ajouté
    }

    // Méthode pour supprimer un agent
    public void removeAgent(int agentIndex) {
        if (agentIndex >= 0 && agentIndex < agentPositions.size()) {
            Point pos = agentPositions.get(agentIndex);
            setOccupied(pos.x, pos.y, false); // Libérer la position occupée
            agentPositions.remove(agentIndex); // Supprimer la position de l'agent
            repaint();
        }
    }
    public List<AID> getAllAgents() {
        List<AID> aidList = new ArrayList<>();
        for (SpaceAgent agent : agents) { //
            aidList.add(agent.getAID()); // A
        }
        return aidList;
    }

    // Méthode pour mettre à jour la capacité d'un agent
    public void updateAgentCapacity(int agentIndex, int collected, int capacity) {
        if (agentIndex >= 0 && agentIndex < agentCapacities.length) {
            agentCapacities[agentIndex] = collected;
            agentMaxCapacity = capacity; // Si la capacité maximale est uniforme
            repaint();
        }
    }

    // Méthode pour initialiser les pierres dans la grille
    private void initializeStones(int numStones) {
        Random rand = new Random();
        for (int i = 0; i < numStones; i++) {
            int x, y;
            do {
                x = rand.nextInt(gridSize);
                y = rand.nextInt(gridSize);
            } while (stones[x][y] > 0); // Assurez-vous de ne pas dépasser une pierre par case
            stones[x][y] = rand.nextInt(7) + 1; // Poser les pierres
        }
    }

    // Vérifie s'il y a une pierre à la position (x, y)
    public boolean hasStone(int x, int y) {
        if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
            return stones[x][y] > 0;
        }
        return false;
    }

    // Méthode pour collecter une pierre
    public void collectStone(int x, int y) {
        if (hasStone(x, y)) {
            stones[x][y]--;
        }
    }

    // Met à jour la position de l'agent sur la grille
    public void updateAgentPosition(int agentIndex, int x, int y) {
        // Assurez-vous que la liste des positions est suffisamment grande
        if (agentIndex >= agentPositions.size()) {
            agentPositions.add(new Point(x, y));
        } else {
            agentPositions.set(agentIndex, new Point(x, y)); // Mettre à jour la position de l'agent
        }
        repaint(); // Redessiner le panneau
    }

    // Vérifie si une case est occupée
    public boolean isOccupied(int x, int y) {
        return occupied[x][y];
    }

    // Marque une case comme occupée ou non
    public void setOccupied(int x, int y, boolean value) {
        occupied[x][y] = value;
    }

    // Retourne la taille de la grille
    public int getGridSize() {
        return gridSize;
    }

    // Retourne l'état de la simulation
    public boolean isRunning() {
        return isRunning;
    }

    // Définit l'état de la simulation
    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    // Dessin des composants graphiques
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner la grille
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);

                // Dessiner les pierres
                if (stones[col][row] > 0) {
                    if (stoneIcon != null) {
                        g.drawImage(stoneIcon, col * cellSize, row * cellSize, this);
                    } else {
                        g.setColor(Color.GRAY);
                        g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize); // Représenter la pierre par un rectangle
                    }
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 20)); // Choisir une police de taille 14 pour le nombre
                    g.drawString(String.valueOf(stones[col][row]),
                            col * cellSize + cellSize -10 , // Position horizontale centrée
                            row * cellSize + 15); // Position verticale légèrement au-dessus de l'icône
                }
            }
        }

        // Dessiner les agents et leur barre de progression de collecte
        for (int i = 0; i < agentPositions.size(); i++) {
            Point pos = agentPositions.get(i);
            if (agentIcon != null) {
                g.drawImage(agentIcon, pos.x * cellSize, pos.y * cellSize, this);
            } else {
                g.setColor(Color.BLUE);
                g.fillRect(pos.x * cellSize, pos.y * cellSize, cellSize, cellSize); // Représenter l'agent par un rectangle
            }

            // Barre de progression pour visualiser la collecte
            int progressHeight = 5;
            int progressWidth = (int) ((double) agentCapacities[i] / agentMaxCapacity * cellSize);

            g.setColor(Color.GREEN);
            g.fillRect(pos.x * cellSize, pos.y * cellSize + cellSize - progressHeight, progressWidth, progressHeight);
        }

        // Dessiner le vaisseau mère
        if (mothership.getIcon() != null) {
            g.drawImage(mothership.getIcon(), mothership.getX() * cellSize, mothership.getY() * cellSize, this);
        } else {
            g.setColor(Color.RED);
            g.fillRect(mothership.getX() * cellSize, mothership.getY() * cellSize, cellSize, cellSize); // Représenter le vaisseau mère par un rectangle rouge par défaut
        }
    }

    // Méthode pour déposer les pierres dans le vaisseau mère
    public void depositStones(int agentIndex, int stones) {
        // Logique pour déposer des pierres dans le vaisseau mère
        mothership.depositStones(stones);

        // Réinitialiser la capacité de l'agent
        agentCapacities[agentIndex] = 0;
        repaint();
    }
}
