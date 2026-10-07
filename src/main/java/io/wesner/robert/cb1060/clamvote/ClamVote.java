package io.wesner.robert.cb1060.clamvote;

import io.wesner.robert.cb1060.clamvote.internal.CVConfig;
import io.wesner.robert.cb1060.clamvote.internal.CVHandler;
import io.wesner.robert.cb1060.clamvote.internal.CVKeys;
import io.wesner.robert.cb1060.clamvote.internal.CVServer;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@NullMarked
public class ClamVote extends JavaPlugin {
    @Nullable
    public static ClamVote plugin;

    @Nullable
    private CVServer server;
    @Nullable
    private CVHandler handler;

    @Override
    public void onDisable() {
        if (server != null) server.stop();
        if (handler != null) handler.stop();

        plugin = null;
        server = null;
        handler = null;

        Bukkit.getLogger().info("Stopped ClamVote server.");
    }

    @Override
    public void onEnable() {
        plugin = this;

        val config = CVConfig.load();
        val keys = CVKeys.load();

        BlockingQueue<byte[]> requestChan = new LinkedBlockingQueue<>();
        server = new CVServer(config.port, requestChan).start();
        handler = new CVHandler(keys, requestChan).start();

        Bukkit.getLogger().info("Started ClamVote server on port " + config.port + ".");
    }
}
