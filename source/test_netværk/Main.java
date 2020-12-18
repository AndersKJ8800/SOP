package com.test_netværk;

import javax.swing.*;

public class Main {

    public Main() {

        JFrame frame = new JFrame();
        Panel panel = new Panel();

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("Test netværk");

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

    }

    public static void main(String[] args) { new Main(); }

}
