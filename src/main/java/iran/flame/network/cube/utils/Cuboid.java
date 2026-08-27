package iran.flame.network.cube.utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import iran.flame.network.cube.enums.QuantityType;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Comparator;
import java.util.Iterator;

public class Cuboid implements Cloneable, Iterable<Block>, ConfigurationSerializable {
    private final String worldName;
    private final int x1;
    private final int y1;
    private final int z1;
    private final int x2;
    private final int y2;
    private final int z2;
    private Block[] mainOppositeCorners;
    private List<Block> cachedBlocks;

    public Cuboid(Location corner1, Location corner2) {
        if (!Objects.equals(corner1.getWorld(), corner2.getWorld())) {
            throw new IllegalArgumentException("Locations must be on the same world");
        }
        this.worldName = Objects.requireNonNull(corner1.getWorld()).getName();
        this.x1 = Math.min(corner1.getBlockX(), corner2.getBlockX());
        this.y1 = Math.min(corner1.getBlockY(), corner2.getBlockY());
        this.z1 = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        this.x2 = Math.max(corner1.getBlockX(), corner2.getBlockX());
        this.y2 = Math.max(corner1.getBlockY(), corner2.getBlockY());
        this.z2 = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        this.mainOppositeCorners = new Block[2];
        this.mainOppositeCorners[0] = corner1.getBlock();
        this.mainOppositeCorners[1] = corner2.getBlock();
        this.cachedBlocks = new ArrayList<>();
    }

    public Cuboid(Location location) {
        this(location, location);
    }

    public Cuboid(Cuboid other) {
        this(other.getWorld().getName(), other.x1, other.y1, other.z1, other.x2, other.y2, other.z2);
    }

    public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = world.getName();
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.y2 = Math.max(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.z2 = Math.max(z1, z2);
    }

    private Cuboid(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = worldName;
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.y2 = Math.max(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.z2 = Math.max(z1, z2);
        this.mainOppositeCorners = new Block[2];
        this.mainOppositeCorners[0] = this.getWorld().getBlockAt(x1, y1, z1);
        this.mainOppositeCorners[1] = this.getWorld().getBlockAt(x2, y2, z2);
        this.cachedBlocks = new ArrayList<>();
    }

    public Cuboid(Map<String, Object> map) {
        this.worldName = (String) map.get("worldName");
        this.x1 = (Integer) map.get("x1");
        this.x2 = (Integer) map.get("x2");
        this.y1 = (Integer) map.get("y1");
        this.y2 = (Integer) map.get("y2");
        this.z1 = (Integer) map.get("z1");
        this.z2 = (Integer) map.get("z2");
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("worldName", this.worldName);
        map.put("x1", this.x1);
        map.put("y1", this.y1);
        map.put("z1", this.z1);
        map.put("x2", this.x2);
        map.put("y2", this.y2);
        map.put("z2", this.z2);
        return map;
    }

    public Location getLowerNE() {
        return new Location(this.getWorld(), this.x1, this.y1, this.z1);
    }

    public Location getUpperSW() {
        return new Location(this.getWorld(), this.x2, this.y2, this.z2);
    }

    public List<Block> getBlocks() {
        if (this.cachedBlocks.isEmpty()) {
            for (Block block : this) {
                this.cachedBlocks.add(block);
            }
        }
        return this.cachedBlocks;
    }

    public Location getCenter() {
        int upperX = this.getUpperX() + 1;
        int upperY = this.getUpperY() + 1;
        int upperZ = this.getUpperZ() + 1;
        return new Location(
                this.getWorld(),
                this.getLowerX() + (upperX - this.getLowerX()) / 2.0,
                this.getLowerY() + (upperY - this.getLowerY()) / 2.0,
                this.getLowerZ() + (upperZ - this.getLowerZ()) / 2.0
        );
    }

    public World getWorld() {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null) {
            throw new IllegalStateException("World '" + this.worldName + "' is not loaded");
        }
        return world;
    }

    public int getSizeX() {
        return this.x2 - this.x1 + 1;
    }

    public int getSizeY() {
        return this.y2 - this.y1 + 1;
    }

    public int getSizeZ() {
        return this.z2 - this.z1 + 1;
    }

    public int getLowerX() {
        return this.x1;
    }

    public int getLowerY() {
        return this.y1;
    }

    public int getLowerZ() {
        return this.z1;
    }

    public int getUpperX() {
        return this.x2;
    }

    public int getUpperY() {
        return this.y2;
    }

    public int getUpperZ() {
        return this.z2;
    }

    public Block[] corners() {
        Block[] corners = new Block[8];
        World world = this.getWorld();
        corners[0] = world.getBlockAt(this.x1, this.y1, this.z1);
        corners[1] = world.getBlockAt(this.x1, this.y1, this.z2);
        corners[2] = world.getBlockAt(this.x1, this.y2, this.z1);
        corners[3] = world.getBlockAt(this.x1, this.y2, this.z2);
        corners[4] = world.getBlockAt(this.x2, this.y1, this.z1);
        corners[5] = world.getBlockAt(this.x2, this.y1, this.z2);
        corners[6] = world.getBlockAt(this.x2, this.y2, this.z1);
        corners[7] = world.getBlockAt(this.x2, this.y2, this.z2);
        return corners;
    }

    public Cuboid expand(CuboidDirection direction, int amount) {
        return switch (direction) {
            case NORTH -> new Cuboid(this.worldName, this.x1, this.y1, this.z1 - amount, this.x2, this.y2, this.z2);
            case SOUTH -> new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, this.z2 + amount);
            case EAST -> new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2 + amount, this.y2, this.z2);
            case WEST -> new Cuboid(this.worldName, this.x1 - amount, this.y1, this.z1, this.x2, this.y2, this.z2);
            case DOWN -> new Cuboid(this.worldName, this.x1, this.y1 - amount, this.z1, this.x2, this.y2, this.z2);
            case UP -> new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2 + amount, this.z2);
            default -> throw new IllegalArgumentException("Invalid direction " + direction);
        };
    }

    public Cuboid expandAllDirections(int amount) {
        return new Cuboid(this.worldName, this.x1 - amount, this.y1 - amount, this.z1 - amount,
                this.x2 + amount, this.y2 + amount, this.z2 + amount);
    }

    public Cuboid expandForGenCube(int n, BlockFace face) {
        int half = (int) Math.ceil(n / 2.0) + 1;
        if (face.equals(BlockFace.NORTH)) {
            return new Cuboid(this.worldName, this.x1 - half, this.y1, this.z1 - (n + 2),
                    this.x2 + half, this.y2 + (n + 2), this.z2);
        }
        if (face.equals(BlockFace.SOUTH)) {
            return new Cuboid(this.worldName, this.x1 - half, this.y1, this.z1,
                    this.x2 + half, this.y2 + (n + 2), this.z2 + (n + 2));
        }
        if (face.equals(BlockFace.EAST)) {
            return new Cuboid(this.worldName, this.x1, this.y1, this.z1 - half,
                    this.x2 + (n + 2), this.y2 + (n + 2), this.z2 + half);
        }
        if (face.equals(BlockFace.WEST)) {
            return new Cuboid(this.worldName, this.x1 - (n + 2), this.y1, this.z1 - half,
                    this.x2, this.y2 + (n + 2), this.z2 + half);
        }
        return null;
    }

    public List<Cuboid> getBorders() {
        List<Cuboid> borders = new ArrayList<>();
        List<CuboidDirection> directions = new ArrayList<>();
        directions.add(CuboidDirection.NORTH);
        directions.add(CuboidDirection.SOUTH);
        directions.add(CuboidDirection.WEST);
        directions.add(CuboidDirection.EAST);

        for (CuboidDirection direction : directions) {
            Cuboid face = this.getFace(direction);
            borders.add(face.getFace(CuboidDirection.UP));
            borders.add(face.getFace(CuboidDirection.DOWN));
            if (direction == CuboidDirection.NORTH || direction == CuboidDirection.SOUTH) {
                borders.add(face.getFace(CuboidDirection.WEST));
                borders.add(face.getFace(CuboidDirection.EAST));
            }
            if (direction == CuboidDirection.EAST || direction == CuboidDirection.WEST) {
                borders.add(face.getFace(CuboidDirection.NORTH));
                borders.add(face.getFace(CuboidDirection.SOUTH));
            }
        }
        return borders;
    }

    public List<Block> getBlocksPercentageToRebuildGenCube(QuantityType quantityType, int n) {
        List<Block> airBlocks = new ArrayList<>();
        for (Block block : this.getBlocks()) {
            if (block.getType() != Material.AIR) continue;
            airBlocks.add(block);
        }
        airBlocks.sort(Comparator.comparing(Block::getY));

        List<Block> result = new ArrayList<>();
        if (quantityType == QuantityType.PERCENTAGE) {
            int count = n * this.getBlocks().size() / 100;
            if (count >= airBlocks.size()) {
                return airBlocks;
            }
            for (int i = 0; i < count; ++i) {
                result.add(airBlocks.get(i));
            }
        }
        if (quantityType == QuantityType.FIXED) {
            for (int i = 0; i < n; ++i) {
                if (i > airBlocks.size() - 1) continue;
                result.add(airBlocks.get(i));
            }
        }
        return result;
    }

    public Cuboid shift(CuboidDirection direction, int amount) {
        return this.expand(direction, amount).expand(direction.opposite(), -amount);
    }

    public Cuboid outset(CuboidDirection direction, int amount) {
        return switch (direction) {
            case HORIZONTAL -> this.expand(CuboidDirection.NORTH, amount)
                    .expand(CuboidDirection.SOUTH, amount)
                    .expand(CuboidDirection.WEST, amount)
                    .expand(CuboidDirection.EAST, amount);
            case VERTICAL -> this.expand(CuboidDirection.UP, amount)
                    .expand(CuboidDirection.DOWN, amount);
            case BOTH -> this.outset(CuboidDirection.HORIZONTAL, amount)
                    .outset(CuboidDirection.VERTICAL, amount);
            default -> throw new IllegalArgumentException("Invalid direction " + direction);
        };
    }

    public Cuboid inset(CuboidDirection direction, int amount) {
        return this.outset(direction, -amount);
    }

    public boolean contains(int x, int y, int z) {
        return x >= this.x1 && x <= this.x2
                && y >= this.y1 && y <= this.y2
                && z >= this.z1 && z <= this.z2;
    }

    public boolean contains(Block block) {
        return this.contains(block.getLocation());
    }

    public boolean contains(Location location) {
        if (!this.worldName.equals(Objects.requireNonNull(location.getWorld()).getName())) {
            return false;
        }
        return this.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public int getVolume() {
        return this.getSizeX() * this.getSizeY() * this.getSizeZ();
    }

    public byte getAverageLightLevel() {
        long total = 0L;
        int count = 0;
        for (Block block : this) {
            if (!block.isEmpty()) continue;
            total += block.getLightLevel();
            ++count;
        }
        if (count > 0) {
            return (byte) (total / count);
        }
        return 0;
    }

    public Cuboid contract() {
        return this.contract(CuboidDirection.UP)
                .contract(CuboidDirection.SOUTH)
                .contract(CuboidDirection.WEST)
                .contract(CuboidDirection.DOWN)
                .contract(CuboidDirection.NORTH)
                .contract(CuboidDirection.EAST);
    }

    public Cuboid contract(CuboidDirection direction) {
        Cuboid face = this.getFace(direction.opposite());
        return switch (direction) {
            case UP -> {
                while (face.containsOnly(0) && face.getLowerY() > this.getLowerY()) {
                    face = face.shift(CuboidDirection.UP, 1);
                }
                yield new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, face.getUpperY(), this.z2);
            }
            case DOWN -> {
                while (face.containsOnly(0) && face.getUpperY() < this.getUpperY()) {
                    face = face.shift(CuboidDirection.DOWN, 1);
                }
                yield new Cuboid(this.worldName, this.x1, face.getLowerY(), this.z1, this.x2, this.y2, this.z2);
            }
            case NORTH -> {
                while (face.containsOnly(0) && face.getLowerZ() > this.getLowerZ()) {
                    face = face.shift(CuboidDirection.NORTH, 1);
                }
                yield new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, face.getUpperZ());
            }
            case SOUTH -> {
                while (face.containsOnly(0) && face.getUpperZ() < this.getUpperZ()) {
                    face = face.shift(CuboidDirection.SOUTH, 1);
                }
                yield new Cuboid(this.worldName, this.x1, this.y1, face.getLowerZ(), this.x2, this.y2, this.z2);
            }
            case EAST -> {
                while (face.containsOnly(0) && face.getUpperX() < this.getUpperX()) {
                    face = face.shift(CuboidDirection.EAST, 1);
                }
                yield new Cuboid(this.worldName, face.getLowerX(), this.y1, this.z1, this.x2, this.y2, this.z2);
            }
            case WEST -> {
                while (face.containsOnly(0) && face.getLowerX() > this.getLowerX()) {
                    face = face.shift(CuboidDirection.WEST, 1);
                }
                yield new Cuboid(this.worldName, this.x1, this.y1, this.z1, face.getUpperX(), this.y2, this.z2);
            }
            default -> throw new IllegalArgumentException("Invalid direction " + direction);
        };
    }

    public Cuboid getFace(CuboidDirection direction) {
        return switch (direction) {
            case UP -> new Cuboid(this.worldName, this.x1, this.y2, this.z1, this.x2, this.y2, this.z2);
            case DOWN -> new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y1, this.z2);
            case NORTH -> new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, this.z1);
            case SOUTH -> new Cuboid(this.worldName, this.x1, this.y1, this.z2, this.x2, this.y2, this.z2);
            case EAST -> new Cuboid(this.worldName, this.x2, this.y1, this.z1, this.x2, this.y2, this.z2);
            case WEST -> new Cuboid(this.worldName, this.x1, this.y1, this.z1, this.x1, this.y2, this.z2);
            default -> throw new IllegalArgumentException("Invalid direction " + direction);
        };
    }

    public boolean containsOnly(int typeId) {
        for (Block block : this) {
            if (block.getType().getId() == typeId) continue;
            return false;
        }
        return true;
    }

    public Cuboid getBoundingCuboid(Cuboid other) {
        if (other == null) {
            return this;
        }
        int minX = Math.min(this.getLowerX(), other.getLowerX());
        int minY = Math.min(this.getLowerY(), other.getLowerY());
        int minZ = Math.min(this.getLowerZ(), other.getLowerZ());
        int maxX = Math.max(this.getUpperX(), other.getUpperX());
        int maxY = Math.max(this.getUpperY(), other.getUpperY());
        int maxZ = Math.max(this.getUpperZ(), other.getUpperZ());
        return new Cuboid(this.worldName, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Block getRelativeBlock(int x, int y, int z) {
        return this.getWorld().getBlockAt(this.x1 + x, this.y1 + y, this.z1 + z);
    }

    public Block getRelativeBlock(World world, int x, int y, int z) {
        return world.getBlockAt(this.x1 + x, this.y1 + y, this.z1 + z);
    }

    public List<Chunk> getChunks() {
        List<Chunk> chunks = new ArrayList<>();
        World world = this.getWorld();
        int minChunkX = this.getLowerX() & 0xFFFFFFF0;
        int maxChunkX = this.getUpperX() & 0xFFFFFFF0;
        int minChunkZ = this.getLowerZ() & 0xFFFFFFF0;
        int maxChunkZ = this.getUpperZ() & 0xFFFFFFF0;
        for (int x = minChunkX; x <= maxChunkX; x += 16) {
            for (int z = minChunkZ; z <= maxChunkZ; z += 16) {
                chunks.add(world.getChunkAt(x >> 4, z >> 4));
            }
        }
        return chunks;
    }

    @Override
    public @NotNull Iterator<Block> iterator() {
        return new CuboidIterator(this, this.getWorld(), this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
    }

    @Override
    public Cuboid clone() throws CloneNotSupportedException {
        Cuboid blocks = (Cuboid) super.clone();
        return new Cuboid(this);
    }

    @Override
    public String toString() {
        return "Cuboid: " + this.worldName + "," + this.x1 + "," + this.y1 + "," + this.z1
                + "=>" + this.x2 + "," + this.y2 + "," + this.z2;
    }

    public Block[] getMainOppositeCorners() {
        return this.mainOppositeCorners;
    }

    public enum CuboidDirection {
        NORTH,
        EAST,
        SOUTH,
        WEST,
        UP,
        DOWN,
        HORIZONTAL,
        VERTICAL,
        BOTH,
        UNKNOWN;

        public CuboidDirection opposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case EAST -> WEST;
                case SOUTH -> NORTH;
                case WEST -> EAST;
                case HORIZONTAL -> VERTICAL;
                case VERTICAL -> HORIZONTAL;
                case UP -> DOWN;
                case DOWN -> UP;
                case BOTH -> BOTH;
                default -> UNKNOWN;
            };
        }
    }

    public static class CuboidIterator implements Iterator<Block> {
        private final World world;
        private final int baseX;
        private final int baseY;
        private final int baseZ;
        private int x;
        private int y;
        private int z;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;

        public CuboidIterator(Cuboid cuboid, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.world = world;
            this.baseX = x1;
            this.baseY = y1;
            this.baseZ = z1;
            this.sizeX = Math.abs(x2 - x1) + 1;
            this.sizeY = Math.abs(y2 - y1) + 1;
            this.sizeZ = Math.abs(z2 - z1) + 1;
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        @Override
        public boolean hasNext() {
            return this.x < this.sizeX && this.y < this.sizeY && this.z < this.sizeZ;
        }

        @Override
        public Block next() {
            Block block = this.world.getBlockAt(this.baseX + this.x, this.baseY + this.y, this.baseZ + this.z);
            if (++this.x >= this.sizeX) {
                this.x = 0;
                if (++this.y >= this.sizeY) {
                    this.y = 0;
                    ++this.z;
                }
            }
            return block;
        }

        @Override
        public void remove() {
        }
    }
}