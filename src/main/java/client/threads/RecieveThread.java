package client.threads;

import client.Client;

import java.net.SocketException;
import java.net.SocketTimeoutException;

public class RecieveThread {

    /**
     * Klient, nad kterym bude vlakno volano.
     */
    Client client;

    public RecieveThread(Client client) {
        this.client = client;
    }

    /**
     * Vytvori nove vlakno, ktere se stara o poslouchani zpravy ze serveru.
     * @return Vytvorene vlakno.
     */
    public Thread handleRecieveThread()
    {
        return new Thread(() -> {
            String message = null;

            while(true) {
                if(client.getConnection().isOpen()) {
                    try {
                        while (client.getConnection().isOpen()) {
                            message = client.getConnection().reader.read();
                            if(message != null) {
                                break;
                            }
                        }

                        if (message != null) {
                            client.setLastTime(System.currentTimeMillis());
                            if (client.isNextStepMessage(message)) {
                                System.out.println("recieve: " + message);
                                client.getNextStep().add(message);
                            } else if (client.isUpdateMessage(message)) {
                                System.out.println("recieve: " + message);
                                client.getUpdate().add(message);
                            } else if (client.isPingMessage(message)) {
                                System.out.println("recieve: " + message);
                                client.getPing().add(message);
                            } else {
                                System.out.println("Prisla nevaldini zprava.");
                                client.getNextStep().add("invalid_message"); //tohle je na nevalidni vstup
                                client.backToLogin();
                            }
                        }
                    } catch (SocketTimeoutException ste) {
                        System.out.println("timeoutexception");
                        if(!client.tryToReconn()) {
                            break;
                        }
                    } catch (SocketException ste) {
                        System.out.println("exception");
                        if(!client.tryToReconn()) {
                            break;
                        }
                    } catch (Exception conEr) {
                        break;
                    }
                }
            }
        });
    }
}
