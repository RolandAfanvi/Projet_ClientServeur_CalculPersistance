package projet.serveur;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import projet.modele.*;

public class ServeurClient extends Thread {
    private int port;
    private ServerSocket socket;

    public ServeurClient(int port) {
        this.port = port;
        try {
            this.socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        try {
            System.out.print("\n=======================================================");
            System.out.println("\nSOCKET ECOUTE CREE => " + socket);
            System.out.println("\nServeur client lancé sur le port " + this.port);
            System.out.print("\n=======================================================");
            while (Serveur.numClient < Serveur.maxClients) {
                /*
                 * 2 - Attente d'une connexion client (la méthode s.accept() est bloquante
                 * tant qu'un client ne se connecte pas)
                 */
                Socket soc = socket.accept();

                /*
                 * 3 - Pour gérer plusieurs clients simultanément, le serveur attend que les
                 * clients se connectent,
                 * et dédie un thread à chacun d'entre eux afin de le gérer indépendamment des
                 * autres clients
                 */
                ConnexionClient cc = new ConnexionClient(Serveur.numClient++, soc);
                System.out.println("\nNOUVELLE CONNEXION CLIENT - SOCKET => " + soc);
                System.out.print("\n=======================================================");
                // Serveur.numWorker++;
                cc.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
