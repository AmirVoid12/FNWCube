package iran.flame.network.cube.tasks.edit;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.block.Block;
import iran.flame.network.cube.BaseBlock;

public class EditBatch {
    private final Map<Block, BaseBlock> blocks = new HashMap<>();
    private boolean done = false;

    public EditBatch() { }

    void edit() {
        for (Block block : this.blocks.keySet()) {
            BaseBlock baseBlock = this.blocks.get(block);
            block.setType(baseBlock.getMaterial());
        }
        this.done = true;
    }

    public void add(Block block, BaseBlock baseBlock) {
        this.blocks.put(block, baseBlock);
    }

    public Map<Block, BaseBlock> getBlocks() {
        return this.blocks;
    }

    public boolean isDone() {
        return this.done;
    }
}