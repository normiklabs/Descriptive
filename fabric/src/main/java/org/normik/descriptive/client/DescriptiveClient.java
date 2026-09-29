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

package org.normik.descriptive.client;

import org.normik.descriptive.Descriptive;
import org.normik.descriptive.client.command.DescriptiveCommand;
import org.normik.descriptive.client.network.ClientNetworkHandler;
import org.normik.descriptive.config.DescriptiveClientConfig;
import org.normik.descriptive.server.network.ServerNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DescriptiveClient implements ClientModInitializer {

    private static DescriptiveClient instance;
    private DescriptiveClientConfig config;

    @Override
    public void onInitializeClient() {
        instance = this;
        Descriptive.LOGGER.info("Initializing {} (Client)", Descriptive.MOD_NAME);
        this.config = new DescriptiveClientConfig();
        this.config.load();
        Descriptive.LOGGER.info("Configuration loaded: color=#{}",
                String.format("%06X", config.getColor()));
        ClientNetworkHandler.initialize();
        ServerNetworkHandler.initialize();
        KeybindingHandler.register();
        DescriptiveCommand.register();
        ItemTooltipHandler.register();
        Descriptive.LOGGER.info("{} Client initialized successfully!", Descriptive.MOD_NAME);
    }

    public static DescriptiveClient getInstance() {
        return instance;
    }

    public DescriptiveClientConfig getConfig() {
        return config;
    }
}