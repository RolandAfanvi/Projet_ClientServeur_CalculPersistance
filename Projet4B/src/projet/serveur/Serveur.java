package projet.serveur;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import projet.modele.Tache;

public class Serveur {
	static int port1 = 8080;
	static int port2 = 8081;
	static final int maxClients = 50;
	static final int maxWorker = 30;

	/*
	 * Creation du fichier pour stocker les données
	 */
	// RandomAccessFile monFichier = new RandomAccessFile("Fichier.dat", "rw");
	static String fichier = "Projet4B/Projet4B/src/projet/serveur/Fichier.dat";
	static File monFichier = new File(fichier);
	static ObjectOutputStream oos;
	static ObjectInputStream ois;
	// fichier.createNewFile();
	// FileOutputStream fos = new FileOutputStream(fichier);
	// FileInputStream fis = new FileInputStream(fichier);

	static PrintWriter pw[];
	static int workerCoeurDispo[]; // Liste des worker dispo avec leur nombre de coeur de leur processeur
	static ArrayBlockingQueue<Tache> listeTache; // Liste des taches dont dispose le serveur
	static Hashtable<BigInteger, Integer> stockageCourant; // stocker les resultat que va envoyer le worker
	static Hashtable<BigInteger, Integer> stockageIntermed;
	static Hashtable<BigInteger, Integer> stockageLibre; // Pour pouvoir faire des echanges lorsque le prmierest plein
	static Hashtable<BigInteger, Hashtable<BigInteger, Integer>> MemoireStockage;
	static final int MaxtailleHashtable = 4000000;
	static ConcurrentHashMap<BigInteger, String> references;

	static int numClient = 0;
	static int numWorker = 0;
	static int numResultat = 0;

	// Pour utiliser un autre port pour le serveur, l'exécuter avec la commande :
	// java ServeurMC 8081
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length > 1) {

			port1 = Integer.parseInt(args[0]);
			port2 = Integer.parseInt(args[1]);
		}
		/*
		 * Initialisation des variable
		 */
		pw = new PrintWriter[maxClients]; // Pour stocker les clients
		workerCoeurDispo = new int[maxWorker]; // Pour stocker l'id des workers avec
		listeTache = new ArrayBlockingQueue<Tache>(8000000);
		stockageCourant = new Hashtable<BigInteger, Integer>();
		stockageIntermed = new Hashtable<BigInteger, Integer>();
		stockageLibre = new Hashtable<BigInteger, Integer>();
		MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger, Integer>>();
		references = new ConcurrentHashMap<BigInteger, String>();
		/*
		 * Creation des threaads et leur lancement sachant qu on a un thread qui
		 * s'occupe de la
		 * production des nombres et les deux autres sont les threads serveur un pour
		 * clients et
		 * l'autre pour le workers
		 */
		synchronized (listeTache) {
			ThreadProductionTaches ProduitTache = new ThreadProductionTaches(listeTache);
			ProduitTache.start();
		}
		ThreadProductionTaches ProduitTache = new ThreadProductionTaches(listeTache);
		ProduitTache.start();

		ServeurClient clientserveur = new ServeurClient(port1);
		clientserveur.start();
		ServeurWorker workerserveur = new ServeurWorker(port2);
		workerserveur.start();

		/*
		 * Ajout de la memoire de stockage de fichier dans un premier temps et pour
		 * enregistrer
		 * une donnée on le recupere sur le fichier et on l ajoute la hashtable puis on
		 * le
		 * remet dans le fichier
		 */
		// storeHashtableFichier(MemoireStockage);

		/*
		 * PARTIE POUR TESTER SI MON PROGRAMME MARCHE BIEN
		 */

		while (true) {
			System.out.println(listeTache.size());
			System.out.println(listeTache.take().getTaille());
			synchronized (references) {
				System.out.println(references.size());
			}

			System.out.println("========================");
			try {
				Thread.currentThread();
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
			}
		}

	}

	/*
	 * Methode pour ajouter lees hashtables dans un fichier
	 */
	// public synchronized static void storeHashtableFichier(
	// Hashtable<BigInteger, Hashtable<BigInteger, Integer>> hashtable) throws
	// IOException {
	// oos = new ObjectOutputStream(new FileOutputStream(fichier));

	// oos.writeObject(hashtable);
	// System.out.println("Ajout dans le fichier ");
	// oos.flush();
	// oos.close();
	// }

	// public synchronized static Hashtable<BigInteger, Hashtable<BigInteger,
	// Integer>> retourneHashtableFichier()
	// throws IOException, ClassNotFoundException {
	// ois = new ObjectInputStream(new FileInputStream(fichier));
	// Hashtable<BigInteger, Hashtable<BigInteger, Integer>> trouveHashtable =
	// (Hashtable<BigInteger, Hashtable<BigInteger, Integer>>) ois
	// .readObject();
	// System.out.println("Recuperation du fichier ");
	// ois.close();
	// return trouveHashtable;
	// }

	/*
	 * Methode qui ajoute les elements dans le fichier
	 */
	public synchronized static void ajoutHashtableFichier(Hashtable<BigInteger, Integer> ht1, BigInteger bd)
			throws ClassNotFoundException {

		try {
			oos = new ObjectOutputStream(new FileOutputStream(fichier));
			ois = new ObjectInputStream(new FileInputStream(fichier));
			Hashtable<BigInteger, Hashtable<BigInteger, Integer>> listeRecup = (Hashtable<BigInteger, Hashtable<BigInteger, Integer>>) ois
					.readObject();
			listeRecup.put(bd, ht1);
			oos.writeObject(listeRecup);
			oos.flush();
			oos.close();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Methode pour simplifier l'ajout des resultats dans la hashtable des resultats
	 */
	public synchronized static void addResultat(ConcurrentHashMap<BigInteger, Integer> concurrentHashMap)
			throws IOException {

		// stockageCourant.putAll(res);
		// Set<BigInteger> setOfkeys = res.keySet();
		// for (BigInteger key : setOfkeys) {

		// if (MaxtailleHashtable > stockageCourant.size()) {
		// stockageCourant.put(key, res.get(key));

		// } else {
		// saveHashtableToFile(stockageCourant);
		saveHashtableToFile(concurrentHashMap);
		// stockageCourant = new Hashtable<BigInteger, Integer>();
		// // recuperation de la hashtable du fichier
		// MemoireStockage = retourneHashtableFichier();
		// MemoireStockage.put(stockageCourant.keySet().iterator().next(),
		// stockageCourant);
		// storeHashtableFichier(MemoireStockage);
		// MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
		// Integer>>();
		// stockageCourant = new Hashtable<BigInteger, Integer>();
		// remettre l hashtable dans le fichier

		// }
		// }

		// System.out.println("Stockage de resultat sur une hashtable");
		// if ((MaxtailleHashtable - stockageCourant.size()) > res.size()) {
		// Set<BigInteger> setOfkeys = res.keySet();
		// for (BigInteger key : setOfkeys) {
		// stockageCourant.put(key, res.get(key));
		// }
		// } else {

		// Set<BigInteger> setOfkeys = res.keySet();

		// for (BigInteger key : setOfkeys) {
		// if ( (MaxtailleHashtable - stockageCourant.size() )<=0) {
		// // on ajoute le stockage courant plein ala memoire et on le remplace par un
		// // autre

		// MemoireStockage.put(stockageCourant.keySet().iterator().next(),
		// stockageCourant);
		// stockageCourant = stockageIntermed;
		// stockageIntermed = new Hashtable<BigInteger,Integer>();
		// }
		// stockageCourant.put(key, res.get(key));
		// }

		// }

	}

	/**
	 * @param nombre
	 * @return
	 *         Methode pour retrouver la persistance d un nombre qu on le donne en
	 *         parametre
	 */
	public synchronized static int RetrouvePersistance(BigInteger nombre) {
		if (stockageCourant.containsKey(nombre)) {
			System.out.println("la clé se trouve dans le stockage courant");
			return (Integer) stockageCourant.get(nombre);
		} else if (stockageLibre.containsKey(nombre)) {
			return (Integer) stockageLibre.get(nombre);
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
			Set<BigInteger> setOfkeys = references.keySet();

			for (BigInteger key : setOfkeys) {
				if (nombre.compareTo(key) <= 0) {
					for (BigInteger cle : setOfkeys) {
						if (cle.compareTo(key) == 0) {
							// cle précédente est la clé recherchée (sauf si c'était la première)
							break;
						}
						clePrecedente = cle;
					}
					stockageLibre.putAll(getHashtableFromFile(references.get(clePrecedente)));
					// stockageLibre.putAll((Hashtable<BigInteger, Integer>)
					// MemoireStockage.get(clePrecedente));
					if (stockageLibre.containsKey(nombre)) {
						System.out.println("la clé se trouve dans la hashtable");
						return (Integer) stockageLibre.get(nombre);
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
			MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger, Integer>>();
			System.out.println("la clé ne se trouve pas dans le stockage courant");

			return -1;
		}

		// return stockageCourant.size();

	}

	/*
	 * Methode pour verifier si la liste des taches est pleine pour faire attendre
	 * le threadProductionTaches
	 */
	public static boolean listTachePlein() {
		return (MaxtailleHashtable - listeTache.size()) == 0;
	}

	/*
	 * Methode de calcul de la moyenne
	 */

	// public static double calculMoyenne() {
	// double moyenne;
	// try {
	// MemoireStockage = retourneHashtableFichier();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (!MemoireStockage.isEmpty()) {
	// double somme = 0;
	// for (BigInteger key : MemoireStockage.keySet()) {
	// somme += MoyenneHashtable(MemoireStockage.get(key));
	// }
	// moyenne = (double) somme / MemoireStockage.size();
	// MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
	// Integer>>();
	// return moyenne;
	// } else {
	// moyenne = MoyenneHashtable(stockageCourant);
	// return moyenne;
	// }

	// }

	// public static double MoyenneHashtable(Hashtable<BigInteger, Integer> hash) {
	// int somme = 0;
	// for (Integer valeur : hash.values()) {
	// somme += valeur;
	// }
	// double moyenne = (double) somme / hash.size();
	// return moyenne;
	// }

	/*
	 * Methode de calcul de la mediane
	 */

	// public static double calculMediane() {
	// double mediane;
	// try {
	// MemoireStockage = retourneHashtableFichier();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (!MemoireStockage.isEmpty()) {

	// List<Double> valeurs = new ArrayList<Double>();

	// for (BigInteger key : MemoireStockage.keySet()) {
	// double med = MedianeHashtable(MemoireStockage.get(key));
	// valeurs.add(med);
	// }
	// // Tri de la liste
	// Collections.sort(valeurs);

	// int taille = valeurs.size();
	// if (taille % 2 == 0) {
	// mediane = (double) (valeurs.get(taille / 2) + valeurs.get((taille / 2) - 1))
	// / 2;
	// } else {
	// mediane = (double) valeurs.get(taille / 2);
	// }
	// MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
	// Integer>>();
	// return mediane;
	// } else {
	// mediane = MedianeHashtable(stockageCourant);
	// return mediane;
	// }

	// }

	// public static double MedianeHashtable(Hashtable<BigInteger, Integer> hash) {
	// List<Integer> valeurs = new ArrayList<>(hash.values());

	// // Tri de la liste
	// Collections.sort(valeurs);

	// // Calcul de la médiane
	// double mediane;
	// int taille = valeurs.size();
	// if (taille % 2 == 0) {
	// mediane = (double) (valeurs.get(taille / 2) + valeurs.get((taille / 2) - 1))
	// / 2;
	// } else {
	// mediane = (double) valeurs.get(taille / 2);
	// }
	// return mediane;
	// }
	/*
	 * Methode pour calculer le nombre d'occcurence
	 */

	// public static int calculOccurrence(int valeurRecherchee) {
	// int somme = 0;
	// try {
	// MemoireStockage = retourneHashtableFichier();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (!MemoireStockage.isEmpty()) {

	// for (BigInteger key : MemoireStockage.keySet()) {
	// somme += OccurrenceHashtable(MemoireStockage.get(key), valeurRecherchee);
	// }
	// MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
	// Integer>>();
	// return somme;
	// } else {
	// somme = OccurrenceHashtable(stockageCourant, valeurRecherchee);
	// return somme;
	// }

	// }

	// public static int OccurrenceHashtable(Hashtable<BigInteger, Integer> hash,
	// int valeurRecherchee) {

	// int nbOccurrences = 0;

	// for (int valeur : hash.values()) {
	// if (valeur == valeurRecherchee) {
	// nbOccurrences++;
	// }

	// }
	// return nbOccurrences;

	// }

	/*
	 * Calcul de maximum d une hashtable
	 */
	// public static List<BigInteger> listePersistanceMax() {
	// int max = 0;
	// List<BigInteger> liste = new ArrayList<BigInteger>();
	// try {
	// MemoireStockage = retourneHashtableFichier();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (!MemoireStockage.isEmpty()) {

	// for (BigInteger key : MemoireStockage.keySet()) {
	// if (max < MaxHashtable(MemoireStockage.get(key))) {
	// max = MaxHashtable(MemoireStockage.get(key));
	// }
	// }
	// for (BigInteger key : MemoireStockage.keySet()) {
	// liste.addAll(getKeysByValue(MemoireStockage.get(key), max));
	// }
	// MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
	// Integer>>();
	// return liste;

	// } else {
	// max = MaxHashtable(stockageCourant);
	// liste = getKeysByValue(stockageCourant, max);
	// return liste;
	// }
	// }

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

	// public static int MaxHashtable(Hashtable<BigInteger, Integer> hash) {
	// int max = 0;
	// for (int valeur : hash.values()) {
	// if (valeur > max) {
	// max = valeur;
	// }

	// }
	// return max;
	// }

	/*
	 * CALCUL DU MAXIMUN DES PERSISTANCES
	 */

	// public static int calculMaxPersistance() {
	// int max = 0;
	// List<BigInteger> liste = new ArrayList<BigInteger>();
	// try {
	// MemoireStockage = retourneHashtableFichier();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (!MemoireStockage.isEmpty()) {

	// for (BigInteger key : MemoireStockage.keySet()) {
	// if (max < MaxHashtable(MemoireStockage.get(key))) {
	// max = MaxHashtable(MemoireStockage.get(key));
	// }
	// }
	// MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger,
	// Integer>>();

	// } else {
	// max = MaxHashtable(stockageCourant);

	// }
	// return max;
	// }

	/*
	 * 
	 */

	public synchronized static void saveHashtableToFile(ConcurrentHashMap<BigInteger, Integer> concurrentHashMap) {
		// Générer un nom de fichier unique basé sur la clé BigInteger de la Hashtable1
		// String filename = hashtable1.keySet().iterator().next().toString() + ".dat";
		String filename = "Projet4B/Projet4B/src/projet/serveur/Stockage/Resultat" + numResultat + ".dat";
		numResultat++;
		try {
			System.out.print("enregistrement de la reference ");
			try {
				synchronized (references) {
					references.put(concurrentHashMap.keySet().iterator().next(), filename);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			// Créer le fichier de sortie
			File outputFile = new File(filename);
			FileOutputStream fileWriter = new FileOutputStream(outputFile);
			ObjectOutputStream writer = new ObjectOutputStream(fileWriter);
			writer.writeObject(concurrentHashMap);// Écrire la Hashtable1 dans le fichier
			// Enregistrer la reference creation du fichier

			writer.close();// Fermer le fichier

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized static Hashtable<BigInteger, Integer> getHashtableFromFile(String filename) {
		Hashtable<BigInteger, Integer> hashtable = new Hashtable<BigInteger, Integer>();

		try {
			// Ouvrir le fichier d'entrée
			File inputFile = new File(filename);
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream reader = new ObjectInputStream(fis);

			// }
			try {
				hashtable = (Hashtable<BigInteger, Integer>) reader.readObject();
				System.out.println("hashtable recupere du fichier " + hashtable);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Fermer le fichier
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return hashtable;
	}

}
