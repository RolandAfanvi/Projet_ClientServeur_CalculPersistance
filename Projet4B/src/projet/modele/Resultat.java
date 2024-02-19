package projet.modele;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Resultat implements Serializable {
  private ConcurrentHashMap<BigInteger, Integer> lstPersistance;
  private int id;
  private String pseudo;
  private BigInteger nbCourant;
  private final int tailleMax = 1000000;

  /**
   * @param id
   */
  public Resultat(int id) {
    lstPersistance = new ConcurrentHashMap<>();
    this.id = id;

  }

  /**
   * @param result
   */
  public synchronized void ajoutResultat(ConcurrentHashMap<BigInteger, Integer> result) {
    this.lstPersistance.putAll(result);
    // Set<BigInteger> setOfkeys = persistance.keySet();
    // for (BigInteger key : setOfkeys) {
    // this.lstPersistance.put(key, persistance.get(key));
    // }
    System.out.println("je viens de rendre les resultats de ma tache");

  }

  public ConcurrentHashMap<BigInteger, Integer> getListPersistance() {
    return lstPersistance;
  }

  public boolean estPlein() {
    return lstPersistance.size() >= tailleMax;
  }

  public int taille() {
    return lstPersistance.size();
  }

}