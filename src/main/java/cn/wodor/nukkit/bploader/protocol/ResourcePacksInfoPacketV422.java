package cn.wodor.nukkit.bploader.protocol;

import cn.nukkit.resourcepacks.ResourcePack;

public class ResourcePacksInfoPacketV422 extends AbstractResourcePacksInfoPacket {

    @Override
    public void encodePayload() {
        this.putBoolean(this.mustAccept);
        this.putBoolean(this.scripting);
        this.encodeBehaviourPacks(this.behaviourPackEntries);
        this.encodeResourcePacks(this.resourcePackEntries);
    }

    protected void encodeBehaviourPacks(ResourcePack[] packs) {
        this.putLShort(packs.length);
        for (ResourcePack entry : packs) {
            this.putString(entry.getPackId().toString());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(""); // encryption key
            this.putString(""); // sub-pack name
            this.putString(""); // content identity
            this.putBoolean(false); // has scripts
        }
    }

    protected void encodeResourcePacks(ResourcePack[] packs) {
        this.putLShort(packs.length);
        for (ResourcePack entry : packs) {
            this.putString(entry.getPackId().toString());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(""); // encryption key
            this.putString(""); // sub-pack name
            this.putString(""); // content identity
            this.putBoolean(false); // seems useless for resource packs
            this.putBoolean(false); // supports RTX
        }
    }

}
