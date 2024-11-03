### Description des fichiers
- **SpaceAgent.java** :
    - Représente un agent explorateur qui collecte des pierres sur une planète.
    - Se déplace aléatoirement sur une grille.
    - Collecte des pierres et envoie des demandes d'aide aux autres agents si nécessaire.
    - Gère les comportements d'exploration et de communication avec d'autres agents.

- **MotherShip.java** :
    - Représente le vaisseau mère, qui ne se déplace pas.
    - Utilisé pour stocker les pierres collectées par les agents spatiaux (SpaceAgent).
    - Gère la position du vaisseau et le nombre total de pierres accumulées.

- **PlanetVisualizer.java** :
    - Interface graphique représentant une grille où les agents spatiaux interagissent pour collecter des pierres.
    - Gère la position et l'état des agents, ainsi que la distribution des pierres sur la grille.
    - Affiche le vaisseau mère, qui sert de dépôt pour les pierres collectées.
    - Fournit des méthodes pour :
        - Ajouter ou supprimer des agents.
        - Mettre à jour les positions des agents.
        - Visualiser la progression dans la collecte.

- **Main** :
    - Point d'entrée de l'application JADE pour visualiser l'interaction des agents spatiaux.
    - Initialise un conteneur d'agents JADE.
    - Crée une interface graphique avec des champs de configuration pour :
        - La taille de la grille.
        - Le nombre de pierres.
        - Le nombre d'agents.
        - Un délai de déplacement.
    - Permet de lancer la simulation, d'ajouter des agents dynamiquement, de mettre en pause ou de reprendre l'exécution, et d'arrêter tous les agents.
    - Gère l'ajout d'un agent représentant le vaisseau mère qui collecte les pierres.


src/
├── main.java
│     ├── AddAgent
│     ├── AddMotherShip │
│     └── ExplorationBehaviour.java
├── SpaceAgent.java
│     ├── Take down
│     ├── Set Visualizer
│     ├── Set Delay
│     ├── Random Path
│     ├── NotifyotherAgents
│     ├── ReturntoMotherShip
│     ├── Move To
│     ├── Handle request
│     ├── Move to Help
│     └── Assist at Position
└── PlanetVisualizer.java
│     ├── Get MotherShip Pos
│     ├── Add Agent Pos
│     ├── Remove Agent
│     ├── Get All Agents
│     ├── Update Agent capacity
│     ├── Init Stones
│     ├── Has Stones / Collect stones
│     ├── Update Agent pos
│     ├── Is Occupied / Set Occupied
│     ├── Get grid size
│     ├── Paint / Draw
│     └── Deposit Stones
└── MotherShip.java
      ├── DepositStones
      ├── get X / Y
      ├── get Collected stones
      └── get Icone