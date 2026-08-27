package iran.flame.network.cube;

import org.bukkit.Material;

public class BaseBlock {
    private final Material material;
    private final short data;

    public BaseBlock(Material material, short data) {
        this.material = material;
        this.data = data;
    }

    public Material getMaterial() {
        return this.material;
    }

    public short getData() {
        return this.data;
    }
}