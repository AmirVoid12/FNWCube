package iran.flame.network.cube.tasks;

import iran.flame.network.cube.GenCubes;

public abstract class PluginTask implements Runnable {
    protected GenCubes plugin;
    private Integer taskId;
    protected Runnable onFinish;
    private Long period;
    private Long delay;
    private boolean sync;

    public PluginTask(Long period, boolean sync) {
        this.plugin = GenCubes.getInstance();
        this.period = period;
        this.delay = 0L;
        this.sync = sync;
    }

    public PluginTask() {
    }

    public void runTask() {
        if (this.period != null) {
            if (this.sync) {
                this.taskId = this.plugin.getServer().getScheduler()
                        .runTaskTimer(this.plugin, this, this.delay, this.period)
                        .getTaskId();
                return;
            }
            this.taskId = this.plugin.getServer().getScheduler()
                    .runTaskTimerAsynchronously(this.plugin, this, this.delay, this.period)
                    .getTaskId();
        }
    }

    public void stopTask() {
        if (this.taskId != null) {
            this.plugin.getServer().getScheduler().cancelTask(this.taskId);
            this.taskId = null;
        }
    }

    public Integer getTaskId() {
        return this.taskId;
    }

    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    public Long getExecuteTime() {
        return this.period;
    }

    public void setExecuteTime(Long period) {
        this.period = period;
    }

    protected final void setDelay(Long delay) {
        this.delay = delay;
    }
}