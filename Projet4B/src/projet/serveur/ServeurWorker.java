package projet.serveur;

import java.io.*;
import java.net.*;

public class ServeurWorker extends Thread {

    private int port;
    private ServerSocket socket;
    // private ObjectOutputStream out = new
    // ObjectOutputStream(socket.getOutputStream());
    // private ObjectInputStream in = new
    // ObjectInputStream(socket.getInputStream());

    public ServeurWorker(int port) {
        this.port = port;
        try {
            this.socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.print("\n=======================================================");
        System.out.println("\nSOCKET ECOUTE CREE => " + socket);
        System.out.println("\nServeur worker lancé sur le port " + this.port);
        System.out.print("\n=======================================================");
        while (Serveur.numWorker < Serveur.maxWorker) {
            /*
             * 2 - Attente d'une connexion client (la méthode s.accept() est bloquante
             * tant qu'un client ne se connecte pas)
             */
            Socket soc;
            try {
                soc = socket.accept();

                /*
                 * 3 - Pour gérer plusieurs clients simultanément, le serveur attend que les
                 * clients se connectent,
                 * et dédie un thread à chacun d'entre eux afin de le gérer indépendamment des
                 * autres clients
                 */
                ConnexionWorker cw = new ConnexionWorker(Serveur.numWorker++, soc);
                System.out.println("\nNOUVELLE CONNEXION WORKER - SOCKET => " + soc);
                System.out.print("\n=======================================================");
                // Serveur.numWorker++;
                cw.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
