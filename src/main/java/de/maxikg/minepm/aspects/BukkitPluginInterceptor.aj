package de.maxikg.minepm.aspects;

import de.maxikg.minepm.plugin.MinePMPluginLoader;
import de.maxikg.minepm.plugin.MinePMServicePlugin;
import de.maxikg.minepm.utils.PluginUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.SimplePluginManager;

import java.io.File;
import java.util.logging.Logger;

public aspect BukkitPluginInterceptor {

    private static final Logger LOGGER = Logger.getLogger(BukkitPluginInterceptor.class.getName());

    pointcut isEnablePlugins(PluginLoadOrder type): execution(public void org.bukkit.craftbukkit..CraftServer.enablePlugins(PluginLoadOrder)) && args(type);

    before(PluginLoadOrder type): isEnablePlugins(type) {
        if (type == PluginLoadOrder.STARTUP) {
            if (!AspectConfiguration.isPluginRequired()) {
                LOGGER.info("Skip interception of MinePM service plugin because no features of the plugin are enabled.");
                return;
            }

            SimplePluginManager pm = PluginUtils.getPluginManager(thisJoinPoint.getTarget());
            if (pm == null)
                return;
            File pluginFile = PluginUtils.getFile();
            Plugin plugin = new MinePMServicePlugin(pluginFile.getParentFile(), MinePMPluginLoader.INSTANCE, AspectConfiguration.BUKKIT_PLAYER_OBSERVATION_ENABLED, AspectConfiguration.BUKKIT_PLAYER_TPS_ENABLED);
            if (plugin != null) {
                LOGGER.info("Enable MinePM service plugin through interception...");
                pm.enablePlugin(plugin);
            }
        }
    }
}
