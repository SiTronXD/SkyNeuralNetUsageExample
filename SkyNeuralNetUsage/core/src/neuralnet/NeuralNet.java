package neuralnet;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class NeuralNet 
{
	ArrayList<Layer> layers;
	
	public NeuralNet(String neuralNetFilePath)
	{
		this.layers = new ArrayList<Layer>();
		
		// Read file
		FileHandle handle = Gdx.files.local(neuralNetFilePath);
		String text = handle.readString();
		String lines[] = text.split("\\r?\\n");
		
		// Number of layers
		String words[] = lines[0].split(" ");
		int numLayers = Integer.parseInt(words[1]);
		
		for(int i = 0; i < numLayers; ++i)
			this.layers.add(new Layer());
		this.layers.trimToSize();
		
		// Number of neurons per layer
		words = lines[1].split(" ");
		int numNeurons = 0;
		for(int i = 0; i < this.layers.size(); ++i)
		{
			numNeurons = Integer.parseInt(words[i + 1]);
			
			this.layers.get(i).addNeurons(numNeurons);
			
			// Add weights for the previous layer, 
			// if this layer is not the input layer
			if(i > 0)
				this.layers.get(i-1).addWeightsForNeurons(numNeurons);
		}
		
		// Read and apply weights from each line
		for(int currLine = 4; currLine < lines.length; ++currLine)
		{
			words = lines[currLine].split(" ");
			
			int layerIndex = Integer.parseInt(words[0]);
			int neuronIndex = Integer.parseInt(words[1]);
			int weightIndex = Integer.parseInt(words[2]);
			double weightValue = Double.parseDouble(words[3]);
			
			// Set weight
			this.layers.get(layerIndex).getNeuron(neuronIndex).setWeight(weightIndex, weightValue);
		}
	}
	
	public void forwardProp(ArrayList<Double> inputValues)
	{
		// Set input values
		this.layers.get(0).setInputWeights(inputValues);
		
		// Calculate output values in hidden/output layers
		for(int i = 1; i < this.layers.size(); ++i)
		{
			this.layers.get(i).calcOutputValues(
				this.layers.get(i - 1),
				i == this.layers.size() - 1
			);
		}
	}
	
	public void getOutputs(ArrayList<Double> exportedOutputValues)
	{
		ArrayList<Neuron> outputNeurons = this.layers.get(this.layers.size()-1).getNeurons();
		
		// Add output values into exportedOutputValues
		for(int i = 0; i < outputNeurons.size() - 1; ++i)
			exportedOutputValues.add(outputNeurons.get(i).getOutputValue());
	}
}
