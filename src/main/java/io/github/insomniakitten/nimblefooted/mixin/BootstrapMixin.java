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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client mixin class for {@link Bootstrap}
 *
 * @author InsomniaKitten
 * @see Bootstrap
 */
@Environment(EnvType.CLIENT)
@Mixin(Bootstrap.class)
abstract class BootstrapMixin {
  @Deprecated
  private BootstrapMixin() {}

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
