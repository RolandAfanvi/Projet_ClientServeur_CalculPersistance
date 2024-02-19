package projet.modele;

import java.io.Serializable;
import java.math.*;

public class Tache implements Serializable {
    private BigInteger debut;
    private BigInteger nbCourant;
    private BigInteger fin;
    private int taille;

    public Tache(BigInteger deb, BigInteger fin) {
        this.debut = deb;
        this.nbCourant = deb;
        this.fin = fin;
        this.taille = fin.subtract(debut).intValue();

    }

    public Tache() {
        this.debut = BigInteger.ZERO;
        this.nbCourant = BigInteger.ZERO;
        this.fin = BigInteger.ZERO;
        this.taille = 0;

    }

    public synchronized BigInteger getNbCourant() {
        if (nbCourant.compareTo(fin) < 0) {
            BigInteger temp = nbCourant;
            nbCourant = nbCourant.add(BigInteger.ONE);
            return temp;
        }
        return BigInteger.valueOf(-1);
    }

    public int getTaille() {
        return taille;
    }

    public BigInteger getDebut() {
        return this.debut;
    }

    public BigInteger getFin() {
        return this.fin;
    }

}
