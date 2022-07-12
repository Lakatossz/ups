package client.threads;

import client.Client;

import client.messageType.MessageType;
import constants.Constants;

/**
 * Trida pro ping vlakno.
 */
public class PingThread {

    /**
     * Klient, nad kterym bude vlakno pracovat.
     */
    Client client;

    public PingThread(Client client) {
        this.client = client;
    }

    /**
     * Vytvori pingovaci vlakno a bude se starat o jeho logiku.
     */
    public Thread handlePingThread() {
        return new Thread(() -> {
            String message, recv;
            int passed, willPass;

            while(true) {
                long thisTime = System.currentTimeMillis();
                if(thisTime - client.getLastTime() > Constants.PING_TIME && client.serverIsOnline()) {
                    client.setLastTime(thisTime);
                    message = MessageType.PING + client.formatIntNumber(client.getMyID());
                    client.send(message);

                    recv = client.getPing().remove();
                    if(recv == null) {
                        break;
                    }
                    passed = 0;
                    willPass = Constants.HEAD.length();
                    if (Constants.HEAD.compareTo(recv.substring(passed, willPass)) != 0) {
                        System.out.println("Server odpovedel spatnou hlavickou");
                        if(client.increaseBadCounter()) {
                            continue;
                        } else {
                            client.backToLogin();
                        }
                    }
                    passed = Constants.HEAD.length() + MessageType.PING.toString().length() + 4;
                    willPass = passed + Constants.INT_FORMAT_LENGTH;
                    if(Integer.parseInt(recv.substring(passed, willPass)) != client.getMyID()) {
                        System.out.println("server odpovedel spatnym ID");
                        if(client.increaseBadCounter()) {
                            continue;
                        } else {
                            client.backToLogin();
                        }
                    }
                }
                try {
                    Thread.sleep(Constants.PING_SLEEP);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }
}
