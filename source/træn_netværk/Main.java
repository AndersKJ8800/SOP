package com.træn_netværk;

import javax.swing.*;
import java.awt.*;

public class Main {

    public Main() {

        JFrame frame = new JFrame();
        Panel panel = new Panel();

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("Træn netværk");

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

    }

    public static void main(String[] args) { new com.træn_netværk.Main(); }

}
