package pw.saber.blocktop.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public interface SchedulerAdapter {

  Executor async();

  Executor sync();

  default void executeAsync(Runnable task) {
    this.async().execute(task);
  }

  default void executeSync(Runnable task) {
    this.sync().execute(task);
  }

  SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit);

  SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit);

  void shutdownScheduler();

  void shutdownExecutor();
}