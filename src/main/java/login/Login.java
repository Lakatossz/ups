package login;

import client.Client;
import client.state.State;

/**
 * Trida Login, zajistujici logiku okna login.
 */
public class Login {

    /**
     * Prezdivka hrace, zadana v okne LoginWindow.
     */
    private String nickname;

    /**
     * IP (hostname) serveru, zadana v okne LoginWindow.
     */
    private String host;

    /**
     * Port serveru, zadany v okne LoginWindow.
     */
    private int port;

    /**
     * Trida Client, zajistujici komunkaci se serverem.
     */
    public Client client;

    /**
     * Okno loginu.
     */
    public LoginWindow window;

    /**
     * True, pokud je okno zobrazovano.
     */
    public boolean displayed = false;

    /**
     * Vytvori novy login.
     * @param client Trida client, zajistujici komunikaci se serverem.
     */
    public Login(Client client) {
        this.client = client;

        nickname = client.getNickname();
        displayLoginWindow();
    }

    /** Nastavi adresu, port a nickname a zavola connect(). */
    public void connect() {
        client.setAddress(host, port);
        client.setNickname(nickname);
        client.connect();
    }

    /**
     * Zjisti, jetsli je sting cislo.
     * @param s String.
     * @return True, pokud se jedna o cislo, false jinak.
     */
    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException nfex) {
            return false;
        }
    }

    /**
     * Zobrazi okno loginu a prepne stav aplikace na State.LOGIN.
     */
    public synchronized void displayLoginWindow() {
        while(displayed) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Neslo otevrit okno login - cekani.");
            }
        }
        displayed = true;
        client.setState(State.LOGIN);
        window = new LoginWindow(this);
        notify();
    }

    /**
     * Zavre okno loginu.
     */
    public synchronized void closeLoginWindow() {
        while(!displayed) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                System.out.println("Neslo otevrit okno login. - cekani");
            }
        }
        displayed = false;
        System.out.println("Zaviram okno login.");
        window.frame.dispose();
        notify();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
