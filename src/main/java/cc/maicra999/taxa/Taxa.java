package cc.maicra999.taxa;

import cc.maicra999.taxa.player.resourcepack.PackReference;
import cc.maicra999.taxa.player.resourcepack.ResourcePackManager;
import java.util.ArrayList;
import java.util.List;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.event.bedrock.SessionLoadResourcePacksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.pack.option.PriorityOption;
import org.geysermc.geyser.api.pack.option.ResourcePackOption;
import org.geysermc.geyser.api.pack.option.SubpackOption;

public class Taxa implements Extension {

    private static final int INITIAL_PACK_PRIORITY = 0;

    private final ResourcePackManager packManager;

    public Taxa() {
        this.packManager = new ResourcePackManager(this);
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        packManager.load();
        logger().info(String.format("Loaded %d bedrock resource pack(s)", packManager.getPackCount()));
    }

    @Subscribe
    public void onDefineCommands(GeyserDefineCommandsEvent event) {
        event.register(Command.builder(this)
                .name("reloadpacks")
                .source(CommandSource.class)
                .description("Reloads the resource pack configuration.")
                .permission("taxa.command.reloadpacks")
                .executor((source, cmd, args) -> {
                    packManager.load();
                    source.sendMessage(
                            String.format("%d bedrock resource pack(s) loaded!", packManager.getPackCount()));
                })
                .build());
    }

    @Subscribe
    public void onSessionLoadResourcePacks(SessionLoadResourcePacksEvent event) {
        List<PackReference> list =
                packManager.getCompatiblePacks(event.connection().platform());

        int priority = INITIAL_PACK_PRIORITY;
        for (PackReference reference : list.reversed()) {
            List<ResourcePackOption<?>> options = new ArrayList<>();
            options.add(PriorityOption.priority(++priority));
            for (String subpack : reference.packEntry().subpacks()) {
                options.add(SubpackOption.named(subpack));
            }

            event.register(reference.resourcePack(), options.toArray(new ResourcePackOption[0]));
        }

        logger().info(String.format(
                "Applying %d packs to %s (%d total, %d external)",
                event.resourcePacks().size(),
                event.connection().bedrockUsername(),
                event.resourcePacks().size(),
                event.resourcePacks().size() - list.size()));
    }
}
