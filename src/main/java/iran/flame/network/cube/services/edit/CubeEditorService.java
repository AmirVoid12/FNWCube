package iran.flame.network.cube.services.edit;

import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.enums.BuildingStatus;
import iran.flame.network.cube.enums.QuantityType;
import iran.flame.network.cube.enums.RebuildType;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.interfaces.EditorService;
import iran.flame.network.cube.services.edit.compatibility.FaweLatest;
import iran.flame.network.cube.tasks.edit.CubeEdition;
import iran.flame.network.cube.enums.EditTask;

public class CubeEditorService {
    private final GenCubes plugin = GenCubes.getInstance();
    private EditorService editorService;

    public CubeEditorService() {
        if (this.plugin.isFawe()) {
            this.editorService = new FaweLatest();
        }
    }

    public BuildingStatus build(GenCube cube, RebuildType rebuildType, QuantityType quantityType, int quantityValue, boolean useEditorService) {
        if (this.plugin.getTaskManager().getCubeEditionByCube(cube) != null) {
            return BuildingStatus.ALREADY_IN_PROGRESS;
        }

        if (this.editorService != null && useEditorService) {
            boolean success = this.editorService.build(cube, rebuildType, quantityType, quantityValue);
            if (!success) {
                return BuildingStatus.ALREADY_BUILT;
            }
        } else {
            EditTask editTask = null;
            if (rebuildType == RebuildType.LINEAL) {
                editTask = EditTask.ADD;
            }
            if (rebuildType == RebuildType.RANDOM) {
                editTask = EditTask.REMOVE;
            }
            assert editTask != null;

            CubeEdition cubeEdition = new CubeEdition(cube, editTask, quantityType, quantityValue);
            this.plugin.getTaskManager().register(cubeEdition);
            if (cubeEdition.isAlreadyBuilt()) {
                return BuildingStatus.ALREADY_BUILT;
            }
        }

        return BuildingStatus.SUCCESS;
    }

    public void upgrade(GenCube cube) {
        if (this.editorService != null) {
            this.editorService.upgrade(cube);
            return;
        }
        if (this.plugin.getTaskManager().getCubeEditionByCube(cube) == null) {
            this.plugin.getTaskManager().register(new CubeEdition(cube, EditTask.MULTIPLY));
        }
    }

    public void remove(GenCube cube) {
        if (this.editorService != null) {
            this.editorService.remove(cube);
            this.plugin.getCubeManager().getCubesStorage().delete(cube.getUuid(), true);
            return;
        }
        if (this.plugin.getTaskManager().getCubeEditionByCube(cube) == null) {
            CubeEdition cubeEdition = new CubeEdition(cube, EditTask.SET);
            cubeEdition.setOnFinish(() -> this.plugin.getCubeManager().getCubesStorage().delete(cube.getUuid(), true));
            this.plugin.getTaskManager().register(cubeEdition);
        } else {
            this.plugin.getCubeManager().getCubesStorage().delete(cube.getUuid(), true);
        }
    }

    public EditorService getEditorService() {
        return this.editorService;
    }
}