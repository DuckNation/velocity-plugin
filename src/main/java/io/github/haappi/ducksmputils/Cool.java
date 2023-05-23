package io.github.haappi.ducksmputils;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.Jedis;

public class Cool {

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerPing(ProxyPingEvent event) {
        try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
            jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
            String encryptedIP = Encryption.encrypt(event.getConnection().getRemoteAddress().getAddress().getHostAddress());
            String exists = jedis.get("motd:" + encryptedIP);

            if (exists != null) {
                ServerPing builder = event.getPing().asBuilder().description(generateMotd(exists)).build();
                event.setPing(builder);
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerConnect(ServerPreConnectEvent event) {
        try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
            jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
            String encryptedIP = Encryption.encrypt(event.getPlayer().getRemoteAddress().getAddress().getHostAddress());
            jedis.set("motd:" + encryptedIP, event.getPlayer().getUsername());
        }
    }

    private Component generateMotd(String username) {
        return (Component.text("Welcome back to Duck", NamedTextColor.YELLOW).append(Component.text("SMP", NamedTextColor.GOLD)).append(Component.text(" " + username, NamedTextColor.AQUA)).append(Component.text("!")));
    }

}
