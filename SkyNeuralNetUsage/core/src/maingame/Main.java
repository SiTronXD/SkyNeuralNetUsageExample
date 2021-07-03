package maingame;

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
	private static final int WIDTH = 1080;
	private static final int HEIGHT = 2280;

	private static OrthographicCamera cam;
	private static Viewport gamePort;
	
	SpriteBatch batch;
	BitmapFont font;

	Texture bitmapFontTexture;
	
	DrawingArea drawingArea;
	
	NeuralNet neuralNet;
	
	int currentAIGuess;
	
	@Override
	public void create () 
	{
		this.batch = new SpriteBatch();
		this.bitmapFontTexture = new Texture("bitmapFont.png");
		
		// Drawing area object
		this.drawingArea = new DrawingArea(this);
		
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
		
		// Neural net, responsible for forward propagation
		this.neuralNet = new NeuralNet("SkyNeuralNetSettings.ini");
		this.currentAIGuess = -1;
	}

	public void askNeuralNetwork()
	{	
		// Input
		ArrayList<Double> inputs = drawingArea.getDownsampledInputs();
		this.neuralNet.forwardProp(inputs);
		
		// Get output
		ArrayList<Double> outputs = new ArrayList<Double>();
		this.neuralNet.getOutputs(outputs);
		
		// Find best guess
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
	
	@Override
	public void render () 
	{
		// Update drawing area
		this.drawingArea.drawingOnArea();
		this.drawingArea.checkForClear();
		
		// Render
		ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);

		// ----- Begin -----
		this.batch.setProjectionMatrix(cam.combined);
		this.batch.begin();
		
		// Render drawing area
		this.drawingArea.render(this.batch, this.font);
		
		// Guess text
		this.font.DrawString(
			this.batch, 
			"AI Guess:", 
			new Vector2(WIDTH / 2, HEIGHT - 150), 
			new Vector2(10, 10)
		);
		this.font.DrawString(
			this.batch, 
			this.currentAIGuess < 0 || this.drawingArea.getIsDrawing() ? "..." : "" + this.currentAIGuess, 
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
		this.drawingArea.dispose();
		
		this.batch.dispose();
		this.bitmapFontTexture.dispose();
	}
	
	public void resize(int w, int h) 
	{
		gamePort.update(w, h);
		gamePort.apply(true);
	}
	
	public void setCurrentAIGuess(int newGuess)
	{
		this.currentAIGuess = newGuess;
	}
	
	public static OrthographicCamera getCam() { return cam; }
	public static Viewport getGamePort() { return gamePort; }
	
	public static int getWindowWidth() { return WIDTH; }
	public static int getWindowHeight() { return HEIGHT; }
}
