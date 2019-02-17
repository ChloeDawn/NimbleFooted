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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class for {@link Enchantments}
 *
 * @author InsomniaKitten
 * @see Enchantments
 */
@Mixin(Enchantments.class)
abstract class EnchantmentsMixin {
  @Deprecated
  private EnchantmentsMixin() {}

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
