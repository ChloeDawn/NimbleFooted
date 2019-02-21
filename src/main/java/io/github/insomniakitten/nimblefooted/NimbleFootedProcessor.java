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
  private final float elevatedStepHeight;
  private final LivingEntity clientPlayer;

  private float lastStepHeight = Float.NaN;
  private boolean wasNimbleFooted = false;

  NimbleFootedProcessor(final ClientPlayerEntity player) {
    this.elevatedStepHeight = NimbleFooted.getElevatedStepHeight();
    this.clientPlayer = player;
  }

  @Override
  public void run() {
    if (isNimbleFooted()) {
      if (!this.wasNimbleFooted) {
        final float stepHeight = this.clientPlayer.stepHeight;
        checkState(!Float.isNaN(stepHeight), "Player stepHeight is NaN!");
        this.lastStepHeight = stepHeight;
        this.clientPlayer.stepHeight = this.elevatedStepHeight;
        this.wasNimbleFooted = true;
      } else if (this.elevatedStepHeight != this.clientPlayer.stepHeight) {
        this.lastStepHeight = this.clientPlayer.stepHeight;
      }
    } else if (this.wasNimbleFooted) {
      checkState(!Float.isNaN(this.lastStepHeight), "Player was nimble-footed but lastStepHeight is NaN!");
      this.clientPlayer.stepHeight = this.lastStepHeight;
      this.wasNimbleFooted = false;
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
      .add("clientPlayer", this.clientPlayer)
      .add("stepHeight", this.clientPlayer.stepHeight)
      .add("lastStepHeight", this.lastStepHeight)
      .add("isNimbleFooted", isNimbleFooted)
      .add("wasNimbleFooted", this.wasNimbleFooted)
      .toString();
  }

  private boolean isNimbleFooted() {
    return NimbleFooted.isNimbleFooted(this.clientPlayer);
  }
}
