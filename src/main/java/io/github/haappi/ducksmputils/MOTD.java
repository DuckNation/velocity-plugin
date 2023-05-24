package io.github.haappi.ducksmputils;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MOTD {
    private static BufferedImage image;

    static {
        setImage("http://quack.boo/server_motd.png");
    }

    static void setImage(String url) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            image = ImageIO.read(connection.getInputStream());
            connection.disconnect();
        } catch (IOException ignored) {

        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerPing(ProxyPingEvent event) {
        try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
            jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
            String encryptedIP = Encryption.encrypt(event.getConnection().getRemoteAddress().getAddress().getHostAddress());
            String exists = jedis.get("motd:" + encryptedIP);

            ServerPing builder = event.getPing();

            if (exists != null) {
                builder = event.getPing().asBuilder().description(generateMotd(exists)).build();
            }

//            builder.asBuilder().favicon(Favicon.create(image));

            event.setPing(builder);
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
        return (Component.text("Welcome back to ", NamedTextColor.GREEN).append(Component.text("Duck", NamedTextColor.YELLOW).append(Component.text("SMP", NamedTextColor.GOLD)).append(Component.text(" " + username, NamedTextColor.AQUA))));
    }

}
