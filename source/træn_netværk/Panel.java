package com.træn_netværk;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Panel extends JPanel implements MouseListener, KeyListener {

    static final int WIDTH = 750, HEIGHT = 750, TRAIN_SET_SIZE = 3000; // er langsommere jo flere der er

    int[] trainLabels = new int[TRAIN_SET_SIZE]; // int array der indeholder labels svarende til trænings billederne
    BufferedImage[] trainImages = new BufferedImage[TRAIN_SET_SIZE]; // billed array med trænings billeder i tilsvarende rækkefølge som labels

    int[] testLabels = new int[10000];
    BufferedImage[] testImages = new BufferedImage[10000];

    // ovenstående arrays fyldes med relevant data
    public void importData()
    {
        try
        {
            // labels læses fra fil og overføres til array, billeder læses fra mappe
            List<String> labels = Files.readAllLines(Paths.get("train_labels.txt"));
            for (int i = 0; i < TRAIN_SET_SIZE; i++)
            {
                trainLabels[i] = Integer.parseInt(labels.get(i+iterationsCompleted));
                trainImages[i] = ImageIO.read(new File("trænings billeder/" + (i+iterationsCompleted) + ".png"));
            }
        } catch (Exception ignore) {}
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

    int totalIterations = 0, iterationsCompleted = 0;
    String totalIterationsString = String.valueOf(TRAIN_SET_SIZE);

    float[] costDifferenceLog, accuracyLog;


    static final float stepSize = (float) 1; // arbitrær, hvor meget et parameter skal ændres for at teste effekten af tabsfunktionen

    NeuralNetworkConfig nn = new NeuralNetworkConfig(); // netværkets konfiguration importeres


    public Panel() {
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        addKeyListener(this);
    }


    // YEEEEEEEET
    float[][] bOutputChanges = new float[TRAIN_SET_SIZE][nn.bOutput.length]; // hvordan biaserne i output laget skal ændres for hvert træningsbillede
    float[] bOutputChangesAverage = new float[nn.bOutput.length];
    {
        for (int i = 0; i < bOutputChanges.length; i++)
        {
            for (int j = 0; j < bOutputChanges[0].length; j++)
            {
                bOutputChanges[i][j] = 0;
            }
        }
    }

    float[][][] bChanges = new float[TRAIN_SET_SIZE][nn.b.length][nn.b[0].length];
    float[][] bChangesAverage = new float[nn.b.length][nn.b[0].length];
    {
        for (int i = 0; i < bChanges.length; i++)
        {
            for (int j = 0; j < bChanges[0].length; j++)
            {
                for (int k = 0; k < bChanges[0][0].length; k++)
                {

                    bChanges[i][j][k] = 0;
                }
            }
        }
    }

    float[][][] wInputChanges = new float[TRAIN_SET_SIZE][nn.wInput.length][nn.wInput[0].length];
    float[][] wInputChangesAverage = new float[nn.wInput.length][nn.wInput[0].length];
    {
        for (int i = 0; i < wInputChanges.length; i++)
        {
            for (int j = 0; j < wInputChanges[0].length; j++)
            {
                for (int k = 0; k < wInputChanges[0][0].length; k++)
                {

                    wInputChanges[i][j][k] = 0;
                }
            }
        }
    }

    float[][][] wOutputChanges = new float[TRAIN_SET_SIZE][nn.wOutput.length][nn.wOutput[0].length];
    float[][] wOutputChangesAverage = new float[nn.wOutput.length][nn.wOutput[0].length];
    {
        for (int i = 0; i < wOutputChanges.length; i++)
        {
            for (int j = 0; j < wOutputChanges[0].length; j++)
            {
                for (int k = 0; k < wOutputChanges[0][0].length; k++)
                {

                    wOutputChanges[i][j][k] = 0;
                }
            }
        }
    }

    float[][][][] wChanges;
    float[][][] wChangesAverage;
    {
        try {
            wChanges = new float[TRAIN_SET_SIZE][nn.w.length][nn.w[0].length][nn.w[0][0].length];
            wChangesAverage = new float[nn.w.length][nn.w[0].length][nn.w[0][0].length];
        } catch (Exception ignore) {}
    }

    {
        try
        {
            for (int i = 0; i < wChanges.length; i++)
            {
                for (int j = 0; j < wChanges[0].length; j++)
                {
                    for (int k = 0; k < wChanges[0][0].length; k++)
                    {
                        for (int l = 0; l < wChanges[0][0][0].length; l++)
                        {
                            wChanges[i][j][k][l] = 0;
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
    }
    // YOINK

    public void logNetworkPerformance(int n)
    {
        float costAverage = (float) 0;
        float accuracyAverage = (float) 0;
        for (int i = 0; i < testLabels.length; i++)
        {
            float[] result = testImg(i,true);
            float[] desiredResult = new float[]{0,0,0,0,0,0,0,0,0,0};
            desiredResult[testLabels[i]] = 1;
            costAverage += costFunction(result,desiredResult);

            boolean correctGuess = true;
            for (int j = 0; j < 10; j++)
            {
                if (result[j] > result[testLabels[i]])
                {
                    correctGuess = false;
                }
            }
            if (correctGuess)
            {
                accuracyAverage += 1;
            }

        }
        costAverage = costAverage / testLabels.length;
        accuracyAverage = accuracyAverage / testLabels.length;
        System.out.println("cost avg: " + costAverage);
        System.out.println("acc avg: " + accuracyAverage);
        costDifferenceLog[n] = costAverage;
        accuracyLog[n] = accuracyAverage;
    }

    public void trainNetwork(int iterations)
    {
        for (int n = 0; n < iterations; n++) // træner hvert billede i træningssættet
        {
            trainImage(n);
            iterationsCompleted++;
            if (iterationsCompleted % 10 == 0)
            {
                System.out.println(iterationsCompleted);
                if (iterationsCompleted % 100 == 0)
                {
                    logNetworkPerformance(n/100);
                }
            }
            for (int j = 0; j < bOutputChangesAverage.length; j++)
            {
                bOutputChangesAverage[j] = 0;
                for (int k = 0; k < TRAIN_SET_SIZE; k++)
                {
                    bOutputChangesAverage[j] += bOutputChanges[k][j];
                }
                bOutputChangesAverage[j] = bOutputChangesAverage[j] / TRAIN_SET_SIZE;
                nn.bOutput[j] += bOutputChangesAverage[j];
            }
            for (int j = 0; j < bChangesAverage.length; j++)
            {
                for (int a = 0; a < bChangesAverage[0].length; a++)
                {
                    bChangesAverage[j][a] = 0;
                    for (int k = 0; k < TRAIN_SET_SIZE; k++)
                    {
                        bChangesAverage[j][a] += bChanges[k][j][a];
                    }
                    bChangesAverage[j][a] = bChangesAverage[j][a] / TRAIN_SET_SIZE;
                    nn.b[j][a] += bChangesAverage[j][a];
                }
            }
            for (int j = 0; j < wInputChangesAverage.length; j++)
            {
                for (int a = 0; a < wInputChangesAverage[0].length; a++)
                {
                    wInputChangesAverage[j][a] = 0;
                    for (int k = 0; k < TRAIN_SET_SIZE; k++)
                    {
                        wInputChangesAverage[j][a] += wInputChanges[k][j][a];
                    }
                    wInputChangesAverage[j][a] = wInputChangesAverage[j][a] / TRAIN_SET_SIZE;
                    nn.wInput[j][a] += wInputChangesAverage[j][a];
                }
            }
            for (int j = 0; j < wOutputChangesAverage.length; j++)
            {
                for (int a = 0; a < wOutputChangesAverage[0].length; a++)
                {
                    wOutputChangesAverage[j][a] = 0;
                    for (int k = 0; k < TRAIN_SET_SIZE; k++)
                    {
                        wOutputChangesAverage[j][a] += wOutputChanges[k][j][a];
                    }
                    wOutputChangesAverage[j][a] = wOutputChangesAverage[j][a] / TRAIN_SET_SIZE;
                    nn.wOutput[j][a] += wOutputChangesAverage[j][a];
                }
            }
            try
            {
                for (int j = 0; j < wChangesAverage.length; j++)
                {
                    for (int a = 0; a < wChangesAverage[0].length; a++)
                    {
                        for (int b = 0; b < wChangesAverage[0].length; b++)
                        {
                            wChangesAverage[j][a][b] = 0;
                            for (int k = 0; k < TRAIN_SET_SIZE; k++)
                            {
                                wChangesAverage[j][a][b] += wChanges[k][j][a][b];
                            }
                            wChangesAverage[j][a][b] = wChangesAverage[j][a][b] / TRAIN_SET_SIZE;
                            nn.w[j][a][b] += wChangesAverage[j][a][b];
                        }
                    }
                }
            } catch (Exception ignore) {}
        }

        try (PrintWriter out = new PrintWriter("results-" + ".csv"))
        {
            out.println("antal iterationer, gennemsnit af tabsfunktioner over testsættet, andel af testsættet som blev gættet korrekt");
            for (int i = 0; i < costDifferenceLog.length; i++)
            {
                out.println(i * 100 + ", " + costDifferenceLog[i] + ", " + accuracyLog[i]);
            }
        }
        catch (Exception ignore) {}

    }

    public void trainImage(int n)
    {
        // netværket med den nuværende konfiguration giver et bud på hvad der er på billedet med navn n
        float[] result = testImg(n,false);
        // det ønskede resultat defineres til 0 nul for alle cifre pånær hvad der rent faktisk er på billedet, hvilket defineres til 1
        float[] desiredResult = new float[]{0,0,0,0,0,0,0,0,0,0};
        desiredResult[trainLabels[n]] = 1;
        // tabsfunktionen (forskel-ish) beregnes ud fra netværkets resultat og det ønskede
        float initialCost = costFunction(result,desiredResult);

        testBOutputChanges(n,desiredResult,initialCost);
        testBChanges(n,desiredResult,initialCost);
        testWInputChanges(n,desiredResult,initialCost);
        testWOutputChanges(n,desiredResult,initialCost);
        testWChanges(n,desiredResult,initialCost);
    }

    public void testBOutputChanges (int nTestImage, float[] desiredResult, float initialCost)
    {
        for (int i = 0; i < nn.bOutput.length; i++)
        {
            nn.bOutput[i] += stepSize;
            float[] newResultIncrease = testImg(nTestImage,false);
            float newCostIncrease = costFunction(newResultIncrease,desiredResult);
            float costDifferenceIncrease = newCostIncrease - initialCost;
            nn.bOutput[i] -= 2 * stepSize;
            float[] newResultDecrease = testImg(nTestImage,false);
            float newCostDecrease = costFunction(newResultDecrease,desiredResult);
            float costDifferenceDecrease = newCostDecrease - initialCost;
            nn.bOutput[i] += stepSize;

            boolean parameterShouldIncrease = false;
            if (costDifferenceIncrease < 0) parameterShouldIncrease = true;
            boolean parameterShouldDecrease = false;
            if (costDifferenceDecrease < 0) parameterShouldDecrease = true;

            if (parameterShouldDecrease && parameterShouldIncrease)
            {
                if (costDifferenceDecrease < costDifferenceIncrease)
                {
                    parameterShouldIncrease = false;
                }
                else
                {
                    parameterShouldDecrease = false;
                }
            }
            if (parameterShouldDecrease)
            {
                bOutputChanges[nTestImage][i] = stepSize * costDifferenceDecrease;
            }
            if (parameterShouldIncrease)
            {
                bOutputChanges[nTestImage][i] = -stepSize * costDifferenceIncrease;
            }
            if (!parameterShouldDecrease && !parameterShouldIncrease)
            {
                bOutputChanges[nTestImage][i] = 0;
            }
        }
    }

    public void testBChanges (int nTestImage, float[] desiredResult, float initialCost)
    {
        for (int i = 0; i < nn.b.length; i++)
        {
            for (int j = 0; j < nn.b[0].length; j++)
            {
                nn.b[i][j] += stepSize;
                float[] newResultIncrease = testImg(nTestImage,false);
                float newCostIncrease = costFunction(newResultIncrease,desiredResult);
                float costDifferenceIncrease = newCostIncrease - initialCost;
                nn.b[i][j] -= 2 * stepSize;
                float[] newResultDecrease = testImg(nTestImage,false);
                float newCostDecrease = costFunction(newResultDecrease,desiredResult);
                float costDifferenceDecrease = newCostDecrease - initialCost;
                nn.b[i][j] += stepSize;

                boolean parameterShouldIncrease = false;
                if (costDifferenceIncrease < 0) parameterShouldIncrease = true;
                boolean parameterShouldDecrease = false;
                if (costDifferenceDecrease < 0) parameterShouldDecrease = true;

                if (parameterShouldDecrease && parameterShouldIncrease)
                {
                    if (costDifferenceDecrease < costDifferenceIncrease)
                    {
                        parameterShouldIncrease = false;
                    }
                    else
                    {
                        parameterShouldDecrease = false;
                    }
                }
                if (parameterShouldDecrease)
                {
                    bChanges[nTestImage][i][j] = stepSize * costDifferenceDecrease;
                }
                if (parameterShouldIncrease)
                {
                    bChanges[nTestImage][i][j] = -stepSize * costDifferenceIncrease;
                }
                if (!parameterShouldDecrease && !parameterShouldIncrease)
                {
                    bChanges[nTestImage][i][j] = 0;
                }
            }
        }
    }

    public void testWInputChanges (int nTestImage, float[] desiredResult, float initialCost)
    {
        for (int i = 0; i < nn.wInput.length; i++)
        {
            for (int j = 0; j < nn.wInput[0].length; j++)
            {
                nn.wInput[i][j] += stepSize;
                float[] newResultIncrease = testImg(nTestImage,false);
                float newCostIncrease = costFunction(newResultIncrease,desiredResult);
                float costDifferenceIncrease = newCostIncrease - initialCost;
                nn.wInput[i][j] -= 2 * stepSize;
                float[] newResultDecrease = testImg(nTestImage,false);
                float newCostDecrease = costFunction(newResultDecrease,desiredResult);
                float costDifferenceDecrease = newCostDecrease - initialCost;
                nn.wInput[i][j] += stepSize;

                boolean parameterShouldIncrease = false;
                if (costDifferenceIncrease < 0) parameterShouldIncrease = true;
                boolean parameterShouldDecrease = false;
                if (costDifferenceDecrease < 0) parameterShouldDecrease = true;

                if (parameterShouldDecrease && parameterShouldIncrease)
                {
                    if (costDifferenceDecrease < costDifferenceIncrease)
                    {
                        parameterShouldIncrease = false;
                    }
                    else
                    {
                        parameterShouldDecrease = false;
                    }
                }
                if (parameterShouldDecrease)
                {
                    wInputChanges[nTestImage][i][j] = stepSize * costDifferenceDecrease;
                }
                if (parameterShouldIncrease)
                {
                    wInputChanges[nTestImage][i][j] = -stepSize * costDifferenceIncrease;
                }
                if (!parameterShouldDecrease && !parameterShouldIncrease)
                {
                    wInputChanges[nTestImage][i][j] = 0;
                }
            }
        }
    }

    public void testWOutputChanges (int nTestImage, float[] desiredResult, float initialCost)
    {
        for (int i = 0; i < nn.wOutput.length; i++)
        {
            for (int j = 0; j < nn.wOutput[0].length; j++)
            {
                nn.wOutput[i][j] += stepSize;
                float[] newResultIncrease = testImg(nTestImage,false);
                float newCostIncrease = costFunction(newResultIncrease,desiredResult);
                float costDifferenceIncrease = newCostIncrease - initialCost;
                nn.wOutput[i][j] -= 2 * stepSize;
                float[] newResultDecrease = testImg(nTestImage,false);
                float newCostDecrease = costFunction(newResultDecrease,desiredResult);
                float costDifferenceDecrease = newCostDecrease - initialCost;
                nn.wOutput[i][j] += stepSize;

                boolean parameterShouldIncrease = false;
                if (costDifferenceIncrease < 0) parameterShouldIncrease = true;
                boolean parameterShouldDecrease = false;
                if (costDifferenceDecrease < 0) parameterShouldDecrease = true;

                if (parameterShouldDecrease && parameterShouldIncrease)
                {
                    if (costDifferenceDecrease < costDifferenceIncrease)
                    {
                        parameterShouldIncrease = false;
                    }
                    else
                    {
                        parameterShouldDecrease = false;
                    }
                }
                if (parameterShouldDecrease)
                {
                    wOutputChanges[nTestImage][i][j] = stepSize * costDifferenceDecrease;
                }
                if (parameterShouldIncrease)
                {
                    wOutputChanges[nTestImage][i][j] = -stepSize * costDifferenceIncrease;
                }
                if (!parameterShouldDecrease && !parameterShouldIncrease)
                {
                    wOutputChanges[nTestImage][i][j] = 0;
                }
            }
        }
    }

    public void testWChanges (int nTestImage, float[] desiredResult, float initialCost)
    {
        for (int i = 0; i < nn.w.length; i++)
        {
            for (int j = 0; j < nn.w[0].length; j++)
            {
                for (int k = 0; k < nn.w[0][0].length; k++)
                {
                    nn.w[i][j][k] += stepSize;
                    float[] newResultIncrease = testImg(nTestImage,false);
                    float newCostIncrease = costFunction(newResultIncrease,desiredResult);
                    float costDifferenceIncrease = newCostIncrease - initialCost;
                    nn.w[i][j][k] -= 2 * stepSize;
                    float[] newResultDecrease = testImg(nTestImage,false);
                    float newCostDecrease = costFunction(newResultDecrease,desiredResult);
                    float costDifferenceDecrease = newCostDecrease - initialCost;
                    nn.w[i][j][k] += stepSize;

                    boolean parameterShouldIncrease = false;
                    if (costDifferenceIncrease < 0) parameterShouldIncrease = true;
                    boolean parameterShouldDecrease = false;
                    if (costDifferenceDecrease < 0) parameterShouldDecrease = true;

                    if (parameterShouldDecrease && parameterShouldIncrease)
                    {
                        if (costDifferenceDecrease < costDifferenceIncrease)
                        {
                            parameterShouldIncrease = false;
                        }
                        else
                        {
                            parameterShouldDecrease = false;
                        }
                    }
                    if (parameterShouldDecrease)
                    {
                        wChanges[nTestImage][i][j][k] = stepSize * costDifferenceDecrease;
                    }
                    if (parameterShouldIncrease)
                    {
                        wChanges[nTestImage][i][j][k] = -stepSize * costDifferenceIncrease;
                    }
                    if (!parameterShouldDecrease && !parameterShouldIncrease)
                    {
                        wChanges[nTestImage][i][j][k] = 0;
                    }
                }
            }
        }
    }

    public float sigmoid(float n)
    {
        return (float) (1 / (1 + Math.pow(2.7182818284590452353602 /* eulers tal */, -n))); // omdanner alle reelle tal til et mellem 0 og 1 i form af logistisk vækst
    }

    public float costFunction(float[] outputResult, float[] desiredResult)
    {
        float cost = 0;

        for (int i = 0; i < 10; i++)
        {
            if (desiredResult[i] == 0)
            {
                cost += 0.1 * (Math.pow(outputResult[i] - desiredResult[i], 2));
            }
            else if (desiredResult[i] == 1)
            {
                cost += Math.pow(outputResult[i] - desiredResult[i], 2);
            }

        }
        return cost;
    }

    public float[] testImg(int imgToTest, boolean useTestSet)
    {
        float[] aInput = new float[784]; // lysstyrken af de 784 pixels i testbilledet, fra venstre mod højre, top til bund, skaleret ned til et tal mellem 0 og 1.
        float[] aOutput = new float[10]; // netværkets gæt på om der er tallene 0 til 9 i testbilledet.
        float[][] aHidden = new float[nn.noOfHiddenLayers][nn.noOfNeuronsPerHiddenLayer];
        // definerer aktivationerne af input-neuronerne
        for (int y = 0; y < 28; y++)
        {
            for (int x = 0; x < 28; x++)
            {
                if (useTestSet)
                {
                    aInput[x+28*y] = (float) (testImages[imgToTest].getRGB(x,y) & 0xff) / 255; // getRGB() bruges og styrken af grøn og blå filtreres ud, da der ikke er en tilsvarende funktion til at få lysstyrken. I monokromatiske billeder giver dette samme resultet
                }
                else
                {
                    aInput[x+28*y] = (float) (trainImages[imgToTest].getRGB(x,y) & 0xff) / 255;
                }
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
                aHidden[i][j] = sigmoid((float) aHidden[i][j]);
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
            aOutput[i] = sigmoid(aOutput[i]);
        }

        return aOutput;
    }

    public void paint(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON));
        g2.setColor(Color.black);
        g2.fillRect(0,0,WIDTH,HEIGHT);

        g2.setColor(Color.white);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 40));
        g2.drawString("vælg antal træningsiterationer", 120, 100);
        g2.drawString(totalIterationsString, 325, 150);

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() > 47 && e.getKeyCode() < 58)
        {
            if (totalIterationsString.length() < 6)
                totalIterationsString += e.getKeyChar();
        }
        if (e.getKeyCode() == 10)
        {
            iterationsCompleted = nn.noOfUnderwentIterations;
            importData();
            totalIterations = Integer.parseInt(totalIterationsString) + iterationsCompleted;
            if (totalIterations > TRAIN_SET_SIZE) totalIterations = TRAIN_SET_SIZE;
            costDifferenceLog = new float[31];
            accuracyLog = new float[31];
            logNetworkPerformance(0);
            trainNetwork((int) (3000));
            createNNConfig();
        }
        if (e.getKeyCode() == 8)
        {
            totalIterationsString = "";
        }
        repaint();
    }

    public void createNNConfig()
    {
        int noOfHiddenLayers = nn.noOfHiddenLayers;
        int noOfNeuronsPerHiddenLayer = nn.noOfNeuronsPerHiddenLayer;
        int noOfUnderwentIterations = nn.noOfUnderwentIterations;

        int totalLayers = noOfHiddenLayers + 2;

        try (PrintWriter out = new PrintWriter("nnconfig.ini")) {
            out.println("; senest opdateret: " + java.util.Calendar.getInstance().getTime());
            out.println("[properties]");
            out.println("noOfHiddenLayers = " + noOfHiddenLayers);
            out.println("noOfNeuronsPerHiddenLayer = " + noOfNeuronsPerHiddenLayer);
            out.println("noOfUnderwentIterations = " + iterationsCompleted);

            out.println("[weights]");

            for (int i = 0; i < nn.wInput.length; i++)
            {
                for (int j = 0; j < nn.wInput[0].length; j++)
                {
                    out.println("w_" + 1 + "_" + i + "_" + j + " = " + nn.wInput[i][j]);
                }
            }

            for (int i = 0; i < nn.w.length; i++)
            {
                for (int j = 0; j < nn.w[0].length; j++)
                {
                    for (int k = 0; k < nn.w[0][0].length; k++)
                    {
                        out.println("w_" + (i + 2) + "_" + j + "_" + k + " = " + nn.w[i][j][k]);
                    }
                }
            }

            for (int i = 0; i < nn.wOutput.length; i++)
            {
                for (int j = 0; j < nn.wOutput[0].length; j++)
                {
                    out.println("w_" + (totalLayers - 1) + "_" + i + "_" + j + " = " + nn.wOutput[i][j]);
                }
            }

            out.println("[biases]");

            for (int i = 0; i < nn.b.length; i++)
            {
                for (int j = 0; j < nn.b[0].length; j++)
                {
                    out.println("b_" + (i + 1) + "_" + j + " = " + nn.b[i][j]);
                }
            }

            for (int i = 0; i < nn.bOutput.length; i++)
            {
                out.println("b_" + (totalLayers - 1) + "_" + i + " = " + nn.bOutput[i]);
            }





        }
        catch (Exception e) {}
    }


    // ubenyttet
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {}
}

