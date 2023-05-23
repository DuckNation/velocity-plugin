package io.github.haappi.ducksmputils;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.command.SimpleCommand;
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
        proxy.getEventManager().register(this, new DiscordChat());
    }

    private void checkConfig() {
        Path path = Paths.get("config.yml");
        loader = YAMLConfigurationLoader.builder()
                .setDefaultOptions(configurationOptions -> configurationOptions.withShouldCopyDefaults(true))
                .setPath(path)
                .build();

        try {
            node = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int port = node.getNode("redis", "port").getInt(-1);
        String host = node.getNode("redis", "host").getString(null);
        String password = node.getNode("redis", "password").getString(null);
        System.out.println(port + " " + host + " " + password);

        if (port == -1 || host == null || password == null) {
        try {
            loader.save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        }

        pool = new JedisPool(host, port);
        jedisPassword = password;


    }

    public String getJedisPassword() {
        return jedisPassword;
    }

    public Jedis getJedis() {
        return pool.getResource();
    }
}
