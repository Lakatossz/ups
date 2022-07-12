package game.player;

import constants.Constants;
import game.enemy.EnemyPlayer;
import game.feed.Feed;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/** Hrac */
public class Player {

    public final int WIDTH = 2000, HEIGHT = 2000;

    /** Pozice objektu na mape */
    private double xPosition, yPosition;

    /** Polomer objektu */
    private double radius;

    /** Barva objektu */
    private Color color;

    /** Smery pohybu. */
    private double xVel, yVel;

    /** Prezdivka. */
    public String nickname;

    /** Skore -> hrac zacina se score 10. */
    int score;

    private boolean inUse = false;

    /** Konstruktor. */
    public Player(double xPosition, double yPosition, double radius, Color color, String nickname) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.radius = radius;
        this.color = color;
        this.xVel = 0;
        this.yVel = 0;
        this.nickname = nickname;
        this.score = 10;
    }

    public synchronized void setPlayer(double xPosition, double yPosition, double radius, Color color) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na nastaveni parametru hrace.");
            }
        }

        inUse = true;

        setXPosition(xPosition);
        setYPosition(yPosition);
        setRadius(radius);
        setColor(color);
        this.xVel = 0;
        this.yVel = 0;

        inUse = false;
        notify();
    }

    public synchronized void resetPlayer() {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na reset stavu hrace.");
            }
        }

        inUse = true;

        this.xVel = 0;
        this.yVel = 0;
        this.score = 10;

        inUse = false;
        notify();
    }

    /**
     * Aktualizuje pozici hrace.
     */
    public synchronized void tick() {
        while (inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na tick hrace.");
            }
        }

        inUse = true;

        if((getXPosition() - Constants.SAFE_ZONE * xVel) < 0 && (xVel < 0) || (getXPosition() + Constants.SAFE_ZONE * xVel) > WIDTH && (xVel > 0)) {
            xVel = 0;
        } else {
            setXPosition(getXPosition() + getXVel());
        }

        if((getYPosition() - Constants.SAFE_ZONE * yVel) < 0 && (yVel < 0) || (getYPosition() + Constants.SAFE_ZONE * yVel) > HEIGHT && (yVel > 0)) {
            yVel = 0;
        } else {
            setYPosition(getYPosition() + getYVel());
        }

        inUse = false;
        notify();
    }

    /**
     * Zobrazi hrace
     */
    public synchronized void paintPlayer(Graphics2D g) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na vykresleni hrace.");
            }
        }

        inUse = true;

        g.setColor(getColor());

        Ellipse2D.Double shape = new Ellipse2D.Double(getXPosition(), getYPosition(), 2 * getRadius(), 2 * getRadius());
        g.fill(shape);

        g.setColor(Color.black);
        g.setFont(new Font("sansserif", Font.BOLD, 20));
        g.drawString(nickname, (float) (getXPosition()), (float) (getYPosition() + getRadius()));

        inUse = false;
        notify();
    }

    /**
     * Zvetsi hrace o snedene jidlo.
     */
    public synchronized void eat(Feed feed) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na sezrani potravy.");
            }
        }

        inUse = true;

        double sum = Math.PI * this.getRadius() * this.getRadius() + Math.PI * feed.getRadius() * feed.getRadius();
        setRadius(Math.sqrt(sum / Math.PI));
        score++;

        inUse = false;
        notify();
    }

    /**
     * Hrac zabil jineho hrace.
     */
    public synchronized void kill(EnemyPlayer enemy) {
        while(inUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Chyba pri cekani na zabiti nepritele.");
            }
        }

        inUse = true;

        setRadius(getRadius() + enemy.getRadius());

        inUse = false;
        notify();
    }

    public void setXVel(double xVel) {
        this.xVel = xVel;
    }

    public double getXVel() {
        return this.xVel;
    }

    public void setYVel(double yVel) {
        this.yVel = yVel;
    }

    public double getYVel() {
        return this.yVel;
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return score;
    }

    public double getXPosition() {
        return xPosition;
    }

    public void setXPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public double getYPosition() {
        return yPosition;
    }

    public void setYPosition(double yPosition) {
        this.yPosition = yPosition;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
