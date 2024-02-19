package projet.serveur;

import java.io.*;
import java.math.*;
import java.util.*;

import projet.modele.Message;

public class GererStat extends Thread {
    private int reference;
    private BigInteger nombre;
    private ObjectOutputStream writer;
    private Message message;

    public GererStat(int i, BigInteger bi, ObjectOutputStream writer, Message m) {
        this.reference = i;
        this.nombre = bi;
        this.writer = writer;
        message = m;
    }

    public void run() {
        switch (reference) {
            case 1: {
                int val = RetrouvePersistance(nombre);
                if (val != -1) {
                    message.texte = "La persistance de " + nombre + " est: " + val;

                } else {
                    message.texte = "La persistance demandé n'est pas encore calculé, veuillez reessayer plus tard MERCI!!";

                }

                try {
                    writer.writeObject(message);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
            case 2: {
                double moy = calculMoyenne();
                if (moy != -1) {
                    message.texte = "La moyenne des persistances est: " + moy;
                    // moy = MoyenneHashtable(Serveur.stockageCourant);
                } else {
                    message.texte = "Veuillez reessayer plus tard, je ne dispose des ressources necessaire pour l'instant";
                }

                try {
                    writer.writeObject(message);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
            case 3: {
                double mediane = calculMediane();
                if (mediane != -1) {
                    // mediane = MedianeHashtable(Serveur.stockageCourant);
                    message.texte = "La mediane des persistances est: " + mediane;
                } else {
                    message.texte = "Veuillez reessayer plus tard, je ne dispose des ressources necessaire pour l'instant";
                }

                try {
                    writer.writeObject(message);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
            case 4: {
                if (isNumber(message.parametre)) {
                    int valeur = Integer.parseInt(message.parametre);
                    int occurence = calculOccurrence(valeur);
                    if (occurence == -1) {
                        message.texte = "Le nombre d'occurence de " + valeur + " est: " + occurence;
                        // occurence = OccurrenceHashtable(Serveur.stockageCourant, valeur);
                    } else {
                        message.texte = "Veuillez reessayer plus tard, je ne dispose des ressources necessaire pour l'instant";
                    }

                    try {
                        writer.writeObject(message);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            }
            case 5: {
                List<BigInteger> liste = listePersistanceMax();
                String res = "|| ";
                for (int i = 0; i < liste.size(); i++) {
                    res += " || " + liste.get(i);
                }
                message.texte = res;
                try {
                    writer.writeObject(message);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
            case 6: {
                double moyenne = 0;
                double mediane = 0;
                int maximum = 0;
                List<Double> valeurs = new ArrayList<Double>();
                if (!Serveur.references.isEmpty()) {
                    double somme = 0;
                    for (BigInteger key : Serveur.references.keySet()) {
                        Hashtable<BigInteger, Integer> h = Serveur.getHashtableFromFile(Serveur.references.get(key));
                        somme += MoyenneHashtable(h);
                        double med = MedianeHashtable(h);
                        valeurs.add(med);
                        int ma = MaxHashtable(h);
                        if (maximum < ma) {
                            maximum = ma;
                        }
                    }
                    moyenne = (double) somme / Serveur.MemoireStockage.size();
                    // Serveur.MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
                    // Integer>>();
                    // Tri de la liste
                    Collections.sort(valeurs);

                    int taille = valeurs.size();
                    if (taille % 2 == 0) {
                        mediane = (double) (valeurs.get(taille / 2) + valeurs.get((taille / 2) - 1)) / 2;
                    } else {
                        mediane = (double) valeurs.get(taille / 2);
                    }

                } else if (!Serveur.stockageCourant.isEmpty()) {
                    moyenne = MoyenneHashtable(Serveur.stockageCourant);
                    mediane = MedianeHashtable(Serveur.stockageCourant);
                    maximum = MaxHashtable(Serveur.stockageCourant);

                }

                String stat = "";
                stat += "La moyenne des persistances est :" + moyenne + "\n";
                stat += "La mediane des persistances est :" + mediane + "\n";
                stat += "Le Max des persistances est :" + maximum + "\n";
                message.texte = stat;
                try {
                    writer.writeObject(message);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /*
     * 
     * frggt
     */

    public synchronized static int RetrouvePersistance(BigInteger nombre) {
        if (Serveur.stockageCourant.containsKey(nombre)) {
            System.out.println("la clé se trouve dans le stockage courant");
            return (Integer) Serveur.stockageCourant.get(nombre);
        } else if (Serveur.stockageLibre.containsKey(nombre)) {
            return (Integer) Serveur.stockageLibre.get(nombre);
        } else {
            // try {
            // MemoireStockage = retourneHashtableFichier();
            // } catch (ClassNotFoundException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            BigInteger clePrecedente = BigInteger.ONE;
            Set<BigInteger> setOfkeys = Serveur.references.keySet();
            System.out.println("Recherche dans les fichiers");
            for (BigInteger key : setOfkeys) {
                if (nombre.compareTo(key) < 0) {
                    for (BigInteger cle : setOfkeys) {
                        if (cle.compareTo(key) == 0) {
                            // cle précédente est la clé recherchée (sauf si c'était la première)
                            break;
                        }
                        clePrecedente = cle;
                    }
                    synchronized (Serveur.stockageLibre) {
                        Serveur.stockageLibre
                                .putAll(Serveur.getHashtableFromFile(Serveur.references.get(clePrecedente)));
                    }

                    // stockageLibre.putAll((Hashtable<BigInteger, Integer>)
                    // MemoireStockage.get(clePrecedente));
                    if (Serveur.stockageLibre.containsKey(nombre)) {
                        System.out.println("la clé se trouve dans la hashtable");
                        return (Integer) Serveur.stockageLibre.get(nombre);
                    }
                    break;
                }

            }
            // System.out.println("Elle etait VIDE");
            // stockageLibre=(Hashtable<BigInteger, Integer>)
            // MemoireStockage.get(clePrecedente).clone();
            // System.out.println("Elle n est pas vide");
            // //return stockageLibre.get(nombre);
            // try {
            // storeHashtableFichier(MemoireStockage);
            // } catch (IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
            // Integer>>();
            System.out.println("la clé ne se trouve pas dans le stockage courant");

            return -1;
        }

        // return stockageCourant.size();

    }

    /*
     * Stocke la hashtable dans le fichier
     * 
     */

    // public synchronized static void storeHashtableFichier(
    // Hashtable<BigInteger, Hashtable<BigInteger, Integer>> hashtable) throws
    // IOException {
    // Serveur.oos = new ObjectOutputStream(new FileOutputStream(Serveur.fichier));

    // Serveur.oos.writeObject(hashtable);
    // System.out.println("Ajout dans le fichier ");
    // Serveur.oos.flush();
    // Serveur.oos.close();
    // }

    // /*
    // * retrouner la hashtable dans le fichier
    // */
    // public synchronized static Hashtable<BigInteger, Hashtable<BigInteger,
    // Integer>> retourneHashtableFichier()
    // throws IOException, ClassNotFoundException {
    // Serveur.ois = new ObjectInputStream(new FileInputStream(Serveur.fichier));
    // Hashtable<BigInteger, Hashtable<BigInteger, Integer>> trouveHashtable =
    // (Hashtable<BigInteger, Hashtable<BigInteger, Integer>>) Serveur.ois
    // .readObject();
    // System.out.println("Recuperation du fichier ");
    // Serveur.ois.close();
    // return trouveHashtable;
    // }

    /*
     * 
     */

    public synchronized static double calculMoyenne() {
        double moyenne;
        // try {
        // Serveur.MemoireStockage = retourneHashtableFichier();
        // } catch (ClassNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (!Serveur.references.isEmpty()) {

            double somme = 0;
            for (BigInteger key : Serveur.references.keySet()) {
                Hashtable<BigInteger, Integer> h = Serveur.getHashtableFromFile(Serveur.references.get(key));
                somme += MoyenneHashtable(h);
            }
            moyenne = (double) somme / Serveur.MemoireStockage.size();
            System.out.println("Retrouvé dans les fichiers");
            // Serveur.MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
            // Integer>>();
            return moyenne;
        } else if (!Serveur.stockageCourant.isEmpty()) {

            moyenne = MoyenneHashtable(Serveur.stockageCourant);
            System.out.println("retrouvé dans le  stockage courant");
            return moyenne;
        }
        return -1;

    }

    public static double MoyenneHashtable(Hashtable<BigInteger, Integer> hash) {
        int somme = 0;
        for (Integer valeur : hash.values()) {
            somme += valeur;
        }
        double moyenne = (double) somme / hash.size();
        return moyenne;
    }

    /*
     * Methode de calcul de la mediane
     */

    public synchronized static double calculMediane() {
        double mediane = -1;
        // try {
        // Serveur.MemoireStockage = retourneHashtableFichier();
        // } catch (ClassNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (!Serveur.references.isEmpty()) {

            List<Double> valeurs = new ArrayList<Double>();

            for (BigInteger key : Serveur.references.keySet()) {
                Hashtable<BigInteger, Integer> h = Serveur.getHashtableFromFile(Serveur.references.get(key));
                double med = MedianeHashtable(h);
                valeurs.add(med);
            }
            // Tri de la liste
            Collections.sort(valeurs);

            int taille = valeurs.size();
            if (taille % 2 == 0) {
                mediane = (double) (valeurs.get(taille / 2) + valeurs.get((taille / 2) - 1)) / 2;
            } else {
                mediane = (double) valeurs.get(taille / 2);
            }
            System.out.println("Retrouvé dans les fichiers");
            // Serveur.MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
            // Integer>>();
            return mediane;
        } else if (!Serveur.stockageCourant.isEmpty()) {
            mediane = MedianeHashtable(Serveur.stockageCourant);
            System.out.println("Retrouvé dans le stockage courant");
            return mediane;
        }
        return mediane;

    }

    public static double MedianeHashtable(Hashtable<BigInteger, Integer> hash) {
        List<Integer> valeurs = new ArrayList<>(hash.values());

        if (valeurs.size() > 0) {
            // Tri de la liste
            Collections.sort(valeurs);

            // Calcul de la médiane
            double mediane;
            int taille = valeurs.size();
            if (taille % 2 == 0) {
                mediane = (double) (valeurs.get(taille / 2) + valeurs.get((taille / 2) - 1)) / 2;
            } else {
                mediane = (double) valeurs.get(taille / 2);
            }
            return mediane;
        }
        return -1;
    }
    /*
     * Methode pour calculer le nombre d'occcurence
     */

    public synchronized static int calculOccurrence(int valeurRecherchee) {
        int somme = 0;
        // try {
        // Serveur.MemoireStockage = retourneHashtableFichier();
        // } catch (ClassNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (!Serveur.references.isEmpty()) {

            for (BigInteger key : Serveur.references.keySet()) {
                Hashtable<BigInteger, Integer> h = Serveur.getHashtableFromFile(Serveur.references.get(key));
                somme += OccurrenceHashtable(h, valeurRecherchee);
            }
            System.out.println("Retrouvé dans les fichiers");
            // Serveur.MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
            // Integer>>();
            return somme;
        } else if (!Serveur.stockageCourant.isEmpty()) {
            somme = OccurrenceHashtable(Serveur.stockageCourant, valeurRecherchee);
            System.out.println("Retrouvé dans le stockage courant");
            return somme;
        }
        return somme;

    }

    public static int OccurrenceHashtable(Hashtable<BigInteger, Integer> hash, int valeurRecherchee) {

        int nbOccurrences = 0;

        for (int valeur : hash.values()) {
            if (valeur == valeurRecherchee) {
                nbOccurrences++;
            }

        }
        return nbOccurrences;

    }

    /*
     * Calcul de maximum d une hashtable
     */
    public static List<BigInteger> listePersistanceMax() {
        int max = 0;
        List<BigInteger> liste = new ArrayList<BigInteger>();
        // try {
        // Serveur.MemoireStockage = retourneHashtableFichier();
        // } catch (ClassNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (!Serveur.references.isEmpty()) {

            for (BigInteger key : Serveur.references.keySet()) {
                Hashtable<BigInteger, Integer> h = Serveur.getHashtableFromFile(Serveur.references.get(key));
                int ma = MaxHashtable(h);
                if (max < ma) {
                    max = ma;
                }
            }
            for (BigInteger key : Serveur.references.keySet()) {
                Hashtable<BigInteger, Integer> h = Serveur.getHashtableFromFile(Serveur.references.get(key));
                liste.addAll(getKeysByValue(h, max));
            }
            System.out.println("Retrouvé dans les fichiers");
            // Serveur.MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
            // Integer>>();
            return liste;

        } else if (!Serveur.stockageCourant.isEmpty()) {
            max = MaxHashtable(Serveur.stockageCourant);
            liste = getKeysByValue(Serveur.stockageCourant, max);
            System.out.println("Retrouvé dans le stockage courant");
            return liste;
        }
        return liste;
    }

    /*
     * FONCTION POUR TROUVER LES CLE DANS UNE HASHTABLE QUI ONT TOUS LA VALEUR QUI
     * EST PASSE EN PARAMETRE
     */

    public static List<BigInteger> getKeysByValue(Hashtable<BigInteger, Integer> hashtable, int value) {
        List<BigInteger> keys = new ArrayList<BigInteger>();
        for (BigInteger key : hashtable.keySet()) {
            if (hashtable.get(key) == (value)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /*
     * FONCTION QUI DETERMINE LE MAXIMUM D UNE HASHTABLE
     */

    public static int MaxHashtable(Hashtable<BigInteger, Integer> hash) {
        int max = 0;
        for (int valeur : hash.values()) {
            if (valeur > max) {
                max = valeur;
            }

        }
        return max;
    }

    /*
     * CALCUL DU MAXIMUN DES PERSISTANCES
     */

    public static int calculMaxPersistance() {
        int max = 0;
        // List<BigInteger> liste = new ArrayList<BigInteger>();
        // try {
        // Serveur.MemoireStockage = retourneHashtableFichier();
        // } catch (ClassNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (!Serveur.references.isEmpty()) {

            for (BigInteger key : Serveur.references.keySet()) {
                Hashtable<BigInteger, Integer> h = Serveur.getHashtableFromFile(Serveur.references.get(key));
                int ma = MaxHashtable(h);
                if (max < ma) {
                    max = ma;
                }
            }
            System.out.println("Retrouvé dans les fichiers");
            // Serveur.MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
            // Integer>>();

        } else if (!Serveur.stockageCourant.isEmpty()) {
            max = MaxHashtable(Serveur.stockageCourant);
            System.out.println("Retrouvé dans le stockage courant");

        }
        return max;
    }

    /*
     * pour verifier qu'un chaine est un entier
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
