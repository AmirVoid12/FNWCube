package iran.flame.network.cube.enums;

public enum RegionManagementPlugin {
    PLOTSQUARED,
    WORLDGUARD;

    private String version = "";

    public final String getVersion() {
        return this.version;
    }

    public final void setVersion(String version) {
        this.version = version;
    }
}