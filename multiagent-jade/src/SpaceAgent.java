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
    private int batteryLevel = 50;
    private int maxbatteryLevel = 50;
    private int lowBatteryThreshold = 10;

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
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private class ExplorationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            if (!visualizer.isRunning()) {
                block(); // Pause si la simulation est arrêtée
                return;
            }

            if(visualizer.isMothershipFull()){
                returnAllToMotherShip();
                return;
            }

            // Prioriser le retour au vaisseau mère si la batterie est faible
            if(batteryLevel<= lowBatteryThreshold){
                System.out.println(getLocalName() + " has low battery (" + batteryLevel + "). Returning to mother ship.");
                returnToMotherShip();
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
            batteryLevel--;

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

        private void requestHelp() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setContent("Help! I'm out of battery at (" + x + "," + y + ")");
            System.out.println(getLocalName() + " is requesting help at (" + x + "," + y + ")");

            List<AID> allAgents = visualizer.getAllAgents();
            if (allAgents == null || allAgents.isEmpty()) {
                System.out.println(getLocalName() + ": No other agents found.");
                return; // Arrête si aucun autre agent n'est trouvé
            }
            for (AID agent : allAgents) {
                if (!agent.equals(getAID())) { // Ne pas envoyer à soi-même
                    message.addReceiver(agent);
                    System.out.println(getLocalName() + " added receiver for battery: " + agent.getLocalName());
                }
            }

            send(message); // Envoyer le message
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


                //Recharger la batterie au niveau max
                batteryLevel = maxbatteryLevel;
                System.out.println(getLocalName() + " recharged to full battery (" + batteryLevel + ").");
                path = generateRandomPath();
            }
        }

        private void returnAllToMotherShip() {
            Point motherShipPosition = visualizer.getMotherShipPosition(); // Obtenir la position du vaisseau mère
            System.out.println(getLocalName() + " returning to mother ship because it is full");
            moveToPosition(motherShipPosition);
            if (x == motherShipPosition.x && y == motherShipPosition.y) {
                takeDown();
            }
            if (visualizer.allAgentsAtMotherShip()) {
                visualizer.finishSimulation(); // End the simulation if all agents are at the mother ship
            }
        }

        private void moveToPosition(Point target) {
            if (batteryLevel <= 0) {
                System.out.println(getLocalName() + " cannot move due to depleted battery. Requesting help...");
                requestHelp(); // Appeler la demande d'aide
                return;
            }
            // Déplacement vers la position cible
            if(x!=target.x){
                if (x < target.x) x++;
                else if (x > target.x) x--;
                batteryLevel--;
            }
            if(y!=target.y) {
                if (y < target.y) y++;
                else if (y > target.y) y--;
                batteryLevel--;
            }
            visualizer.updateAgentPosition(agentIndex, x, y);
            System.out.println(getLocalName() + " current position: (" + x + "," + y + "). Battery level: " + batteryLevel);
            // Si la batterie est épuisée en chemin, arrêter le mouvement
            if (batteryLevel <= 0) {
                System.out.println(getLocalName() + " battery depleted before reaching the mother ship!");
                return;
            }
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
            if (content.startsWith("Help! I'm out of battery")) {
                String[] parts = content.split(" ");
                String coordinates = parts[5].replace("(", "").replace(")", "");
                String[] coords = coordinates.split(",");
                int helpX = Integer.parseInt(coords[0]);
                int helpY = Integer.parseInt(coords[1]);
                moveToHelpBattery(helpX, helpY);

            }

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

        private void moveToHelpBattery(int helpX, int helpY) {
            while (Math.abs(x - helpX) <= 1 && Math.abs(y - helpY) <= 1) {
                if (x < helpX) x++;
                else if (x > helpX) x--;

                if (y < helpY) y++;
                else if (y > helpY) y--;

                visualizer.updateAgentPosition(agentIndex, x, y);
                block(delay);
            }
            // Vérifier si l'agent est à côté de l'agent qui demande de l'aide
            assistAgent(helpX, helpY);
        }

        private void assistAgent(int helpX, int helpY) {
            // Vérifier que cet agent peut donner de la batterie
            if (batteryLevel > lowBatteryThreshold) {
                int batteryToGive = batteryLevel / 2; // Donner la moitié de la batterie
                if(batteryLevel - batteryToGive < lowBatteryThreshold){
                    batteryToGive = batteryLevel - lowBatteryThreshold;
                }
                batteryLevel -= batteryToGive; // Déduire la batterie de cet agent
                System.out.println(getLocalName() + " is assisting at (" + helpX + ", " + helpY + ") by giving " + batteryToGive + " battery.");

                // L'agent qui a demandé de l'aide reçoit la batterie
                SpaceAgent helpRequestingAgent = visualizer.getAgentAt(helpX, helpY);
                if (helpRequestingAgent != null) {
                    helpRequestingAgent.batteryLevel += batteryToGive;
                    System.out.println(helpRequestingAgent.getLocalName() + " received " + batteryToGive + " battery from " + getLocalName() + ".");
                }
            } else {
                System.out.println(getLocalName() + " does not have enough battery to assist.");
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
