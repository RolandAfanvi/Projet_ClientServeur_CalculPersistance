package projet.modele;

import java.io.Serializable;

public class Message implements Serializable {
	public String pseudo;
	public Commande commande;
	public String parametre;
	public String texte;

	public Message(String pseudo, Commande commande, String parametre, String texte) {
		this.pseudo = pseudo;
		this.commande = commande;
		this.parametre = parametre;
		this.texte = texte;
	}

	public String toString() {
		return "";
	}
}
