package cn.wodor.nukkit.bploader.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.math.MathHelper;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePackChunkDataPacket;
import cn.nukkit.network.protocol.ResourcePackChunkRequestPacket;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import cn.nukkit.network.protocol.ResourcePackDataInfoPacket;
import cn.nukkit.network.protocol.ResourcePackStackPacket;
import cn.nukkit.network.protocol.ResourcePacksInfoPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.resourcepacks.ResourcePackManager;
import cn.wodor.nukkit.bploader.BehaviourPackLoaderPlugin;
import cn.wodor.nukkit.bploader.BehaviourPackManager;
import cn.wodor.nukkit.bploader.protocol.AbstractResourcePacksInfoPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PackListener implements Listener {

    private final BehaviourPackLoaderPlugin plugin;
    private final Server server;
    private final int protocol;

    private final BehaviourPackManager behaviourPackManager;
    private final ResourcePackManager resourcePackManager;

    private final int packChunkSize;

    public PackListener(BehaviourPackLoaderPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.protocol = ProtocolInfo.CURRENT_PROTOCOL;
        this.behaviourPackManager = plugin.getBehaviourPackManager();
        this.resourcePackManager = this.server.getResourcePackManager();
        this.packChunkSize = plugin.getPackChunkSize();
    }

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        DataPacket packet = event.getPacket();
        Player player = event.getPlayer();
        if (packet instanceof ResourcePackClientResponsePacket) {
            ResourcePackClientResponsePacket pk = (ResourcePackClientResponsePacket) packet;
            int status = pk.responseStatus;

            if (status == ResourcePackClientResponsePacket.STATUS_SEND_PACKS) {
                List<ResourcePackDataInfoPacket> responses = new ArrayList<>();
                for (ResourcePackClientResponsePacket.Entry entry : pk.packEntries) {
                    UUID id = entry.uuid;
                    boolean behaviour = false; // default is resource

                    ResourcePack pack = this.resourcePackManager.getPackById(id);
                    if (pack == null) {
                        pack = this.behaviourPackManager.getPackById(id);
                        if (pack == null) {
                            return;
                        }
                        behaviour = true;
                    }

                    ResourcePackDataInfoPacket response = new ResourcePackDataInfoPacket();
                    response.packId = pack.getPackId();
                    response.maxChunkSize = this.packChunkSize;
                    response.chunkCount = MathHelper.ceil(pack.getPackSize() / (float) this.packChunkSize);
                    response.compressedPackSize = pack.getPackSize();
                    response.sha256 = pack.getSha256();
                    if (behaviour && this.protocol >= 361) { // 1.12+
                        // Multi-version compatible
                        if (this.protocol < 388) { // 1.13
                            response.type = 2;
                        } else {
                            response.type = ResourcePackDataInfoPacket.TYPE_BEHAVIOR;
                        }
                    }
                    responses.add(response);
                }
                responses.forEach(player::dataPacket);

                event.setCancelled();
            } else if (status == ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS) {
                ResourcePackStackPacket response = new ResourcePackStackPacket();
                response.mustAccept = this.server.getForceResources();
                response.resourcePackStack = this.resourcePackManager.getResourceStack();
                response.behaviourPackStack = this.behaviourPackManager.getResourceStack();
                player.dataPacket(response);

                event.setCancelled();
            }
        } else if (packet instanceof ResourcePackChunkRequestPacket) {
            ResourcePackChunkRequestPacket pk = (ResourcePackChunkRequestPacket) packet;
            UUID id = pk.packId;

            ResourcePack pack = this.resourcePackManager.getPackById(id);
            if (pack == null) {
                pack = this.behaviourPackManager.getPackById(id);
                if (pack == null) {
                    return;
                }
            }

            int index = pk.chunkIndex;
            int progress = this.packChunkSize * index;

            ResourcePackChunkDataPacket response = new ResourcePackChunkDataPacket();
            response.packId = pack.getPackId();
            response.chunkIndex = index;
            response.data = pack.getPackChunk(progress, this.packChunkSize);
            response.progress = progress;
            player.dataPacket(response);

            event.setCancelled();
        }
    }

    @EventHandler
    public void onDataPacketSend(DataPacketSendEvent event) {
        DataPacket packet = event.getPacket();
        if (packet instanceof ResourcePacksInfoPacket) {
            ResourcePacksInfoPacket pk = (ResourcePacksInfoPacket) packet;
            pk.behaviourPackEntries = this.behaviourPackManager.getResourceStack();

            // Fix packet encoding
            AbstractResourcePacksInfoPacket fixPacket = this.plugin.createResourcePacksInfoPacket();
            if (fixPacket != null) {
                fixPacket.mustAccept = pk.mustAccept;
                fixPacket.behaviourPackEntries = pk.behaviourPackEntries;
                fixPacket.resourcePackEntries = pk.resourcePackEntries;
                event.getPlayer().dataPacket(fixPacket);

                event.setCancelled();
            }
        }
    }
}
