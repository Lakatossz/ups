package client.state;

/**
 * Stav klienta. Slouzi pro GUI.
 */
public enum State {
    DISCONNECTED,   /** Stav odpojen. */
    LOGIN,          /** Stav v lobby. */
    LOBBY,          /** Stav v loginu. */
    GAME,           /** Stav uvnitr hry. */
}
