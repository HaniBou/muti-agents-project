import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class Main {
    private static AgentContainer ac; // Conteneur d'agents JADE
    private static int agentCounter = 0; // Compteur pour nommer les agents dynamiquement
    private static DefaultListModel<String> agentListModel; // Modèle pour la liste des agents

    public static void main(String[] args) {
        // Démarrage de JADE dans un nouveau thread
        new Thread(() -> {
            Runtime runtime = Runtime.instance();
            Properties properties = new ExtendedProperties();
            properties.setProperty(Profile.GUI, "true");
            Profile profile = new ProfileImpl(jade.util.leap.Properties.toLeapProperties(properties));
            ac = runtime.createMainContainer(profile);
        }).start();

        // Initialisation de l'interface graphique
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Planet Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 800); // Largeur augmentée pour le panneau de contrôle à droite
            frame.setLayout(new BorderLayout());

            // Panneau de contrôle avec GridBagLayout
            JPanel controlPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JTextField gridSizeField = new JTextField("10", 5);
            JTextField stoneCountField = new JTextField("10", 5);
            JTextField agentCountField = new JTextField("5", 5);
            JTextField delayField = new JTextField("1000", 5);
            JTextField obstacleCountField = new JTextField("10", 5);

            gbc.gridx = 0; gbc.gridy = 0;
            controlPanel.add(new JLabel("Settings"), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            controlPanel.add(new JLabel("Taille de la grille :"), gbc);
            gbc.gridx = 1;
            controlPanel.add(gridSizeField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            controlPanel.add(new JLabel("Nombre de tas de pierres :"), gbc);
            gbc.gridx = 1;
            controlPanel.add(stoneCountField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            controlPanel.add(new JLabel("Nombre d'agents :"), gbc);
            gbc.gridx = 1;
            controlPanel.add(agentCountField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            controlPanel.add(new JLabel("Délai entre déplacements (ms) :"), gbc);
            gbc.gridx = 1;
            controlPanel.add(delayField, gbc);

            gbc.gridx = 0; gbc.gridy = 5;
            controlPanel.add(new JLabel("Nombre d'obstacles :"), gbc);
            gbc.gridx = 1;
            controlPanel.add(obstacleCountField, gbc);

            // Initialisation du modèle pour la liste des agents
            agentListModel = new DefaultListModel<>();
            JList<String> agentList = new JList<>(agentListModel);
            JScrollPane agentScrollPane = new JScrollPane(agentList);
            agentScrollPane.setPreferredSize(new Dimension(150, 200));

            // Section liste des agents
            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
            controlPanel.add(new JLabel("Liste des agents présents :"), gbc);

            gbc.gridy = 7;
            controlPanel.add(agentScrollPane, gbc);

            // Ajouter les boutons
            JButton startButton = new JButton("Lancer");
            JButton addAgentButton = new JButton("Ajouter Agent");
            JButton pauseButton = new JButton("Pause");
            JButton resumeButton = new JButton("Reprendre");
            JButton stopButton = new JButton("Arrêter");

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(startButton);
            buttonPanel.add(addAgentButton);
            buttonPanel.add(pauseButton);
            buttonPanel.add(resumeButton);
            buttonPanel.add(stopButton);

            // Ajouter le panneau de contrôle à droite
            frame.add(controlPanel, BorderLayout.EAST);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            // Action bouton "Lancer"
            startButton.addActionListener(e -> {
                int gridSize;
                int stoneCount;
                int obstcleCount;
                int numAgents;
                int delay;

                try {
                    gridSize = Integer.parseInt(gridSizeField.getText());
                    stoneCount = Integer.parseInt(stoneCountField.getText());
                    numAgents = Integer.parseInt(agentCountField.getText());
                    delay = Integer.parseInt(delayField.getText());
                    obstcleCount = Integer.parseInt(obstacleCountField.getText());

                    // Création du visualisateur et du conteneur de défilement
                    PlanetVisualizer visualizer = new PlanetVisualizer(gridSize, stoneCount, obstcleCount);
                    JScrollPane scrollPane = new JScrollPane(visualizer);
                    frame.add(scrollPane, BorderLayout.CENTER);
                    frame.revalidate();

                    // Configurer les paramètres des agents
                    SpaceAgent.setVisualizer(visualizer);
                    SpaceAgent.setDelay(delay);

                    pauseButton.addActionListener(ev -> visualizer.setRunning(false));
                    resumeButton.addActionListener(ev -> visualizer.setRunning(true));

                    // Bouton d'arrêt
                    stopButton.addActionListener(ev -> stopAllAgents());

                    // Démarrer l'agent "vaisseau mère"
                    addMotherShipAgent();

                    // Créer les agents
                    for (int i = 0; i < numAgents; i++) {
                        addAgent();
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Veuillez entrer des nombres valides pour tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Ajouter un agent dynamiquement
            addAgentButton.addActionListener(e -> {
                addAgent();
            });

            frame.setVisible(true);
        });
    }

    // Méthode pour ajouter un agent
    private static void addAgent() {
        try {
            String agentName = "Agent" + agentCounter++;
            AgentController agent = ac.createNewAgent(agentName, "SpaceAgent", new Object[]{});
            agent.start();
            agentListModel.addElement(agentName); // Ajouter le nom de l'agent dans la liste
        } catch (StaleProxyException ex) {
            ex.printStackTrace();
        }
    }

    // Méthode pour ajouter le vaisseau mère
    private static void addMotherShipAgent() {
        try {
            String motherShipName = "motherShip";
            AgentController motherShipAgent = ac.createNewAgent(motherShipName, "MotherShip", null);
            motherShipAgent.start();
            agentListModel.addElement(motherShipName); // Ajouter le nom du vaisseau mère dans la liste
        } catch (StaleProxyException ex) {
            ex.printStackTrace();
        }
    }

    // Méthode pour arrêter tous les agents et vider la liste
    private static void stopAllAgents() {
        try {
            ac.kill();
            agentListModel.clear(); // Effacer la liste des agents affichée
        } catch (StaleProxyException ex) {
            ex.printStackTrace();
        }
    }
}
