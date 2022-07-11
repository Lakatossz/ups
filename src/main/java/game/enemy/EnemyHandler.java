package game.enemy;

import constants.Constants;
import game.vector.MyVector;
import java.awt.*;

/**
 * Stara se o metody volane nad neprately.
 */
public class EnemyHandler {

    /** Seznam nepratel. */
    public MyVector<EnemyPlayer> enemyPlayers = new MyVector<>();

    private boolean inUse = false;

    /**
     * Vykresli nepratela.
     */
    public synchronized void paintEnemies(Graphics2D g) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na vykreslovani nepratel.");
            }
        }

        inUse = true;

        for(int i = 0; i < enemyPlayers.size(); ++i) {
            try {
                enemyPlayers.elementAt(i).paintEnemy(g);
            } catch (InterruptedException e) {
                System.out.println("Chyba pri renderovani nepratel.");
            }
        }

        inUse = false;
        notify();
    }

    /**
     * Zpracuje informace o nepriteli, pokud ho zna,
     * aktualizuje jeho stav, jinak ho prida do vektoru.
     */
    public synchronized void handleEnemyPlayer(int id, double xPosition, double yPosition, double radius, Color color, String nickname, boolean connState) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na zpracovani udaju o nepriteli.");
            }
        }

        inUse = true;

        for(int i = 0; i < enemyPlayers.size(); ++i) {
            try {
                if(enemyPlayers.elementAt(i).getId() == id) {
                    enemyPlayers.elementAt(i).move(xPosition, yPosition, radius, connState);
                    inUse = false;
                    notify();
                    return;
                }
            } catch (InterruptedException e) {
                System.out.println("Chyba pri zpracovani nepratel.");
            }
        }
        this.enemyPlayers.add(new EnemyPlayer(id, xPosition, yPosition, radius, color, nickname, connState));
        System.out.println("Pridal jsem nepritele.");
        System.out.println("Pocet nepratel: " + enemyPlayers.size());

        inUse = false;
        notify();
    }

    /**
     * Odebere nepritele ze seznamu.
     */
    public synchronized void removeEnemy(EnemyPlayer player) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na odstraneni nepritele.");
            }
        }

        inUse = true;

        for(int i = 0; i < enemyPlayers.size(); ++i) {
            try {
                if(enemyPlayers.elementAt(i) == player) {
                    enemyPlayers.remove(i);
                }
            } catch (InterruptedException e) {
                System.out.println("Chyba pri odstranovani nepritele ze seznamu.");
            }
        }

        inUse = false;
        notify();
    }

    /**
     * Odstrani vsechny nepratele.
     */
    public synchronized void deleteEnemyPlayers() {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na odstranovani nepratel.");
            }
        }

        inUse = true;

        try {
            enemyPlayers.removeAllElements();
        } catch (InterruptedException e) {
            System.out.println("Chyba pri odstranovani vsech nepratel.");
        }

        inUse = false;
        notify();
    }
}

