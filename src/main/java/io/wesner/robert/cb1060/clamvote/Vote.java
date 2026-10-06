package io.wesner.robert.cb1060.clamvote;

import lombok.Data;
import org.jspecify.annotations.NullMarked;

@Data
@NullMarked
public final class Vote { // TODO: maybe VoteEvent, if i do decide to not implement the old style of handlers
    private final String service;
    private final String username;
    private final String address;
    private final String timestamp;

    // TODO: am contemplating to make this a "rich entity" that has convenience logic for grabbing more player data straight from bukkit, maybe within a "VoteMeta" class composition to not clutter completion, maybe not at all, who even cares, not like anyone besides me will ever use this but hey
}
