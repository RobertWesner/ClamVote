package io.wesner.robert.cb1060.clamvote;

import io.wesner.robert.cb1060.clamvote.internal.CVServer;
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

    @Override
    public void onDisable() {
        if (server != null) server.stop();

        // TODO

        plugin = null;
        server = null;
    }

    @Override
    public void onEnable() {
        plugin = this;

        BlockingQueue<byte[]> requestChan = new LinkedBlockingQueue<>();
        server = new CVServer(8192, requestChan).start(); // TODO: port from config

        // TODO
    }
}
