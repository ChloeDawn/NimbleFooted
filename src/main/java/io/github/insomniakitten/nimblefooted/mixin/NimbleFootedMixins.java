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
import net.minecraft.Bootstrap;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

final class NimbleFootedMixins {
  private NimbleFootedMixins() {}

  @Environment(EnvType.CLIENT)
  @Mixin(Bootstrap.class)
  private abstract static class BootstrapMixin {
    /**
     * Circumvents error logging for our potentially missing translation key. The translation key
     * should only ever be absent if Fabric API is absent, but we ensure that the API is absent
     * regardless for sanity checking. This way, if the mod somehow ends up in an illegal state
     * where the API is present and the translation key is not, an error will still be logged.
     *
     * @author InsomniaKitten
     */
    @Inject(
      // FIXME: Plugin cannot identify synthetic methods
      method = "Lnet/minecraft/Bootstrap;method_17596(Ljava/lang/String;)V",
      at = @At(value = "CONSTANT", args = "stringValue=Missing translations: "),
      cancellable = true
    )
    private static void nimblefooted$skipLogging(final String translationKey, final CallbackInfo callbackInfo) {
      if (NimbleFooted.isMissingApi() && "enchantment.nimblefooted.nimble_footed".equals(translationKey)) {
        callbackInfo.cancel();
      }
    }
  }

  @Environment(EnvType.CLIENT)
  @Mixin(ClientPlayerEntity.class)
  private abstract static class ClientPlayerEntityMixin extends LivingEntity {
    /**
     * The client player's nimble-footed processor. This exists as a data class as to
     * produce a minimal injection footprint into the target class' field declarations,
     * and also ensures all execution is handled in the context of the processor.
     *
     * @author InsomniaKitten
     */
    private final Runnable nimblefooted$processor =
      NimbleFooted.constructProcessor((ClientPlayerEntity) (Object) this);

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

  @Mixin(Enchantment.class)
  @SuppressWarnings("RedundantCast")
  private abstract static class EnchantmentMixin {
    /**
     * Replaces the translation key text component with an {@link StringTextComponent} if
     * the enchantment is our nimble-footed enchantment and the Fabric API is not loaded.
     * This prevents the in-game tooltip from showing an untranslated string in the tooltip;
     * it is preferable to have a readable string when translations cannot be loaded for the mod.
     *
     * @author InsomniaKitten
     */
    @ModifyVariable(
      method = "Lnet/minecraft/enchantment/Enchantment;getTextComponent(I)Lnet/minecraft/text/TextComponent;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isCursed()Z")
    )
    private TextComponent nimblefooted$modifyTextComponent(final TextComponent component) {
      if (NimbleFooted.isMissingApi() && (Object) this == NimbleFooted.getEnchantment()) {
        return new StringTextComponent("Nimble-footed");
      }
      return component;
    }
  }

  @Mixin(Enchantments.class)
  private abstract static class EnchantmentsMixin {
    @Shadow
    private static Enchantment register(final String id, final Enchantment enchantment) {
      throw new AssertionError("Not callable");
    }

    /**
     * Registers the nimble-footed enchantment to the game's enchantment registry, immediately
     * after vanilla enchantments have been registered statically in {@link Enchantments}.
     *
     * @author InsomniaKitten
     */
    // FIXME: Plugin does not recognize static init
    @Inject(method = "Lnet/minecraft/enchantment/Enchantments;<clinit>()V", at = @At("TAIL"))
    private static void nimblefooted$registerEnchantment(final CallbackInfo callbackInfo) {
      NimbleFooted.registerEnchantment(EnchantmentsMixin::register);
    }
  }
}
