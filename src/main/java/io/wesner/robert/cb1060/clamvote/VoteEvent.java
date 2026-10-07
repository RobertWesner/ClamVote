package io.wesner.robert.cb1060.clamvote;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftOfflinePlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Getter
@NullMarked
public final class VoteEvent extends Event {
    /**
     * A devious curse from the depths of my sorcerers handbook.
     */
    @Nullable
    private static Object poseidonV2CompatHandlerList = null;

    /**
     * Per-server-list identifier, unmapped arbitrary string.
     */
    private final String service;

    /**
     * Username set in the server list, arbitrary input without validation.
     */
    private final String username;

    /**
     * Usually IPv4, but can be anything.
     */
    private final String address;

    /**
     * Beware! There is no guarantee that this is an actual integer timestamp, and even if it is, there is no timezone information, this is a dead useless field.
     */
    @Deprecated
    private final String timestamp;

    @Getter(AccessLevel.NONE)
    public final VoteEventApi api = new VoteEventApi();

    // mhmmm... delicious boilerplate
    public VoteEvent(
        String service,
        String username,
        String address,
        String timestamp
    ) {
        super("ClamVote");

        this.service = service;
        this.username = username;
        this.address = address;
        this.timestamp = timestamp;
    }

    public HandlerList getHandlers() {
        return getHandlerList();
    }

    @SneakyThrows
    public static HandlerList getHandlerList() {
        if (poseidonV2CompatHandlerList == null) {
            poseidonV2CompatHandlerList = Class.forName("org.bukkit.event.HandlerList").newInstance();
        }

        return (HandlerList) poseidonV2CompatHandlerList;
    }

    /**
     * Purely for convenient access.
     */
    @NullMarked
    public class VoteEventApi {
        public final VoteEventPlayerApi player = new VoteEventPlayerApi();

        // may be expanded as needed

        public class VoteEventPlayerApi {
            @Nullable
            public CraftPlayer retrieveOnlineOrNull() {
                val player = Bukkit.getPlayer(username);
                if (player == null) return null;

                return (CraftPlayer)player;
            }

            /**
             * Cannot return null! Bukkit always creates an object.
             */
            public CraftOfflinePlayer retrieveOffline() {
                return (CraftOfflinePlayer) Bukkit.getOfflinePlayer(username);
            }

            public boolean isOnline() {
                return Bukkit.getPlayer(username) != null;
            }

            public boolean isBanned() {
                return Bukkit.getOfflinePlayer(username).isBanned();
            }

            public boolean isWhitelisted() {
                return Bukkit.getOfflinePlayer(username).isWhitelisted();
            }
        }
    }
}
