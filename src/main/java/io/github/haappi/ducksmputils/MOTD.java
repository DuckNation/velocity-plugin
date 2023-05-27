package io.github.haappi.ducksmputils;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MOTD {
    private static BufferedImage image;

    static {
        setImage("https://quack.boo/server_motd.png");
    }

    static void setImage(String link) {
        try {
            URL url = new URL(link);
            InputStream inputStream = url.openStream();
            BufferedImage img = ImageIO.read(inputStream);

            BufferedImage scaledImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(img, 0, 0, 64, 64, null);
            g2d.dispose();

            MOTD.image = scaledImage;
        } catch (IOException exception) {
            System.out.println("Failed to set MOTD image to " + link);
            exception.printStackTrace();
        }
        System.out.println("Set MOTD image to " + link);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerPing(ProxyPingEvent event) {
        try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
            jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
            String encryptedIP = Encryption.encrypt(event.getConnection().getRemoteAddress().getAddress().getHostAddress());
            String exists = jedis.get("motd:" + encryptedIP);

            ServerPing.Builder builder = event.getPing().asBuilder();

            if (exists != null) {
                builder.description(generateMotd(exists));
            }

            Favicon favicon = Favicon.create(image);
            builder.favicon(favicon);

            event.setPing(builder.build());
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
