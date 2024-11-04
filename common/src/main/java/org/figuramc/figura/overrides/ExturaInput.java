package org.figuramc.figura.overrides;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.client.Options;

@Environment(EnvType.CLIENT)

public class ExturaInput extends Input {
   private final Options options;

   public ExturaInput(Options options) {
	  this.options = options;
   }
   private static float calculateImpulse(boolean forward, boolean back) {
	  return (forward == back ? .0F : forward ? 1.0F : -1.0F);
   }
	public int upOverride = 0;
	public int downOverride = 0;
	public int leftOverride = 0;
	public int rightOverride = 0;
	public int jumpOverride = 0;
	public int shiftOverride = 0;
	public boolean useVec = false;
	public double moveX = 0.;
	public double moveY = 0.;
	@Override
	public void tick(boolean mult, float multAmount) {
		if (useVec){
			this.up = false;
			this.down = false;
			this.left = false;
			this.right = false;
			this.jumping = jumpOverride != 0 ? jumpOverride == 2 : this.options.keyJump.isDown();
			this.shiftKeyDown = shiftOverride != 0 ? shiftOverride == 2 : this.options.keyShift.isDown();
			return;
		}
		this.up = upOverride != 0 ? upOverride == 2 : this.options.keyUp.isDown();
		this.down = downOverride != 0 ? downOverride == 2 : this.options.keyDown.isDown();
		this.left = leftOverride != 0 ? leftOverride == 2 : this.options.keyLeft.isDown();
		this.right = rightOverride != 0 ? rightOverride == 2 : this.options.keyRight.isDown();
		this.jumping = jumpOverride != 0 ? jumpOverride == 2 : this.options.keyJump.isDown();
		this.shiftKeyDown = shiftOverride != 0 ? shiftOverride == 2 : this.options.keyShift.isDown();
		this.forwardImpulse = calculateImpulse(this.up, this.down);
		this.leftImpulse = calculateImpulse(this.left, this.right);
		if (mult) {
			this.leftImpulse *= multAmount;
			this.forwardImpulse *= multAmount;
		}
	}
	@Override
	public Vec2 getMoveVector() {
	  return useVec ? Vec2(moveX, moveY) :new Vec2(this.leftImpulse, this.forwardImpulse);
   }
}
