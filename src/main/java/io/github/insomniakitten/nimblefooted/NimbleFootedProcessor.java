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

import com.google.common.base.MoreObjects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;

import static com.google.common.base.Preconditions.checkState;

/**
 * The nimble-footed state processor for a given client player entity.
 * Handles tracking and restoration of the previous step height.
 * 
 * @author InsomniaKitten
 */
@Environment(EnvType.CLIENT)
final class NimbleFootedProcessor implements Runnable {
  private final float jumpHeight;
  private final LivingEntity clientPlayer;

  private float lastStepHeight = Float.NaN;
  private boolean wasNimbleFooted = false;

  NimbleFootedProcessor(final ClientPlayerEntity player) {
    jumpHeight = NimbleFooted.getDefaultJumpHeight();
    clientPlayer = player;
  }

  @Override
  public void run() {
    if (isNimbleFooted()) {
      if (!wasNimbleFooted) {
        lastStepHeight = clientPlayer.stepHeight;
        clientPlayer.stepHeight = jumpHeight;
        wasNimbleFooted = true;
      } else if (jumpHeight != clientPlayer.stepHeight) {
        lastStepHeight = clientPlayer.stepHeight;
      }
    } else if (wasNimbleFooted) {
      checkState(!Float.isNaN(lastStepHeight), "wasNimbleFooted was true but lastStepHeight is absent (NaN)");
      clientPlayer.stepHeight = lastStepHeight;
      wasNimbleFooted = false;
    }
  }

  @Override
  public String toString() {
    String isNimbleFooted; // TODO Remove?
    try {
      isNimbleFooted = String.valueOf(isNimbleFooted());
    } catch (final IllegalStateException e) {
      isNimbleFooted = "<error>";
    }
    return MoreObjects.toStringHelper(this)
      .add("clientPlayer", clientPlayer)
      .add("stepHeight", clientPlayer.stepHeight)
      .add("lastStepHeight", lastStepHeight)
      .add("isNimbleFooted", isNimbleFooted)
      .add("wasNimbleFooted", wasNimbleFooted)
      .toString();
  }

  private boolean isNimbleFooted() {
    return NimbleFooted.isNimbleFooted(clientPlayer);
  }
}
