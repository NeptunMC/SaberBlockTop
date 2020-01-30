package pw.saber.blocktop.utils;

import java.util.UUID;

public class PlayerObject {

    private int blockBroke;
    private UUID uuid;

    public PlayerObject(UUID uuid, int blockBroke) {
        this.uuid = uuid;
        this.blockBroke = blockBroke;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getBlockBroke() {
        return blockBroke;
    }

    public void setBlockBroke(int amount) {
        this.blockBroke = blockBroke + amount;
    }

    public void addBlockBroke(int amount) {
        this.blockBroke = blockBroke + amount;
    }

}
