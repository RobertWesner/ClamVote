package io.wesner.robert.cb1060.clamvote;

import io.wesner.robert.cb1060.clamvote.internal.CVConfig;
import io.wesner.robert.cb1060.clamvote.internal.CVHandler;
import io.wesner.robert.cb1060.clamvote.internal.CVKeys;
import io.wesner.robert.cb1060.clamvote.internal.CVServer;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// TODO: implement Bukkit events, probably going to ditch the original handler stuff that came in normal votifier, this is not intended as drop-in compat replacement

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
    }

    @Override
    public void onEnable() {
        plugin = this;

        val config = CVConfig.load();
        val keys = CVKeys.load();

        BlockingQueue<byte[]> requestChan = new LinkedBlockingQueue<>();
        server = new CVServer(config.port, requestChan).start();
        handler = new CVHandler(keys, requestChan).start();
    }
}
