package cn.wodor.nukkit.bploader.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ResourcePacksInfoPacket;
import cn.nukkit.resourcepacks.ResourcePack;

public abstract class AbstractResourcePacksInfoPacket extends DataPacket {

    public static final byte NETWORK_ID = ResourcePacksInfoPacket.NETWORK_ID;

    public boolean mustAccept;
    public boolean scripting;
    public boolean forceServerPacks;
    public ResourcePack[] behaviourPackEntries = new ResourcePack[0];
    public ResourcePack[] resourcePackEntries = new ResourcePack[0];

    @Override
    public final void decode() {

    }

    @Override
    public final void encode() {
        this.reset();
        this.encodePayload();
    }

    @Override
    public final byte pid() {
        return NETWORK_ID;
    }

    protected abstract void encodePayload();
}
