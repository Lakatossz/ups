package game.feed;

import constants.Constants;
import game.vector.MyVector;

import java.awt.*;

/**
 * Stara se o metody volane nad jidlem.
 */
public class FeedHandler {

    /** Vektor jidel. */
    public MyVector<Feed> feeds = new MyVector<>();

    private boolean inUse = false;

    /**
     * Vykresli jidlo.
     */
    public synchronized void paintFeeds(Graphics2D g) {

        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na vykreselni potravy.");
            }
        }

        inUse = true;

        for(int i = 0; i < feeds.size(); ++i) {
            try {
                feeds.elementAt(i).paintFeed(g);
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na vykreslovani potravy.");
            }
        }
        inUse = false;
        notify();
    }

    /**
     * Prida jidlo do vektoru.
     */
    public synchronized void addFeed(Feed feed) {
        while (inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na pridavani potravy.");
            }
        }

        inUse = true;

        feeds.add(feed);

        inUse = false;
        notify();
    }

    /**
     * Odebere jidlo z vektoru na pozici index.
     */
    public synchronized void eatFeed(int index) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na pojidani potravy.");
            }
        }

        inUse = true;

        try {
            this.feeds.remove(index);
        } catch (InterruptedException e) {
            System.out.println("Chyba pri sezrani potravy.");
        }
        this.feeds.addToIndex(index, new Feed(0, -1, -1, 0, Color.white));

        inUse = false;
        notify();
    }

    /**
     * Zmeni pozici potravy na dane parametry.
     * @param index Id potravy.
     * @param xPos X-ova souradnice.
     * @param yPos Y-ova souradnice.
     * @param radius Polomer.
     * @param color Barva.
     */
    public synchronized void respawnFeed(int index, double xPos, double yPos, double radius, Color color) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na respawn jidla.");
            }
        }

        inUse = true;

        try {
            feeds.elementAt(index).setXPosition(xPos);
            feeds.elementAt(index).setYPosition(yPos);
            feeds.elementAt(index).setRadius(radius);
            feeds.elementAt(index).setColor(color);
        } catch (InterruptedException e) {
            System.out.println("Chyba pri respawnovani potravy.");
        }

        inUse = false;
        notify();
    }

    /**
     * Odstrani vsechnu potravu z vektoru.
     */
    public synchronized void deleteFeeds() {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na odstraneni jidla.");
            }
        }

        inUse = true;

        try {
            feeds.removeAllElements();
        } catch (InterruptedException e) {
            System.out.println("Chyba pri odstranovani potravy.");
        }

        inUse = false;
        notify();
    }
}
