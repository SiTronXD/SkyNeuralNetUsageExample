package mainGame;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import neuralnet.NeuralNet;

public class Main extends ApplicationAdapter 
{
	public static final int WIDTH = 1080;
	public static final int HEIGHT = 2280;
	
	private static final int PEN_RADIUS = 50;

	private static OrthographicCamera cam;
	private static Viewport gamePort;
	
	SpriteBatch batch;
	BitmapFont font;

	Texture drawingAreaTexture;
	Texture downsampledDrawingAreaTexture;
	Texture bitmapFontTexture;
	
	NeuralNet neuralNet;
	
	int drawingAreaPosX;
	int drawingAreaPosY;
	
	int currentAIGuess;
	
	boolean isDrawing;
	
	@Override
	public void create () 
	{
		this.batch = new SpriteBatch();
		this.drawingAreaTexture = new Texture("BlackBackground_1080x1080.jpg");
		this.downsampledDrawingAreaTexture = new Texture("BlackBackground_28x28.jpg");
		this.bitmapFontTexture = new Texture("bitmapFont.png");
		
		// Camera
		cam = new OrthographicCamera();
		cam.translate(WIDTH / 2, HEIGHT / 2);
		cam.setToOrtho(false);
		cam.update();
		gamePort = new FitViewport(WIDTH, HEIGHT, cam);
		
		// Font
		String[] characterOrder = 
			{
			"abcdefghij",
			"klmnopqrst",
			"uvwxyz+-.'",
			"0123456789",
			"!?,<>:()#^",
			"@*% |XXXXX"
		};
		this.font = new BitmapFont(this.bitmapFontTexture, characterOrder, new Vector2(16, 16), 2);
		this.font.SetCharacterCutoutRectangle("'", new Rectangle(6, 0, 7, 16));
		this.font.SetCharacterCutoutRectangle(" ", new Rectangle(0, 0, 10, 16));

		// Drawing area
		this.drawingAreaPosX = 0;
		this.drawingAreaPosY = 700;
		this.isDrawing = false;
		
		this.currentAIGuess = -1;
		
		// Neural net
		this.neuralNet = new NeuralNet("SkyNeuralNetSettings.ini");
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
	
	private void askNeuralNetwork()
	{
		// Create downsampled texture for input
		this.createDownsampledTexture();
		
		// Input
		ArrayList<Double> inputs = new ArrayList<Double>();
		if(!this.downsampledDrawingAreaTexture.getTextureData().isPrepared())
			this.downsampledDrawingAreaTexture.getTextureData().prepare();
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
		
		this.neuralNet.forwardProp(inputs);
		
		// Get output
		ArrayList<Double> outputs = new ArrayList<Double>();
		this.neuralNet.getOutputs(outputs);
		
		// Get best guess
		double currentBestGuess = -99999.99;
		for(int i = 0; i < outputs.size(); ++i)
		{
			double currOutput = outputs.get(i); 
			
			if(currOutput > currentBestGuess)
			{
				currentBestGuess = currOutput;
				this.currentAIGuess = i; 
			}
		}
	}
	
	private void drawOnArea()
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
		        	int tempY = (int) (HEIGHT - touchPos.y - (HEIGHT - drawingAreaPosY - this.drawingAreaTexture.getHeight()) + y);
		        	
		        	if(tempX < 0 || tempX > WIDTH - 1 || tempY < 0 || tempY > WIDTH - 1)
		        		continue;
		        	
		        	float length = (float) Math.sqrt(x*x + y*y);
		        	float addCol = 1.0f * (length <= PEN_RADIUS ? 1.0f : 0.0f);
			        Color col = new Color(pixmap.getPixel(tempX, tempY));
			        col.r += addCol;
			        col.g += addCol;
			        col.b += addCol;
			        
			        
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
				this.askNeuralNetwork();
			
			this.isDrawing = false;
		}
	}
	
	private void clearTexture(Texture textureToClear)
	{
		// Make sure texture data is "prepared"
		if(!textureToClear.getTextureData().isPrepared())
			textureToClear.getTextureData().prepare();
		
		// Get data
		Pixmap pixmap = textureToClear.getTextureData().consumePixmap();
		
		// Write color
		for (int x = 0; x < WIDTH; x++) 
		{
	        for (int y = 0; y < HEIGHT; y++) 
	        {	
		        pixmap.setColor(Color.BLACK);
		        pixmap.drawPixel(x, y);
	        }
	    }
		
		// Reset and write data
		textureToClear.dispose();
		textureToClear = new Texture(pixmap, Format.RGB888, false);
	}
	
	private void checkForClear()
	{
		if(Gdx.input.isTouched() && !this.isDrawing)
		{
			Vector2 touchPos = InputManager.getTouchPos(0);
			
			if(touchPos.y < this.drawingAreaPosY)
			{
				this.clearTexture(this.drawingAreaTexture);
				this.clearTexture(this.downsampledDrawingAreaTexture);
				
				// Remove AI guess
				this.currentAIGuess = -1;
			}
		}
	}
	
	@Override
	public void render () 
	{
		// Update
		this.drawOnArea();
		this.checkForClear();
		
		// Render
		ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);

		// ----- Begin -----
		this.batch.setProjectionMatrix(cam.combined);
		this.batch.begin();
		
		// Drawing area
		this.batch.draw(
			this.drawingAreaTexture, 
			this.drawingAreaPosX, 
			this.drawingAreaPosY, 
			1080, 
			1080
		);
		if(!this.isDrawing)
		{
			this.batch.draw(
				this.downsampledDrawingAreaTexture, 
				this.drawingAreaPosX, 
				this.drawingAreaPosY, 
				1080, 
				1080
			);
		}
		
		// Clear text
		this.font.DrawString(this.batch, "Clear", new Vector2(WIDTH / 2, 350), new Vector2(10, 10));
		
		// Guess text
		this.font.DrawString(
			this.batch, 
			"AI Guess:", 
			new Vector2(WIDTH / 2, HEIGHT - 150), 
			new Vector2(10, 10)
		);
		this.font.DrawString(
			this.batch, 
			this.currentAIGuess < 0 || this.isDrawing ? "..." : "" + this.currentAIGuess, 
			new Vector2(WIDTH / 2, HEIGHT - 370), 
			new Vector2(10, 10)
		);
		
		// ----- End -----
		this.batch.end();
		

		InputManager.lookForEscape();
	}
	
	@Override
	public void dispose () 
	{
		this.batch.dispose();
		
		this.drawingAreaTexture.dispose();
		this.downsampledDrawingAreaTexture.dispose();
		this.bitmapFontTexture.dispose();
	}
	
	private float clamp(float minVal, float maxVal, float val)
	{
		if(val < minVal)
			return minVal;
		else if(val > maxVal)
			return maxVal;
		
		return val;
	}
	
	public void resize(int w, int h) 
	{
		gamePort.update(w, h);
		gamePort.apply(true);
	}
	
	public static OrthographicCamera getCam() { return cam; }
	public static Viewport getGamePort() { return gamePort; }
}
