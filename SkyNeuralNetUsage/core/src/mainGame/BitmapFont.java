package mainGame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class BitmapFont {
	Texture bitmapTexture;
	
	final String[] characterOrder;
	
	Vector2 characterCutoutSize;
	Rectangle[][] characterCutoutRect;
	
	TextureRegion[][] characterTexture;
	
	int characterSpace;
	
	public BitmapFont(Texture bitmapTexture, String[] characterOrder, Vector2 characterCutoutSize, int characterSpace)
	{
		this.bitmapTexture = bitmapTexture;
		this.characterOrder = characterOrder;
		this.characterCutoutSize = characterCutoutSize;
		this.characterSpace = characterSpace;
		
		characterCutoutRect = new Rectangle[characterOrder[0].length()][characterOrder.length];
		characterTexture = new TextureRegion[characterOrder[0].length()][characterOrder.length];
		
		for(int y = 0; y < characterCutoutRect[0].length; y++)
		{
			for(int x = 0; x < characterCutoutRect.length; x++)
			{
				characterCutoutRect[x][y] = new Rectangle(0, 0, characterCutoutSize.x, characterCutoutSize.y);
				characterTexture[x][y] = new TextureRegion(
						bitmapTexture, 
						(int) (x * characterCutoutSize.x + characterCutoutRect[x][y].x), 
						(int) (y * characterCutoutSize.y + characterCutoutRect[x][y].y), 
						(int) characterCutoutRect[x][y].width, 
						(int) characterCutoutRect[x][y].height
						);
			}
		}
		
		CalculateCutouts();
	}
	
	private void CalculateCutouts()
	{
		if(!bitmapTexture.getTextureData().isPrepared())
			bitmapTexture.getTextureData().prepare();
		
		Pixmap pixmap = bitmapTexture.getTextureData().consumePixmap();
		
		for(int y = 0; y < characterCutoutRect[0].length; y++)
		{
			for(int x = 0; x < characterCutoutRect.length; x++)
			{
				int minX = (int) characterCutoutSize.x-1;
				int minY = (int) characterCutoutSize.y-1;
				int maxX = 0;
				int maxY = 0;
				boolean found = false;
				
				for(int py = 0; py < characterCutoutSize.y; py++)
				{
					for(int px = 0; px < characterCutoutSize.x; px++)
					{
						Color pixColor = new Color(
									pixmap.getPixel(
										(int) (x * characterCutoutSize.x) + px, 
										(int) (y * characterCutoutSize.y) + py
									)
								);
						
						//Transparent pixels
						if(pixColor.a >= 1)
						{
							minX = Math.min(minX, px);
							minY = Math.min(minY, py);
							maxX = Math.max(maxX, px);
							maxY = Math.max(maxY, py);
							
							found = true;
						}
					}
				}
				
				//Done with one single character from the picture
				if(found)
				{
					characterCutoutRect[x][y] = new Rectangle(
							minX, 
							minY, 
							maxX - minX + 1, 
							maxY - minY + 1
						);
				}
				
				
				characterTexture[x][y] = new TextureRegion(
						bitmapTexture, 
						(int) (x * characterCutoutSize.x + characterCutoutRect[x][y].x), 
						(int) (y * characterCutoutSize.y + characterCutoutRect[x][y].y), 
						(int) characterCutoutRect[x][y].width, 
						(int) characterCutoutRect[x][y].height
						);
			}
		}
	}
	
	/*
	public BitmapFont(Texture bitmapTexture, String[] characterOrder, Vector2 characterCutoutSize, int characterSpace, Rectangle[][] characterCutoutRect)
	{
		this.bitmapTexture = bitmapTexture;
		this.characterOrder = characterOrder;
		this.characterCutoutSize = characterCutoutSize;
		this.characterSpace = characterSpace;
		this.characterCutoutRect = characterCutoutRect;
	}
	*/
	
	public void DrawString(SpriteBatch spriteBatch, String text, Vector2 position, Vector2 characterScale)
	{
		//DrawString(spriteBatch, text, position, characterSize, 99999);
		
		//int wholeWidth = (int) ((text.length() * characterSize.x) + (text.length() - 1) * characterSpace);
		
		int wholeWidth = 0;
		
		for(int i = 0; i < text.length(); i++)
		{
			for(int y = 0; y < characterOrder.length; y++)
			{
				for(int x = 0; x < characterOrder[y].length(); x++)
				{
					String c = text.substring(i, i+1);
					
					if(c.equalsIgnoreCase(characterOrder[y].substring(x, x+1)))
						if(i > 0)
							wholeWidth += (characterCutoutRect[x][y].width * characterScale.x) + characterSpace;
						else
							wholeWidth += (characterCutoutRect[x][y].width * characterScale.x);
				}
			}
		}
		
		//Draws the text
		int currentTextSize = 0;
		
		for(int i = 0; i < text.length(); i++)
		{
			for(int y = 0; y < characterOrder.length; y++)
			{
				for(int x = 0; x < characterOrder[y].length(); x++)
				{
					String c = text.substring(i, i+1);
					
					if(c.equalsIgnoreCase(characterOrder[y].substring(x, x+1)))
					{
						spriteBatch.draw(
								characterTexture[x][y], 
								position.x + currentTextSize - wholeWidth/2, 
								(position.y) - (characterCutoutSize.y/2 * characterScale.y), 
								characterCutoutRect[x][y].width * characterScale.x, 
								characterCutoutRect[x][y].height * characterScale.y
								);
						
						currentTextSize += (characterCutoutRect[x][y].width * characterScale.x) + characterSpace;
					}
				}
			}
		}
	}
	
	public void DrawString(SpriteBatch spriteBatch, String text, Vector2 position, Vector2 characterScale, float maxWidth)
	{
		float wholeWidth = 0;
		
		for(int i = 0; i < text.length(); i++)
		{
			for(int y = 0; y < characterOrder.length; y++)
			{
				for(int x = 0; x < characterOrder[y].length(); x++)
				{
					String c = text.substring(i, i+1);
					
					if(c.equalsIgnoreCase(characterOrder[y].substring(x, x+1)))
						wholeWidth += (characterCutoutRect[x][y].width * characterScale.x) + characterSpace;
				}
			}
		}
		
		float tempScale = 1;
		
		if(wholeWidth > maxWidth)
		{
			tempScale = (float) (maxWidth / wholeWidth);
			wholeWidth = maxWidth;
		}
		
		int currentTextSize = 0;
		
		for(int i = 0; i < text.length(); i++)
		{
			for(int y = 0; y < characterOrder.length; y++)
			{
				for(int x = 0; x < characterOrder[y].length(); x++)
				{
					String c = text.substring(i, i+1);
					
					if(c.equals(characterOrder[y].substring(x, x+1)))
					{
						//Actually draws
						spriteBatch.draw(
								characterTexture[x][y], 
								position.x + currentTextSize - wholeWidth/2, 
								(position.y) - (characterCutoutSize.y/2 * characterScale.y * tempScale), 
								characterCutoutRect[x][y].width * characterScale.x * tempScale, 
								characterCutoutRect[x][y].height * characterScale.y * tempScale
								);
						
						currentTextSize += (characterCutoutRect[x][y].width * characterScale.x * tempScale) + characterSpace;
					}
				}
			}
		}
	}
	
	public void SetCharacterCutoutRectangle(String characterToSet, Rectangle newRect)
	{
		for(int y = 0; y < characterOrder.length; y++)
		{
			for(int x = 0; x < characterOrder[y].length(); x++)
			{
				//Found character to set rect of
				if(characterToSet.equals(characterOrder[y].substring(x, x+1)))
				{
					characterCutoutRect[x][y] = newRect;
					
					characterTexture[x][y] = new TextureRegion(
							bitmapTexture, 
							(int) (x * characterCutoutSize.x + characterCutoutRect[x][y].x), 
							(int) (y * characterCutoutSize.y + characterCutoutRect[x][y].y), 
							(int) characterCutoutRect[x][y].width, 
							(int) characterCutoutRect[x][y].height
							);
					
					return;
				}
			}
		}
	}
}
