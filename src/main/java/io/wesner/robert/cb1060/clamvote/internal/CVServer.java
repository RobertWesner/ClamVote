package io.wesner.robert.cb1060.clamvote.internal;

import io.wesner.robert.cb1060.clamvote.ClamVote;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Slightly overengineered async server for the Votifier v1 socket.
 */
@RequiredArgsConstructor
@NullMarked
public final class CVServer {
    private final int port;
    private final BlockingQueue<byte[]> requestChan;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final Object lifecycleLock = new Object(); // love me some Java magic with arbitrary Objects

    @Nullable
    private volatile ServerSocket server;

    public CVServer start() {
        cancelled.set(false);

        if (
            Bukkit.getScheduler().scheduleAsyncDelayedTask(ClamVote.plugin, this::run) == -1
        ) throw new RuntimeException("Could not schedule server task.");

        return this;
    }

    public void stop() {
        synchronized (lifecycleLock) {
            cancelled.set(true);

            // local copy to prevent async race conditions
            val server = this.server;
            try {
                if (server != null) server.close();
            } catch (IOException ignored) {} finally {
                this.server = null;
            }
        }
    }

    private void run() {
        try (val server = new ServerSocket(port)) {
            synchronized (lifecycleLock) {
                // safety check, in case stop and run collide
                if (cancelled.get()) return;

                // exposing to the class scope to properly close
                this.server = server;
            }

            while (!cancelled.get()) {
                val socket = server.accept();
                socket.setSoTimeout(5000);

                Bukkit.getScheduler()
                    .scheduleAsyncDelayedTask(
                        ClamVote.plugin,
                        () -> handleSocket(socket)
                    );
            }
        } catch (IOException exception) {
            if (cancelled.get()) return;

            Bukkit.getLogger()
                .severe(
                    "ClamVote server crashed! Requires manual restart. "
                    + ExceptionUtils.getStackTrace(exception)
                );
        } finally {
            // only happens when killed since the while loop has no exit besides cancellation, still sane
            synchronized (lifecycleLock) {
                this.server = null;
            }
        }
    }

    private void handleSocket(Socket socket) {
        try (SocketIO io = new SocketIO(socket)) {
            io.write("VOTIFIER 1\n");
            requestChan.add(io.read());
        } catch (IOException exception) {
            Bukkit.getLogger()
                .warning(
                    "Handling Votifier socket failed, potential loss of single vote. Continuing to handle requests. "
                    + ExceptionUtils.getStackTrace(exception)
                );
        }
    }

    @RequiredArgsConstructor
    private static class SocketIO implements AutoCloseable {
        private final Socket socket;

        public void write(String s) throws IOException {
            socket.getOutputStream().write(bytes(s));
        }

        public byte[] read() throws IOException {
            val in = socket.getInputStream();
            byte[] packet = new byte[256];
            int offset = 0;

            while (offset < packet.length) {
                int read = in.read(packet, offset, packet.length - offset);
                if (read == -1) {
                    throw new IOException("Unexpected EOF after " + offset + " bytes.");
                }

                offset += read;
            }

            return packet;
        }

        public void close() throws IOException {
            socket.close();
        }

        private byte[] bytes(String s) {
            return s.getBytes(StandardCharsets.UTF_8);
        }
    }
}
