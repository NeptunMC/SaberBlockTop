package pw.saber.blocktop.listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.user.User;

public class ConnectionListener implements Listener {
  private final NeptunTopPlugin plugin;
  private final Set<UUID> uniqueConnections = ConcurrentHashMap.newKeySet();
  private final Set<UUID> deniedAsyncLogin = Collections.synchronizedSet(new HashSet<>());
  private final Set<UUID> deniedLogin = Collections.synchronizedSet(new HashSet<>());

  public ConnectionListener(NeptunTopPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
    /* Called when the player first attempts a connection with the server.
    Listening on LOW priority to allow plugins to modify username / UUID data here. (auth plugins)
    Also, give other plugins a chance to cancel the event. */

    /* wait for the plugin to enable. because these events are fired async, they can be called before
    the plugin has enabled.  */
    try {
      //noinspection ResultOfMethodCallIgnored
      this.plugin.getEnableLatch().await(60, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
      // another plugin has disallowed the login.
      this.plugin
          .getLogger()
          .info(
              "Another plugin has cancelled the connection for "
                  + e.getUniqueId()
                  + " - "
                  + e.getName()
                  + ". No permissions data will be loaded.");
      this.deniedAsyncLogin.add(e.getUniqueId());
      return;
    }

    /* Actually process the login for the connection.
    We do this here to delay the login until the data is ready.
    If the login gets cancelled later on, then this will be cleaned up.
    This includes:
    - loading uuid data
    - loading permissions
    - creating a user instance in the UserManager for this connection.
    - setting up cached data. */
    try {
      this.loadUser(e.getUniqueId(), e.getName());
      this.recordConnection(e.getUniqueId());
    } catch (Exception ex) {
      this.plugin
          .getPluginLogger()
          .severe(
              "Exception occurred whilst loading data for " + e.getUniqueId() + " - " + e.getName(),
              ex);

      // deny the connection
      this.deniedAsyncLogin.add(e.getUniqueId());

      e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "LOADING_DATABSE_ERROR");
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerPreLoginMonitor(AsyncPlayerPreLoginEvent e) {
    /* Listen to see if the event was cancelled after we initially handled the connection
    If the connection was cancelled here, we need to do something to clean up the data that was loaded. */

    // Check to see if this connection was denied at LOW.
    if (this.deniedAsyncLogin.remove(e.getUniqueId())) {
      // their data was never loaded at LOW priority, now check to see if they have been magically
      // allowed since then.

      // This is a problem, as they were denied at low priority, but are now being allowed.
      if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
        this.plugin.getLogger().severe("Player connection was re-allowed for " + e.getUniqueId());
        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "");
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerLogin(PlayerLoginEvent e) {
    /* Called when the player starts logging into the server.
    At this point, the users data should be present and loaded. */

    final Player player = e.getPlayer();

    final User user = this.plugin.getUserManager().getIfLoaded(player.getUniqueId());

    /* User instance is null for whatever reason. Could be that it was unloaded between asyncpre and now. */
    if (user == null) {
      this.deniedLogin.add(player.getUniqueId());

      if (!this.getUniqueConnections().contains(player.getUniqueId())) {
        this.plugin
            .getPluginLogger()
            .warn(
                "User "
                    + player.getUniqueId()
                    + " - "
                    + player.getName()
                    + " doesn't have data pre-loaded, they have never been processed during pre-login in this session."
                    + " - denying login.");

      } else {
        this.plugin
            .getPluginLogger()
            .warn(
                "User "
                    + player.getUniqueId()
                    + " - "
                    + player.getName()
                    + " doesn't currently have data pre-loaded, but they have been processed before in this session."
                    + " - denying login.");
      }

      e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "LOADING_STATE_ERROR");
    }
  }

  // Wait until the last priority to unload, so plugins can still perform permission checks on this
  // event
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerLoginMonitor(PlayerLoginEvent e) {
    /* Listen to see if the event was cancelled after we initially handled the login
    If the connection was cancelled here, we need to do something to clean up the data that was loaded. */

    // Check to see if this connection was denied at LOW. Even if it was denied at LOW, their data
    // will still be present.
    if (this.deniedLogin.remove(e.getPlayer().getUniqueId())) {
      // This is a problem, as they were denied at low priority, but are now being allowed.
      if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
        this.plugin
            .getLogger()
            .severe("Player connection was re-allowed for " + e.getPlayer().getUniqueId());
        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "");
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent e) {
    final Player player = e.getPlayer();
    this.handleDisconnect(player.getUniqueId());
  }

  public Set<UUID> getUniqueConnections() {
    return this.uniqueConnections;
  }

  protected void recordConnection(UUID uniqueId) {
    this.uniqueConnections.add(uniqueId);
  }

  public User loadUser(UUID uniqueId, String username) {
    final long startTime = System.currentTimeMillis();

    // register with the housekeeper to avoid accidental unloads
    this.plugin.getUserManager().getHouseKeeper().registerUsage(uniqueId);

    User user = this.plugin.getStorage().loadUser(uniqueId).join();
    if (user == null) {
      throw new NullPointerException("User is null");
    }

    final long time = System.currentTimeMillis() - startTime;
    if (time >= 1000) {
      this.plugin
          .getPluginLogger()
          .warn("Processing login for " + username + " took " + time + "ms.");
    }

    return user;
  }

  public void handleDisconnect(UUID uniqueId) {
    // Register with the housekeeper, so the User's instance will stick
    // around for a bit after they disconnect
    this.plugin.getUserManager().getHouseKeeper().registerUsage(uniqueId);
  }
}