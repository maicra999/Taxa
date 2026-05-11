package cc.maicra999.taxa.player.resourcepack;

import com.google.common.base.Preconditions;
import java.util.Set;
import org.geysermc.api.util.BedrockPlatform;
import org.jetbrains.annotations.NotNull;

public record PackEntry(
        @NotNull String name,
        @NotNull Set<BedrockPlatform> exclusive,
        @NotNull Set<BedrockPlatform> ignored,
        @NotNull Set<String> subpacks) {

    public PackEntry {
        Preconditions.checkNotNull(name, "Pack name cannot be null");
        Preconditions.checkNotNull(exclusive, "Exclusive platforms set cannot be null");
        Preconditions.checkNotNull(ignored, "Ignored platforms set cannot be null");
        Preconditions.checkNotNull(subpacks, "Subpacks set cannot be null");

        if (!exclusive.isEmpty() && !ignored.isEmpty()) {
            throw new IllegalArgumentException("A PackEntry cannot have both exclusive and ignored platforms set.");
        }
    }

    public boolean isCompatible(BedrockPlatform platform) {
        if (!exclusive.isEmpty()) {
            return exclusive.contains(platform);
        }
        if (!ignored.isEmpty()) {
            return !ignored.contains(platform);
        }
        return true;
    }
}
