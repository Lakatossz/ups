package client.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class MyReader {

    /**
     * Buffer pro prijem zprav ze socketu.
     */
    private BufferedReader reader;

    /**
     * Ukazuje, jestli je buffer otevreny.
     */
    private boolean open;

    /**
     * Vytvori cteci buffer.
     */
    public MyReader(Reader in) {
        reader = new BufferedReader(in);
        open = true;
    }

    /**
     * Zavre cteci buffer.
     */
    public synchronized void closeReader() {

        while(!open) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Zavreni readeru - cekani.");
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Zavreni readeru - ostatni.");
        }
        notify();
    }

    /**
     * Precte zpravu ze socketu.
     */
    public synchronized String read() throws IOException, InterruptedException {
        String read;
        while(!open) {
            wait();
        }

        read = reader.readLine();

        return read;
    }

    /**
     * Vrati, jestli je buffer oteverny.
     */
    public boolean isOpen() {
        if(reader == null) {
            return false;
        }
        return open;
    }
}
