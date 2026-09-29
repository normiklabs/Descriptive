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

package org.normik.descriptiveserver;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DescriptiveServer extends JavaPlugin implements Listener {

    private final Map<UUID, DescriptivePayload> cache = new ConcurrentHashMap<>();
    private ServerConfig config;

    @Override
    public void onEnable() {
        config = new ServerConfig(getDataFolder().toPath().resolve("descriptive-server.json"), getLogger());
        config.load();

        getServer().getMessenger().registerIncomingPluginChannel(this, DescriptivePayload.CHANNEL,
                (_, player, message) -> handlePayload(player, message));
        getServer().getMessenger().registerOutgoingPluginChannel(this, DescriptivePayload.CHANNEL);
        getServer().getMessenger().registerOutgoingPluginChannel(this, ServerStatusPacket.CHANNEL);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();
        getServer().getScheduler().runTaskLater(this, () -> {
            sendStatusPacket(joining, config.isEnabled());
            if (!config.isEnabled()) return;
            for (Map.Entry<UUID, DescriptivePayload> entry : cache.entrySet()) {
                if (entry.getKey().equals(joining.getUniqueId())) continue;
                sendPayload(joining, entry.getValue());
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cache.remove(event.getPlayer().getUniqueId());
    }

    private void handlePayload(Player sender, byte[] message) {
        if (!config.isEnabled()) return;

        DescriptivePayload payload = DescriptivePayload.decode(message);
        if (payload == null) {
            getLogger().warning("Received malformed packet from " + sender.getName() + ", dropping");
            return;
        }

        UUID senderUuid = sender.getUniqueId();
        cache.put(senderUuid, payload);

        for (Player target : getServer().getOnlinePlayers()) {
            if (target.getUniqueId().equals(senderUuid)) continue;
            sendPayload(target, payload);
        }
    }

    private void sendPayload(Player target, DescriptivePayload payload) {
        try {
            target.sendPluginMessage(this, DescriptivePayload.CHANNEL, payload.encode());
        } catch (Exception e) {
            getLogger().severe("Failed to send payload to " + target.getName() + ": " + e.getMessage());
        }
    }

    private void sendStatusPacket(Player target, boolean enabled) {
        try {
            target.sendPluginMessage(this, ServerStatusPacket.CHANNEL, ServerStatusPacket.encode(enabled));
        } catch (Exception e) {
            getLogger().severe("Failed to send status packet to " + target.getName() + ": " + e.getMessage());
        }
    }
}
