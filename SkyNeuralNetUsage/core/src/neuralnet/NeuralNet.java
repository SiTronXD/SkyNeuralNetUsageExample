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
		
		// Go through each line
		for(int currLine = 0; currLine < lines.length; ++currLine)
		{
			String words[] = lines[currLine].split(" ");
			
			System.out.println("Line: " + lines[currLine]);
			
			// Number of layers
			if(words[0].matches("NumLayers:"))
			{
				int numLayers = Integer.parseInt(words[1]);
				
				for(int i = 0; i < numLayers; ++i)
				{
					this.layers.add(new Layer());
				}

				this.layers.trimToSize();
			}
			// Number of neurons per layer
			else if(words[0].matches("NumNeuronsPerLayer:"))
			{
				int numNeurons = 0;
				
				for(int i = 0; i < this.layers.size(); ++i)
				{
					numNeurons = Integer.parseInt(words[i + 1]);
					
					this.layers.get(i).addNeurons(numNeurons);
					
					// Add weights for the previous layer, 
					// if this layer is not the input layer
					if(i > 0)
					{
						this.layers.get(i-1).addWeightsForNeurons(numNeurons);
					}
				}
			}
		}
	}
	
	public void forwardProp()
	{
		
	}
	
	public void getOutputs(ArrayList<Double> outputValues)
	{
		outputValues.add(1337.0);
	}
}
