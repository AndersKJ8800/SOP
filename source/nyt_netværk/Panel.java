package com.nyt_netværk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;

public class Panel extends JPanel implements KeyListener {

    static final int WIDTH = 750, HEIGHT = 750;

    static final int noOfInputNeurons = 784, noOfOutputNerons = 10;

    static final Font
            bodyText = new Font("SansSerif", Font.PLAIN, 24),
            heading = new Font("SansSerif", Font.PLAIN, 48);

    String returnMessage = "";
    String currentlySelecting = "noOfHiddenLayers";
    int noOfHiddenLayers = 0, noOfNeuronsPerHiddenLayer = 0;
    String userInputHiddenLayers = "2", userInputNeurons = "16";

    public Panel() {
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
    }

    public void paint(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.black);
        g2.fillRect(0,0,WIDTH,HEIGHT);

        g2.setColor(Color.white);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setFont(bodyText);
        g2.drawString("vælg antal gemte lag", 250, 200);
        g2.drawString("vælg antal neuroner per gemte lag", 190, 400);

        g2.setFont(heading);
        g2.drawString(userInputHiddenLayers, 350, 260);
        g2.drawString(userInputNeurons, 340, 460);

        g2.setFont(bodyText);
        if (currentlySelecting == "noOfHiddenLayers") g2.drawString("➤",300, 252);
        if (currentlySelecting == "noOfNeurons") g2.drawString("➤",300, 452);

        g2.drawString(returnMessage, 165, 670);

    }


    @Override
    public void keyReleased(KeyEvent e) {
        char key = e.getKeyChar();
        int keyCode = e.getKeyCode();

        // vælg string som skal ændres
        String string = "";
        if (currentlySelecting == "noOfHiddenLayers")
        {
            string = userInputHiddenLayers;
        }
        else if (currentlySelecting == "noOfNeurons")
        {
            string = userInputNeurons;
        }

        // ændre ovennævnte string
        if (e.getKeyCode() >= 48 /* 0 */ && e.getKeyCode() <= 57 /* 57 */)
        {
            string += e.getKeyChar();
        }
        if (e.getKeyCode() == 8 /* delete */)
        {
            if (string.length() > 1)
            {
                string = string.substring(0, string.length() - 1); // fjern sidste tegn
            }
            else
            {
                string = ""; // er der kun èt tegn, fjern det hele; ovenstående virker ikke
            }
        }

        // port string
        if (currentlySelecting == "noOfHiddenLayers")
        {
            userInputHiddenLayers = string;
        }
        else if (currentlySelecting == "noOfNeurons")
        {
            userInputNeurons = string;
        }

        // enter confirmer
        if (keyCode == 10)
        {
            if (string != "")
            {
                if (currentlySelecting == "noOfHiddenLayers") currentlySelecting = "noOfNeurons";
                else
                {
                    if (currentlySelecting != "none") createNNConfig();
                    currentlySelecting = "none";
                }
            }
        }
        repaint();
    }

    public void createNNConfig()
    {
        noOfHiddenLayers = Integer.parseInt(userInputHiddenLayers);
        noOfNeuronsPerHiddenLayer = Integer.parseInt(userInputNeurons);

        int totalLayers = noOfHiddenLayers + 2;

        try (PrintWriter out = new PrintWriter("nnconfig.ini")) {
            out.println("; senest opdateret: " + java.util.Calendar.getInstance().getTime());
            out.println("[properties]");
            out.println("noOfHiddenLayers = " + noOfHiddenLayers);
            out.println("noOfNeuronsPerHiddenLayer = " + noOfNeuronsPerHiddenLayer);

            out.println("[weights]");

            for (int i = noOfHiddenLayers + 1; i > 0; i--)
            {
                int noOfNeuronsInPrevLayer = noOfInputNeurons;
                if (i < noOfHiddenLayers + 1) noOfNeuronsInPrevLayer = noOfNeuronsPerHiddenLayer;
                int noOfNeuronsInNextLayer = noOfOutputNerons;
                if (i > 1) noOfNeuronsInNextLayer = noOfNeuronsPerHiddenLayer;

                for (int j = 0; j < noOfNeuronsInPrevLayer; j++)
                {
                    for (int k = 0; k < noOfNeuronsInNextLayer; k++)
                    {
                        out.println("w_" + (totalLayers - i) + "_" + j + "_" + k + " = " + randomNumber());
                    }
                }
            }

            out.println("[biases]");
            for (int i = noOfHiddenLayers + 1; i > 0; i--)
            {
                int noOfNeuronsInThisLayer = noOfNeuronsPerHiddenLayer;
                if (i == 1) noOfNeuronsInThisLayer = noOfOutputNerons;
                for (int j = 0; j < noOfNeuronsInThisLayer; j++)
                {
                    out.println("b_" + (totalLayers - i) + "_" + j + " = " + randomNumber());
                }
            }

            returnMessage = "success: netværket blev lavet problemfrit";
        }
        catch (Exception e)
        {
            returnMessage = "fejl: netværket blev ikke lavet";
        }

    }

    public float randomNumber()
    {

        double n = Math.random();
        if (n < 0.01) n = 0.01;
        if (n > 0.99) n = 0.99;

        return ((float) -Math.log(-((-1+n)/n))); // invers sigmoid funktion
    }

    // ubenyttet
    @Override
    public void keyTyped(KeyEvent e) {

    }

    // ubenyttet
    @Override
    public void keyPressed(KeyEvent e) {

    }
}

