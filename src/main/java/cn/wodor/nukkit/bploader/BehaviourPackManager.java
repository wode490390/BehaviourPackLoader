package cn.wodor.nukkit.bploader;

import cn.nukkit.Server;
import cn.nukkit.lang.BaseLang;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.resourcepacks.ZippedResourcePack;
import cn.nukkit.utils.Logger;
import com.google.common.io.Files;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BehaviourPackManager {

    private final Map<UUID, ResourcePack> packsById = new HashMap<>();
    private final ResourcePack[] packs;

    public BehaviourPackManager(BehaviourPackLoaderPlugin plugin, File path) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(path, "path");

        Server server = plugin.getServer();
        BaseLang language = server.getLanguage();
        Logger logger = plugin.getLogger();

        if (!path.exists()) {
            if (!path.mkdirs()) {
                this.packs = new ResourcePack[0];
                return;
            }
        } else if (!path.isDirectory()) {
            throw new IllegalArgumentException("Behaviour packs path '" + path.getName() + "' exists and is not a directory"); // "nukkit.resources.invalid-path"
        }

        List<ResourcePack> packs = new ArrayList<>();
        for (File file : path.listFiles()) {
            try {
                ResourcePack pack = null;

                if (!file.isDirectory()) { //TODO: directory resource pack
                    switch (Files.getFileExtension(file.getName())) {
                        case "zip":
                        case "mcpack":
                            pack = new ZippedResourcePack(file);
                            break;
                        default:
                            logger.warning(language.translateString("nukkit.resources.unknown-format", file.getName()));
                            break;
                    }
                }

                if (pack != null) {
                    packs.add(pack);
                    this.packsById.put(pack.getPackId(), pack);
                }
            } catch (IllegalArgumentException e) {
                logger.warning(language.translateString("nukkit.resources.fail", file.getName(), e.getMessage()));
            }
        }

        this.packs = packs.toArray(new ResourcePack[0]);
        logger.info("Successfully loaded " + this.packs.length + " behaviour packs"); // "nukkit.resources.success"
    }

    public ResourcePack[] getResourceStack() {
        return this.packs;
    }

    public ResourcePack getPackById(UUID id) {
        return this.packsById.get(id);
    }
}
