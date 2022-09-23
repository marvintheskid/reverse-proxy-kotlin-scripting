package me.marvin.scripting.kotlin;

import me.marvin.proxy.addon.ProxyAddon;
import me.marvin.proxy.networking.PacketListener;

import java.util.StringJoiner;

public class Main extends ProxyAddon {
    private ProxyScriptHandler handler;

    @Override
    public void onEnable() {
        ProxyScriptConstants.initialize(this, proxy, logger, rootFolder, commandTree);
        this.handler = new ProxyScriptHandler(logger, rootFolder);

        commandTree.register(args -> {
            logger.info("Available commands:");
            logger.info("  kts list - Prints out all the enabled scripts");
            logger.info("  kts load/enable [script] - Enables the given script");
            logger.info("  kts unload/disable [script] - Disables the given script");
            logger.info("  kts restart/reload [script] - Reloads the given script");
            return true;
        }, "kts", "kts help");

        commandTree.register(args -> {
            StringJoiner joiner = new StringJoiner(", ");
            handler.forEachScript((__, script) -> joiner.add(script.getName()));
            logger.info("Enabled scripts: " + joiner);
            return true;
        }, "kts list");

        commandTree.register(args -> {
            if (args.length != 1) {
                proxy.logger().info("Usage: kts load [script]");
                return false;
            }

            logger.info("Enabling '{}'...", args[0]);
            handler.enableScript(args[0]);
            return true;
        }, "kts load", "kts enable");

        commandTree.register(args -> {
            if (args.length != 1) {
                proxy.logger().info("Usage: kts unload [script]");
                return false;
            }

            logger.info("Disabling '{}'...", args[0]);
            handler.disableScript(args[0]);
            return true;
        }, "kts unload", "kts disable");

        commandTree.register(args -> {
            if (args.length != 1) {
                proxy.logger().info("Usage: kts restart [script]");
                return false;
            }

            logger.info("Restarting '{}'...", args[0]);
            handler.restartScript(args[0]);
            return true;
        }, "kts restart", "kts reload");
    }

    @Override
    public void onDisable() {
        this.handler.forEachScript((__, script) -> script.destruct());
    }

    @Override
    public void registerListeners(PacketListener... listeners) {
        super.registerListeners(listeners);
    }

    @Override
    public void unregisterListeners(Object... objects) {
        super.unregisterListeners(objects);
    }
}