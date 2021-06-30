package neuralnet;

import java.util.ArrayList;

public class Layer 
{
	ArrayList<Neuron> neurons;
	
	public Layer()
	{
		this.neurons = new ArrayList<Neuron>();
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
}
