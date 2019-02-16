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

import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * The nimble-footed enchantment implementation class
 *
 * @author InsomniaKitten
 */
final class NimbleFootedEnchantment extends Enchantment {
  private static final Logger LOGGER = LogManager.getLogger("NimbleFootedEnchantment");
  private static final NimbleFootedEnchantment INSTANCE = new NimbleFootedEnchantment();

  private boolean shouldLogApiWarning = true;

  private NimbleFootedEnchantment() {
    super(Enchantment.Weight.UNCOMMON, EnchantmentTarget.WEARABLE, new EquipmentSlot[] { EquipmentSlot.FEET });
    if (FabricLauncherBase.getLauncher().isDevelopment()) {
      Arrays.stream(Thread.currentThread().getStackTrace()).skip(1).forEach(LOGGER::info);
    }
  }

  static NimbleFootedEnchantment getInstance() {
    return INSTANCE;
  }

  @Override // TODO Enchantment#getTextComponent mixin to avoid implementation recreation?
  public TextComponent getTextComponent(final int level) {
    if (NimbleFooted.isMissingApi()) {
      if (shouldLogApiWarning) {
        LOGGER.warn("Fabric API is required for localization, falling back to hardcoded strings");
        shouldLogApiWarning = false;
      }
      final TextComponent component = new StringTextComponent("Nimble-footed").applyFormat(TextFormat.GRAY);
      if (level != 1) {
        component.append(" ").append(new TranslatableTextComponent("enchantment.level." + level));
      }
      return component;
    }
    return super.getTextComponent(level);
  }

  @Override
  public String toString() {
    return "NimbleFootedEnchantment";
  }
}
