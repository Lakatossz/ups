package client.connection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;

import client.reader.MyReader;
import client.writer.MyWriter;
import constants.Constants;

public class Connection {

    /**
     * Socket, na kterem se spojeni vytvori.
     */
    Socket socket;

    /**
     * Vstup zprav ze serveru.
     */
    public MyReader reader;

    /**
     * Vystup zprav na server.
     */
    public MyWriter writer;

    /**
     * Vytvori novy socket.
     */
    public Connection() {
        socket = new Socket();
    }

    /**
     * Vytvori spojeni a buffery.
     */
    public synchronized boolean connect(InetSocketAddress address) {
        try {
            socket.connect(address, address.getPort());
            socket.setSoTimeout(Constants.SOCKET_TIMEOUT);
            reader = new MyReader(new InputStreamReader(this.getSocket().getInputStream()));
            writer = new MyWriter(new OutputStreamWriter(this.getSocket().getOutputStream()));
        } catch (IOException | InterruptedException e) {
            System.out.println("Chyba pri vytvareni socketu.");
            notifyAll();
            return false;
        }
        notifyAll();
        return true;
    }

    /**
     * Uzavre socket a buffery.
     */
    public synchronized void close() {
        while(socket.isClosed()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri zavirani socketu.");
            }
        }
        if(!socket.isClosed()) {
            try {
                if(reader != null) {
                    reader.closeReader();
                }
                if(writer != null) {
                    writer.closeWriter();
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Chyba pri zavirani socketu.");
            }
            System.out.println("Zavrel jsem socket.");
            notifyAll();
        }
    }

    public synchronized Socket getSocket() throws InterruptedException {
        while(socket.isClosed()) {
            wait();
        }

        notifyAll();
        return socket;
    }

    public synchronized boolean isOpen() {
        while(socket.isClosed()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        notify();
        return !(socket != null && socket.isClosed() && reader != null && reader.isOpen() && writer != null && writer.isOpen());
    }
}
