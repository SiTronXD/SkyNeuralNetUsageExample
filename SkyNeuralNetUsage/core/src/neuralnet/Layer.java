package neuralnet;

import java.util.ArrayList;

public class Layer 
{
	ArrayList<Neuron> neurons;
	
	public Layer()
	{
		this.neurons = new ArrayList<Neuron>();
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
			this.neurons.add(new Neuron());
		
		this.neurons.trimToSize();
	}
	
	public void addWeightsForNeurons(int numWeights)
	{
		for(int i = 0; i < this.neurons.size(); ++i)
			this.neurons.get(i).setNumWeights(numWeights);
	}
	
	public Neuron getNeuron(int index) { return this.neurons.get(index); }
}
