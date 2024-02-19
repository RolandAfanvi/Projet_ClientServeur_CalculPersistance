package projet.client;

import java.io.*;
import java.math.BigInteger;

import projet.modele.*;

public class GererSaisie extends Thread {
	private BufferedReader entreeClavier;
	private ObjectOutputStream pw;

	public GererSaisie(ObjectOutputStream pw) {
		entreeClavier = new BufferedReader(new InputStreamReader(System.in));
		this.pw = pw;
	}

	public void run() {
		String str;
		try {
			while (!(str = entreeClavier.readLine()).equals("END")) {
				Message message = new Message(Client.pseudo, Commande.Message, "", str);
				if (str.startsWith(Commande.Persistance.texte)) {
					traiterCommandePersistance(message, str);
				} else if (str.startsWith(Commande.Moyenne.texte)) {
					traiterCommandeMoyenne(message);
				} else if (str.startsWith(Commande.Mediane.texte)) {
					traiterCommandeMediane(message);
				} else if (str.startsWith(Commande.List.texte)) {
					traiterCommandeList(message);
				} else if (str.startsWith(Commande.NombreDoccurence.texte)) {
					traiterCommandeNombreOccurence(message, str);
				} else if (str.startsWith(Commande.Statistique.texte)) {
					traiterCommandeStatistique(message);
				} else {
					System.out.println("\nSuivre les instructions donneés ci-haut!!!!!!!!!\n");
				}

				// 8bis - Le client envoie un message au serveur grâce à son PrintWriter
				pw.writeObject(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Client.arreter = true;

	}

	// Methode specifique pour traiter chaque commande
	private void traiterCommandePersistance(Message message, String str) {
		if (str.split(" ").length > 1) {
			if (isBigInteger(str.split(" ")[1])) {
				message.commande = Commande.Persistance;
				message.parametre = str.split(" ")[1];
			} else {
				System.out.println("\nSuivre les instructions données ci-haut!!!!!!! \n");
			}
		} else {
			System.out.println("\nSuivre les instructions données ci-haut!!!!!!! \n");
		}
	}

	private void traiterCommandeMoyenne(Message message) {
		message.commande = Commande.Moyenne;
	}

	private void traiterCommandeMediane(Message message) {
		message.commande = Commande.Mediane;
	}

	private void traiterCommandeList(Message message) {
		message.commande = Commande.List;
	}

	private void traiterCommandeNombreOccurence(Message message, String str) {
		if (str.split(" ").length > 2) {
			int entier = Integer.parseInt(str.split(" ")[2]);
			if (entier >= 0 && entier <= 11) {
				message.commande = Commande.NombreDoccurence;
				message.parametre = str.split(" ")[2];
			}
		}
	}

	private void traiterCommandeStatistique(Message message) {
		message.commande = Commande.Statistique;
	}

	// Methode pour verifier si une chaine caractere est un nombre
	public static boolean estEntier(Object variable) {
		try {
			Integer.parseInt(variable.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isBigInteger(String s) {
		try {
			new BigInteger(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
