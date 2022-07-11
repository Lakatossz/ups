package game.player;

import constants.Constants;
import game.Game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Stara se o vstup z klavesy pro pohyb hrace po mape
 */
public class KeyInput extends KeyAdapter {

    /** Hrac, ktery se bude pohybovat. */
    private final Player player;

    /** Konstruktor */
    public KeyInput(Game game) {
        this.player = game.getPlayer();
    }

    /** Zmacknuti tlacitek vyvola pohyb -> W - nahoru, A - doleva, S - dolu, D - doprava */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_W) {
            if((player.getYPosition() - Constants.SAFE_ZONE * Constants.VELOCITY_CONSTANT) > 0) {
                player.setYVel(-Constants.VELOCITY_CONSTANT);
            } else {
                player.setYVel(0);
            }
        } else if(key == KeyEvent.VK_A) {
            if((player.getXPosition() - Constants.SAFE_ZONE * Constants.VELOCITY_CONSTANT) > 0) {
                player.setXVel(-Constants.VELOCITY_CONSTANT);
            } else {
                player.setXVel(0);
            }
        } else if(key == KeyEvent.VK_S) {
            if((player.getYPosition() + Constants.SAFE_ZONE * Constants.VELOCITY_CONSTANT) < Constants.HEIGHT) {
                player.setYVel(Constants.VELOCITY_CONSTANT);
            } else {
                player.setYVel(0);
            }
        } else if(key == KeyEvent.VK_D) {
            if((player.getXPosition() + Constants.SAFE_ZONE * Constants.VELOCITY_CONSTANT) < Constants.WIDTH) {
                player.setXVel(Constants.VELOCITY_CONSTANT);
            } else {
                player.setXVel(0);
            }
        }
    }

    /** Uvolneni tlacitek zastavi pohyb */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_W :
                player.setYVel(0);
                break;
            case KeyEvent.VK_S :
                player.setYVel(0);
                break;
            case KeyEvent.VK_A :
                player.setXVel(0);
                break;
            case KeyEvent.VK_D :
                player.setXVel(0);
                break;
        }
    }
}
