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
   private static float calculateImpulse(boolean p_205578_, boolean p_205579_) {
      if (p_205578_ == p_205579_) {
         return 0.0F;
      } else {
         return p_205578_ ? 1.0F : -1.0F;
      }
   }
	public int upOverride = 0;
	public int downOverride = 0;
	public int leftOverride = 0;
	public int rightOverride = 0;
	public int jumpOverride = 0;
	public int shiftOverride = 0;
    @Override
   public void tick(boolean mult, float multAmount) {
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
}
