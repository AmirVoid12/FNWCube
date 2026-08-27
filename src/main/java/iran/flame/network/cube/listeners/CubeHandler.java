package iran.flame.network.cube.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.CubeManager;
import iran.flame.network.cube.utils.ContainerUtils;
import iran.flame.network.cube.enums.Sound;
import iran.flame.network.cube.interfaces.Versionable;

public class CubeHandler implements Listener {
    private final GenCubes plugin = GenCubes.getInstance();
    private final CubeManager cubeManager = this.plugin.getCubeManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();

        boolean isMainHand = Versionable.isAtMost(1, 8) || event.getHand() == EquipmentSlot.HAND;

        if (!isMainHand || clickedBlock == null || clickedBlock.getType() == Material.AIR) {
            return;
        }

        GenCube pairingCube = this.cubeManager.getCubeTryingToPair(player.getUniqueId());
        if (pairingCube == null) {
            return;
        }

        BlockState clickedBlockState = clickedBlock.getState();

        if (ContainerUtils.isAContainer(clickedBlockState)) {
            pairingCube.setLinkedContainer(clickedBlockState.getBlock());
            pairingCube.getInventory().getIconByKey("inventory-linker").refresh();
            GenCubes.sendMessage(player, "Linked.", false);
            pairingCube.getPairingAttempts().remove(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.ANVIL_USE.bukkitSound(), 100.0f, 1.0f);
            return;
        }

        GenCubes.sendMessage(player, "Fail to link", false);
        pairingCube.getPairingAttempts().remove(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        GenCube pairingCube = this.cubeManager.getCubeTryingToPair(playerId);
        if (pairingCube != null) {
            pairingCube.getPairingAttempts().remove(playerId);
        }
    }

    @EventHandler
    public void onExtendBlock(BlockPistonExtendEvent event) {
        Set<Location> destinationLocations = new HashSet<>();
        BlockFace direction = event.getDirection();

        for (Block block : event.getBlocks()) {
            destinationLocations.add(
                    block.getLocation().clone().add(direction.getModX(), direction.getModY(), direction.getModZ())
            );
        }

        this.cancelIfOverlapsCube(destinationLocations, event);
    }

    @EventHandler
    public void onRetractBlock(BlockPistonRetractEvent event) {
        Set<Location> blockLocations = new HashSet<>();

        for (Block block : event.getBlocks()) {
            blockLocations.add(block.getLocation());
        }

        this.cancelIfOverlapsCube(blockLocations, event);
    }

    private void cancelIfOverlapsCube(Set<Location> locations, Cancellable event) {
        for (Location location : locations) {
            boolean isLoadedCube = this.cubeManager.getCubeByLocation(location, true) != null;
            boolean isUnloadedCube = this.cubeManager.getCubeByLocation(location, false) != null;

            if (isLoadedCube || isUnloadedCube) {
                event.setCancelled(true);
            }
        }
    }
}