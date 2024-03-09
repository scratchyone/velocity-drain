package com.scratchyone.velocitydrain;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;

@Plugin(
    id = "velocitydrain",
    name = "Velocity Drain",
    version = "0.1.0-SNAPSHOT",
    description = "Prevents velocity from exiting on SIGINT until all players have disconnected",
    authors = {"scratchyone"})
public class VelocityDrain {

  private final ProxyServer server;
  private final Logger logger;

  private final CountDownLatch shutdownLatch = new CountDownLatch(1);

  public static final PlainTextComponentSerializer SERIALIZER =
      PlainTextComponentSerializer.builder().flattener(ComponentFlattener.basic()).build();

  @Inject
  public VelocityDrain(ProxyServer server, Logger logger) {
    this.server = server;
    this.logger = logger;
  }

  @Subscribe()
  public void onProxyInitialization(ProxyInitializeEvent event) {
    // Do some operation demanding access to the Velocity API here.
    // For instance, we could register an event:
    // server.getEventManager().register(this, new PluginListener());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onProxyShutdown(ListenerCloseEvent event) {
    logger.info("Proxy is shutting down. Waiting for all players to disconnect...");
    this.server
        .getScheduler()
        .buildTask(
            this,
            () -> {
              tick();
            })
        .delay(1L, TimeUnit.SECONDS)
        .schedule();
    try {
      this.shutdownLatch.await();
      logger.info("Shutdown latch finished...");
    } catch (InterruptedException e) {
      logger.error("Unexpected error, failed to prevent shutdown:", e);
    }
  }

  private void tick() {
    if (this.server.getPlayerCount() == 0) {
      shutdownLatch.countDown();
      logger.info("Player count is 0, now shutting down velocity...");
    } else {
      this.server
          .getScheduler()
          .buildTask(
              this,
              () -> {
                tick();
              })
          .delay(1L, TimeUnit.SECONDS)
          .schedule();
    }
  }
}
