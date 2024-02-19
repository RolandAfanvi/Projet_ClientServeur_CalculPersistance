package projet.worker;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import projet.modele.*;

public class Worker {
    static String NbCoeur = "8";
    static int port = 8081;
    static boolean arreter = false;
    static int id = 0;
    static String ip = "172.16.54.204";
    static ThreadCalcul[] threadCalcul;
    static ArrayBlockingQueue<Tache> listTaches;
    static Resultat resultat;
    static int numTacheCourant = 0;

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("world");
        if (args.length != 0) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
            NbCoeur = args[2];

        }
        listTaches = new ArrayBlockingQueue<Tache>(1000000);
        resultat = new Resultat(id);

        // Creation des threads qui s'occupe chacune d une tache

        threadCalcul = new ThreadCalcul[Integer.parseInt(NbCoeur)];
        for (int i = 0; i < Integer.parseInt(NbCoeur); i++) {
            threadCalcul[i] = new ThreadCalcul(i);
            // threadCalcul[i].start();
        }

        try {
            Socket socket = new Socket(ip, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            // on lance les thread calculs
            for (int i = 0; i < Integer.parseInt(NbCoeur); i++) {
                threadCalcul[i].start();
            }

            while (true) {

                int coeurdispo = Runtime.getRuntime().availableProcessors();
                // System.out.println(coeurdispo);
                if (listTaches.size() <= 0) {

                    // System.out.println(listTaches.size()); // taille du resultat

                    String coeur = Integer.toString(coeurdispo);
                    Message message = new Message("Worker", Commande.Message, coeur,
                            "Voici le Nbre de coeur  de mon processeur ");
                    out.writeObject(message);
                    // System.out.println("J'ai envoyé le nbre de coeur dispo de mon processeur");
                    try {
                        ArrayList<Tache> ta = (ArrayList<Tache>) in.readObject();
                        if (ta != null) {
                            try {
                                // System.out.println("Tache");
                                for (Tache t : ta) {
                                    listTaches.put(t);
                                }
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            // System.out.println("Taches ajouté avec succès");
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                if (resultat.taille() >= coeurdispo * 100000) {
                    try {
                        // Acquérir le verrou avant d'accéder à la variable de résultat partagée
                        synchronized (resultat) {
                            out.writeObject(resultat);
                            resultat = new Resultat(id);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * Methode qui va permettre au worker de distribuer les taches a ses
     * threadCalcul
     */
    public synchronized static Tache getTache() {
        // System.out.println("Je donne une tache ");
        numTacheCourant++;
        Tache t = new Tache();
        try {
            t = listTaches.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // System.out.println("Tache donnée avec succès");
        return t;
    }

    /*
     * Methode permettant de verifier qune la chaine entrée est un nombre
     */
    public static boolean isNumber(String chaine) {
        try {
            Integer.parseInt(chaine);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
