package cc.maicra999.taxa.player.resourcepack;

import cc.maicra999.taxa.Taxa;
import cc.maicra999.taxa.util.PathUtil;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import org.geysermc.api.util.BedrockPlatform;
import org.geysermc.geyser.api.pack.PackCodec;
import org.geysermc.geyser.api.pack.ResourcePack;

public class ResourcePackManager {

    private final Taxa taxa;

    private final List<PackEntry> packEntries = new ArrayList<>();
    private final Map<PackEntry, ResourcePack> packs = new HashMap<>();

    public ResourcePackManager(Taxa taxa) {
        this.taxa = taxa;
    }

    public int getPackCount() {
        return packEntries.size();
    }

    public void load() {
        readEntries();
        loadPacks();
    }

    private void readEntries() {
        packEntries.clear();

        Path pluginHome = PathUtil.getOrCreateDirectory(taxa.dataFolder());
        File packsFile = new File(pluginHome.resolve("resource_packs.json").toUri());

        try (JsonReader reader = new JsonReader(new FileReader(packsFile))) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray packsArray = json.getAsJsonArray("packs");

            for (JsonElement element : packsArray) {
                JsonObject entry = element.getAsJsonObject();
                String name = entry.get("name").getAsString();
                Set<BedrockPlatform> exclusive =
                        entry.has("exclusive") ? parsePlatforms(entry.getAsJsonArray("exclusive")) : Set.of();
                Set<BedrockPlatform> ignored =
                        entry.has("ignore") ? parsePlatforms(entry.getAsJsonArray("ignore")) : Set.of();
                Set<String> subpacks =
                        entry.has("subpacks") ? parseSubpacks(entry.getAsJsonArray("subpacks")) : Set.of();

                PackEntry packEntry = new PackEntry(name, exclusive, ignored, subpacks);
                packEntries.add(packEntry);
            }
        } catch (JsonParseException e) {
            taxa.logger().error("Failed to parse resource_packs.json", e);
        } catch (FileNotFoundException e) {
            taxa.logger().warning("resource_packs.json is missing");
        } catch (Exception e) {
            taxa.logger().error("Failed to read resource pack definitions", e);
        }

        taxa.logger().info(String.format("Loaded %d bedrock resource pack entries", packEntries.size()));
    }

    private void loadPacks() {
        packs.clear();

        Path pluginHome = PathUtil.getOrCreateDirectory(taxa.dataFolder());
        Path packsDir = PathUtil.getOrCreateSubdirectory(pluginHome, "packs");

        for (PackEntry entry : packEntries) {
            File packFile = new File(packsDir.resolve(entry.name() + ".mcpack").toUri());
            if (!packFile.exists()) {
                taxa.logger().warning(String.format("Resource pack file not found: %s", packFile.getAbsolutePath()));
                continue;
            }

            try {
                ResourcePack resourcePack = ResourcePack.create(PackCodec.path(packFile.toPath()));
                packs.put(entry, resourcePack);
            } catch (Exception e) {
                taxa.logger().error(String.format("Failed to load resource pack: %s", packFile.getAbsolutePath()), e);
            }
        }
    }

    public List<PackReference> getCompatiblePacks(BedrockPlatform platform) {
        List<PackReference> compatiblePacks = new ArrayList<>();
        for (PackEntry entry : packEntries) {
            ResourcePack pack = packs.get(entry);
            if (pack != null && entry.isCompatible(platform)) {
                compatiblePacks.add(new PackReference(pack, entry));
            }
        }
        return compatiblePacks;
    }

    private Set<BedrockPlatform> parsePlatforms(JsonArray array) {
        Set<BedrockPlatform> platforms = new HashSet<>();
        for (JsonElement element : array) {
            String platformStr = element.getAsString().toUpperCase(Locale.ROOT);
            try {
                BedrockPlatform platform = BedrockPlatform.valueOf(platformStr);
                platforms.add(platform);
            } catch (IllegalArgumentException e) {
                taxa.logger().warning(String.format("Unknown Bedrock platform: %s", platformStr));
            }
        }
        return platforms;
    }

    private static Set<String> parseSubpacks(JsonArray array) {
        Set<String> subpacks = new HashSet<>();
        for (JsonElement element : array) {
            subpacks.add(element.getAsString());
        }
        return subpacks;
    }
}
