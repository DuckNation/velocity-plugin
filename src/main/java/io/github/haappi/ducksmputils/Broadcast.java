package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Broadcast implements RawCommand {
    private final ProxyServer server;

    public Broadcast(ProxyServer server) {
        this.server = server;
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("duck.broadcast");
    }

    @Override
    public void execute(final Invocation invocation) {
        Component invoker;

        if (invocation.source() instanceof Player) {
            invoker = Component.text(((Player) invocation.source()).getUsername() + ": ", NamedTextColor.GOLD);
        } else {
            invoker = Component.text("Console: ", NamedTextColor.GOLD);
        }
        Component message = Component.text("[Broadcast] ", NamedTextColor.RED).append(invoker).append(Component.text(invocation.arguments(), NamedTextColor.YELLOW));
        server.getAllPlayers().forEach(player -> player.sendMessage(message));
    }
}
