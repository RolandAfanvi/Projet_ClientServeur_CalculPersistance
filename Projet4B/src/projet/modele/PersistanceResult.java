package projet.modele;

public class PersistanceResult {
    private int number;
    private int persistance;

    public PersistanceResult(int number, int persistence) {
        this.number = number;
        this.persistance = persistence;
    }

    public int getNumber() {
        return number;
    }

    public int getPersistance() {
        return persistance;
    }
}