package pw.saber.blocktop.storage;

import java.util.UUID;
import pw.saber.blocktop.user.User;

public interface StorageImplementation {
  String getImplementationName();

  void init() throws Exception;

  void shutdown();

  User loadUser(UUID uuid) throws Exception;

  void saveUser(User user) throws Exception;
}