/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.RailwaysBuildInfo;
import com.railwayteam.railways.config.CRConfigs;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

// Checks Modrinth's public keyless version API rather than CurseForge, which requires a Core API key.
public enum UpdateChecker {
  INSTANCE;

  private static final String MODRINTH_PROJECT_ID = "YIXN6XZJ";
  private static final String API_URL = "https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT_ID
    + "/version?loaders=" + encode("[\"fabric\"]")
    + "&game_versions=" + encode("[\"" + RailwaysBuildInfo.MINECRAFT_VERSION + "\"]");
  private static final String DOWNLOAD_URL = "https://modrinth.com/mod/" + MODRINTH_PROJECT_ID + "/versions";

  private static final String CURSEFORGE_SLUG = "steam-n-rails-fly";
  // gameVersionTypeId=4 is CurseForge's id for the "Minecraft 1.21" version group; it may need
  // bumping the next time Steam 'n' Rails moves to a new Minecraft major version.
  private static final String CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/" + CURSEFORGE_SLUG
    + "/files/all?page=1&pageSize=20&version=" + RailwaysBuildInfo.MINECRAFT_VERSION
    + "&gameVersionTypeId=4&showAlphaFiles=hide";

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private volatile boolean checked = false;
  private volatile String latestVersion = null;
  private final AtomicBoolean notified = new AtomicBoolean(false);

  public void init() {
    if (checked || !CRConfigs.client().enableUpdateChecker.get())
      return;

    CompletableFuture.runAsync(() -> {
      try {
        HttpClient client = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(5))
          .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
          .timeout(Duration.ofSeconds(10))
          .header("User-Agent", "Layers-of-Railways/railways-update-checker")
          .GET()
          .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
          Railways.LOGGER.warn("Failed to check for updates: Modrinth returned status {} for {}: {}",
            response.statusCode(), API_URL, response.body());
          return;
        }

        JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
        if (!versions.isEmpty()) {
          JsonElement first = versions.get(0);
          if (first instanceof JsonObject object && object.has("version_number")) {
            latestVersion = object.get("version_number").getAsString();
          }
        }
      } catch (Exception e) {
        Railways.LOGGER.warn("Failed to check for updates: {}", e.toString());
      } finally {
        checked = true;
      }
    });
  }

  public boolean isUpdateAvailable() {
    return checked && latestVersion != null && !latestVersion.equals(RailwaysBuildInfo.VERSION);
  }

  public String getLatestVersion() {
    return latestVersion;
  }

  public String getDownloadUrl() {
    return DOWNLOAD_URL;
  }

  public String getCurseForgeUrl() {
    return CURSEFORGE_URL;
  }

  // True only the first call while an update is available, so the client announces it once per session.
  public boolean claimNotification() {
    return isUpdateAvailable() && notified.compareAndSet(false, true);
  }
}
