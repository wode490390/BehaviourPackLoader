package cn.wodor.nukkit.bploader;

import cn.nukkit.Nukkit;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.wodor.nukkit.bploader.listener.PackListener;
import cn.wodor.nukkit.bploader.protocol.AbstractResourcePacksInfoPacket;
import cn.wodor.nukkit.bploader.protocol.ResourcePacksInfoPacketV422;
import cn.wodor.nukkit.bploader.protocol.ResourcePacksInfoPacketV448;
import cn.wodor.nukkit.bploader.util.MetricsLite;

import java.io.File;

public class BehaviourPackLoaderPlugin extends PluginBase {

    private int packChunkSize;

    private BehaviourPackManager behaviourPackManager;

    private Class<? extends AbstractResourcePacksInfoPacket> resourcePacksInfoPacketClass;

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 13407);
        } catch (Throwable ignored) {

        }
        this.saveDefaultConfig();
        Config config = this.getConfig();
        this.packChunkSize = Math.max(config.getInt("pack-chunk-size", 8 * 1024), 1024); // default: 8KB, minimum: 1KB

        // Fix packet encoding
        // see https://github.com/CloudburstMC/Nukkit/commit/1ee150c165ebacd2441e34a8d7e75abea28b2625#diff-3400a7ff4bc3883e451fc6754ecb64116d75d39607efe35af6c73ff56320f421R28
        int protocol = ProtocolInfo.CURRENT_PROTOCOL;
        if (protocol < 475) { // 1.18
            if (protocol >= 448) { // 1.17.10
                this.resourcePacksInfoPacketClass = ResourcePacksInfoPacketV448.class;
            } else if (protocol >= 422) { // 1.16.200
                this.resourcePacksInfoPacketClass = ResourcePacksInfoPacketV422.class;
            }
        }

        this.behaviourPackManager = new BehaviourPackManager(this, new File(Nukkit.DATA_PATH, "behavior_packs"));

        this.getServer().getPluginManager().registerEvents(new PackListener(this), this);
    }

    public BehaviourPackManager getBehaviourPackManager() {
        return this.behaviourPackManager;
    }

    public int getPackChunkSize() {
        return this.packChunkSize;
    }

    public AbstractResourcePacksInfoPacket createResourcePacksInfoPacket() {
        if (this.resourcePacksInfoPacketClass != null) {
            try {
                return this.resourcePacksInfoPacketClass.newInstance();
            } catch (Exception e) {
                this.getLogger().error("Failed to create ResourcePacksInfoPacket", e);
            }
        }
        return null;
    }
}
