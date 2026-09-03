package net.coreprotect.paper.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import net.coreprotect.command.TabHandler;
import net.coreprotect.command.parser.MaterialParser;

/**
 * 在数据包加载或重载后刷新标签缓存。
 *
 * /minecraft:reload 与 /datapack enable|disable 都会触发该事件，
 * 标签集合可能因此变化，缓存必须跟着失效。
 */
public final class ServerResourcesReloadedListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerResourcesReloaded(ServerResourcesReloadedEvent event) {
        MaterialParser.invalidateDynamicTags();
        TabHandler.invalidateMaterials();
    }
}
