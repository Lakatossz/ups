package client.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class MyWriter {

    /**
     * Buffer pro zapis na socket.
     */
    private BufferedWriter writer;

    /**
     * Ukazuje, jestli je buffer otevreny.
     */
    private boolean open;

    /**
     * Vytvori zapisovy buffer.
     */
    public MyWriter(Writer out) {
        writer = new BufferedWriter(out);
        open = true;
    }

    /**
     * Zavre zapisovy buffer.
     */
    public synchronized void closeWriter() {
        while(!open) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Zavreni writeru - cekani.");
            }
        }
        try {
            writer.close();
            open = false;
        } catch (IOException e) {
            System.out.println("Zavreni writeru - ostatni.");
        }
        notify();
    }

    /**
     * Posle zpravu na socket.
     */
    public synchronized void write(String message) {
        while(!open) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Poslani zpravy - cekani.");
            }
        }
        try {
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Poslani zpravy - chyba.");
        }
    }

    /**
     * Vrati, jestli je buffer oteverny.
     */
    public boolean isOpen() {
        if(writer == null) {
            return false;
        }
        return open;
    }
}
