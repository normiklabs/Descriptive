/*
 * Copyright 2026 The normik Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.normik.descriptive.client.mixin;

import org.normik.descriptive.client.animation.PlayerAnimationContext;
import org.normik.descriptive.common.util.NameBuilder;
import org.normik.descriptive.common.util.TextReplacer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SignText.class)
public class SignBlockEntityRendererMixin {

    @Inject(method = "getMessages", at = @At("RETURN"), cancellable = true)
    private void descriptive$modifySignLines(boolean filtered, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        List<Component> result = new ArrayList<>(original);
        boolean modified = false;
        try {
            for (int i = 0; i < result.size(); i++) {
                Component line = result.get(i);
                if (line == null) continue;
                String lineStr = line.getString();
                for (var player : client.level.players()) {
                    String name = player.getName().getString();
                    if (lineStr.contains(name)) {
                        PlayerAnimationContext.setCurrentPlayer(player.getUUID());
                        result.set(i, TextReplacer.replaceText(line, name,
                                NameBuilder.buildCustomName(player.getUUID(), name)));
                        modified = true;
                    }
                }
            }
        } catch (Exception ignored) {
            PlayerAnimationContext.clear();
            return;
        }
        if (modified) cir.setReturnValue(result);
    }
}
