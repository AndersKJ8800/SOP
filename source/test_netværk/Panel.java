package com.test_netværk;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Panel extends JPanel implements MouseListener {

    static final int WIDTH = 900, HEIGHT = 600;

    int imgToTest = 0;

    int[] testLabels = new int[10000]; // int array der indeholder labels svarende til test billederne
    BufferedImage[] testImages = new BufferedImage[10000]; // billed array med test billeder i tilsvarende rækkefølge som labels

    // ovenstående arrays fyldes med relevant data
    {
        try
        {
            // labels læses fra fil og overføres til array, billeder læses fra mappe
            List<String> labels = Files.readAllLines(Paths.get("test_labels.txt"));
            for (int i = 0; i < 10000; i++)
            {
                testLabels[i] = Integer.parseInt(labels.get(i));
                testImages[i] = ImageIO.read(new File("test billeder/" + i + ".png"));
            }
        } catch (Exception ignore) {}
    }

    NeuralNetworkConfig nn = new NeuralNetworkConfig();

    public float[] TestImg(int imgToTest)
    {
        float[] aInput = new float[784]; // lysstyrken af de 784 pixels i testbilledet, fra venstre mod højre, top til bund, skaleret ned til et tal mellem 0 og 1.
        float[] aOutput = new float[10]; // netværkets gæt på om der er tallene 0 til 9 i testbilledet.
        float[][] aHidden = new float[nn.noOfHiddenLayers][nn.noOfNeuronsPerHiddenLayer];
        // definerer aktivationerne af input-neuronerne
        for (int y = 0; y < 28; y++)
        {
            for (int x = 0; x < 28; x++)
            {
                aInput[x+28*y] = (float) (testImages[imgToTest].getRGB(x,y) & 0xff) / 255; // getRGB() bruges og styrken af grøn og blå filtreres ud, da der ikke er en tilsvarende funktion til at få lysstyrken. I monokromatiske billeder giver dette samme resultet
            }
        }
        // beregner aktivationerne i de gemte lag
        for (int i = 0; i < nn.noOfHiddenLayers; i++)
        {
            for (int j = 0; j < nn.noOfNeuronsPerHiddenLayer; j++)
            {
                aHidden[i][j] = 0;
                if (i == 0)
                {
                    // beregnes ud fra input-laget
                    for (int n = 0; n < 784; n++)
                    {
                        aHidden[i][j] += aInput[n] * nn.wInput[n][j];
                    }
                }
                else
                {
                    // beregnes ud fra det forrige gemte lag
                    for (int n = 0; n < nn.noOfNeuronsPerHiddenLayer; n++)
                    {
                        aHidden[i][j] += aHidden[i-1][n] * nn.w[i-1][n][j];
                    }
                }
                aHidden[i][j] += nn.b[i][j];
                aHidden[i][j] = Sigmoid((float) aHidden[i][j]);
            }
        }
        // beregner aktivationerne i output-laget
        for (int i = 0; i < 10; i++)
        {
            aOutput[i] = 0;
            for (int j = 0; j < nn.noOfNeuronsPerHiddenLayer; j++)
            {
                aOutput[i] += aHidden[aHidden.length-1][j] * nn.wOutput[j][i];
            }
            aOutput[i] += nn.bOutput[i];
            aOutput[i] = Sigmoid(aOutput[i]);
        }

        return aOutput;
    }

    public float Sigmoid(float n)
    {
        return (float) (1 / (1 + Math.pow(2.7182818284590452353602 /* eulers tal */, -n))); // omdanner alle reelle tal til et mellem 0 og 1 i form af logistisk vækst
    }

    public Panel() {
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
    }

    public void paint(Graphics g) {
        newImgToTest();

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON));


        g2.setColor(Color.black);
        g2.fillRect(0,0,WIDTH,HEIGHT);

        float[] result = TestImg(imgToTest);

        g2.setFont(new Font("Sans-Serif",Font.PLAIN,60));
        g2.setColor(Color.white);
        g2.drawString(String.valueOf(testLabels[imgToTest]), 220, 550);
        g2.scale(15,15);
        g2.drawImage(testImages[imgToTest], 3,3,this);
        g2.scale((double) 1/15, (double) 1/15);

        for (int i = 0; i < 10; i++)
        {
            g2.setFont(new Font("Sans-Serif",Font.PLAIN,40));
            g2.drawString(String.valueOf((i)),550,70 + i * 55);
            g2.drawString("%",755,71 + i * 55);

            g2.setFont(new Font("Monospaced",Font.PLAIN,40));

            g2.setFont(new Font("Monospaced",Font.BOLD,40));
            String arrow = "→";
            for (int j = 0; j < 10; j++)
            {
                if (result[i] < result[j])
                {
                    g2.setFont(new Font("Monospaced",Font.PLAIN,40));
                    arrow = "";
                }
            }

            String n = String.valueOf(Math.round(result[i] * 100));
            g2.drawString(n,700 + (2 - n.length()) * 20,70 + i * 55);
            g2.setColor(Color.white);
            g2.drawString(arrow, 500, 68 + i * 55);

        }
        g2.setColor(Color.green);
        g2.drawString(String.valueOf(imgToTest + ": " + testLabels[imgToTest]),100,700);


    }

    public void newImgToTest()
    {
        imgToTest = (int) (Math.random() * 10000);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        repaint();
    }





    // ubenyttet
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

