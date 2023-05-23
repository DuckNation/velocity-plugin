package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Transfer implements SimpleCommand {

    private final ProxyServer server;

    public Transfer(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 2) {
            invocation.source().sendMessage(Component.text("Usage: /transfer <player> <server>", NamedTextColor.RED));
            return;
        }
        Player toMove = server.getPlayer(invocation.arguments()[0]).orElse(null);
        if (toMove == null) {
            invocation.source().sendMessage(Component.text("Player not found", NamedTextColor.RED));
            return;
        }


        Component invoker;
        if (invocation.source() instanceof Player) {
            Player player = (Player) invocation.source();
            invoker = Component.text(player.getUsername(), NamedTextColor.YELLOW);
        } else {
            invoker = Component.text("Console", NamedTextColor.YELLOW);
        }
        Component message = invoker.append(Component.text(" is moving you to ", NamedTextColor.GRAY).append(Component.text(invocation.arguments()[1], NamedTextColor.GREEN)));
        String serverName = invocation.arguments()[1];
        RegisteredServer registeredServer = server.getServer(serverName).orElse(null);
        if (registeredServer == null) {
            invocation.source().sendMessage(Component.text("Server not found", NamedTextColor.RED));
            return;
        }
        toMove.sendMessage(message);
        server.getScheduler()
                .buildTask(DuckSMPUtils.getInstance(), () -> {
                    toMove.createConnectionRequest(registeredServer).fireAndForget();
                })
                .delay(3L, TimeUnit.SECONDS)
                .schedule();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> server.getAllPlayers().stream()
                .map(Player::getUsername)
                .collect(Collectors.toList()));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("duck.transfer");
    }
}
