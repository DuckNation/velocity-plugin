package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NetworkBan implements SimpleCommand {

    private final ProxyServer server;

    public NetworkBan(ProxyServer server) {
        this.server = server;
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("duck.network-ban");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(server.getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toList()));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        if (args.length < 1) {
            source.sendMessage(Component.text("Usage: /network-ban <player>", NamedTextColor.RED));
        }

        Player player = server.getPlayer(args[0]).orElse(null);
        if (player == null) {
            source.sendMessage(Component.text("Player not found", NamedTextColor.RED));
            return;
        }
        String reason = "No reason specified";
        if (args.length > 1) {
            args[0] = "";
            reason = String.join(" ", args);
        }

        player.disconnect(Component.text("You have been banned: ", NamedTextColor.RED).append(Component.text(reason, NamedTextColor.GRAY)));
    }
}
