package mainGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class InputManager 
{
	public static void lookForEscape()
	{
		// Exit when escape is pressed, for debugging
		/*if(Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			Gdx.app.exit();*/
	}
	
	public static Vector2 getTouchPos(int touchIndex)
	{
		Vector3 p = new Vector3(Gdx.input.getX(touchIndex), Gdx.input.getY(touchIndex), 0);
		Main.getGamePort().unproject(p);

		return new Vector2(p.x, p.y);
	}
}
