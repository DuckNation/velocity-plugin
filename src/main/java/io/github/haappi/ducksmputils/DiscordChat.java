package io.github.haappi.ducksmputils;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.haappi.ducksmputils.Enums.REQUEST_ONLINE;

public class DiscordChat {
    private final ProxyServer server;
    JedisPubSub jedisPubSub = new JedisPubSub() {

        @Override
        public void onMessage(String channel, String message) {
            String _channel = message.split(";")[0];
            String _message = message.split(";")[1];

            if (Objects.equals(_message, REQUEST_ONLINE.toString())) {
                String onlinePlayerNames = server.getAllPlayers().stream().map(Player::getUsername).collect(Collectors.joining(", "));
                write(onlinePlayerNames, REQUEST_ONLINE);
                return;
            }

            Component deserialized = DuckSMPUtils.miniMessage.deserialize(_message);
            if (_channel.equals("operator_chat")) {
                server.getAllPlayers().stream().filter(player -> player.hasPermission("duck.operator_chat")).forEach(player -> player.sendMessage(deserialized));
                return;
            }
            server.getAllPlayers().forEach(player -> player.sendMessage(deserialized));
        }
    };

    public DiscordChat() {
        this.server = DuckSMPUtils.getInstance().getProxy();

        server.getScheduler()
                .buildTask(DuckSMPUtils.getInstance(), () -> {
                    try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
                        jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
                        jedis.subscribe(jedisPubSub, "toMinecraft");
                    }
                }).schedule();
        server.getScheduler()
                .buildTask(DuckSMPUtils.getInstance(), this::writePlayerToJedis)
                .repeat(10, TimeUnit.MINUTES)
                .schedule();
    }

    private void writePlayerToJedis() {
        String onlinePlayerNames = server.getAllPlayers().stream().map(Player::getUsername).collect(Collectors.joining(", "));
        write(String.format("**%s/%s** players online. Join at **quack.boo**\n\nPlayers: %s", server.getPlayerCount(), server.getConfiguration().getShowMaxPlayers(), onlinePlayerNames), Enums.STATUS_UPDATE);
    }

    private void write(String message, Enums type) {
        try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
            jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
            jedis.publish("toDiscord", type + ";" + message);
        }
    }

    private String getServerName(RegisteredServer server) {
        return server == null ? "unknown" : server.getServerInfo().getName();
    }

    private String getServerName(ServerConnection server) {
        return server == null ? "unknown" : server.getServerInfo().getName();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.getResult() == PlayerChatEvent.ChatResult.denied()) {
            return;
        }
        String serverName = getServerName(event.getPlayer().getCurrentServer().orElse(null)).toUpperCase();
        write(String.format("[%s] **%s**: %s", serverName, event.getPlayer().getUsername(), event.getMessage()), Enums.CHAT);

        NamedTextColor serverColor = NamedTextColor.WHITE;

        for (Player player : server.getAllPlayers()) {
            if (!getServerName(player.getCurrentServer().orElse(null)).toUpperCase().equals(serverName)) {
                player.sendMessage(Component.text(String.format("[%s] ", serverName), serverColor).append(Component.text(event.getPlayer().getUsername(), NamedTextColor.WHITE)).append(Component.text(": " + event.getMessage())));
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerJoin(ServerConnectedEvent event) {
        String serverName = getServerName(event.getServer());

        write(String.format("**%s** connected to **%s**", event.getPlayer().getUsername(), serverName), Enums.JOIN);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerLeave(DisconnectEvent event) {
        write(String.format("**%s** disconnected", event.getPlayer().getUsername()), Enums.LEAVE);
    }
}
