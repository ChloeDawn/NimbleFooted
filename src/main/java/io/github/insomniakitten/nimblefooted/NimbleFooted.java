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

package io.github.insomniakitten.nimblefooted;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkState;

/**
 * NimbleFooted's main class
 * Provides callback hooks and utility functions
 *
 * @author InsomniaKitten
 */
public final class NimbleFooted {
  public static final String ID = "nimblefooted";

  private static final Logger LOGGER = LogManager.getLogger("NimbleFooted");

  private static final float ELEVATED_STEP_HEIGHT = 1.25F;

  @Nullable private static Boolean missingApi;

  private static boolean registeredEnchantment;
  private static boolean constructedProcessor;

  private NimbleFooted() {
    throw new UnsupportedOperationException("Please rethink your priorities");
  }

  /**
   * The elevated step height for players. Currently, this is a fixed
   * constant, but it is planned to support determining this value
   * dynamically, in case mods ever wish to modify the player's
   * elevated step height. // TODO
   *
   * @author InsomniaKitten
   */
  public static float getElevatedStepHeight() {
    return ELEVATED_STEP_HEIGHT;
  }

  /**
   * Determines if the Fabric API is absent in the loaded mods list. This
   * method must not be called to early, as we need to ensure mods have
   * been loaded by Fabric. Unfortunately, there is currently no way to
   * check the loader's state. // TODO
   *
   * @author InsomniaKitten
   * @see FabricLoader#isModLoaded
   */
  public static boolean isMissingApi() {
    if (missingApi == null) {
      missingApi = !FabricLoader.getInstance().isModLoaded("fabric");
    }
    return missingApi;
  }

  /**
   * Registers the nimble-footed enchantment using the given registrar function. This
   * method is intended for mixin callback invocation, and should not be called normally.
   *
   * @throws IllegalStateException If the enchantment is already registered
   * @author InsomniaKitten
   */
  public static void registerEnchantment(final BiConsumer<String, Enchantment> registrar) {
    checkState(!registeredEnchantment, "Enchantment already registered!");
    LOGGER.debug("Registering enchantment with registrar {}", registrar);
    final String id = new Identifier(ID, "nimble_footed").toString();
    registrar.accept(id, NimbleFootedEnchantment.getInstance());
    registeredEnchantment = true;
  }

  /**
   * Constructs a nimble-footed processor for the given player entity. This method
   * is intended for mixin callback invocation, and should not be called normally.
   *
   * @param entity The client player entity to construct a processor for
   * @return A {@link NimbleFootedProcessor} exposed as {@link Runnable}
   * @throws IllegalStateException If a processor has already been constructed
   * @author InsomniaKitten
   */
  @Nonnull
  @Environment(EnvType.CLIENT)
  public static Runnable constructProcessor(final ClientPlayerEntity entity) {
    checkState(!constructedProcessor, "Processor already constructed! (%s)", entity);
    LOGGER.debug("Constructing processor for client player entity {}", entity);
    final Runnable processor = new NimbleFootedProcessor(entity);
    constructedProcessor = true;
    return processor;
  }

  /**
   * Determines if the given entity is wearing an item in their {@link EquipmentSlot#FEET}
   * that is attributed with a nimble-footed {@link NimbleFootedEnchantment} enchantment.
   *
   * @param entity The living entity to be queried
   * @throws IllegalStateException If the enchantment is not yet registered
   * @author InsomniaKitten
   */
  public static boolean isNimbleFooted(final LivingEntity entity) {
    checkState(registeredEnchantment, "Enchantment not registered!");
    final ItemStack stack = entity.getEquippedStack(EquipmentSlot.FEET);
    return EnchantmentHelper.getLevel(NimbleFootedEnchantment.getInstance(), stack) > 0;
  }
}
