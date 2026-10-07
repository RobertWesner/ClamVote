# ClamVote

A custom Votifier-v1-protocol-compatible library plugin built for the modern beta developers.

Targets CB1060 via Project Poseidon [v1](https://github.com/retromcorg/Project-Poseidon) and [v2](https://github.com/legacyminecraft/Project-Poseidon-V2).

Made with 🐚 for use in [BetaMC.org](https://betamc.org).

## Usage

Use Poseidon `@EventHandler` annotations:

```java
@EventHandler(ignoreCancelled = true)
private void onVote(VoteEvent event) {
    this.plugin.getLogger().info(
        event.getUsername() + " voted on \""
            + event.getService() + "\"!"
    );
}
```

## License

[//]: # (no one will use this but might as well)

[MIT](./LICENSE.txt)
