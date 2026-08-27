package iran.flame.network.cube.utils;

public record Range(Double low, Double high) {
    public boolean contains(Double value) {
        return value >= this.low && value <= this.high;
    }
}