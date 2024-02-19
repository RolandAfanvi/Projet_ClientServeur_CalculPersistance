package projet.client;

import java.io.*;
import java.net.Socket;

import projet.modele.Commande;
import projet.modele.Message;

public class Client {
	static int port = 8080;
	static boolean arreter = false;
	static String pseudo = "pseudo";

	// Le client attend comme argument l'adresse du serveur et le pseudo (ex. : java
	// Client 127.0.0.1 pseudo pour l'exécuter)
	/**
	 * @param args
	 * @throws Exception
	 */

	public static void main(String[] args) throws Exception {
		pseudo = "non";
		// 4 - le client ouvre une connexion avec le serveur
		// "172.16.54.204"
		Socket socket = new Socket("172.16.54.204", port);
		System.out.println("SOCKET = " + socket);

		/*
		 * 5b - A partir du Socket connectant le serveur au client, le client ouvre 2
		 * flux :
		 * 1) un flux entrant (BufferedReader) afin de recevoir ce que le serveur envoie
		 * 2) un flux sortant (ObjectOutputStream) afin d'envoyer des messages au
		 * serveur
		 */

		// BufferedReader sisr = new BufferedReader(
		// new InputStreamReader(socket.getInputStream()));

		ObjectOutputStream sisw = new ObjectOutputStream(socket.getOutputStream());

		ObjectInputStream sisr = new ObjectInputStream(socket.getInputStream());

		// String s = "";
		// s += "\nVous etes connectés au serveur !!\n";
		// s += "Veuiller suivre les instructions suivantes pour pouvoir communiquer
		// avec,le serveur\n";
		// s += "Entrer :\n";
		// s += "PERSISTANCE X -----> Pour consulter la persistance d'un nombre(X)\n";
		// s += "LIST -----> Pour consulter la liste des nombres avec la plus grande
		// persistance\n";
		// s += "MOYENNE -----> Pour consulter la moyenne de la persistance \n";
		// s += "MEDIANE -----> Pour consulter la mediane de la persistance \n";
		// s += "NombreDoccurence -----> Pour consulter le nombre d'occurrence par
		// valeur de persistance\n";
		// s += " \n";
		// ((PrintWriter) sisw).print(s);

		// Gestion des messages écrits via le terminal
		GererSaisie saisie = new GererSaisie(sisw);
		saisie.start();

		while (!arreter) {
			/*
			 * 7 - Le client attend les messages du serveur. La méthode sisr.ready() permet
			 * de vérifier
			 * si un message est dans le flux, ce qui permet de rendre l'action non
			 * bloquante
			 */
			try {
				Object recu = sisr.readUnshared();
				if (recu != null) {
					if (recu.getClass().getName().equals("projet.modele.Message")) {
						Message reponse = (Message) recu;
						System.out.println(reponse.texte);
					} else {
						System.out.println("C'est autre chose");
					}
					System.out.print("\n=======================================================\n");
				}
			} catch (Exception e) {
				try {
					Thread.currentThread();
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}

		System.out.println("VOUS ETES DECONNECTÉ"); // message de fermeture
		// 9 - Le client envoie un message pour mettre fin à la connexion, qui fera
		// sortir le serveur de son while
		Message message = new Message(Client.pseudo, Commande.Message, "", "END");
		sisw.writeObject(message);

		// 10b - Le client ferme ses flux
		sisr.close();
		sisw.close();
		socket.close();
	}

}
