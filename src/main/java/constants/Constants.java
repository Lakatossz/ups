package constants;

/**
 * Trida konstant.
 */
public final class Constants {

    private Constants() {
    }

//    Klient
    /**
     * Konstanta delky formatu dvojciferneho celeho cisla.
     */
    public static final int INT_FORMAT_LENGTH = 2;

    /**
     * Konstanta delky formatu ctyrciferneho celeho cisla.
     */
    public static final int LONG_INT_FORMAT_LENGTH = 4;

    /**
     * Konstanta delky formatu realneho cisla.
     */
    public static final int DOUBLE_FORMAT_LENGTH = 7;

    /**
     * Konstanta maximalni delky prezdivky hrace.
     */
    public static final int MAX_NICK_LENGTH = 32;

    /**
     * Konstanta minimalni delky realneho cisla.
     */
    public static final int MIN_NICK_LENGTH = 1;

    /**
     * Konstanta nejvetsiho mozneho portu, na kterem server posloucha.
     */
    public static final int MAX_PORT = 65535;

    /**
     * Konstanta nejmensiho mozneho portu, na kterem server posloucha.
     */
    public static final int MIN_PORT = 1023;

    /**
     * Timeout odpovedi socketu.
     */
    public static final int SOCKET_TIMEOUT = 10000;

    /**
     * Cas timeoutu, kdy se od posledni komunikace provede ping.
     */
    public static final long PING_TIME = 5000;

    /**
     * Konstanta pro uspani vlakna mezi cekanim na ping.
     */
    public static final long PING_SLEEP = 2000;

    /**
     * Konstanta pro cekani na dalsi reconnect.
     */
    public static final long RECONNECT_SLEEP = 10000;

    /**
     * Potvrzeni o zpravnem prevzeti zpravy.
     */
    public static final int ROGER = 0;

    /**
     * Oznameni o spatnem formatu zpravy.
     */
    public static final int DENIAL = 11;

    /**
     * Znacka plne mistnosti.
     */
    public static final int ROOM_FULL = 22;

    /**
     * Cislo vyherce, posle server, kdyz je hrac posledni v mistnosti.
     */
    public static final int WIN = 99;

    /**
     * Cislo porazeneho, posle server, kdyz hrac umre.
     */
    public static final int LOSE = 66;

    /**
     * Hlavicka zpravy, posilane mezi sevrerem a klientem.
     */
    public static final String HEAD = "GAME";

    /**
     * Minimalni delka posilane zpravy.
     */
    public static final int MINIMAL_LENGTH = 8;

    /**
     * Maximalni pocet nevalidnich zprav.
     */
    public static final int BAD_ANSWERS = 3;

//    Login
    /**
     * Rozmery okna loginu.
     */
    public static final int LOGIN_WIDTH = 720, LOGIN_HEIGHT = 480;

//    Lobby
    /**
     * Rozmery okna lobby.
     */
    public static final int LOBBY_WIDTH = 720, LOBBY_HEIGHT = 480;

//    Hra
    /**
     * Rozmery herniho okna.
     */
    public static final int GAME_WIDTH = 1280, GAME_HEIGHT = 720;

    /**
     * Rozmery herniho pole.
     */
    public static final int WIDTH = 2000, HEIGHT = 2000;

    /**
     * Slouzi pro rozliseni velikosti kulicek hracu pri souboji.
     */
    public static final double FIGHT_CONSTANT = 0.75;

    /**
     * Maximalni radius hrace.
     */
    public static final double MAX_RADIUS = 9999;

    /**
     * Konstanta pro uspani v herni smycce.
     */
    public static final int GAME_SLEEP = 8;

//    Hrac
    /** Konstanta pro zamezeni pristupu k hranici pole.
     * Soucin s polomerem kulicky oznacuje hranicy, kam se nemuze dostat. */
    public static final int SAFE_ZONE = 4;

//    Pohyb hrace
    /** Konstanta rychlosti pro pohyb hrace. */
    public static final int VELOCITY_CONSTANT = 2;

    /** Konstanta pro posilani pohybu. */
    public static final int SEND_MOVE = 15;
}
