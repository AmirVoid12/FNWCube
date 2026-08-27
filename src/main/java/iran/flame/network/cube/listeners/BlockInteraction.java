package iran.flame.network.cube.listeners;

import java.util.*;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.CubeBuilder;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.managers.CubeManager;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.managers.TaskManager;
import iran.flame.network.cube.managers.yml.Configuration;
import iran.flame.network.cube.tasks.GenCuboidPreview;
import iran.flame.network.cube.tasks.edit.CubeEdition;
import iran.flame.network.cube.enums.EditTask;
import iran.flame.network.cube.utils.ContainerUtils;
import iran.flame.network.cube.utils.CubeUtils;
import iran.flame.network.cube.utils.Cuboid;
import iran.flame.network.cube.utils.LocationUtils;
import iran.flame.network.cube.enums.Sound;
import iran.flame.network.cube.utils.inventory.Icon;
import iran.flame.network.cube.utils.inventory.InventoryUtils;

public class BlockInteraction implements Listener {

    private final GenCubes plugin = GenCubes.getInstance();
    private final DataManager dataManager = this.plugin.getDataManager();
    private final Configuration configuration = this.plugin.getConfiguration();
    private final CubeManager cubeManager = this.plugin.getCubeManager();
    private final TaskManager taskManager = this.plugin.getTaskManager();
    private final Economy economy = this.plugin.getEcon();
    private final Random random = new Random();
    private static final BlockFace[] CARDINAL_FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();

        for (String cubeId : this.dataManager.getCubesNames()) {
            ItemStack cubeIcon = this.dataManager.getIcon(cubeId);
            if (!InventoryUtils.areSimilarItems(itemInHand, cubeIcon)) {
                continue;
            }

            event.setCancelled(true);

            BlockFace facing = getPlacementFacing(player.getLocation().getYaw());
            Location borderCorner = offsetLocation(block.getLocation(), facing);
            assert borderCorner != null;
            Cuboid cubeCuboid = new Cuboid(borderCorner, borderCorner).expandForGenCube(this.dataManager.getSize(cubeId) - 1, facing);

            if (CubeUtils.isASafeLocationToBuild(cubeCuboid, block.getLocation(), player, new ArrayList<>())) {
                GenCuboidPreview preview = this.taskManager.getGenCuboidPreviewByPlayer(player);
                if (preview != null) {
                    preview.stopTask();
                }

                InventoryUtils.removeSingleItemInHand(player, event.getHand(), itemInHand);

                GenCube cube = new CubeBuilder(cubeId, LocationUtils.serializeLoc(borderCorner), facing)
                        .setOwner(player.getUniqueId())
                        .build();

                boolean skipBuildAnimation = !(Boolean) this.configuration.getOptions()
                        .get("fastasyncworldedit").get("show_block_animation_on_building");

                cube.build(
                        this.dataManager.getBuildType(cube.getType()),
                        this.dataManager.getBuildQuantityType(cube.getType()),
                        this.dataManager.getBuildQuantityValue(cube.getType()),
                        skipBuildAnimation
                );

                player.playSound(player.getLocation(), Sound.DIG_WOOD.bukkitSound(), 100.0f, 1.0f);
                return;
            }

            return;
        }

        if (this.cubeManager.getCubeByLocation(block.getLocation(), true) != null) {
            event.setCancelled(true);
        }
        if (this.taskManager.isCubeBeingRemovedAtLocation(block.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        GenCube cube = this.cubeManager.getCubeByLocation(block.getLocation(), true);
        Player player = event.getPlayer();

        GenCube linkedCube = this.cubeManager.getCubeByLinkedContainer(block.getLocation());
        if (linkedCube != null) {
            linkedCube.setLinkedContainer(null);
            Icon linkerIcon = linkedCube.getInventory().getIconByKey("inventory-linker");
            if (linkerIcon != null) {
                linkerIcon.refresh();
            }
        }

        if (cube == null) {
            GenCube unloadedCube = this.cubeManager.getCubeByLocation(block.getLocation(), false);
            if (unloadedCube != null) {
                event.setCancelled(true);
                GenCubes.sendMessage(player, "&cThis cube was not loaded correctly, please contact an admin!", false);
                return;
            }
            if (this.taskManager.isCubeBeingRemovedAtLocation(block.getLocation())) {
                event.setCancelled(true);
            }
            return;
        }

        if (cube.getBorderBlocks().contains(block)) {
            event.setCancelled(true);
            return;
        }

        if (!cube.getOwner().equals(player.getUniqueId()) && !player.hasPermission("gencubes.admin") && !player.isOp()) {
            event.setCancelled(true);
            return;
        }

        ItemStack toolInHand = player.getInventory().getItemInMainHand();

        if ((Boolean) this.configuration.getOptions().get("gui").get("enable")) {
            List<ItemStack> overflowItems = new ArrayList<>();

            for (ItemStack drop : block.getDrops(toolInHand)) {
                boolean hasFortune = toolInHand.getEnchantments().containsKey(Enchantment.LOOT_BONUS_BLOCKS);
                boolean hasSilkTouch = toolInHand.getEnchantments().containsKey(Enchantment.SILK_TOUCH);

                if (hasFortune && block.getType().toString().contains("ORE") && !hasSilkTouch) {
                    int fortuneLevel = toolInHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                    int amount = calculateDropAmount(drop.getType(), fortuneLevel, this.random);
                    drop.setAmount(amount);
                }

                if (hasSilkTouch) {
                    drop.setType(block.getType());
                    drop.setAmount(1);
                }

                boolean depositedIntoContainer = tryDepositIntoLinkedContainer(cube, drop);

                if (!depositedIntoContainer) {
                    CubeInventory cubeInventory = cube.getInventory();

                    HashMap<Integer, ItemStack> leftover = cubeInventory.getInventory().addItem(drop);
                    if (!leftover.isEmpty()) {
                        overflowItems.addAll(leftover.values());
                    }

                    Icon sellAllIcon = cube.getInventory().getIconByKey("sell-all");
                    if (sellAllIcon != null) {
                        sellAllIcon.refresh();
                    }
                }
            }

            Location dropLocation = block.getLocation();
            for (ItemStack overflowItem : overflowItems) {
                org.bukkit.entity.Item droppedEntity = Objects.requireNonNull(dropLocation.getWorld()).dropItem(dropLocation, overflowItem);
                droppedEntity.setVelocity(new Vector());
            }

            event.setDropItems(false);
        }

        try {
            player.incrementStatistic(Statistic.MINE_BLOCK, block.getType());
        } catch (Exception ignored) {
        }
    }

    private boolean tryDepositIntoLinkedContainer(GenCube cube, ItemStack drop) {
        Location linkedLocation = cube.getLinkedContainer();
        if (linkedLocation == null) {
            return false;
        }

        BlockState containerState = linkedLocation.getBlock().getState();
        Inventory containerInventory = ContainerUtils.getContainerInventory(containerState);

        if (containerInventory == null) {
            cube.setLinkedContainer(null);
            Icon linkerIcon = cube.getInventory().getIconByKey("inventory-linker");
            if (linkerIcon != null) {
                linkerIcon.refresh();
            }
            return false;
        }

        HashMap<Integer, ItemStack> leftover = containerInventory.addItem(drop);
        if (leftover.isEmpty()) {
            return true;
        }

        int leftoverAmount = 0;
        for (ItemStack remaining : leftover.values()) {
            leftoverAmount += remaining.getAmount();
        }
        drop.setAmount(leftoverAmount);
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (event.getHand() != EquipmentSlot.HAND || block == null) {
            return;
        }

        GenCube cube = this.cubeManager.getCubeByLocation(block.getLocation(), true);

        if (cube != null) {
            if (cube.getBorderBlocks().contains(block)) {
                handleBorderBlockInteraction(cube, player, action);
                return;
            }

            GenCube unloadedCube = this.cubeManager.getCubeByLocation(block.getLocation(), false);
            if (unloadedCube != null && player.hasPermission("gencubes.admin")
                    && player.isSneaking() && action == Action.RIGHT_CLICK_BLOCK) {
                unloadedCube.remove();
                if (this.dataManager.isACube(unloadedCube.getType())) {
                    ItemStack cubeIcon = this.dataManager.getIcon(unloadedCube.getType());
                    player.getInventory().addItem(cubeIcon);
                    return;
                }
                GenCubes.sendMessage(player, "&cThis cube doesn't have any configuration therefore you won't receive the cube item!", false);
            }
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            for (String cubeId : this.dataManager.getCubesNames()) {
                ItemStack cubeIcon = this.dataManager.getIcon(cubeId);
                if (!InventoryUtils.areSimilarItems(itemInHand, cubeIcon)) {
                    continue;
                }

                event.setCancelled(true);

                GenCuboidPreview existingPreview = this.taskManager.getGenCuboidPreviewByPlayer(player);
                if (existingPreview != null) {
                    existingPreview.stopTask();
                }

                BlockFace facing = getPlacementFacing(player.getLocation().getYaw());
                Location previewCorner = offsetLocation(block.getLocation().clone().add(0.0, 1.0, 0.0), facing);
                assert previewCorner != null;
                Cuboid previewCuboid = new Cuboid(previewCorner, previewCorner)
                        .expandForGenCube(this.dataManager.getSize(cubeId) - 1, facing);

                this.taskManager.register(new GenCuboidPreview(
                        previewCuboid, block.getLocation().clone().add(0.0, 1.0, 0.0), player));
            }
        }
    }

    private void handleBorderBlockInteraction(GenCube cube, Player player, Action action) {
        boolean isOwner = cube.getOwner().equals(player.getUniqueId());

        if (isOwner) {
            boolean shiftRemoveEnabled = (Boolean) this.configuration.getOptions().get("actions")
                    .getOrDefault("shift_right_click_cube_remove", Boolean.TRUE);

            if (shiftRemoveEnabled && player.isSneaking() && action == Action.RIGHT_CLICK_BLOCK
                    && this.taskManager.getCubeEditionByCube(cube) == null) {

                boolean waitForEmptyInventory = (Boolean) this.configuration.getOptions().get("actions")
                        .getOrDefault("onremove_wait_for_empty_cube_inventory", Boolean.FALSE);

                if (!cube.getInventory().isEmpty() && waitForEmptyInventory) {
                    return;
                }

                ItemStack cubeIcon = this.dataManager.getIcon(cube.getType());
                boolean canStack = cubeIcon.getMaxStackSize() > 1;

                if (InventoryUtils.getSpacesForItem(player.getInventory(), cubeIcon, canStack) > 0) {
                    double removePrice = this.dataManager.getRemovePrice(cube.getType());

                    if (this.economy != null && removePrice > 0.0) {
                        if (this.economy.getBalance(player) >= removePrice) {
                            this.economy.withdrawPlayer(player, removePrice);
                        } else {
                            player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                            return;
                        }
                    }

                    cube.remove();

                    boolean returnCubeItem = (Boolean) this.configuration.getOptions()
                            .get("actions").get("get_cube_back_when_removing");
                    if (returnCubeItem) {
                        player.getInventory().addItem(cubeIcon);
                    }
                    return;
                }

                return;
            }

            if (!player.isSneaking() && action == Action.RIGHT_CLICK_BLOCK) {
                CubeEdition activeEdition = this.taskManager.getCubeEditionByCube(cube);
                if (activeEdition != null && activeEdition.getEditTask() == EditTask.SET) {
                    return;
                }

                boolean guiEnabled = (Boolean) this.configuration.getOptions().get("gui").get("enable");
                if (guiEnabled || player.hasPermission("gencubes.admin")) {
                    cube.getInventory().open(player);
                }
            }
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (player.hasPermission("gencubes.admin")) {
                cube.getInventory().open(player);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            GenCube cube = this.cubeManager.getCubeByLocation(block.getLocation(), true);
            return cube != null && cube.getBorderBlocks().contains(block);
        });
    }

    private void damageItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return;
        }
        damageable.setDamage(damageable.getDamage() + 1);
        item.setItemMeta(meta);
    }

    private static Location offsetLocation(Location location, BlockFace face) {
        if (face == BlockFace.NORTH) {
            return location.clone().add(0.0, 0.0, -1.0);
        }
        if (face == BlockFace.SOUTH) {
            return location.clone().add(0.0, 0.0, 1.0);
        }
        if (face == BlockFace.EAST) {
            return location.clone().add(1.0, 0.0, 0.0);
        }
        if (face == BlockFace.WEST) {
            return location.clone().add(-1.0, 0.0, 0.0);
        }
        return null;
    }

    private static BlockFace getPlacementFacing(float yaw) {
        return CARDINAL_FACES[Math.round(yaw / 90.0f) & 3].getOppositeFace();
    }

    private static int getBaseDropAmount(Material material, Random random) {
        if (material == Material.LAPIS_ORE) {
            return 4 + random.nextInt(5);
        }
        return 1;
    }

    private static int calculateDropAmount(Material material, int fortuneLevel, Random random) {
        if (fortuneLevel > 0) {
            int bonus = random.nextInt(fortuneLevel + 2) - 1;
            if (bonus < 0) {
                bonus = 0;
            }
            return getBaseDropAmount(material, random) * (bonus + 1);
        }
        return getBaseDropAmount(material, random);
    }
}