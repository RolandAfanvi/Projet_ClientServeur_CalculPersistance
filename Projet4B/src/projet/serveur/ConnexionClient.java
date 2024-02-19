package projet.serveur;

import java.io.BufferedWriter;
import java.io.*;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.List;

import projet.modele.Commande;
import projet.modele.Message;

public class ConnexionClient extends Thread {
	private int id;
	private boolean arret = false;
	private Socket s;
	private ObjectInputStream sisr;
	private ObjectOutputStream sisw;
	// private PrintWriter sisw;
	// private Canal canal;

	public ConnexionClient(int id, Socket s) {
		this.id = id;
		this.s = s;
		// canal = Serveur.canaux.get("Général");
		// canal.subscribe(id);

		try {
			sisr = new ObjectInputStream(s.getInputStream());
			sisw = new ObjectOutputStream(s.getOutputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
		// Serveur.pw[id] = sisw;

	}

	public void run() {
		try {
			String str = "";
			str += "\nVous etes connectés au serveur !!\n";
			str += "Veuiller suivre les instructions suivantes pour pouvoir communiquer avec,le serveur\n";
			str += "Entrer :\n";
			str += "PERSISTANCE X -----> Pour consulter la persistance d'un nombre(X)\n";
			str += "STATISTIQUE  -----> Pour consulter les stats c-a-d \n";
			str += "LIST -----> Pour consulter la liste des nombres avec la plus grande persistance\n";
			str += "MOYENNE -----> Pour consulter la moyenne de la persistance \n";
			str += "MEDIANE -----> Pour consulter la mediane de la persistance \n";
			str += "NB OCCURRENCE -----> Pour consulter le nombre d'occurrence par valeur de persistance\n";
			str += " \n";

			Message m = new Message("", Commande.Message, "", str);
			sisw.writeObject(m);

			while (true) {
				Message message = (Message) sisr.readObject();
				if (message.texte.equals("END"))
					break;
				System.out.print("\n=======================================================\n");
				System.out.println("recu de client " + id + "," + message.pseudo + "=> " + message.texte);
				Message msg = new Message(Integer.toString(id), Commande.Message, " ", " ");
				int ref = -1;
				BigInteger bigI = BigInteger.ZERO;
				if (message.commande == Commande.Persistance) {
					// Canal nouveauCanal = new Canal(message.parametre);
					// Serveur.canaux.put(message.parametre, nouveauCanal);
					if (!message.parametre.equals("")) {

						BigInteger persistance = new BigInteger(message.parametre);
						// sisw.println("je te retourne la persistance de " + persistance);
						ref = 1;
						bigI = persistance;
						// int val = Serveur.RetrouvePersistance(persistance);

						// if (val != -1) {
						// msg.texte = "La persistance de " + persistance + " est: " + val;
						// sisw.writeObject(msg);
						// } else {
						// msg.texte = "La persistance demandé n'est pas encore calculé, veuillez
						// reessayer plus tard MERCI!!";
						// sisw.writeObject(msg);
						// }

					}

				} else if (message.commande == Commande.Moyenne) {
					ref = 2;
					// double moy = Serveur.calculMoyenne();
					// if (moy == -1) {
					// moy = Serveur.MoyenneHashtable(Serveur.stockageCourant);
					// }
					// msg.texte = "La moyenne des persistances est: " + moy;
					// sisw.writeObject(msg);

				} else if (message.commande == Commande.Mediane) {
					ref = 3;
					// double mediane = Serveur.calculMediane();
					// if (mediane == -1) {
					// mediane = Serveur.MedianeHashtable(Serveur.stockageCourant);
					// }
					// msg.texte = "La mediane des persistances est: " + mediane;
					// sisw.writeObject(msg);

				} else if (message.commande == Commande.NombreDoccurence) {
					if (!message.parametre.equals(""))
						ref = 4;
					// int valeur = Integer.parseInt(message.parametre);
					// int occurence = Serveur.calculOccurrence(valeur);
					// if (occurence == -1) {
					// occurence = Serveur.OccurrenceHashtable(Serveur.stockageCourant, valeur);
					// }
					// msg.texte = "Le nombre d'occurence de " + valeur + " est: " + occurence;
					// sisw.writeObject(msg);

				} else if (message.commande == Commande.List) {
					ref = 5;
					// List<BigInteger> liste = Serveur.listePersistanceMax();
					// String res = "|| ";
					// for (int i = 0; i < liste.size(); i++) {
					// res += " || " + liste.get(i);
					// }
					// msg.texte = res;
					// sisw.writeObject(msg);

					// System.out.print("\n=======================================================\n");
				}

				else if (message.commande == Commande.Statistique) {
					ref = 6;
					// double moy = Serveur.calculMoyenne();
					// double mediane = Serveur.calculMediane();
					// int maximum = Serveur.calculMaxPersistance();

					// String stat = "";
					// stat += "La moyenne des persistances est :" + moy + "\n";
					// stat += "La mediane des persistances est :" + mediane + "\n";
					// stat += "Le Max des persistances est :" + maximum + "\n";
					// msg.texte = stat;
					// sisw.writeObject(msg);

				}
				GererStat gs = new GererStat(ref, bigI, sisw, msg);
				gs.start();
			}
			System.out.print("Le client " + id + ", vient de se deconnecter");
			System.out.print("\n=======================================================\n");
			// Le serveur ferme ses flux
			sisr.close();
			sisw.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean estEntier(Object variable) {
		try {
			Integer.parseInt(variable.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
