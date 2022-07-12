package client;

import client.threads.PingThread;
import client.connection.Connection;
import client.queue.MyQueue;
import client.messageType.MessageType;
import client.state.State;
import client.threads.RecieveThread;
import client.threads.UpdateThread;
import game.Game;
import game.feed.Feed;
import lobby.Lobby;
import login.Login;
import constants.Constants;

import java.net.*;
import java.util.Locale;

/**
 * Trida, zajistujici komunikaci se serverem.
 */
public class Client {

    /**
     * Adresa serveru.
     */
    private InetSocketAddress address;

    /**
     * Stav, ze ktereho bude klient posilat zpravu serveru.
     */
    private State state;

    /**
     * Prezdivka hrace.
     */
    private String nickname = "Jan_Novak_123";

    /**
     * ID, pridelena po pripojeni na server (Socket clienta na strane serveru.).
     */
    private int myID;

    /**
     * Pocet mistnosti na sevreru. V kazde mistnosti probiha oddelena komunikace.
     */
    private int numOfRooms;

    /**
     * Cislo misnosti, ve ktere se hrac nachazi.
     */
    private int myRoom;

    /**
     * Pocitadlo spatnych odpovedi ze strany serveru (Po 3 (zatim) se klient odpoji.).
     */
    private int badCounter;

    /**
     * Trida Login, slouzici pro logiku uvnitr okna LoginWindow.
     */
    private final Login login;

    /**
     * Trida Lobby, slouzici pro logiku uvnitr okna LobbyWindow.
     */
    private Lobby lobby;

    /**
     * Trida Game, slouzici pro logiku cele hry a okna GameWindow.
     */
    private Game game;

    /**
     * Fronta zprav, ktere jsou odpovedi iniciativy klienta.
     */
    private MyQueue  nextStep;

    /**
     * Fronta zprav, ktere aktualizuji stav hry, kdyz klient nic nevykonava.
     */
    private MyQueue update;

    /**
     * Fronta zprav typu ping.
     */
    private MyQueue ping;

    /**
     * Vlakno pro prijimani zprav.
     */
    private Thread recieveThread;

    /**
     * Vlakno pro provadeni aktualizacni zpravy.
     */
    private Thread updateThread;

    /**
     * Vlakno pro pingovani.
     */
    private Thread pingThread;

    /**
     * Posledni cas komunikace se serverem.
     */
    private long lastTime;

    /**
     * Id posledni potravy, kterou hrac sezral.
     */
    private int lastEatenFeed;

    /**
     * Id posledniho nepritele, ktere hrac zabil.
     */
    private int lastEnemyKilled;

    /**
     * Objekt pro pripojeni.
     */
    private Connection connection;

    /**
     * Vytvori noveho klienta.
     */
    public Client() {
        state = State.DISCONNECTED;
        login = new Login(this);
    }

    /**
     * Vytvori nove pripojeni, spusti vlakna a vytvori fronty.
     */
    public void connect() {
        System.out.println("Zakladam spojeni.");
        if(makeNewSocket()) {
            setQueues();
            setThreads();
            tryToLogin();
        }
    }

    /**
     * Zajisti pripojeni se serverem a zahaji pocatecni zpravu.
     * Zaroven nastavy vstupni a vystupni proudy.
     */
    public boolean makeNewSocket() {
        connection = new Connection();
        if(connection.connect(address)) {
            lastTime = System.currentTimeMillis();
            return true;
        }
        login.window.print.setText(" Neplatny host  ");
        return false;
    }

    /**
     * Zajisti radne ukonceni spojeni se serverem.
     */
    public void close() {
        connection.close();
    }

    /**
     * Odesle validnim zpusobem zpravu na server. Ke zprave prida na zacatek hlavicku a delku informaci.
     * @param message Odesilana zprava.
     */
    public void send(String message) {
        if(connection.writer != null && connection.writer.isOpen()) {
            connection.writer.write(Constants.HEAD + formatLongIntNumber(message.length()) + message);
            System.out.println("send: " + Constants.HEAD + formatLongIntNumber(message.length()) + message);
        }
    }

    /**
     * Odesle zadost o pripojeni na server.
     */
    public void tryToLogin() {
        System.out.println("Zadam o pripojeni na server.");
        routine(MessageType.LOGIN);
        if(state == State.LOBBY) {
            game = new Game(this);
        }
    }

    /**
     * Odesle serveru zadost o zruseni spojeni.
     */
    public void askForClose() {
        if(connection.isOpen()) {
            System.out.println("Zadam o odpojeni ze serveru");
            routine(MessageType.DISCONNECT);
            backToLogin();
        } else {
            backToLogin();
        }
    }

    public boolean tryToReconn() {
        int i = 0;

        if(state == State.LOBBY) {
            lobby.window.print.setText("Server je nedostupny");
        } else if(state == State.GAME) {
            game.setPrintError(true);
        }

        System.out.println("Zkousim se znovu pripojit.");

        close(); // radsi

        while(i < 3) {
            System.out.println("Pokus c. " + (i + 1));
            if(makeNewSocket()) {
                setPingThread();
                if(connection.isOpen()) {
                    if(reconnect()) {
                        System.out.println("Podarilo se pripojit.");
                        if(state == State.LOBBY) {
                            lobby.window.print.setText("");
                        } else if(state == State.GAME) {
                            game.setPrintError(false);
                        }
                        return true;
                    }
                }
            }
            ++i;
            System.out.println("Nepodarilo se pripojit.");
            try {
                Thread.sleep(Constants.RECONNECT_SLEEP);
            } catch (InterruptedException e) {
                System.out.println("Problem uspani vlakna.");
            }
        }
        backToLogin();

        return false;
    }

    /**
     * Oznameni serveru o znovupripojeni.
     */
    public boolean reconnect() {
        try {
            System.out.println("Znovu se pripojuji.");
            send(MessageType.RECONNECT + formatIntNumber(myID));

            String recv = connection.reader.read();
            System.out.println("recieve: " + recv);
            if(checkHead(recv)) {
                handleReconnection(recv);
                return true;
            } else {
                if (!increaseBadCounter()) {
                    return false;
                } else {
                    reconnect();
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Pozada server o moznost pripojeni do mistnosti, ktera byla vybrana.
     */
    public void joinRoom() {
        System.out.println("Zadam o pripojeni do mistnosti");
        routine(MessageType.JOIN);
    }

    /**
     * Oznami serveru posun na hernim poli.
     */
    public void move() {
        try {
            routine(MessageType.MOVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Oznami sevreru, ze snedl zradlo.
     */
    public void eatFood() {
        try {
            System.out.println("Sezral jsem zradlo");
            routine(MessageType.EATFOOD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Oznami serveru, ze snedl nepritele.
     */
    public void killEnemy() {
        try {
            System.out.println("Zabil jsem nepritele.");
            routine(MessageType.KILL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zobrazeni lobby, po prohre.
     */
    public void lostToLobby() {
        if(state == State.GAME) {
            game.closeGameWindow();
        }
        game.resetGame();
        lobby.lostLobbyWindow();
    }

    /**
     * Zobrazeni lobby, po vyhre.
     */
    public void wonToLobby() {
        if(state == State.GAME) {
            game.closeGameWindow();
        }
        game.resetGame();
        lobby.wonLobbyWindow();
    }

    /**
     * Vrati hru zpet do stavu Login.
     */
    public void backToLogin() {
        if(lobby != null && state == State.LOBBY) {
            lobby.closeLobbyWindow();
        } else if(game != null && state == State.GAME){
            game.closeGameWindow();
        } else if(login != null && state == State.LOGIN) {
            login.closeLoginWindow();
        }
        resetClient();
        login.displayLoginWindow();
    }

    /**
     * Vynuluje vse podstatne v klientu.
     */
    public void resetClient() {
        if(game != null) {
            game.resetGame();
        }
        close();
    }

    /**
     * Pokud je badCounter mensi 3, incrementuje ho.
     * @return True, pokud byl inkrementovan, jinak false.
     */
    public boolean increaseBadCounter() {
        if(badCounter < Constants.BAD_ANSWERS) {
            badCounter++;
            return true;
        }
        return false;
    }

    /**
     * Hlavni metoda, zajistujici validaci zprav iniciovanych klientem.
     * @param type Typ, zpravy, ktera bude provedena.
     */
    private void routine(MessageType type) {
        String message, recv;

        if(true) {
            if (type == MessageType.LOGIN) {

                message = type + formatIntNumber(getNickname().length()) + getNickname();
                send(message);
                recv = nextStep.remove();
                if(recv.compareTo("invalid_message") == 0) {
                    return;
                }
                if(recv != null) {
                    if(checkHead(recv)) {
                        handleLoginResponse(recv);
                        lobby = new Lobby(this);
                    } else {
                        if (!increaseBadCounter()) {
                            backToLogin();
                        }
                    }
                }
            } else if (type == MessageType.JOIN) {

                message = type + formatIntNumber(myID) + formatIntNumber(myRoom);
                send(message);
                recv = nextStep.remove();
                if(recv != null) {
                    if(checkHead(recv)) {
                        handleJoinResponse(recv);
                    } else {
                        if (!increaseBadCounter()) {
                            backToLogin();
                        }
                    }
                }
            } else if (type == MessageType.MOVE) {
                message = type + formatIntNumber(myID) + formatDoubleNumber(game.getPlayer().getXPosition()) + formatDoubleNumber(game.getPlayer().getYPosition()) + formatDoubleNumber(game.getPlayer().getRadius());
                send(message);
                recv = nextStep.remove();
                if(recv != null) {
                    if(checkHead(recv)) {
                        handleMoveResponse(recv);
                    } else {
                        if (!increaseBadCounter()) {
                            backToLogin();
                        }
                    }
                }
            } else if (type == MessageType.KILL) {
                message = type + formatIntNumber(myID) + formatIntNumber(lastEnemyKilled);
                send(message);

                recv = nextStep.remove();
                if(recv != null) {
                    if(checkHead(recv)) {
                        handleKillResponse(recv);
                    } else {
                        if (!increaseBadCounter()) {
                            backToLogin();
                        }
                    }
                }
            } else if (type == MessageType.EATFOOD) {
                message = type + formatIntNumber(myID) + formatIntNumber(lastEatenFeed);
                send(message);

                recv = nextStep.remove();
                if(recv != null) {
                    if(checkHead(recv)) {
                        hadnleEatFoodResponse(recv);
                    } else {
                        if (!increaseBadCounter()) {
                            backToLogin();
                        }
                    }
                }
            } else if (type == MessageType.DISCONNECT) {
                message = type + formatIntNumber(myID);
                send(message);

                recv = nextStep.remove();
                if(recv != null) {
                    if(checkHead(recv)) {
                        handleDisconnectResponse(recv);
                    } else {
                        if (!increaseBadCounter()) {
                            backToLogin();
                        }
                    }
                }
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na login.
     */
    public void handleLoginResponse(String recv) {
        int passed = Constants.HEAD.length() + MessageType.LOGIN.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + nickname.length();

        if (nickname.compareTo(recv.substring(passed, willPass)) != 0) {
            System.out.println("Server odpovedel spatnym nickem");
            if (increaseBadCounter()) {
                takeAction(MessageType.LOGIN);
            } else {
                backToLogin();
            }
        }

        passed = willPass;
        willPass += Constants.INT_FORMAT_LENGTH;

        /* Povoleni k pristupu */
        if (Integer.parseInt(recv.substring(passed, willPass)) == 0) {
            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;
            myID = Integer.parseInt(recv.substring(passed, willPass));
            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;
            numOfRooms = Integer.parseInt(recv.substring(passed, willPass));

            if(myID < 0 || numOfRooms < 1) {
                System.out.println("Neplatne ID nebo pocet mistnosti");
                if (increaseBadCounter()) {
                    takeAction(MessageType.LOGIN);
                } else {
                    backToLogin();
                }
            }
        } else {
            System.out.println("Server neposlal nic dalsiho");
            if(increaseBadCounter()) {
                takeAction(MessageType.LOGIN);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na pripojeni do herni mistnosti.
     */
    public void handleJoinResponse(String recv) {
        double xPosition, yPosition, radius;
        int id, nickLength, color_id, cooldownTime;
        String nickname;

        int passed = Constants.HEAD.length() + MessageType.JOIN.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + Constants.INT_FORMAT_LENGTH;

        if (Integer.parseInt(recv.substring(passed, willPass)) != myID) {
            if (increaseBadCounter()) {
                System.out.println("Spatne id pri napojeni do mistnosti.");
                takeAction(MessageType.JOIN);
            } else {
                backToLogin();
            }
        }

        passed = willPass;
        willPass += Constants.INT_FORMAT_LENGTH;

        /* povoleni pristupu do roomky */
        if (Integer.parseInt(recv.substring(passed, willPass)) == 0) {

            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;

            cooldownTime = Integer.parseInt(recv.substring(passed, willPass));
            game.setCooldownTime(cooldownTime);

            passed = willPass;
            willPass += Constants.DOUBLE_FORMAT_LENGTH;
            xPosition = Double.parseDouble(recv.substring(passed, willPass));

            passed = willPass;
            willPass += Constants.DOUBLE_FORMAT_LENGTH;
            yPosition = Double.parseDouble(recv.substring(passed, willPass));

            passed = willPass;
            willPass += Constants.DOUBLE_FORMAT_LENGTH;
            radius = Double.parseDouble(recv.substring(passed, willPass));

            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;
            color_id = Integer.parseInt(recv.substring(passed, willPass));

            if (xPosition > Constants.WIDTH || xPosition < 0 || yPosition > Constants.HEIGHT || yPosition < 0 || radius < Constants.MAX_RADIUS || color_id < game.getColors().length) {
                game.getPlayer().setPlayer(xPosition, yPosition, radius, game.getColor(color_id));
            } else {
                if (increaseBadCounter()) {
                    System.out.println("Spatne parametry pro me.");
                    takeAction(MessageType.JOIN);
                } else {
                    backToLogin();
                }
            }

            while (willPass < recv.length()) {
                passed = willPass;
                willPass += Constants.INT_FORMAT_LENGTH;

                if ("pp".compareTo(recv.substring(passed, willPass)) == 0) {

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;

                    id = Integer.parseInt(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;

                    nickLength = Integer.parseInt(recv.substring(passed, willPass));

                    if (nickLength <= Constants.MIN_NICK_LENGTH || nickLength > Constants.MAX_NICK_LENGTH) {
                        if (increaseBadCounter()) {
                            System.out.println("Spatny nick nepritele.");
                            takeAction(MessageType.JOIN);
                        } else {
                            backToLogin();
                        }
                    }

                    passed = willPass;
                    willPass += nickLength;

                    nickname = recv.substring(passed, willPass);

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                    radius = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;

                    color_id = Integer.parseInt(recv.substring(passed, willPass));

                    if (!nickname.isBlank() || xPosition < Constants.WIDTH || xPosition > 0 || yPosition < Constants.HEIGHT || yPosition > 0 || radius > 0 || radius < Constants.MAX_RADIUS || color_id < game.getColors().length) {
                        passed = willPass;
                        willPass += Constants.INT_FORMAT_LENGTH;

                        if (Integer.parseInt(recv.substring(passed, willPass)) == 1) {
                            game.enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, game.getColor(color_id), nickname, true);
                        } else if (Integer.parseInt(recv.substring(passed, willPass)) == 0) {
                            game.enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, game.getColor(color_id), nickname, false);
                        } else {
                            System.out.println("Spatny stav pripojeni nepritele.");
                            if (increaseBadCounter()) {
                                takeAction(MessageType.JOIN);
                            } else {
                                backToLogin();
                            }
                        }
                    } else {
                        System.out.println("Spatne parametry nepritele.");
                        if (increaseBadCounter()) {
                            takeAction(MessageType.JOIN);
                        } else {
                            backToLogin();
                        }
                    }
                } else if ("ff".compareTo(recv.substring(passed, willPass)) == 0) {

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    id = Integer.parseInt(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    radius = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    color_id = Integer.parseInt(recv.substring(passed, willPass));


                    if (xPosition <= Constants.WIDTH && xPosition > 0 && yPosition <= Constants.HEIGHT && yPosition > 0 && radius > 0 && radius < Constants.MAX_RADIUS && color_id < game.getColors().length) {
                        game.feedHandler.addFeed(new Feed(id, xPosition, yPosition, radius, game.getColor(color_id)));
                    } else {
                        if (increaseBadCounter()) {
                            System.out.println("Spatne parametry kulicek");
                            takeAction(MessageType.JOIN);
                        } else {
                            backToLogin();
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            game.displayGameWindow();
        } else if(Integer.parseInt(recv.substring(passed, willPass)) == Constants.ROOM_FULL) {
            lobby.window.print.setText("Mistnost je plna");
        } else {
            System.out.println("Server neudelil pristup.");
            if (increaseBadCounter()) {
                takeAction(MessageType.JOIN);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na pohyb hrace.
     */
    public void handleMoveResponse(String recv) {
        double xPosition, yPosition, radius;
        int id, color_id, nickLength;

        int passed = Constants.HEAD.length() + MessageType.MOVE.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + Constants.INT_FORMAT_LENGTH;
        if(Integer.parseInt(recv.substring(passed, willPass)) != myID) {
            if(increaseBadCounter()) {
                takeAction(MessageType.MOVE);
            } else {
                backToLogin();
            }
        }

        passed = willPass;
        willPass += Constants.INT_FORMAT_LENGTH;

        if (Integer.parseInt(recv.substring(passed, willPass)) == Constants.WIN) {
            System.out.println("Server potvrdil vyhru.");
            wonToLobby();
        } else if (Integer.parseInt(recv.substring(passed, willPass)) == 0) {
            while (willPass < recv.length()) {
                passed = willPass;
                willPass += Constants.INT_FORMAT_LENGTH;

                if ("pp".compareTo(recv.substring(passed, willPass)) == 0) {

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    id = Integer.parseInt(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    nickLength = Integer.parseInt(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += nickLength;
                    nickname = recv.substring(passed, willPass);

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    radius = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    color_id = Integer.parseInt(recv.substring(passed, willPass));

                    if (!nickname.isBlank() || xPosition < Constants.WIDTH || xPosition > 0 || yPosition < Constants.HEIGHT || yPosition > 0 || radius > 0 || radius < Constants.MAX_RADIUS || color_id < game.getColors().length) {
                        passed = willPass;
                        willPass += Constants.INT_FORMAT_LENGTH;

                        if(Integer.parseInt(recv.substring(passed, willPass)) == 1) {
                            game.enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, game.getColor(color_id), nickname, true);
                        } else if(Integer.parseInt(recv.substring(passed, willPass)) == 0) {
                            game.enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, game.getColor(color_id), nickname, false);
                        } else {
                            System.out.println("Spatny format stavu pripojeni nepritele.");
                            if (increaseBadCounter()) {
                                takeAction(MessageType.JOIN);
                            } else {
                                backToLogin();
                                break;
                            }
                        }
                    } else {
                        System.out.println("Spatne parametry nepritele.");
                        if (increaseBadCounter()) {
                            takeAction(MessageType.JOIN);
                        } else {
                            backToLogin();
                        }
                    }
                } else if ("ff".compareTo(recv.substring(passed, willPass)) == 0) {

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    id = Integer.parseInt(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    radius = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    color_id = Integer.parseInt(recv.substring(passed, willPass));

                    if (xPosition <= Constants.WIDTH && xPosition >= 0 && yPosition <= Constants.HEIGHT && yPosition >= 0 && radius > 0 && radius < Constants.MAX_RADIUS && color_id < game.getColors().length) {
                        game.feedHandler.respawnFeed(id, xPosition, yPosition, radius, game.getColor(color_id));
                    } else {
                        if (increaseBadCounter()) {
                            takeAction(MessageType.MOVE);
                        } else {
                            backToLogin();
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        } else {
            if (increaseBadCounter()) {
                takeAction(MessageType.MOVE);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na zabiti nepritele.
     */
    public void handleKillResponse(String recv) {
        int passed = Constants.HEAD.length() + MessageType.KILL.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + Constants.INT_FORMAT_LENGTH;
        if (Integer.parseInt(recv.substring(passed, willPass)) != myID) {
            System.out.println("Server odpovedel spatnym ID.");
            if (increaseBadCounter()) {
                takeAction(MessageType.KILL);
            } else {
                backToLogin();
            }
        }

        passed = willPass;
        willPass = passed + Constants.INT_FORMAT_LENGTH;
        if(Integer.parseInt(recv.substring(passed, willPass)) == Constants.WIN) {
            wonToLobby();
        } else if (Integer.parseInt(recv.substring(passed, willPass)) != 0) {
            if(increaseBadCounter()) {
                takeAction(MessageType.KILL);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na sezrani potravy.
     */
    public void hadnleEatFoodResponse(String recv) {
        int passed = Constants.HEAD.length() + MessageType.EATFOOD.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + Constants.INT_FORMAT_LENGTH;
        if(Integer.parseInt(recv.substring(passed, willPass)) != myID) {
            System.out.println("server odpovedel spatnym ID");
            if(increaseBadCounter()) {
                takeAction(MessageType.EATFOOD);
            } else {
                backToLogin();
            }
        }

        passed = willPass;
        willPass = passed + Constants.INT_FORMAT_LENGTH;
        if(Integer.parseInt(recv.substring(passed, willPass)) == Constants.WIN) {
            wonToLobby();
        } else if (Integer.parseInt(recv.substring(passed, willPass)) != 0) {
            System.out.println("Server nedovolil sezrani.");
            if (increaseBadCounter()) {
                takeAction(MessageType.EATFOOD);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na odpojeni.
     */
    public void handleDisconnectResponse(String recv) {
        int passed = Constants.HEAD.length() + MessageType.DISCONNECT.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + Constants.INT_FORMAT_LENGTH;
        if(Integer.parseInt(recv.substring(passed, willPass)) != myID) {
            System.out.println("Server odpovedel spatnym ID.");
            if(increaseBadCounter()) {
                takeAction(MessageType.DISCONNECT);
            } else {
                backToLogin();
            }
        }

        passed = willPass;
        willPass = passed + Constants.INT_FORMAT_LENGTH;
        if(Integer.parseInt(recv.substring(passed, willPass)) != 0) {
            if(increaseBadCounter()) {
                takeAction(MessageType.DISCONNECT);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na reconnect.
     */
    public void handleReconnection(String recv) {
        if(state == State.LOBBY) {
            handleReconToLobby(recv);
        } else if(state == State.GAME) {
            handleReconToGame(recv);
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na reconnect do lobby.
     */
    public void handleReconToLobby(String recv) {
        int passed = Constants.HEAD.length() + MessageType.RECONNECT.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + Constants.INT_FORMAT_LENGTH;

        /* Povoleni k pristupu */
        if (Integer.parseInt(recv.substring(passed, willPass)) == 0) {
            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;
            myID = Integer.parseInt(recv.substring(passed, willPass));
            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;
            numOfRooms = Integer.parseInt(recv.substring(passed, willPass));

            if(myID < 0 || numOfRooms < 1) {
                System.out.println("Neplatne ID nebo pocet mistnosti");
                if (increaseBadCounter()) {
                    takeAction(MessageType.RECONNECT);
                } else {
                    backToLogin();
                }
            }
        } else {
            System.out.println("Server neposlal nic dalsiho");
            if(increaseBadCounter()) {
                takeAction(MessageType.RECONNECT);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Kontrola a zpracovani odpovedi na reconnect hry.
     */
    public void handleReconToGame(String recv) {
        double xPosition, yPosition, radius;
        int id, nickLength, color_id, cooldownTime;

        int passed = Constants.HEAD.length() + MessageType.RECONNECT.toString().length() + Constants.LONG_INT_FORMAT_LENGTH;
        int willPass = passed + Constants.INT_FORMAT_LENGTH;

        if (Integer.parseInt(recv.substring(passed, willPass)) != myID) {
            if (increaseBadCounter()) {
                System.out.println("Spatne id pri napojeni do mistnosti.");
                takeAction(MessageType.RECONNECT);
            } else {
                backToLogin();
            }
        }

        passed = willPass;
        willPass += Constants.INT_FORMAT_LENGTH;

        System.out.println(recv.substring(passed, willPass));

        /* povoleni pristupu do roomky */
        if (Integer.parseInt(recv.substring(passed, willPass)) == 0) {

            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;

            cooldownTime = Integer.parseInt(recv.substring(passed, willPass));
            game.setCooldownTime(cooldownTime);

            passed = willPass;
            willPass += Constants.DOUBLE_FORMAT_LENGTH;
            xPosition = Double.parseDouble(recv.substring(passed, willPass));

            passed = willPass;
            willPass += Constants.DOUBLE_FORMAT_LENGTH;
            yPosition = Double.parseDouble(recv.substring(passed, willPass));

            passed = willPass;
            willPass += Constants.DOUBLE_FORMAT_LENGTH;
            radius = Double.parseDouble(recv.substring(passed, willPass));

            passed = willPass;
            willPass += Constants.INT_FORMAT_LENGTH;
            color_id = Integer.parseInt(recv.substring(passed, willPass));

            if (xPosition > Constants.WIDTH || xPosition < 0 || yPosition > Constants.HEIGHT || yPosition < 0 || radius < Constants.MAX_RADIUS || color_id < game.getColors().length) {
                game.getPlayer().setPlayer(xPosition, yPosition, radius, game.getColor(color_id));
            } else {
                if (increaseBadCounter()) {
                    System.out.println("Spatne parametry pro me.");
                    takeAction(MessageType.RECONNECT);
                } else {
                    backToLogin();
                }
            }

            while (willPass < recv.length()) {
                passed = willPass;
                willPass += Constants.INT_FORMAT_LENGTH;

                if ("pp".compareTo(recv.substring(passed, willPass)) == 0) {

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;

                    id = Integer.parseInt(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;

                    nickLength = Integer.parseInt(recv.substring(passed, willPass));

                    if (nickLength <= Constants.MIN_NICK_LENGTH || nickLength > Constants.MAX_NICK_LENGTH) {
                        if (increaseBadCounter()) {
                            System.out.println("Spatny nick nepritele.");
                            takeAction(MessageType.RECONNECT);
                        } else {
                            backToLogin();
                        }
                    }

                    passed = willPass;
                    willPass += nickLength;

                    nickname = recv.substring(passed, willPass);

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                    radius = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;

                    color_id = Integer.parseInt(recv.substring(passed, willPass));

                    if (!nickname.isBlank() || xPosition < Constants.WIDTH || xPosition > 0 || yPosition < Constants.HEIGHT || yPosition > 0 || radius > 0 || radius < Constants.MAX_RADIUS || color_id < game.getColors().length) {
                        passed = willPass;
                        willPass += Constants.INT_FORMAT_LENGTH;

                        if (Integer.parseInt(recv.substring(passed, willPass)) == 1) {
                            game.enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, game.getColor(color_id), nickname, true);
                        } else if (Integer.parseInt(recv.substring(passed, willPass)) == 0) {
                            game.enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, game.getColor(color_id), nickname, false);
                        } else {
                            System.out.println("Spatny stav pripojeni nepritele.");
                            if (increaseBadCounter()) {
                                takeAction(MessageType.RECONNECT);
                            } else {
                                backToLogin();
                            }
                        }
                    } else {
                        System.out.println("Spatne parametry nepritele.");
                        if (increaseBadCounter()) {
                            takeAction(MessageType.RECONNECT);
                        } else {
                            backToLogin();
                        }
                    }
                } else if ("ff".compareTo(recv.substring(passed, willPass)) == 0) {

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    id = Integer.parseInt(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                    radius = Double.parseDouble(recv.substring(passed, willPass));

                    passed = willPass;
                    willPass += Constants.INT_FORMAT_LENGTH;
                    color_id = Integer.parseInt(recv.substring(passed, willPass));


                    if (xPosition <= Constants.WIDTH && xPosition > 0 && yPosition <= Constants.HEIGHT && yPosition > 0 && radius > 0 && radius < Constants.MAX_RADIUS && color_id < game.getColors().length) {
                        game.feedHandler.respawnFeed(id, xPosition, yPosition, radius, game.getColor(color_id));
                    } else {
                        if (increaseBadCounter()) {
                            System.out.println("Spatne parametry kulicek");
                            takeAction(MessageType.RECONNECT);
                        } else {
                            backToLogin();
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            game.displayGameWindow();
        } else if(Integer.parseInt(recv.substring(passed, willPass)) == Constants.ROOM_FULL) {
            lobby.window.print.setText("Mistnost je plna");
        } else {
            System.out.println("Server neudelil pristup.");
            if (increaseBadCounter()) {
                takeAction(MessageType.RECONNECT);
            } else {
                backToLogin();
            }
        }
    }

    /**
     * Zvaliduje spravnost hlavicky zpravy.
     * @param recv Zpráva.
     */
    private boolean checkHead(String recv) {
        int passed = 0;
        int willPass = Constants.HEAD.length();
        if (Constants.HEAD.compareTo(recv.substring(passed, willPass)) != 0) {
            System.out.println("Server odpovedel spatnou hlavickou.");
            return false;
        }
        return true;
    }

    /**
     * Zjisti, jestli se jedna o jednu z zpravu, ktera je odpoved na inicativu klienta.
     * @param message Kontrolovana zprava.
     * @return True - je danna zprava, jinak false.
     */
    public boolean isNextStepMessage(String message) {
        if(message.length() < Constants.MINIMAL_LENGTH) {
            return false;
        }
        String substring = message.substring(Constants.HEAD.length() + Constants.LONG_INT_FORMAT_LENGTH, Constants.HEAD.length() + Constants.LONG_INT_FORMAT_LENGTH + MessageType.LOGIN.toString().length());
        return (substring.compareTo(MessageType.LOGIN.toString()) == 0 || substring.compareTo(MessageType.JOIN.toString()) == 0 || substring.compareTo(MessageType.MOVE.toString()) == 0 || substring.compareTo(MessageType.KILL.toString()) == 0 || substring.compareTo(MessageType.EATFOOD.toString()) == 0 || substring.compareTo(MessageType.DISCONNECT.toString()) == 0);
    }

    /**
     * Zjisti, jestli se jedna o jednu z zpravu aktualizace stavu.
     * @param message Kontrolovana zprava.
     * @return True - je aktualizujici zprava, jinak false.
     */
    public boolean isUpdateMessage(String message) {
        if(message.length() < Constants.MINIMAL_LENGTH) {
            return false;
        }
        String substring = message.substring(Constants.HEAD.length() + Constants.LONG_INT_FORMAT_LENGTH, Constants.HEAD.length() + Constants.LONG_INT_FORMAT_LENGTH + MessageType.LOBBY.toString().length());
        return (substring.compareTo(MessageType.LOBBY.toString()) == 0 || substring.compareTo(MessageType.ROOM.toString()) == 0 || substring.compareTo(MessageType.BACKTOLOBBY.toString()) == 0);
    }

    /**
     * Zjisit, jestli je zprava Ping.
     * @param message Kontrolovana zprava.
     * @return True - je ping, jinak false.
     */
    public boolean isPingMessage(String message) {
        if(message.length() < Constants.MINIMAL_LENGTH) {
            return false;
        }
        String substring = message.substring(Constants.HEAD.length() + Constants.LONG_INT_FORMAT_LENGTH, Constants.HEAD.length() + Constants.LONG_INT_FORMAT_LENGTH + MessageType.PING.toString().length());
        return (substring.compareTo(MessageType.PING.toString()) == 0);
    }

    /**
     * Pouziva se pri nepovedene zprave.
     * @param type Typ zpravy, ktera se posle a nasledne provede.
     */
    public void takeAction(MessageType type) {
        if(type == MessageType.LOGIN) {
            tryToLogin();
        } else if(type == MessageType.JOIN) {
            joinRoom();
        } else if(type == MessageType.MOVE) {
            move();
        } else if(type == MessageType.KILL) {
            killEnemy();
        } else if(type == MessageType.EATFOOD) {
            eatFood();
        } else if(type == MessageType.DISCONNECT) {
            askForClose();
        } else if(type == MessageType.BACKTOLOBBY) {
            wonToLobby();
        } else if(type == MessageType.RECONNECT) {
            reconnect();
        }
    }

    public boolean serverIsOnline() {

        return connection.isOpen();
    }

    /**
     * Prevede cele cislo do dvojciferneho formatu, vhodneho pro komunikaci se serverem, napr.: 01.
     * @param number Cislo, prevadene do daneho formatu.
     * @return String cisla v danem formatu.
     */
    public String formatIntNumber(int number) {
        return String.format("%02d", number);
    }

    /**
     * Prevede cele cislo do ctyrciferneho formatu, vhodneho pro komunikaci se serverem, napr.: 0001.
     * @param number Cislo, prevadene do daneho formatu.
     * @return String cisla v danem formatu.
     */
    private String formatLongIntNumber(int number) {
        return String.format("%04d", number);
    }

    /**
     * Prevede realne cislo do formatu, vhodneho pro komunikaci se serverem, napr.: 0001.01.
     * @param number Cislo, prevadene do daneho formatu.
     * @return String cisla v danem formatu.
     */
    private String formatDoubleNumber(double number) {
        return String.format(Locale.US, "%07.2f", number);
    }

    /**
     * Vytvori fronty.
     */
    private void setQueues() {
        nextStep = new MyQueue();
        update = new MyQueue();
        ping = new MyQueue();
    }

    /**
     * Vytvori vlakna.
     */
    private void setThreads() {
        /*
         * Vlakno, prijimajici zpravy.
         */
        RecieveThread ct = new RecieveThread(this);
        recieveThread = ct.handleRecieveThread();
        recieveThread.start();

        /*
         * Vlakno, naslouchajici serveru.
         */
        UpdateThread ut = new UpdateThread(this);
        updateThread = ut.handleUpdateThread();
        updateThread.start();

        setPingThread();
    }

    /**
     * Vlakno, pro pislani ping zpravy.
     */
    public void setPingThread() {
        PingThread pt = new PingThread(this);
        pingThread = pt.handlePingThread();
        pingThread.start();
    }

    public void setAddress(String host, int port) {
        address = new InetSocketAddress(host, port);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getNumOfRooms() {
        return numOfRooms;
    }

    public void setMyRoom(int myRoom) {
        this.myRoom = myRoom;
    }

    public int getMyID() {
        return myID;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public MyQueue getUpdate() {
        return update;
    }

    public MyQueue getPing() {
        return ping;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public void setLastEatenFeed(int lastEatenFeed) {
        this.lastEatenFeed = lastEatenFeed;
    }

    public void setLastEnemyKilled(int lastEnemyKilled) {
        this.lastEnemyKilled = lastEnemyKilled;
    }

    public MyQueue getNextStep() {
        return nextStep;
    }

    public Connection getConnection() {
        return connection;
    }
}
