package io.wesner.robert.cb1060.clamvote.internal;

import io.wesner.robert.cb1060.clamvote.ClamVote;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@NullMarked
public final class CVHandler {
    private final CVKeys keys;
    private final BlockingQueue<byte[]> requestChan;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public CVHandler start() {
        cancelled.set(false);

        if (
            Bukkit.getScheduler().scheduleAsyncDelayedTask(ClamVote.plugin, this::run) == -1
        ) throw new RuntimeException("Could not schedule server task.");

        return this;
    }

    public void stop() {
        cancelled.set(true);
        requestChan.add(new byte[0]);
    }

    @SneakyThrows(InterruptedException.class)
    private void run() {
        while (!cancelled.get()) {
            byte[] packet = requestChan.take();
            if (cancelled.get()) break;

            handle(packet);
        }
    }

    private void handle(byte[] packet) {
        try {
            System.out.println(new String(keys.decrypt(packet))); // TODO: parsing is easy enough, but my brain is spent
        } catch (IOException exception) {
            // TODO
            exception.printStackTrace();
        }
    }
}
