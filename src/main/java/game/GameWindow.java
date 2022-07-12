package game;

import constants.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *  Okno hry.
 */
public class GameWindow extends JPanel {

    /** Hlavni frame okna. */
    public final JFrame frame;

    /** Okno hry. */
    public GameWindow(Game game) {
        frame = new JFrame("Game");

        frame.setSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));

        frame.setBackground(Color.white);
        frame.getContentPane().add(game);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                game.client.askForClose();
            }
        });
        game.start();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
