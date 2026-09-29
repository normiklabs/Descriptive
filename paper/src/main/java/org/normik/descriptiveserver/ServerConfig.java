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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class ServerConfig {

    private static final String DEFAULT_CONFIG = """
            # Master switch. If false, the server will reject all Descriptive packets
            # and notify clients that custom name display is disabled on this server.
            enabled = true
            """;

    private final Path   configPath;
    private final Logger logger;
    private boolean enabled = true;

    public ServerConfig(Path configPath, Logger logger) {
        this.configPath = configPath;
        this.logger     = logger;
    }

    public void load() {
        if (!Files.exists(configPath)) {
            save();
            logger.info("Config created at " + configPath);
            return;
        }
        try {
            for (String raw : Files.readAllLines(configPath)) {
                String line = raw.contains("#") ? raw.substring(0, raw.indexOf('#')) : raw;
                line = line.trim();
                if (line.startsWith("enabled")) {
                    String value = line.substring(line.indexOf('=') + 1).trim();
                    enabled = Boolean.parseBoolean(value);
                }
            }
            logger.info("Config loaded - enabled=" + enabled);
        } catch (Exception e) {
            logger.warning("Failed to load config, using defaults: " + e.getMessage());
            enabled = true;
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, DEFAULT_CONFIG);
        } catch (IOException e) {
            logger.warning("Failed to save config: " + e.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }
}