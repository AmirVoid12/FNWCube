package iran.flame.network.cube.gencube;

import java.util.UUID;
import org.bukkit.block.BlockFace;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.managers.DataManager;

public class CubeBuilder {
    private final GenCube cube;
    private Object[] inventoryContent;

    public CubeBuilder(String type, String serializedLocation, BlockFace direction) {
        this.cube = new GenCube(type, serializedLocation, direction);
        DataManager dataManager = GenCubes.getInstance().getDataManager();
        this.cube.setSize(dataManager.getSize(type));
        this.cube.setUuid(UUID.randomUUID());
    }

    public CubeBuilder setSize(Integer size) {
        if (size % 2 != 0 && size > 0) {
            this.cube.setSize(size);
        }
        return this;
    }

    public CubeBuilder setUUID(UUID uuid) {
        this.cube.setUuid(uuid);
        return this;
    }

    public CubeBuilder setOwner(UUID owner) {
        this.cube.setOwner(owner);
        return this;
    }

    public CubeBuilder setInventoryContent(Object[] content) {
        if (content.length == 35) {
            this.inventoryContent = content;
        }
        return this;
    }

    public CubeBuilder setLinkedContainer(String serializedLocation) {
        this.cube.linkedContainerLocation = serializedLocation;
        return this;
    }

    public GenCube build() {
        this.cube.load(this.inventoryContent);
        return this.cube;
    }
}