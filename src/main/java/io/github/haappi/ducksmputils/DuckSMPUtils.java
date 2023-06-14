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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Plugin(
        id = "duck-smp-utils",
        name = "DuckSMPUtils",
        version = BuildConstants.VERSION,
        authors = {"haappi"}
)
public class DuckSMPUtils {
    public static final MiniMessage miniMessage = MiniMessage.miniMessage();
    public static final CloseableHttpClient httpClient = HttpClients.createDefault();
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
                .build(), new MoveAll(proxy));

        commandManager.register(commandManager.metaBuilder("motd-image")
                .plugin(this)
                .build(), new ChangeMotdImage());

        BrigadierCommand networkBan = NetworkBan.createBrigadierCommand(proxy);


        CommandMeta nbanmeta = commandManager.metaBuilder("nban")
                .aliases("networkban")
                .plugin(this)
                .build();

        commandManager.register(nbanmeta, networkBan);

        commandManager.register("message", new Message(), "msg", "tell", "whisper", "w", "m");
        commandManager.register("reply", new Reply(), "r");

        BrigadierCommand networkUnban = NetworkUnban.createBrigadierCommand(proxy);


        CommandMeta nunbanmeta = commandManager.metaBuilder("nunban")
                .aliases("networkunban")
                .plugin(this)
                .build();

        commandManager.register(nunbanmeta, networkUnban);

        CommandMeta unverify = commandManager.metaBuilder("unverify")
                .plugin(this)
                .build();

        commandManager.register(unverify, new Unverify());

        CommandMeta verify = commandManager.metaBuilder("verify")
                .plugin(this)
                .build();

        commandManager.register(verify, new Verify());


        CommandMeta staffChat = commandManager.metaBuilder("staff-chat")
                .aliases("sc")
                .plugin(this)
                .build();

        proxy.getEventManager().register(this, new DiscordChat());
        proxy.getEventManager().register(this, new NetworkBan());
        proxy.getEventManager().register(this, new MOTD());

        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    private void cleanup() {
        if (pool != null) {
            pool.close();
        }
    }

    private void checkConfig() {
        Path path = Path.of("plugins/DuckSMP/config.yml");
        try {
            Files.createDirectories(Path.of("plugins/DuckSMP/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        String host = node.getNode("redis", "host").getString();
        String password = node.getNode("redis", "password").getString();
        String encryptString = node.getNode("motd", "encryption").getString(UUID.randomUUID().toString());
        Config.API_KEY = node.getNode("api", "api-key").getString();
        System.out.println(port + " " + host + " " + password);

        if (port == Integer.MIN_VALUE || host == null || password == null) {
            try {
                node.getNode("redis", "port").setValue(Integer.MIN_VALUE);
                node.getNode("redis", "host").setValue(null);
                node.getNode("redis", "password").setValue(null);
                node.getNode("motd", "encryption").setValue(UUID.randomUUID().toString());
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
