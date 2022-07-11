package client.messageType;

/**
 * Vyctovy typu zprav.
 */
public enum MessageType {
    LOGIN("logi"),          /** Zprava pro zadost o vstup na server. */
    JOIN("join"),           /** Zprava pro zadost o vstup do herni mistnosti. */
    MOVE("move"),           /** Zprava pro oznameni pohybu. */
    KILL("kill"),           /** Zprava o oznameni, ze hrac nekoho zabil.*/
    EATFOOD("eatf"),        /** Zprava o oznameni, ze hrac sezral potravu. */
    DISCONNECT("disc"),     /** Zprava pro zadost o opusteni serveru. */
    RECONNECT("recn"),      /** Zprava pro zadost o znovupripojeni. */
    // aktualizujici zprava
    LOBBY("lobb"),          /** Zprava pro aktualizaci stavu v lobby. */
    ROOM("room"),           /** Zprava pro aktualizaci stavu v herni mistnosti. */
    BACKTOLOBBY("bckl"),    /** Zprava pro navrat do lobby. */
    // ping zprava
    PING("ping");           /** Zprava ping. */

    private final String text;


    MessageType(final String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
