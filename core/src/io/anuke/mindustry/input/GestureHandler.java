package io.anuke.mindustry.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import io.anuke.mindustry.Vars;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Inputs;
import io.anuke.ucore.scene.ui.layout.Unit;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.*;

public class GestureHandler extends GestureAdapter{
	AndroidInput input;
	Vector2 pinch1 = new Vector2(-1, -1), pinch2 = pinch1.cpy();
	Vector2 vector = new Vector2();
	float initzoom = -1;
	boolean zoomed = false;
	
	GestureHandler(AndroidInput input){
		this.input = input;
	}
	
	@Override
	public boolean longPress(float x, float y){
		return false;
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		if(ui.hasMouse() || input.brokeBlock) return false;
		
		if(!control.getInput().placeMode.pan || control.getInput().recipe == null){
			input.mousex = x;
			input.mousey = y;
			
			if(control.getInput().recipe == null)
				control.getInput().breakMode.tapped(input.getBlockX(), input.getBlockY());
			else
				control.getInput().placeMode.tapped(input.getBlockX(), input.getBlockY());
		}
		return false;
	}
	
	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY){
		if(Vars.control.showCursor() && !Inputs.keyDown("select")) return false;

		if(!Vars.control.showCursor() && !(control.getInput().recipe != null && Vars.control.hasItems(control.getInput().recipe.requirements) && control.getInput().placeMode.lockCamera) &&
				!(control.getInput().recipe == null && control.getInput().breakMode.lockCamera)){
			float dx = deltaX*Core.camera.zoom/Core.cameraScale, dy = deltaY*Core.camera.zoom/Core.cameraScale;
			player.x -= dx;
			player.y += dy;
			player.targetAngle = Mathf.atan2(dx, -dy);
		}else if(control.getInput().placeMode.lockCamera && (control.getInput().placeMode.pan && control.getInput().recipe != null)){
			input.mousex += deltaX;
			input.mousey += deltaY;
		}
		
		return false;
	}
	
	@Override
	public boolean pinch (Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		if(control.getInput().recipe == null && !control.getInput().breakMode.lockCamera)
			return false;
		
		if(pinch1.x < 0){
			pinch1.set(initialPointer1);
			pinch2.set(initialPointer2);
		}
		
		Vector2 vec = (vector.set(pointer1).add(pointer2).scl(0.5f)).sub(pinch1.add(pinch2).scl(0.5f));
		
		player.x -= vec.x*Core.camera.zoom/Core.cameraScale;
		player.y += vec.y*Core.camera.zoom/Core.cameraScale;
		
		pinch1.set(pointer1);
		pinch2.set(pointer2);
		
		return false;
	}
	
	@Override
	public boolean zoom(float initialDistance, float distance){
		if(initzoom < 0){
			initzoom = initialDistance;
		}
		
		if(Math.abs(distance - initzoom) > Unit.dp.scl(100f) && !zoomed){
			int amount = (distance > initzoom ? 1 : -1);
			renderer.scaleCamera(Math.round(Unit.dp.scl(amount)));
			initzoom = distance;
			zoomed = true;
			return true;
		}
		
		return false;
	}
	
	@Override
	public void pinchStop () {
		initzoom = -1;
		pinch2.set(pinch1.set(-1, -1));
		zoomed = false;
	}
	
	int touches(){
		int sum = 0;
		for(int i = 0; i < 10; i ++){
			if(Gdx.input.isTouched(i)) sum++;
		}
		return sum;
	}
}
