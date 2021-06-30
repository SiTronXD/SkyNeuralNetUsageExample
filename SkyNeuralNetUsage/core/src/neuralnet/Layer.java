package neuralnet;

import java.util.ArrayList;

public class Layer 
{
	ArrayList<Neuron> neurons;
	
	public Layer()
	{
		this.neurons = new ArrayList<Neuron>();
	}
	
	private double activateRelu(double x)
	{
		return Math.max(0.0, x);
	}
	
	private double activateSigmoid(double x)
	{
		return 1.0 / (1.0 + Math.exp(-x));
	}
	
	public void calcOutputValues(Layer previousLayer, boolean isOutputLayer)
	{
		// Loop through each neuron, except for the bias
		for(int i = 0; i < this.neurons.size() - 1; ++i)
		{
			double result = 0.0;
			
			// Loop through each neuron, except for the bias
			for(int j = 0; j < previousLayer.getNeurons().size(); ++j)
			{
				Neuron prevNeuron = previousLayer.getNeuron(j);
				
				result += prevNeuron.getOutputValue() * prevNeuron.getOutputWeight(i);
			}
			
			// Activation function
			if(isOutputLayer)
				result = this.activateSigmoid(result);
			else
				result = this.activateRelu(result);
			
			this.neurons.get(i).setOutputValue(result);
		}
	}
	
	public void setInputWeights(ArrayList<Double> inputValues)
	{
		// Make sure the sizes are correct
		if(inputValues.size() != this.neurons.size() - 1)
		{
			System.out.println("Number of inputs does not equal number of neurons...");
			return;
		}
		
		// Set output values in input layer
		for(int i = 0; i < this.neurons.size() - 1; ++i)
		{
			this.neurons.get(i).setOutputValue(inputValues.get(i));
		}
	}
	
	public void addNeurons(int numNeurons)
	{
		// Number of neurons + 1 bias neuron
		for(int i = 0; i < numNeurons + 1; ++i)
		{
			this.neurons.add(new Neuron(i != numNeurons ? 0.0 : 1.0));
		}
		
		this.neurons.trimToSize();
	}
	
	public void addWeightsForNeurons(int numWeights)
	{
		for(int i = 0; i < this.neurons.size(); ++i)
			this.neurons.get(i).setNumWeights(numWeights);
	}
	
	public ArrayList<Neuron> getNeurons() { return this.neurons; }
	public Neuron getNeuron(int index) { return this.neurons.get(index); }
}
