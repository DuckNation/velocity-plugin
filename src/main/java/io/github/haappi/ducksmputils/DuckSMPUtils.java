package io.github.haappi.ducksmputils;

import com.google.inject.Inject;
import com.velocitypowered.api.command.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Plugin(
        id = "duck-smp-utils",
        name = "DuckSMPUtils",
        version = BuildConstants.VERSION,
        authors = {"haappi"}
)
public class DuckSMPUtils {
    public static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static DuckSMPUtils instance;
    private final ProxyServer proxy;
    // https://github.com/Matt-MX/ReconnectVelocity/blob/master/src/main/java/com/mattmx/reconnect/ReconnectVelocity.java#L26
    @Inject
    private Logger logger;
    private ConfigurationNode node;
    private YAMLConfigurationLoader loader;
    private JedisPool pool;
    private String jedisPassword;
    private String encryptionKey;


    @Inject
    public DuckSMPUtils(ProxyServer proxy) {
        this.proxy = proxy;
        instance = this;
    }

    public static DuckSMPUtils getInstance() {
        return instance;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        checkConfig();

        SimpleCommand transfer = new Transfer(proxy);
        RawCommand broadcast = new Broadcast(proxy);

        CommandManager commandManager = proxy.getCommandManager();
        commandManager.register(commandManager.metaBuilder("transfer")
                .plugin(this)
                .build(), transfer);
        commandManager.register(commandManager.metaBuilder("broadcast")
                .plugin(this)
                .build(), broadcast);
        commandManager.register(commandManager.metaBuilder("moveall")
                .plugin(this)
                .build(), new Moveall(proxy));
        BrigadierCommand networkBan = NetworkBan.createBrigadierCommand(proxy);

        // Finally, you can register the command
        commandManager.register((CommandMeta) commandManager.metaBuilder("nban")
                // This will create a new alias for the command "/test"
                // with the same arguments and functionality
                .aliases("networkban")
                .plugin(this), networkBan);


        BrigadierCommand networkUnban = NetworkUnban.createBrigadierCommand(proxy);

        commandManager.register((CommandMeta) commandManager.metaBuilder("nunban")
                // This will create a new alias for the command "/test"
                // with the same arguments and functionality
                .aliases("networkunban")
                .plugin(this), networkUnban);

        proxy.getEventManager().register(this, new DiscordChat());
        proxy.getEventManager().register(this, new Cool());



    }

    private void checkConfig() {
        Path path = Path.of("config.yml");
        loader = YAMLConfigurationLoader.builder()
                .setDefaultOptions(configurationOptions -> configurationOptions.withShouldCopyDefaults(true))
                .setPath(path)
                .build();

        try {
            node = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int port = node.getNode("redis", "port").getInt(Integer.MIN_VALUE);
        String host = node.getNode("redis", "host").getString(null);
        String password = node.getNode("redis", "password").getString(null);
        String encryptString = node.getNode("motd", "encryption").getString(null);
        System.out.println(port + " " + host + " " + password);

        if (port == Integer.MIN_VALUE || host == null || password == null || encryptString == null) {
            try {
                loader.save(node);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
                    pool = new JedisPool(host, port);
        jedisPassword = password;
        encryptionKey = encryptString;
        }



    }

    public String getJedisPassword() {
        return jedisPassword;
    }

    public Jedis getJedis() {
        return pool.getResource();
    }
}
