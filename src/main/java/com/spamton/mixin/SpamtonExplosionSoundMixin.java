package com.spamton.mixin;

import com.spamton.entity.SpamtonDamageHandler;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Scales explosion sound volume when set by Spamton mini explosion (volume / batch size).
 */
@Mixin(Level.class)
public class SpamtonExplosionSoundMixin {

    @ModifyVariable(
            method = "playSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float scaleMiniExplosionVolumeAtPos(float volume) {
        Float scale = SpamtonDamageHandler.consumeNextMiniExplosionVolumeScale();
        return scale != null ? volume * scale : volume;
    }
}
