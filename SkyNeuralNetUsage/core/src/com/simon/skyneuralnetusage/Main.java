package com.simon.skyneuralnetusage;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter 
{
	public static final int WIDTH = 1080;
	public static final int HEIGHT = 2280;
	
	SpriteBatch batch;
	Texture drawingAreaTexture;
	
	int drawingAreaPosX;
	int drawingAreaPosY;
	
	private static OrthographicCamera cam;
	private static Viewport gamePort;
	
	@Override
	public void create () 
	{
		this.batch = new SpriteBatch();
		this.drawingAreaTexture = new Texture("BlackBackground_1080x1080.jpg");
		
		// Camera
		cam = new OrthographicCamera();
		cam.translate(WIDTH / 2, HEIGHT / 2);
		cam.setToOrtho(false);
		cam.update();
		gamePort = new FitViewport(WIDTH, HEIGHT, cam);
		

		this.drawingAreaPosX = 0;
		this.drawingAreaPosY = 700;
	}

	private float clamp(float minVal, float maxVal, float val)
	{
		if(val < minVal)
			return minVal;
		else if(val > maxVal)
			return maxVal;
		
		return val;
	}
	
	private void drawOnArea()
	{		
		if(Gdx.input.isTouched())
		{
			Vector2 touchPos = InputManager.getTouchPos(0);
			
			// Make sure texture data is "prepared"
			if(!this.drawingAreaTexture.getTextureData().isPrepared())
				this.drawingAreaTexture.getTextureData().prepare();
			
			// Get data
			Pixmap pixmap = this.drawingAreaTexture.getTextureData().consumePixmap();
			
			// Write color
			int r = 1080/8;
			for (int x = -r; x < r; x++) 
			{
		        for (int y = -r; y < r; y++) 
		        {		        	
		        	int tempX = (int) (touchPos.x - drawingAreaPosX + x);
		        	int tempY = (int) (HEIGHT - touchPos.y - (HEIGHT - drawingAreaPosY - this.drawingAreaTexture.getHeight()) + y);
		        	
		        	if(tempX < 0 || tempX > WIDTH - 1 || tempY < 0 || tempY > WIDTH - 1)
		        		continue;
		        	
		        	float length = (float) Math.sqrt(x*x + y*y);
		        	float addCol = 1.0f * (length <= r ? 1.0f : 0.0f);
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
	}
	
	@Override
	public void render () 
	{
		// Update
		InputManager.lookForEscape();
		this.drawOnArea();
		
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
		
		// ----- End -----
		this.batch.end();
	}
	
	@Override
	public void dispose () 
	{
		this.batch.dispose();
		this.drawingAreaTexture.dispose();
	}
	
	public void resize(int w, int h) 
	{
		gamePort.update(w, h);
		gamePort.apply(true);
	}
	
	public static OrthographicCamera getCam() { return cam; }
	public static Viewport getGamePort() { return gamePort; }
}
