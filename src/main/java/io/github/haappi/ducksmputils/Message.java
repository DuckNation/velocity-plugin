package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Message implements SimpleCommand {
    public static final ConcurrentHashMap<Player, Player> lastMessaged = new ConcurrentHashMap<>();

    public Message() {
        ProxyServer proxy = DuckSMPUtils.getInstance().getProxy();
        proxy.getScheduler().buildTask(DuckSMPUtils.getInstance(), lastMessaged::clear).repeat(5L, TimeUnit.MINUTES).schedule();
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 2) {
            invocation.source().sendMessage(Component.text("Usage: /message <player> <message>", NamedTextColor.RED));
            return;
        }
        Player toMessage = DuckSMPUtils.getInstance().getProxy().getPlayer(invocation.arguments()[0]).orElse(null);
        if (toMessage == null) {
            invocation.source().sendMessage(Component.text("Player not found", NamedTextColor.RED));
            return;
        }

        Reply.doMessage(invocation, toMessage, String.join(" ", invocation.arguments()).replaceFirst(invocation.arguments()[0], ""));

        if (invocation.source() instanceof Player) {
            lastMessaged.put((Player) invocation.source(), toMessage);
            lastMessaged.put(toMessage, (Player) invocation.source());
        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        ArrayList<String> players = new ArrayList<>();
        for (Player player : DuckSMPUtils.getInstance().getProxy().getAllPlayers()) {
            players.add(player.getUsername());
        }
        return players;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("duck.message");
    }
}
