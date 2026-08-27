package iran.flame.network.cube;

import org.bukkit.Material;

public class GenerationBlock extends BaseBlock {
    private double a;

    public GenerationBlock(Material material, short s, double d) {
        super(material, s);
        this.setPercentage(d);
    }

    public boolean isSimilarTo(GenerationBlock generationBlock) {
        return generationBlock.getMaterial().equals(this.getMaterial()) && generationBlock.getData() == this.getData();
    }

    public double getPercentage() {
        return this.a;
    }

    public void setPercentage(double d) {
        this.a = d;
    }
}

