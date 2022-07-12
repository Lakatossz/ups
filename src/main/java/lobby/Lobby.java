package lobby;

import client.Client;
import client.state.State;

/**
 * Trida Lobby, zajistujici logiku okna lobby.
 */
public class Lobby {

    /**
     * Trida Client, zajistujici komunikaci se serverem.
     */
    public Client client;

    /**
     * Okno lobby.
     */
    public LobbyWindow window;

    /**
     * Udruze, je-li okno zobrazeno.
     */
    private boolean displayed = false;

    /**
     * Vytvori nove lobby.
     * @param client  Trida client, zajistujici komunikaci se serverem.
     */
    public Lobby(Client client) {
        this.client = client;
        welcomeLobbyWindow();
    }

    /**
     * Nastavi mistnost, do ktere hraci vstoupi.
     * @param room Cislo mistnosti.
     */
    public void joinRoom(int room) {
        client.setMyRoom(room);
        client.joinRoom();
    }

    /**
     * Zobrazi okno lobby pri pristupu na server.
     */
    public void welcomeLobbyWindow() {
        if(!displayed) {
            client.setState(State.LOBBY);
            displayed = true;
            window = new LobbyWindow(this, client.getNickname());
        }
    }

    /**
     * Zobrazi okno lobby po umreni ve hre.
     */
    public void lostLobbyWindow() {
        if(!displayed) {
            client.setState(State.LOBBY);
            displayed = true;
            window = new LobbyWindow(this, "Umrel jsi!");
        }
    }

    /**
     * Zobrazi okno lobby po vyhre ve hre.
     */
    public void wonLobbyWindow() {
        if(!displayed) {
            client.setState(State.LOBBY);
            displayed = true;
            window = new LobbyWindow(this, "Vyhral jsi!");
        }
    }

    /**
     * Zavre okno lobby.
     */
    public void closeLobbyWindow() {
        if(displayed) {
            displayed = false;
            System.out.println("Zaviram okno lobby.");
            window.frame.dispose();
        }
    }

    public int getNumOfRooms() {
        return client.getNumOfRooms();
    }
}
