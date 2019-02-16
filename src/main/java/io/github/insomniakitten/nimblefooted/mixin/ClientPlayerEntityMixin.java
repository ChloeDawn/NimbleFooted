/*
 * Copyright (C) 2018 InsomniaKitten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.insomniakitten.nimblefooted.mixin;

import io.github.insomniakitten.nimblefooted.NimbleFooted;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client mixin class for {@link ClientPlayerEntity}
 *
 * @author InsomniaKitten
 * @see ClientPlayerEntity
 */
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin extends LivingEntity {
  /**
   * The client player's nimble-footed processor. This exists as a data class as to
   * produce a minimal injection footprint into the target class' field declarations,
   * and also ensures all execution is handled in the context of the processor.
   *
   * @author InsomniaKitten
   */
  private final Runnable nimblefooted$processor =
    NimbleFooted.constructProcessor((ClientPlayerEntity) (Object) this);

  @Deprecated
  private ClientPlayerEntityMixin() {
    super(EntityType.PLAYER, null);
  }

  @Shadow
  public abstract boolean method_3149();

  /**
   * Checks and updates the client player's {@code nimblefooted$processor}. Only executed
   * if the player has auto-jump disabled, is on the ground, is not sneaking, and is not
   * riding a vehicle entity.
   *
   * @author InsomniaKitten
   */
  @Inject(
    method = "Lnet/minecraft/client/network/ClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;DDD)V", 
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;method_3148(FF)V")
  )
  private void nimblefooted$updateProcessor(final CallbackInfo callbackInfo) {
    if (!method_3149() && onGround && !isSneaking() && !hasVehicle()) {
      nimblefooted$processor.run();
    }
  }
}
