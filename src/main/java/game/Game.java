package game;

import client.Client;
import client.state.State;
import game.enemy.EnemyHandler;
import game.enemy.EnemyPlayer;
import game.feed.Feed;
import game.feed.FeedHandler;
import game.player.KeyInput;
import game.player.Player;
import constants.Constants;

import javax.swing.*;
import java.awt.*;

public class Game extends JPanel implements Runnable {

    /**
     * Pole barev, ktere muze hrac nebo zradlo nabyvat (Pridelene serverem.).
     */
    private final Color[] colors = {Color.red, Color.orange, Color.yellow, Color.green, Color.blue, Color.cyan};


    /**
     * Vlakno pro beh hry.
     */
    Thread thread;

    /**
     * True pokud hra bezi, false jinak.
     */
    boolean running = false;

    /**
     * Objekt hrace.
     */
    private final Player player;

    /**
     * Stara se o nepratele -> render, atd.
     */
    public final EnemyHandler enemyHandler;

    /**
     * Stara se o vse kolem jidla -> render, respawn, atd.
     */
    public final FeedHandler feedHandler;

    /**
     * Cas cooldownu po pripojeni hrace do roomky.
     */
    private int cooldownTime;

    /**
     * Promenna pro ukladani posledni ho casu komunikace se serverem.
     */
    private long lastTime;

    /**
     * Objekt klient.
     */
    public Client client;

    /**
     * Okno hry.
     */
    public GameWindow window;

    /**
     * Casove promenne pro posilani zprav o pohybu.
     */
    private long lastMoveTime, thisMoveTime;

    /**
     * Pokud je true - je zobrazena hlaska o nedostupnosti serveru.
     */
    private boolean printError = false;

    /**
     * Konstruktor objektu hra.
     */
    public Game(Client client) {
        this.client = client;
        enemyHandler = new EnemyHandler();
        feedHandler = new FeedHandler();
        player = new Player(0, 0, 0, Color.white, client.getNickname());
        this.addKeyListener(new KeyInput(this));
        this.setFocusable(true);
    }

    /**
     * Zobrazi okno hry a prepne stav na State.GAME.
     */
    public void displayGameWindow() {
        if(!running) {
            client.setState(State.GAME);
            window = new GameWindow(this);
        }
    }

    /**
     * Zavre okno hry.
     */
    public void closeGameWindow() {
        if(running) {
            running = false;
            System.out.println("Zaviram okno hry.");
            window.frame.dispose();
        }
    }

    /**
     * Smaze vse podstatne ve hre.
     */
    public void resetGame() {
        feedHandler.deleteFeeds();
        enemyHandler.deleteEnemyPlayers();
        player.resetPlayer();
    }

    /**
     * Aktualizuje pozici objektu -> hrace
     */
    private void tick() {
        player.tick();
        if((Math.abs(player.getXVel()) > 0 || Math.abs(player.getYVel()) > 0)) {
            if(player.getXPosition() > 0 && player.getXPosition() < Constants.WIDTH && player.getYPosition() > 0 && player.getYPosition() < Constants.HEIGHT) {
                thisMoveTime = System.currentTimeMillis();
                if(thisMoveTime - lastMoveTime > Constants.SEND_MOVE) {
                    if(client.serverIsOnline() && client.getState() == State.GAME) {
                        client.move();
                        lastMoveTime = thisMoveTime;
                    }
                }
            }
        }
    }

    /**
     * Provede prekresleni JPanelu.
     */
    public void paintComponent(Graphics g) {
        paintGame((Graphics2D) g);
    }

    /**
     * Provede prekresleni hry.
     */
    private void paintGame(Graphics2D g) {
        showBackground(g);
        feedHandler.paintFeeds(g);
        enemyHandler.paintEnemies(g);
        player.paintPlayer(g);
        showScore(g);
        showCooldown(g);
        if(printError) {
            showError(g);
        }
    }

    /**
     * Zobtrai pozadi a vycentruje obraz.
     */
    private void showBackground(Graphics2D g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        g.translate((double) Constants.GAME_WIDTH/2 - player.getXPosition() - player.getRadius(), (double) Constants.GAME_HEIGHT/2 - player.getYPosition() - player.getRadius());
    }

    /**
     * Zobrazi nedostupnost serveru (pokud je nedostupny).
     */
    private void showError(Graphics2D g) {
        g.setColor(Color.RED);
        g.setFont(new Font("sansserif", Font.BOLD, 60));
        g.drawString("SERVER IS NOT RESPONDING", (float) (player.getXPosition() + player.getRadius()) - 450, (float) (player.getYPosition() + player.getRadius()) + 200);
    }

    /**
     * Zobrazi v levem hornim rohu skore.
     */
    private void showScore(Graphics2D g) {
        g.setColor(Color.black);
        g.setFont(new Font("sansserif", Font.BOLD, 15));
//        g.drawString(player.getNickname() + " score: " + player.getScore(), (int) (player.getXPosition() + player.getRadius()) - (Constants.GAME_WIDTH / 2 - 50), (int) (player.getYPosition() + player.getRadius()) - (Constants.GAME_HEIGHT / 2 - 30));
        g.drawString(player.getNickname() + " score: " + player.getScore(), (float)(player.getXPosition() + player.getRadius() - (Constants.GAME_WIDTH / 2 - 50)), (float)((player.getYPosition() + player.getRadius()) - (Constants.GAME_HEIGHT / 2 - 30)));
    }

    /**
     * Zobrazi uprostred odpocet cooldownu (pokud je vetsi nez nula).
     */
    private void showCooldown(Graphics2D g) {
        if(cooldownTime > 0) {
            g.setColor(Color.black);
            g.setFont(new Font("sansserif", Font.BOLD, 60));
            g.drawString("Cooldown:" + cooldownTime, (float) (player.getXPosition() + player.getRadius()) - 160, (float) (player.getYPosition() + player.getRadius()) - 200);
        }
    }

    /**
     * Odecita cooldown.
     */
    private void countCooldown() {
        long thisTime = System.currentTimeMillis();
        if(thisTime - lastTime > 1000) {
            lastTime = thisTime;
            cooldownTime--;
        }
    }

    /**
     * Pokud se hrac dotkne jidla, sni ho -> jidlo zmizi a hrac se zvetsi o velikost jidla
     */
    private void eatFeed() {
        Feed feed;

        for(int index = 0; index < feedHandler.feeds.size(); index++) {
            try {
                feed = feedHandler.feeds.elementAt(index);
                if(distance(player.getXPosition() + player.getRadius(), player.getYPosition() + player.getRadius(), feed.getXPosition() + feed.getRadius(), feed.getYPosition() + feed.getRadius()) < (float)(player.getRadius())) {
                    feedHandler.eatFeed(index);     // odebere ho ze seznamu
                    client.setLastEatenFeed(index);   // zaznamena jeho index
                    client.eatFood();               // posle zpravu
                    player.eat(feed);               // zvetsi hrace
                }
            } catch (InterruptedException e) {
                System.out.println("Neslo vybrat jidlo.");
            }
        }
    }

    /**
     * Predstavuje souboj, tj. kdyz se hrac potka s nekterym z nepratel.
     */
    private void fightEnemy() {
        EnemyPlayer enemy;

        if(!(cooldownTime > 0)) {
            for(int index = 0; index < enemyHandler.enemyPlayers.size(); index++) {
                try {
                    enemy = enemyHandler.enemyPlayers.elementAt(index);

                    if(distance(player.getXPosition() + player.getRadius(), player.getYPosition() + player.getRadius(), enemy.getXPosition() + enemy.getRadius(), enemy.getYPosition() + enemy.getRadius()) < Constants.FIGHT_CONSTANT * player.getRadius() + enemy.getRadius()) {
                        if(player.getRadius() > enemy.getRadius()) {
                            client.setLastEnemyKilled(enemy.getId());
                            client.killEnemy();

                            if(enemyHandler.enemyPlayers.size() == 1) {
                                client.wonToLobby();
                            }
                            enemyHandler.removeEnemy(enemy);
                            player.kill(enemy);
                        } else if(enemy.getRadius() > player.getRadius()) {
                            System.out.println("Umrel jsem.");
                            client.lostToLobby();
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Neslo vybrat nepritele.");
                }
            }
        }
    }

    /**
     * Vypocita euklidovskou vzdalenost dvou bodu.
     */
    public double distance(double x1, double y1, double x2, double y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 -y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Zacatek vlakna -> hry
     */
    public void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    /**
     * Konec vlakna -> hry
     */
    public void stop() {
        try {
            thread.join();
            running = false;
        } catch(Exception e) {
            System.out.println("Chyba pri zavirani herniho vlakna.");
        }
    }

    /**
     * Cyklus hry
     */
    public void run() {

        while(running) {
            if(cooldownTime > 0) {
                countCooldown();
            }
            eatFeed();
            if(!(cooldownTime > 0)) {
                fightEnemy();
            }
            tick();
            repaint();
            try {
                Thread.sleep(Constants.GAME_SLEEP);
            } catch (InterruptedException e) {
                System.out.println("Chyba pri uspani herniho vlakna.");
            }
        }
        stop();
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public Color[] getColors() {
        return colors;
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public void setPrintError(boolean printError) {
        this.printError = printError;
    }
}