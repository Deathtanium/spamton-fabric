package com.spamton.mixin;

import com.spamton.entity.SpamtonSpawner;
import com.spamton.entity.SpamtonSpawnerData;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(Villager.class)
public class SpamtonGlassesPersistenceMixin {

    private static final String KEY_GLASSES_LEFT = "SpamtonGlassesLeft";
    private static final String KEY_GLASSES_RIGHT = "SpamtonGlassesRight";

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveGlassesUuids(ValueOutput output, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        if (!self.getTags().contains(SpamtonSpawner.TAG_SPAMTON_MERCHANT)) return;
        UUID leftId = SpamtonSpawnerData.getGlassesLeft(self.getUUID());
        UUID rightId = SpamtonSpawnerData.getGlassesRight(self.getUUID());
        if (leftId != null && rightId != null) {
            output.putString(KEY_GLASSES_LEFT, leftId.toString());
            output.putString(KEY_GLASSES_RIGHT, rightId.toString());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadGlassesUuids(ValueInput input, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        Optional<String> leftOpt = input.getString(KEY_GLASSES_LEFT);
        Optional<String> rightOpt = input.getString(KEY_GLASSES_RIGHT);
        if (leftOpt.isPresent() && rightOpt.isPresent()) {
            try {
                UUID leftId = UUID.fromString(leftOpt.get());
                UUID rightId = UUID.fromString(rightOpt.get());
                SpamtonSpawnerData.setGlasses(self.getUUID(), leftId, rightId);
            } catch (IllegalArgumentException ignored) {
                // Invalid UUID strings; skip restore
            }
        }
    }
}
