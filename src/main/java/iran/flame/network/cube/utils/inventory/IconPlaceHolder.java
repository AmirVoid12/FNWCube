package iran.flame.network.cube.utils.inventory;

public abstract class IconPlaceHolder {
    private final String placeHolder;

    public IconPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public abstract String getReplacement();

    public String getPlaceHolder() {
        return this.placeHolder;
    }
}