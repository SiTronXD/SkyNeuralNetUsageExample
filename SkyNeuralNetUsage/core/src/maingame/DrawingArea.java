package maingame;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class DrawingArea 
{
	private static final int PEN_RADIUS = 50;

	private Texture drawingAreaTexture;
	private Texture downsampledDrawingAreaTexture;
	
	private int drawingAreaPosX;
	private int drawingAreaPosY;

	private boolean isDrawing;
	
	private Main mainGame;
	
	public DrawingArea(Main mainGame)
	{
		this.mainGame = mainGame;
		
		// Load black textures containing correct resolutions
		this.drawingAreaTexture = new Texture("BlackBackground_1080x1080.jpg");
		this.downsampledDrawingAreaTexture = new Texture("BlackBackground_28x28.jpg");
		
		this.drawingAreaPosX = 0;
		this.drawingAreaPosY = 700;
		this.isDrawing = false;
	}
	
	private void createDownsampledTexture()
	{
		if(!this.drawingAreaTexture.getTextureData().isPrepared())
			this.drawingAreaTexture.getTextureData().prepare();
		Pixmap pixmap = this.drawingAreaTexture.getTextureData().consumePixmap();
		
		if(!this.downsampledDrawingAreaTexture.getTextureData().isPrepared())
			this.downsampledDrawingAreaTexture.getTextureData().prepare();
		Pixmap newPixmap = this.downsampledDrawingAreaTexture.getTextureData().consumePixmap();
		
		for(int y = 0; y < this.downsampledDrawingAreaTexture.getHeight(); ++y)
		{
			for(int x = 0; x < this.downsampledDrawingAreaTexture.getWidth(); ++x)
			{
				// Not an integer size, but it's fine for this purpose
				int sideSize = this.drawingAreaTexture.getWidth() / 
						this.downsampledDrawingAreaTexture.getWidth();
				int numSamples = 0;
				double result = 0.0;
				
				for(int ty = 0; ty < sideSize; ++ty)
				{
					for(int tx = 0; tx < sideSize; ++tx)
					{
						Color col = new Color(pixmap.getPixel(x * sideSize + tx, y * sideSize + ty));
						
						result += col.r;
						numSamples++;
					}
				}
				
				result /= (double) numSamples;
				
				// Update color
				float floatResult = (float) result;
				Color newCol = new Color(floatResult, floatResult, floatResult, 1.0f);
				newPixmap.setColor(newCol);
				newPixmap.drawPixel(x, y);
			}
		}
		
		this.downsampledDrawingAreaTexture.dispose();
		this.downsampledDrawingAreaTexture = new Texture(newPixmap, Format.RGB888, false);
	}
	
	private void clearTexture(Texture textureToClear)
	{
		// Make sure texture data is "prepared"
		if(!textureToClear.getTextureData().isPrepared())
			textureToClear.getTextureData().prepare();
		
		// Get data
		Pixmap pixmap = textureToClear.getTextureData().consumePixmap();
		
		// Write color
		for (int x = 0; x < textureToClear.getWidth(); ++x) 
		{
	        for (int y = 0; y < textureToClear.getHeight(); ++y) 
	        {	
		        pixmap.setColor(Color.BLACK);
		        pixmap.drawPixel(x, y);
	        }
	    }
		
		// Reset and write data
		textureToClear.dispose();
		textureToClear = new Texture(pixmap, Format.RGB888, false);
	}
	
	public void drawingOnArea()
	{		
		if(Gdx.input.isTouched())
		{	
			Vector2 touchPos = InputManager.getTouchPos(0);

			if(touchPos.y > this.drawingAreaPosY)
				this.isDrawing = true;
			
			// Make sure texture data is "prepared"
			if(!this.drawingAreaTexture.getTextureData().isPrepared())
				this.drawingAreaTexture.getTextureData().prepare();
			
			// Get data
			Pixmap pixmap = this.drawingAreaTexture.getTextureData().consumePixmap();
			
			// Write color
			for (int x = -PEN_RADIUS; x < PEN_RADIUS; x++) 
			{
		        for (int y = -PEN_RADIUS; y < PEN_RADIUS; y++) 
		        {		        	
		        	int tempX = (int) (touchPos.x - drawingAreaPosX + x);
		        	int tempY = (int) (Main.getWindowHeight() - touchPos.y - (Main.getWindowHeight() - drawingAreaPosY - this.drawingAreaTexture.getHeight()) + y);
		        	
		        	// The drawing area size is WIDTH x WIDTH
		        	if(tempX < 0 || tempX > Main.getWindowWidth() - 1 || tempY < 0 || tempY > Main.getWindowWidth() - 1)
		        		continue;
		        	
		        	// Add color, if the pixel is within the circle
		        	float length = (float) Math.sqrt(x*x + y*y);
		        	float addCol = 1.0f * (length <= PEN_RADIUS ? 1.0f : 0.0f);
			        Color col = new Color(pixmap.getPixel(tempX, tempY));
			        col.r += addCol;
			        col.g += addCol;
			        col.b += addCol;
			        
			        // Clamp and set color
			        col.r = this.clamp(0.0f, 1.0f, col.r);
			        col.g = this.clamp(0.0f, 1.0f, col.g);
			        col.b = this.clamp(0.0f, 1.0f, col.b);
			        pixmap.setColor(col);
			        pixmap.drawPixel(tempX, tempY);
		        }
		    }
			
			// Reset and write data
			this.drawingAreaTexture.dispose();
			this.drawingAreaTexture = new Texture(pixmap, Format.RGB888, false);
		}
		else
		{
			if(this.isDrawing)
				this.mainGame.askNeuralNetwork();
			
			this.isDrawing = false;
		}
	}
	
	public void checkForClear()
	{
		if(Gdx.input.isTouched() && !this.isDrawing)
		{
			Vector2 touchPos = InputManager.getTouchPos(0);
			
			if(touchPos.y < this.drawingAreaPosY)
			{
				this.clearTexture(this.drawingAreaTexture);
				this.clearTexture(this.downsampledDrawingAreaTexture);
				
				// Remove AI guess
				this.mainGame.setCurrentAIGuess(-1);
			}
		}
	}
	
	public void render(SpriteBatch batch, BitmapFont font)
	{
		// Regular drawing area
		batch.draw(
			this.drawingAreaTexture, 
			this.drawingAreaPosX, 
			this.drawingAreaPosY, 
			1080, 
			1080
		);
		
		// Show downsampled drawing area
		if(!this.isDrawing)
		{
			batch.draw(
				this.downsampledDrawingAreaTexture, 
				this.drawingAreaPosX, 
				this.drawingAreaPosY, 
				1080, 
				1080
			);
		}

		// Clear text
		font.DrawString(batch, "Clear", new Vector2(Main.getWindowWidth() / 2, 350), new Vector2(10, 10));
	}
	
	public void dispose()
	{
		this.drawingAreaTexture.dispose();
		this.downsampledDrawingAreaTexture.dispose();
	}
	
	public ArrayList<Double> getDownsampledInputs()
	{
		// Create downsampled texture for input
		this.createDownsampledTexture();
		
		
		ArrayList<Double> inputs = new ArrayList<Double>();
		
		// Prepare data
		if(!this.downsampledDrawingAreaTexture.getTextureData().isPrepared())
			this.downsampledDrawingAreaTexture.getTextureData().prepare();
		
		// Collect data from downsampled texture
		Pixmap pixmap = this.downsampledDrawingAreaTexture.getTextureData().consumePixmap();
		for(int y = 0; y < this.downsampledDrawingAreaTexture.getHeight(); ++y)
		{
			for(int x = 0; x < this.downsampledDrawingAreaTexture.getWidth(); ++x)
			{
				Color tempCol = new Color(pixmap.getPixel(x, y));
				
				// Grey-scale value from red channel
				double interpretedColor = tempCol.r;
				
				inputs.add(interpretedColor);
			}
		}
		
		return inputs;
	}
	
	public float clamp(float minVal, float maxVal, float val)
	{
		if(val < minVal)
			return minVal;
		else if(val > maxVal)
			return maxVal;
		
		return val;
	}
	
	public boolean getIsDrawing() { return this.isDrawing; }
}
