package io.wesner.robert.cb1060.clamvote.internal;

import io.wesner.robert.cb1060.clamvote.ClamVote;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
@NullMarked
public class CVConfig {
    public final int port;

    @SneakyThrows(IOException.class)
    public static CVConfig load() {
        if (ClamVote.plugin == null) throw new NullPointerException("Cannot load configuration on unloaded plugin.");

        val dataFolder = ClamVote.plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) throw new IOException("Failed to create plugin directory.");

        val configFile = new File(dataFolder, "config.yml");
        try (
            val in = ClamVote.class.getResourceAsStream("/config.yml");
            val out = new FileOutputStream(configFile)
        ) {
            byte[] buffer = new byte[8192];
            int read;

            while (true) {
                assert in != null;
                if ((read = in.read(buffer)) == -1) break;
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        val config = ClamVote.plugin.getConfiguration();

        return new CVConfig(
            config.getInt("server.port", 8192)
        );
    }
}
