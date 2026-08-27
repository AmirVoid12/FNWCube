package iran.flame.network.cube;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.managers.CubeBankManager;
import iran.flame.network.cube.managers.DataManager;

public class CubeBankAccount {
    private final CubeBankManager cubeBankManager;
    private final DataManager dataManager;
    private final UUID owner;
    private final Map<String, Integer> cubes;

    public CubeBankAccount(UUID owner, Map<String, Integer> cubes) {
        GenCubes plugin = GenCubes.getInstance();
        this.cubeBankManager = plugin.getCubeBankManager();
        this.dataManager = plugin.getDataManager();
        this.owner = owner;
        this.cubes = cubes;
    }

    public void add(String cubeType, int amount) {
        if (this.cubes.containsKey(cubeType)) {
            this.cubes.replace(cubeType, this.cubes.get(cubeType) + amount);
            return;
        }
        this.cubes.put(cubeType, amount);
    }

    public void remove(String cubeType, int amount) {
        if (this.cubes.containsKey(cubeType)) {
            int currentAmount = this.cubes.get(cubeType);
            int newAmount = currentAmount - amount;
            if (newAmount <= 0) {
                this.cubes.remove(cubeType);
                return;
            }
            this.cubes.replace(cubeType, newAmount);
        }
    }

    public boolean redeem(Player player) {
        Set<String> cubeTypesSnapshot = new HashSet<>(this.cubes.keySet());

        for (String cubeType : cubeTypesSnapshot) {
            int amount = this.cubes.getOrDefault(cubeType, 0);
            ItemStack icon = this.dataManager.getIcon(cubeType);

            for (int i = 0; i < amount; i++) {
                HashMap<Integer, ItemStack> leftoverItems = player.getInventory().addItem(icon);
                if (!leftoverItems.isEmpty()) {
                    this.save();
                    return false;
                }
                this.remove(cubeType, 1);
            }
        }
        this.save();
        return true;
    }

    public void save() {
        this.cubeBankManager.getStorage().save(this);
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Map<String, Integer> getCubes() {
        return this.cubes;
    }
}