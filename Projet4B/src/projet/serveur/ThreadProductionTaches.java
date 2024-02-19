package projet.serveur;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import projet.modele.*;

public class ThreadProductionTaches extends Thread {
    private final BlockingQueue<Tache> queue; // La file d'attente où seront ajoutées les tâches produites
    private BigInteger nbCourant;
    private BigInteger tailleTache;

    /**
     * @param queue
     */
    public ThreadProductionTaches(BlockingQueue<Tache> queue) {
        this.queue = queue;
        this.nbCourant = BigInteger.ZERO;
        this.tailleTache = new BigInteger("100000");
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Créer une nouvelle instance de la classe Tache
                while (true) {
                    if (Serveur.listTachePlein() == false)
                        break;
                }
                Tache nouvelleTache = new Tache(this.nbCourant, this.nbCourant.add(tailleTache));
                this.nbCourant = this.nbCourant.add(tailleTache);

                // Ajouter la tâche à la file d'attente
                queue.put(nouvelleTache);

                // Attendre un certain temps avant de produire une nouvelle tâche
                Thread.sleep(1000); // 1 seconde
            }
        } catch (InterruptedException e) {
            System.err.println("Le thread ProductionTache a été interrompu.");
            Thread.currentThread().interrupt();
        }
    }
}
