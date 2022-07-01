package net.sf.borg.common;

import javax.swing.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ModalMessageServer {

    static volatile private ModalMessageServer singleton = null;

    static public ModalMessageServer getReference() {
        if (singleton == null) {
            ModalMessageServer b = new ModalMessageServer();
            singleton = b;
        }
        return (singleton);
    }

    private ModalMessage modalMessage = null;

    private BlockingQueue<String> messageQ = new LinkedBlockingQueue<>();

    private ModalMessageServer(){
        Thread t = new Thread(){

            @Override
            public void run() {
                try {
                    while (true) {
                        String msg = messageQ.take();
                        if (msg.startsWith("lock:")) {
                            final String lockmsg = msg.substring(5);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (modalMessage == null || !modalMessage.isShowing()) {
                                        modalMessage = new ModalMessage(lockmsg, false);
                                        modalMessage.setVisible(true);
                                    } else {
                                        modalMessage.appendText(lockmsg);
                                    }
                                    modalMessage.setEnabled(false);
                                    modalMessage.toFront();
                                }
                            });

                        } else if (msg.startsWith("log:")) {
                            final String lockmsg = msg.substring(4);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (modalMessage != null && modalMessage.isShowing()) {
                                        modalMessage.appendText(lockmsg);
                                        // modalMessage.setEnabled(false);
                                        // modalMessage.toFront();
                                    }

                                }
                            });

                        } else if (msg.equals("unlock")) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (modalMessage.isShowing()) {
                                        modalMessage.setEnabled(true);
                                    }
                                }
                            });

                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        t.start();
    }

    public void sendMessage(String msg){
        messageQ.add(msg);
    }

    public void sendLogMessage(String msg) {
        sendMessage("log:" + msg);
    }

}
