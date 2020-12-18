package com.test_netværk;

import org.ini4j.Ini;

import java.io.File;

public class NeuralNetworkConfig {

    Ini ini;
    int noOfHiddenLayers, noOfNeuronsPerHiddenLayer, noOfInputNeurons, noOfOutputNeurons;
    float[][] wInput, wOutput; // de vægtninger der går fra input-laget til det første gemte lag og de fra sidste gemte lag til output-laget, f.eks. wInput[5][3], mat. not. v^(1)_5,3
    float[][][] w; // de vægtninger der går mellem de miderste gemte lag, f.eks. w[2][5][9], mat. not. v^(2)_5,9. Bemærk at de to første dobbelt arrays (w[0] og w[1]) ikke bruges, men findes blot for at det stemmer overens med den matematiske notation
    float bOutput[]; // biaser til neuroner i output-laget, f.eks. bn[5], mat. not. b^(n)_5, hvor n er antallet af lag i netværket minus 1
    float b[][]; // biaser til neuronerne i de miderste lag, f.eks. b[3][4], mat. not. b^(3)_4

    // konfigurationsfilen importeres
    {
        try
        {
            ini = new Ini(new File("nnconfig.ini"));
        } catch (Exception ignore) {}
    }

    public NeuralNetworkConfig()
    {
        // egenskaber læses fra ini-fil
        this.noOfHiddenLayers = Integer.parseInt(ini.get("properties","noOfHiddenLayers"));
        this.noOfNeuronsPerHiddenLayer = Integer.parseInt(ini.get("properties","noOfNeuronsPerHiddenLayer"));
        this.noOfInputNeurons = 784;
        this.noOfOutputNeurons = 10;

        // vægtninger mellem input-laget og første gemte lag
        this.wInput = new float[noOfInputNeurons][noOfNeuronsPerHiddenLayer];
        for (int i = 0; i < noOfInputNeurons; i++)
        {
            for (int j = 0; j < noOfNeuronsPerHiddenLayer; j++)
            {
                this.wInput[i][j] = Float.parseFloat(ini.get("weights","w_" + 1 + "_" + i + "_" + j));
            }
        }

        // vægtninger mellem sidste gemte lag og output-laget
        this.wOutput = new float[noOfNeuronsPerHiddenLayer][noOfOutputNeurons];
        for (int i = 0; i < noOfNeuronsPerHiddenLayer; i++)
        {
            for (int j = 0; j < noOfOutputNeurons; j++)
            {
                this.wOutput[i][j] = Float.parseFloat(ini.get("weights","w_" + (noOfHiddenLayers + 1) + "_" + i + "_" + j));
            }
        }

        // vægtninger mellem de gemte lag
        this.w = new float[noOfHiddenLayers-1][noOfNeuronsPerHiddenLayer][noOfNeuronsPerHiddenLayer];
        for (int i = 0; i < noOfHiddenLayers - 1; i++)
        {
            for (int j = 0; j < noOfNeuronsPerHiddenLayer; j++)
            {
                for (int k = 0; k < noOfNeuronsPerHiddenLayer; k++)
                {
                    this.w[i][j][k] = Float.parseFloat(ini.get("weights","w_" + (2 + i) + "_" + j + "_" + k));
                }
            }
        }

        this.b = new float[noOfHiddenLayers][noOfNeuronsPerHiddenLayer];
        for (int i = 0; i < noOfHiddenLayers; i++)
        {
            for (int j = 0; j < noOfNeuronsPerHiddenLayer; j++)
            {
                this.b[i][j] = Float.parseFloat(ini.get("biases","b_" + (i+1) + "_" + j));
            }
        }

        this.bOutput = new float[noOfOutputNeurons];
        for (int i = 0; i < noOfOutputNeurons; i++)
        {
            this.bOutput[i] = Float.parseFloat(ini.get("biases","b_" + (noOfHiddenLayers + 1) + "_" + i));
        }

    }

}
