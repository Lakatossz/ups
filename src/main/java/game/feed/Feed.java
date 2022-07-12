package game.feed;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Feed {

    /**
     * ID potravy.
     */
    int id;

    /** Pozice objektu na mape */
    private double xPosition, yPosition;

    /** Polomer objektu */
    private double radius;

    /** Barva objektu */
    private Color color;

    /**
     * Vytvori novou potravu.
     */
    public Feed(int id, double xPosition, double yPosition, double radius, Color color) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.radius = radius;
        this.color = color;
        this.id = id;
    }


    /**
     * Zobrazi jidlo.
     */
    public void paintFeed(Graphics2D g) {
        g.setColor(getColor());

        Ellipse2D.Double shape = new Ellipse2D.Double(getXPosition(), getYPosition(), 2 * getRadius(), 2 * getRadius());
        g.draw(shape);
        g.fill(shape);
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
