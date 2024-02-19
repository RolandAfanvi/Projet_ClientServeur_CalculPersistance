package projet.modele;

import java.io.*;

public enum Commande implements Serializable {
	// CreateChannel("CREATE CHANNEL"),
	// SetChannel("SET CHANNEL"),
	// SubscribeChannel("SUBSCRIBE CHANNEL"),
	// UnsubscribeChannel("UNSUBSCRIBE CHANNEL"),
	// List("LIST"),
	List("LIST "),
	Mediane("MEDIANE"),
	NombreDoccurence("NB OCCURRENCE"),
	Moyenne("MOYENNE"),
	Persistance("PERSISTANCE"),
	Statistique("STATISTIQUES"),
	Message("");

	public String texte = "";

	private Commande(String texte) {
		this.texte = texte;
	}
}
