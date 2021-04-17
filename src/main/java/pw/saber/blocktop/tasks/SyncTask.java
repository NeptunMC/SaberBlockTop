package pw.saber.blocktop.tasks;

import java.util.concurrent.TimeUnit;
import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.cache.BufferedRequest;

public class SyncTask implements Runnable {
  private final NeptunTopPlugin plugin;

  public SyncTask(NeptunTopPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    // Reload all online users.
    this.plugin.getUserManager().loadAllUsers().join();
  }

  public static class Buffer extends BufferedRequest<Void> {
    private final NeptunTopPlugin plugin;

    public Buffer(NeptunTopPlugin plugin) {
      super(500L, TimeUnit.MILLISECONDS, plugin.getScheduler());
      this.plugin = plugin;
    }

    @Override
    protected Void perform() {
      new SyncTask(this.plugin).run();
      return null;
    }
  }
}