package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Moveall implements SimpleCommand {
    private final ProxyServer server;

    public Moveall(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(Component.text("Usage: /moveall <server>", NamedTextColor.RED));
            return;
        }
        String serverName = args[0];
        RegisteredServer server = DuckSMPUtils.getInstance().getProxy().getServer(serverName).orElse(null);
        if (server == null) {
            invocation.source().sendMessage(Component.text("Server not found", NamedTextColor.RED));
            return;
        }

        Component invoker;
        if (invocation.source() instanceof Player) {
            Player player = (Player) invocation.source();
            invoker = Component.text(player.getUsername(), NamedTextColor.YELLOW);
        } else {
            invoker = Component.text("Console", NamedTextColor.YELLOW);
        }

        Component message = invoker.append(Component.text(" is moving you to ", NamedTextColor.GRAY).append(Component.text(serverName, NamedTextColor.GREEN)));

        this.server.getAllPlayers().forEach(player -> player.sendMessage(message));

        DuckSMPUtils.getInstance().getProxy().getScheduler()
                .buildTask(DuckSMPUtils.getInstance(), () -> {
                    this.server.getAllPlayers().forEach(player -> player.createConnectionRequest(server).fireAndForget());
                })
                .delay(3L, TimeUnit.SECONDS)
                .schedule();


    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() ->
                server.getAllServers().stream().map(RegisteredServer::getServerInfo).map(ServerInfo::getName).collect(Collectors.toList()
                ));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("duck.move-all");
    }
}