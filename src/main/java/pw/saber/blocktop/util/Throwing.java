package pw.saber.blocktop.util;

public final class Throwing {
  private Throwing() {
    throw new AssertionError();
  }

  @FunctionalInterface
  public interface Runnable {
    void run() throws Exception;
  }

  @FunctionalInterface
  public interface Consumer<T> {
    void accept(T t) throws Exception;
  }
}