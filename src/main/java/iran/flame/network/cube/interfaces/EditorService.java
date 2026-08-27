package iran.flame.network.cube.interfaces;

import iran.flame.network.cube.enums.QuantityType;
import iran.flame.network.cube.enums.RebuildType;
import iran.flame.network.cube.gencube.GenCube;

public interface EditorService {
    boolean build(GenCube cube, RebuildType rebuildType, QuantityType quantityType, int quantityValue);
    void upgrade(GenCube cube);
    void remove(GenCube cube);
}