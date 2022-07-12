package login;

import client.state.State;
import constants.Constants;

import javax.swing.*;
import java.awt.*;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Trida, zajistujici zobrazeni prvniho okna aplikace.
 */
public class LoginWindow extends Canvas {

    /**
     * Hlavni frame okna Login.
     */
    public final JFrame frame;

    /**
     * Label s nazvem hry.
     */
    public JLabel gameLabel;

    /**
     * Pole pro zadani prezdivky hrace.
     */
    public JTextField nickField;

    /**
     * Pole pro zadani IP nebo hostname serveru.
     */
    public JTextField ipField;

    /**
     * Pole pro zadani portu na kterem server posloucha.
     */
    public JTextField portField;

    /**
     * Tlacitko pro pripojeni na server.
     */
    public JButton connect;

    /**
     * Tlacitko pro opusteni okna login (Okno lze taktez zavrit cervenym krizkem.).
     */
    public JButton exit;

    /**
     * Label, ktery slouzi pro vypis zprav uzivateli (Na zacatku je prazdny.).
     */
    public JLabel print;

    /**
     * Vytvori nove okno Loginu s polemi pro zadani prezdivky, ip (hostname) a
     * portu serveru. Pokud se neco nezdari, hlaseni je vypsano pomoci labelu print.
     * @param login Trida, zajistujici beh loginu.
     */
    public LoginWindow(Login login) {
        frame = new JFrame("Login");

        gameLabel = new JLabel("Moje hra", JLabel.CENTER);

        nickField = new JTextField(login.getNickname());
        ipField = new JTextField("147.228.67.111"); // pro debug 147.228.63.10
        portField = new JTextField("10001"); // pro debug
        connect = new JButton("CONNECT");
        exit = new JButton("CLOSE");

        print = new JLabel("", JLabel.CENTER);

//      pozice widgetu
        gameLabel.setBounds(Constants.LOGIN_WIDTH / 4, 20, Constants.LOGIN_WIDTH / 2, 100);
        gameLabel.setFont(new Font("sansserif", Font.BOLD, 70));

        nickField.setBounds(Constants.LOGIN_WIDTH / 4, 2 * Constants.LOGIN_HEIGHT/5 - 40, Constants.LOGIN_WIDTH / 2, 30);
        nickField.setHorizontalAlignment(JTextField.CENTER);
        ipField.setBounds(Constants.LOGIN_WIDTH / 4, 2 * Constants.LOGIN_HEIGHT/5 + 20, Constants.LOGIN_WIDTH / 2, 30);
        ipField.setHorizontalAlignment(JTextField.CENTER);
        portField.setBounds(Constants.LOGIN_WIDTH / 4, 2 * Constants.LOGIN_HEIGHT/5 + 80, Constants.LOGIN_WIDTH / 2, 30);
        portField.setHorizontalAlignment(JTextField.CENTER);
        connect.setBounds(Constants.LOGIN_WIDTH / 4, 3 * Constants.LOGIN_HEIGHT / 4 - 40, 100, 40);
        exit.setBounds(3 * Constants.LOGIN_WIDTH / 4 - 100, 3 * Constants.LOGIN_HEIGHT / 4 - 40, 100, 40);
        print.setBounds(Constants.LOGIN_WIDTH / 4,  3 * Constants.LOGIN_HEIGHT / 4, Constants.LOGIN_WIDTH / 2, 50);
        print.setFont(new Font("sansserif", Font.BOLD, 20));
        print.setForeground(Color.red);

//      funkce tlacitek
        exit.addActionListener(e-> System.exit(0));
        connect.addActionListener(e -> {
//          prezdivka
            if(nickField.getText().length() <= Constants.MIN_NICK_LENGTH) {
                print.setText("Kratky nickname ");
                return;
            } else if(nickField.getText().length() > Constants.MAX_NICK_LENGTH) {
                print.setText("Dlouhy nickname ");
                return;
            } else if (nickField.getText().isBlank()) {
                print.setText(" Nick bez znaku ");
                return;
            } else {
                login.setNickname(nickField.getText());
            }

//          ip (hostname)
            try {
                InetAddress address = InetAddress.getByName(ipField.getText());

                if(!(address instanceof Inet4Address)) {
                    print.setText("  Nevalidni ip  ");
                    return;
                }

                if(!address.isReachable(2)) {
                    print.setText("  Nedostupna ip ");
                }

                login.setHost(ipField.getText());
            } catch (Exception hex) {
                print.setText(" Neplatny host  ");
                return;
            }

//          port
            if(Login.isNumber(portField.getText())) {
                if(Integer.parseInt(portField.getText()) < Constants.MAX_PORT && Integer.parseInt(portField.getText()) > Constants.MIN_PORT) {
                    login.setPort(Integer.parseInt(portField.getText()));
                } else {
                    print.setText(" Neplatny port  ");
                    return;
                }
            } else {
                print.setText("Port neni cislo ");
                return;
            }

//          Pokus o pripojeni na server.
            login.connect();
//          Pripojeni se podarilo.
            if(login.client.getState() == State.LOBBY) {
                login.closeLoginWindow();
            } else {
                print.setText("Neplatny server ");
            }
        });

        frame.add(gameLabel);
        frame.add(nickField);
        frame.add(ipField);
        frame.add(portField);
        frame.add(exit);
        frame.add(connect);
        frame.add(print);

        frame.setSize(Constants.LOGIN_WIDTH, Constants.LOGIN_HEIGHT);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setVisible(true);
    }
}
