package neuralnet;

import java.util.ArrayList;
import java.util.Random;

public class Neuron 
{
	double outputValue;
	ArrayList<Double> outputWeights;
	
	public Neuron()
	{
		this.outputWeights = new ArrayList<Double>();
	}
	
	void setNumWeights(int numWeights)
	{
		Random random = new Random();
		
		for(int i = 0; i < numWeights; ++i)
			this.outputWeights.add(random.nextDouble());
		
		this.outputWeights.trimToSize();
	}
	
	public void setWeight(int weightIndex, double weightValue) 
	{
		this.outputWeights.set(weightIndex, weightValue);
	}
	
	public void setOutputValue(double outputValue)
	{
		this.outputValue = outputValue;
	}
}
