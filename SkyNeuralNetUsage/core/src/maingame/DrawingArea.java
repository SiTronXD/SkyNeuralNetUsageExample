package maingame;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;

public class DrawingArea 
{
	private static final int PEN_RADIUS = 50;

	private Texture drawingAreaTexture;
	private Texture downsampledDrawingAreaTexture;

	private Main mainGame;
	
	private Vector2 lastDrawPos;
	
	private int drawingAreaPosX;
	private int drawingAreaPosY;

	private boolean isDrawing;
	
	public DrawingArea(Main mainGame)
	{
		this.mainGame = mainGame;
		
		// Load black textures containing correct resolutions
		this.drawingAreaTexture = new Texture("BlackBackground_1080x1080.jpg");
		this.downsampledDrawingAreaTexture = new Texture("BlackBackground_28x28.jpg");
		
		this.drawingAreaPosX = 0;
		this.drawingAreaPosY = 700;
		this.isDrawing = false;
		
		lastDrawPos = new Vector2(-99, -99);
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
	
	// Get distance from point to line segment
	float distToLineSeg(Vector2 p, Vector2 a, Vector2 b)
	{
		// Get deltas
		Vector2 pb = new Vector2(p).sub(b);
	    Vector2 ab = new Vector2(a).sub(b);
	    
	    // Project point onto line segment
	    float t = Vector2.dot(pb.x, pb.y, ab.x, ab.y) / Vector2.dot(ab.x, ab.y, ab.x, ab.y);
	    t = this.clamp(t, 0.0f, 1.0f);
	    
	    // Interpolate to find closest point
	    Vector2 closestPoint = a.scl(t).add(b.scl((1.0f - t)));
	    
	    // Length between point and closest point
	    Vector2 delta = new Vector2(closestPoint).sub(p);
	    return Vector2.len(delta.x, delta.y);
	}
	
	private Vector2 viewportToDrawingAreaPos(Vector2 inputPos)
	{
		return this.viewportToDrawingAreaPos(inputPos.x, inputPos.y); 
	}
	private Vector2 viewportToDrawingAreaPos(float x, float y)
	{
		Vector2 newPos = new Vector2(
			x - drawingAreaPosX, 
			this.drawingAreaTexture.getHeight() - (y - drawingAreaPosY)
		);
		return newPos; 
	}
	
	private void drawCircle(Vector2 position, Pixmap pixmap)
	{
		for (int x = -PEN_RADIUS; x < PEN_RADIUS; x++) 
		{
	        for (int y = -PEN_RADIUS; y < PEN_RADIUS; y++) 
	        {	
	        	Vector2 tempPos = viewportToDrawingAreaPos(x + position.x, y + position.y);

	        	// Make sure the position is within the drawing area
	        	if(	tempPos.x < 0 || tempPos.x > this.drawingAreaTexture.getWidth() - 1 || 
	        		tempPos.y < 0 || tempPos.y > this.drawingAreaTexture.getHeight() - 1)
		        		continue;
	        	
	        	// Add color, if the pixel is within the circle
	        	float addCol = x*x + y*y <= PEN_RADIUS * PEN_RADIUS ? 1.0f : 0.0f;
		        Color col = new Color(pixmap.getPixel((int) tempPos.x, (int) tempPos.y));
		        col.r += addCol;
		        col.g += addCol;
		        col.b += addCol;
		        
		        // Clamp and set color
		        col.r = this.clamp(col.r, 0.0f, 1.0f);
		        col.g = this.clamp(col.g, 0.0f, 1.0f);
		        col.b = this.clamp(col.b, 0.0f, 1.0f);
		        pixmap.setColor(col);
		        pixmap.drawPixel((int) tempPos.x, (int) tempPos.y);
	        }
	    }
	}
	
	private void drawLine(Vector2 position1, Vector2 position2, Pixmap pixmap)
	{
		// Bounds to contain line
		Vector2 minPos = new Vector2(Math.min(position1.x, position2.x), Math.min(position1.y, position2.y)).sub(PEN_RADIUS, PEN_RADIUS);
		Vector2 maxPos = new Vector2(Math.max(position1.x, position2.x), Math.max(position1.y, position2.y)).add(PEN_RADIUS, PEN_RADIUS);
		
		for (int x = 0; x < maxPos.x - minPos.x; ++x) 
		{
	        for (int y = 0; y < maxPos.y - minPos.y; ++y) 
	        {		        	
	        	Vector2 tempPos = viewportToDrawingAreaPos(x + minPos.x, y + minPos.y);
	        	
	        	// Make sure the position is within the drawing area
	        	if(	tempPos.x < 0 || tempPos.x > this.drawingAreaTexture.getWidth() - 1 || 
	        		tempPos.y < 0 || tempPos.y > this.drawingAreaTexture.getHeight() - 1)
	        		continue;
	        	
	        	// Add color, if the pixel is within the line segment
	        	float addCol = this.distToLineSeg(
	        		tempPos, 
        			this.viewportToDrawingAreaPos(position1), 
        			this.viewportToDrawingAreaPos(position2)
        			) <= PEN_RADIUS ? 1.0f : 0.0f;
		        Color col = new Color(pixmap.getPixel((int) tempPos.x, (int) tempPos.y));
		        col.r += addCol;
		        col.g += addCol;
		        col.b += addCol;
		        
		        // Clamp and set color
		        col.r = this.clamp(col.r, 0.0f, 1.0f);
		        col.g = this.clamp(col.g, 0.0f, 1.0f);
		        col.b = this.clamp(col.b, 0.0f, 1.0f);
		        pixmap.setColor(col);
		        pixmap.drawPixel((int) tempPos.x, (int) tempPos.y);
	        }
	    }
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
			if(lastDrawPos.x < 0.0)
				this.drawCircle(touchPos, pixmap);
			else
				this.drawLine(touchPos, this.lastDrawPos, pixmap);
			
			// Reset and write data
			this.drawingAreaTexture.dispose();
			this.drawingAreaTexture = new Texture(pixmap, Format.RGB888, false);

			this.lastDrawPos = touchPos;
		}
		else
		{
			if(this.isDrawing)
				this.mainGame.askNeuralNetwork();
			
			this.isDrawing = false;
			
			lastDrawPos = new Vector2(-99, -99);
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
		/*if(!this.isDrawing)
		{
			batch.draw(
				this.downsampledDrawingAreaTexture, 
				this.drawingAreaPosX, 
				this.drawingAreaPosY, 
				1080, 
				1080
			);
		}*/

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
	
	public float clamp(float val, float minVal, float maxVal)
	{
		if(val < minVal)
			return minVal;
		else if(val > maxVal)
			return maxVal;
		
		return val;
	}
	
	public boolean getIsDrawing() { return this.isDrawing; }
}
