package io.wesner.robert.cb1060.clamvote.internal;

import io.wesner.robert.cb1060.clamvote.ClamVote;
import io.wesner.robert.cb1060.clamvote.VoteEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            val parts = new String(keys.decrypt(packet), StandardCharsets.UTF_8)
                .split("\n", 6); // spec explicitly says LF, no CR, no CRLF

            if (parts.length < 5) {
                Bukkit.getLogger().warning("Invalid Votifier packet detected, expected 5 parts, got " + parts.length + ".");

                return;
            }

            if (!parts[0].equals("VOTE")) {
                Bukkit.getLogger().warning("Invalid Votifier packet detected, expected \"VOTE\" segment, got " + parts[0] + ".");

                return;
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(
                ClamVote.plugin,
                () -> Bukkit.getPluginManager().callEvent(
                    new VoteEvent(
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4]
                    )
                )
            );
        } catch (IOException exception) {
            Bukkit.getLogger().warning("Invalid Votifier packet detected, could not decrypt, error: " + exception.getMessage());
        }
    }
}
