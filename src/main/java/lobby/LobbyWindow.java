package lobby;

import client.state.State;
import constants.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LobbyWindow extends Canvas {

    /**
     * Hlavni frame okna lobby.
     */
    public final JFrame frame;

    /**
     * Hlavni label v lobby.
     */
    public JLabel lobbyLabel;

    /**
     * Pole labelu mistnosti.
     */
    public JLabel[] rooms;

    /**
     * Pole tlacitek pro vstup do mistnosti.
     */
    public JButton[] buttons;

    /**
     * Tlacitko na zavreni okna a odpojeni ze serveru.
     */
    public JButton disconnect;

    /**
     * Label, ktery slouzi pro vypis zprav uzivateli (Na zacatku je prazdny.).
     */
    public JLabel print;

    /**
     * Vytvori nove okno Lobby s tlacitky pro vstup do jednotlivych mistnosti.
     * Pokud se neco nezdari, hlaseni je vypsano pomoci labelu print.
     * @param lobby Trida, zajistujici beh lobby.
     */
    public LobbyWindow(Lobby lobby, String string) {
        frame = new JFrame("Lobby");

        lobbyLabel = new JLabel(string, JLabel.CENTER);

        rooms = new JLabel[lobby.getNumOfRooms()];
        buttons = new JButton[lobby.getNumOfRooms()];

        print = new JLabel("", JLabel.CENTER);

        lobbyLabel.setBounds(0, 100, Constants.LOBBY_WIDTH, 100);
        lobbyLabel.setFont(new Font("sansserif", Font.BOLD, 60));

        for(int i = 0; i < lobby.getNumOfRooms(); i++)
        {
            rooms[i] = new JLabel("ROOM " + (i + 1), JLabel.CENTER);
            buttons[i] = new JButton("JOIN");
            rooms[i].setBounds( 2 * i * Constants.LOBBY_WIDTH / 8 + 30,  Constants.LOBBY_HEIGHT / 2, 100, 50);
            buttons[i].setBounds(2 * i * Constants.LOBBY_WIDTH / 8 + 30, Constants.LOBBY_HEIGHT / 2 + 50, 100, 50);
            frame.add(rooms[i]);
            frame.add(buttons[i]);
        }

        disconnect = new JButton("LEAVE LOBBY");

        print.setBounds(Constants.LOBBY_WIDTH / 4, 2 * Constants.LOBBY_HEIGHT / 5, Constants.LOBBY_WIDTH / 2, 50);
        print.setFont(new Font("sansserif", Font.BOLD, 20));
        print.setForeground(Color.red);

        disconnect.setBounds(Constants.LOBBY_WIDTH / 2 - 100, 3 * Constants.LOBBY_HEIGHT / 4, 200, 50);

        frame.add(lobbyLabel);

        for(int i = 0; i < lobby.getNumOfRooms(); i++)
        {
            int finalI = i;
            buttons[i].addActionListener(e -> {
                try {
                    lobby.joinRoom(finalI + 1);
                } catch (Exception con_e) {
                    con_e.printStackTrace();
                    print.setText(" Nelze pripojit ");
                    return;
                }
                if(lobby.client.getState() == State.GAME) {
                    lobby.closeLobbyWindow();
                }
            });
        }

        disconnect.addActionListener(e -> lobby.client.askForClose());

        frame.add(disconnect);
        frame.add(print);

        frame.setSize(Constants.LOBBY_WIDTH, Constants.LOBBY_HEIGHT);

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                lobby.client.askForClose();
            }
        });
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setVisible(true);
    }
}
