import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SpaceAgent extends Agent {

    private static PlanetVisualizer visualizer;
    private static int delay = 1000; // Délai entre les mouvements
    private int capacity = 3; // Capacité maximale de collecte
    private int collected = 0; // Nombre de pierres collectées
    private int agentIndex; // L'index de cet agent dans PlanetVisualizer
    private int x, y; // Position de l'agent
    private List<Point> path = new ArrayList<>(); // Chemin de l'agent

    @Override
    protected void setup() {
        if (visualizer == null) {
            throw new IllegalStateException("Visualizer must be set before agent setup.");
        }

        visualizer.registerAgent(this);
        // Trouver une position disponible
        do {
            x = (int) (Math.random() * visualizer.getGridSize());
            y = (int) (Math.random() * visualizer.getGridSize());
        } while (visualizer.isOccupied(x, y));

        agentIndex = visualizer.addAgentPosition(x, y);
        path = generateRandomPath(); // Générer un chemin aléatoire
        addBehaviour(new ExplorationBehaviour());
        addBehaviour(new CommunicationBehaviour());
    }

    @Override
    protected void takeDown() {
        visualizer.removeAgent(agentIndex); // Utilise agentIndex pour supprimer l'agent
        System.out.println("Agent " + getLocalName() + " terminé.");
    }

    public static void setVisualizer(PlanetVisualizer v) {
        visualizer = v;
    }

    public static void setDelay(int d) {
        delay = d;
    }

    private List<Point> generateRandomPath() {
        List<Point> newPath = new ArrayList<>();
        int currentX = x;
        int currentY = y;

        for (int i = 0; i < 10; i++) {
            int direction = (int) (Math.random() * 4); // 0: up, 1: down, 2: left, 3: right

            switch (direction) {
                case 0: // Up
                    if (currentY > 0) currentY--;
                    break;
                case 1: // Down
                    if (currentY < visualizer.getGridSize() - 1) currentY++;
                    break;
                case 2: // Left
                    if (currentX > 0) currentX--;
                    break;
                case 3: // Right
                    if (currentX < visualizer.getGridSize() - 1) currentX++;
                    break;
            }


            newPath.add(new Point(currentX, currentY)); // Ajouter le nouveau point au chemin
        }

        return newPath;
    }

    private class ExplorationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            if (!visualizer.isRunning()) {
                block(); // Pause si la simulation est arrêtée
                return;
            }

            // Vérifiez si la capacité de collecte est atteinte
            if (collected >= capacity) {
                returnToMotherShip(); // Retourner si la capacité est atteinte
                return;
            }

            // Si l'agent est sur une cellule avec des pierres, collectez-en autant que possible
            if (visualizer.hasStone(x, y) && collected < capacity) {
                collected++;
                visualizer.collectStone(x, y);
                visualizer.updateAgentCapacity(agentIndex, collected, capacity); // Mettre à jour la capacité

                // Informer les autres agents pour obtenir de l'aide
                notifyOtherAgents();

                // Attendre avant de réessayer de collecter une autre pierre
                block(delay);
                return; // Rester ici tant que nous avons des pierres à collecter
            }

            // Si le chemin est vide, générez un nouveau chemin
            if (path.isEmpty()) {
                path = generateRandomPath(); // Générer un nouveau chemin si vide
            }

            Point nextPoint = path.get(0);
            if (visualizer.isOccupied(nextPoint.x, nextPoint.y)) {
                // Si la cellule suivante est occupée, changer de direction
                path.remove(0); // Retirer le point actuel du chemin
                block(delay); // Attendre avant de réessayer
                return;
            }

            // Déplacer vers nextPoint
            x = nextPoint.x;
            y = nextPoint.y;
            path.remove(0); // Supprimer le point du chemin

            visualizer.updateAgentPosition(agentIndex, x, y); // Mettre à jour la position de l'agent
            block(delay); // Délai entre les actions
        }

        private void notifyOtherAgents() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setContent("Help at " + x + "," + y);
            System.out.println(getLocalName() + " is preparing a help request at (" + x + "," + y + ")");

            List<AID> allAgents = visualizer.getAllAgents();
            if (allAgents == null || allAgents.isEmpty()) {
                System.out.println(getLocalName() + ": No other agents found.");
                return; // Arrête si aucun autre agent n'est trouvé
            }

            for (AID agent : allAgents) {
                if (!agent.equals(getAID())) { // Ne pas envoyer à soi-même
                    message.addReceiver(agent);
                    System.out.println(getLocalName() + " added receiver: " + agent.getLocalName());
                }
            }

            send(message); // Envoyer le message
            System.out.println(getLocalName() + " sent help request to " + (message.getAllReceiver()) + " agents.");
        }

        private void returnToMotherShip() {
            Point motherShipPosition = visualizer.getMotherShipPosition(); // Obtenir la position du vaisseau mère
            System.out.println(getLocalName() + " returning to mother ship to deposit stones.");
            moveToPosition(motherShipPosition);
            if (x == motherShipPosition.x && y == motherShipPosition.y) {
                visualizer.depositStones(agentIndex, collected); // Déposer les pierres
                System.out.println(getLocalName() + " deposited " + collected + " stones at mother ship.");
                collected = 0; // Réinitialiser les pierres collectées
                visualizer.updateAgentCapacity(agentIndex, collected, capacity); // Mettre à jour la capacité après dépôt
                path = generateRandomPath();
            }
        }

        private void moveToPosition(Point target) {
            // Déplacement vers la position cible
            if (x < target.x) x++;
            else if (x > target.x) x--;

            if (y < target.y) y++;
            else if (y > target.y) y--;

            visualizer.updateAgentPosition(agentIndex, x, y);
            block(delay);
        }
    }

    private class CommunicationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                System.out.println("Message reçu par " + getLocalName() + ": " + msg.getContent());
                handleHelpRequest(msg); // Réagir au message d'aide reçu
            } else {
                System.out.println(getLocalName() + " didn't receive any message in this cycle.");
            }
            block();
        }

        private void handleHelpRequest(ACLMessage msg) {
            String content = msg.getContent();
            System.out.println(getLocalName() + " received message: " + content);

            if (content.startsWith("Help at")) {
                // Extraire la position à laquelle l'aide est demandée
                String[] parts = content.split(" ");
                String[] coordinates = parts[2].split(",");
                int helpX = Integer.parseInt(coordinates[0]);
                int helpY = Integer.parseInt(coordinates[1]);

                // Déplacer cet agent vers la position demandée
                System.out.println(getLocalName() + " moving to help at (" + helpX + "," + helpY + ")");
                moveToHelpPosition(helpX, helpY);
            }
        }

        private void moveToHelpPosition(int helpX, int helpY) {
            System.out.println(getLocalName() + " moving to (" + helpX + "," + helpY + ")");
            while (x != helpX || y != helpY) {
                if (x < helpX) x++;
                else if (x > helpX) x--;

                if (y < helpY) y++;
                else if (y > helpY) y--;

                visualizer.updateAgentPosition(agentIndex, x, y);
                System.out.println(getLocalName() + " current position: (" + x + "," + y + ")");
                block(delay);
            }
            // Une fois arrivé, effectuer une action d'aide
            assistAtPosition(helpX, helpY);
        }

        private void assistAtPosition(int helpX, int helpY) {
            // Effectuer une action d'assistance ici, par exemple collecter des pierres
            System.out.println(getLocalName() + " assisting at (" + helpX + "," + helpY + ")");
            if (visualizer.hasStone(helpX, helpY)) {
                collected++;
                visualizer.collectStone(helpX, helpY);
                visualizer.updateAgentCapacity(agentIndex, collected, capacity); // Mettre à jour la capacité
                System.out.println(getLocalName() + " collected stone at (" + helpX + "," + helpY + "). Total collected: " + collected);
            }
        }
    }
}
