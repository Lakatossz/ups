package game.enemy;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/** Hrac */
public class EnemyPlayer {

    /** ID nepritele. */
    private final int id;

    /** Pozice objektu na mape */
    private double xPosition, yPosition;

    /** Polomer objektu */
    private double radius;

    /** Barva objektu */
    private Color color;

    /** Prezdivka */
    private final String nickname;

    /** Stav pripojeni protihrace (TRUE - pripojen, FALSE - docasne nedostupny). */
    private boolean connState;

    /** Konstruktor */
    public EnemyPlayer(int id, double xPosition, double yPosition, double radius, Color color, String nickname, boolean connState) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.radius = radius;
        this.color = color;
        this.id = id;
        this.nickname = nickname;
        this.connState = connState;
    }

    /** Aktualizuje pozici nepritele. */
    public void move(double xPosition, double yPosition, double radius, boolean connState) {
        setXPosition(xPosition);
        setYPosition(yPosition);
        setRadius(radius);
        setConnState(connState);
    }

    /**
     * Zobrazi hrace
     */
    public void paintEnemy(Graphics2D g) {
        g.setColor(getColor());

        Ellipse2D.Double shape = new Ellipse2D.Double(getXPosition(), getYPosition(), 2 * getRadius(), 2 * getRadius());
        g.fill(shape);

        if(connState) {
            g.setColor(Color.black);
        } else {
            g.setColor(Color.RED);
        }
        g.setFont(new Font("sansserif", Font.BOLD, 20));
        g.drawString(nickname, (float) (getXPosition()), (float) (getYPosition() + getRadius()));
    }

    public int getId() {
        return id;
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

    public void setConnState(boolean connState) {
        this.connState = connState;
    }
}
