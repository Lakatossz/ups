package client.threads;

import client.Client;
import client.messageType.MessageType;
import constants.Constants;

public class UpdateThread {

    /**
     * Klient nad kterym bude vlakno pracovat.
     */
    Client client;

    public UpdateThread(Client client) {
        this.client  = client;
    }

    /**
     * Metoda, vytvori nove vlakno a zajistuje logiku pro poslouchani zprav aktualizace stavu.
     * @return Vytvorene vlakno.
     */
    public Thread handleUpdateThread() {
        return new Thread(() -> {
            String message, recv;
            int passed, willPass, id, color_id, nickLength;
            double xPosition, yPosition, radius;
            String nickname;

            while(true) {
                recv = client.getUpdate().remove();
                if(recv == null) {
                    continue;
                }
                passed = 0;
                willPass = Constants.HEAD.length();
                if(Constants.HEAD.compareTo(recv.substring(passed,willPass)) != 0) {
                    System.out.println("Spatna hlavicka.");
                    if(!client.increaseBadCounter()) {
                        client.backToLogin();
                    }
                } else {
                    willPass += Constants.LONG_INT_FORMAT_LENGTH;
                    passed = willPass;
                    willPass += MessageType.LOBBY.toString().length();

                    if(MessageType.LOBBY.toString().compareTo(recv.substring(passed, willPass)) == 0) {
                        passed = willPass;
                        willPass += Constants.INT_FORMAT_LENGTH;

                        if(Integer.parseInt(recv.substring(passed,willPass)) == client.getMyID()) {
                            message = MessageType.LOBBY + client.formatIntNumber(client.getMyID()) + client.formatIntNumber(Constants.ROGER);
                            client.send(message);
                        } else {
                            System.out.println("Spatne id v lobby.");
                            if(!client.increaseBadCounter()) {
                                client.backToLogin();
                                break;
                            }
                        }
                    } else if(MessageType.ROOM.toString().compareTo(recv.substring(passed, willPass)) == 0) {
                        passed = willPass;
                        willPass += Constants.INT_FORMAT_LENGTH;

                        if(Integer.parseInt(recv.substring(passed,willPass)) == client.getMyID()) {
                            while (willPass < recv.length()) {
                                passed = willPass;
                                willPass += Constants.INT_FORMAT_LENGTH;

                                if ("pp".compareTo(recv.substring(passed, willPass)) == 0) {

                                    passed = willPass;
                                    willPass += Constants.INT_FORMAT_LENGTH;

                                    id = Integer.parseInt(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.INT_FORMAT_LENGTH;

                                    nickLength = Integer.parseInt(recv.substring(passed, willPass));

                                    if (nickLength <= Constants.MIN_NICK_LENGTH || nickLength > Constants.MAX_NICK_LENGTH) {
                                        System.out.println("Spatny nick nepritele.");
                                        if (!client.increaseBadCounter()) {
                                            client.backToLogin();
                                            break;
                                        }
                                    }

                                    passed = willPass;
                                    willPass += nickLength;

                                    nickname = recv.substring(passed, willPass);

                                    passed = willPass;
                                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.DOUBLE_FORMAT_LENGTH;

                                    radius = Double.parseDouble(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.INT_FORMAT_LENGTH;
                                    color_id = Integer.parseInt(recv.substring(passed, willPass));

                                    if (!nickname.isBlank() || xPosition < Constants.WIDTH || xPosition > 0 || yPosition < Constants.HEIGHT || yPosition > 0 || radius > 0 || radius < Constants.MAX_RADIUS || color_id < client.getGame().getColors().length) {

                                        passed = willPass;
                                        willPass += Constants.INT_FORMAT_LENGTH;
                                        if(Integer.parseInt(recv.substring(passed, willPass)) == 1) {
                                            client.getGame().enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, client.getGame().getColor(color_id), nickname, true);
                                        } else if(Integer.parseInt(recv.substring(passed, willPass)) == 0) {
                                            client.getGame().enemyHandler.handleEnemyPlayer(id, xPosition, yPosition, radius, client.getGame().getColor(color_id), nickname, false);
                                        } else {
                                            System.out.println("Spatny format stavu pripojeni nepritele.");
                                            if (!client.increaseBadCounter()) {
                                                client.backToLogin();
                                                break;
                                            }
                                        }
                                    } else {
                                        System.out.println("Spatne parametry nepritele v updatu.");
                                        if (!client.increaseBadCounter()) {
                                            client.backToLogin();
                                            break;
                                        }
                                    }
                                } else if ("ff".compareTo(recv.substring(passed, willPass)) == 0) {

                                    passed = willPass;
                                    willPass += Constants.INT_FORMAT_LENGTH;
                                    id = Integer.parseInt(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                                    xPosition = Double.parseDouble(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                                    yPosition = Double.parseDouble(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.DOUBLE_FORMAT_LENGTH;
                                    radius = Double.parseDouble(recv.substring(passed, willPass));

                                    passed = willPass;
                                    willPass += Constants.INT_FORMAT_LENGTH;
                                    color_id = Integer.parseInt(recv.substring(passed, willPass));

                                    if (xPosition <= Constants.WIDTH && xPosition > 0 && yPosition <= Constants.HEIGHT && yPosition > 0 && radius > 0 && radius < Constants.MAX_RADIUS && color_id < client.getGame().getColors().length && color_id >= 0) {
                                        /* Potrava je sezrana. */
                                        client.getGame().feedHandler.respawnFeed(id, xPosition, yPosition, radius, client.getGame().getColor(color_id));
                                    } else {
                                        System.out.println("Spatne parametry kulicek v updatu.");
                                        if (!client.increaseBadCounter()) {
                                            client.backToLogin();
                                            break;
                                        }
                                    }
                                }
                            }
                            message = MessageType.ROOM + client.formatIntNumber(client.getMyID()) + client.formatIntNumber(Constants.ROGER);
                            client.send(message);
                        } else {
                            System.out.println("Spatne id v update room.");
                            if(!client.increaseBadCounter()) {
                                client.backToLogin();
                                break;
                            }
                        }
                    } else if(MessageType.BACKTOLOBBY.toString().compareTo(recv.substring(passed, willPass)) == 0) {
                        passed = willPass;
                        willPass += Constants.INT_FORMAT_LENGTH;
                        if(Integer.parseInt(recv.substring(passed,willPass)) == client.getMyID()) {

                            passed = willPass;
                            willPass += Constants.INT_FORMAT_LENGTH;
                            if (Integer.parseInt(recv.substring(passed, willPass)) == Constants.WIN) {
                                message = MessageType.BACKTOLOBBY + client.formatIntNumber(client.getMyID()) + client.formatIntNumber(Constants.ROGER);
                                client.send(message);
                                client.wonToLobby();
                            } else if (Integer.parseInt(recv.substring(passed, willPass)) == Constants.LOSE) {
                                client.lostToLobby();
                                message = MessageType.BACKTOLOBBY + client.formatIntNumber(client.getMyID()) + client.formatIntNumber(Constants.ROGER);
                                client.send(message);
                            } else {
                                System.out.println("Server poslal spatne cislo pro vyhru.");
                                if (client.increaseBadCounter()) {
                                    message = MessageType.BACKTOLOBBY + client.formatIntNumber(client.getMyID()) + client.formatIntNumber(Constants.DENIAL);
                                    client.send(message);
                                } else {
                                    client.backToLogin();
                                    break;
                                }
                            }
                        }
                    } else {
                        System.out.println("Prisel jiny update, nez by mel.");
                        if (!client.increaseBadCounter()) {
                            client.backToLogin();
                            break;
                        }
                    }
                }
            }
        });
    }
}
