package projet.serveur;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import projet.modele.*;

public class ConnexionWorker extends Thread {
    // Creation des attributs
    private int id;
    private boolean arret = false;
    private Socket s;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private static final String CLASS_NAME_RESULTAT = Resultat.class.getName();
    private static final String CLASS_NAME_MESSAGE = Message.class.getName();

    public ConnexionWorker(int id, Socket s) {

        this.id = id;
        this.s = s;

        try {
            in = new ObjectInputStream(this.s.getInputStream());
            out = new ObjectOutputStream(this.s.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        try {
            // Le serveur attend que le worker lui envoi son nombre de coeur
            // pour pouvoir savoir combien de tache il peut lui attribuer

            // Tourne en permanence et genere des tache qu'il distribue au worker
            while (arret != true) {

                // Le serveur attend que le worker lui envoi son nombre de coeur
                // pour pouvoir savoir combien de tache il peut lui attribuer
                Object m = in.readObject();

                if (m != null) {
                    /*
                     * La partie qui gere si le message envoyé par le worker est un message
                     * simple ou le resultat envoye des taches qui lui ont été assignées
                     */
                    // System.out.println("Une reponse du worker");
                    if (m.getClass().getName().equals(CLASS_NAME_RESULTAT)) {
                        // System.out.println("C'est un resultat");
                        Resultat resultat = (Resultat) m;

                        Serveur.addResultat(resultat.getListPersistance());

                        System.out.println("Resultat du worker" + id + "ajouté avec succès");

                    }

                    /*
                     * cette partie gere la reception d un objet de type message
                     */
                    else if (m.getClass().getName().equals(CLASS_NAME_MESSAGE)) {
                        Message msg = (Message) m;
                        // System.out.println("C'est un message du worker" + id);
                        int p = Integer.parseInt(msg.parametre);
                        synchronized (Serveur.workerCoeurDispo) {
                            Serveur.workerCoeurDispo[this.id] = p;
                        }

                        ArrayList<Tache> lestaches = new ArrayList<Tache>();
                        for (int i = 0; i < Serveur.workerCoeurDispo[this.id]; i++) {

                            Tache ta = Serveur.listeTache.take();
                            lestaches.add(ta);
                        }
                        out.writeObject(lestaches);
                        out.flush();
                        // System.out.println("Tache donné au worker" + id);
                    }

                }

                // System.out.println("recu de ");

            }

            System.out.println("Le worker " + this.id + ", vient de deconnecter");
            System.out.print("\n=======================================================\n");
            // Le serveur ferme ses flux
            in.close();
            out.close();
            this.s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
