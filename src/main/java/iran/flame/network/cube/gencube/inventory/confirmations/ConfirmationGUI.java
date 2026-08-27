package iran.flame.network.cube.gencube.inventory.confirmations;

import org.bukkit.entity.Player;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.utils.inventory.InteractiveInventory;

public abstract class ConfirmationGUI extends InteractiveInventory {
    protected GenCube cube;

    ConfirmationGUI(GenCube genCube) {
        this.cube = genCube;
        this.setupInventory();
        this.loadActions();
    }

    public void open(Player player) {
        player.openInventory(this.getInventory());
    }

    abstract void setupInventory();

    abstract void loadActions();

    public GenCube getCube() {
        return this.cube;
    }
}