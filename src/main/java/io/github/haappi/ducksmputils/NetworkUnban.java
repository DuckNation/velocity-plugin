package io.github.haappi.ducksmputils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redis.clients.jedis.Jedis;

public class NetworkUnban {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer server) {
        LiteralCommandNode<CommandSource> helloNode = LiteralArgumentBuilder
                .<CommandSource>literal("nunban")
                .requires(source -> source.hasPermission("duck.nban"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> {
                            server.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUniqueId().toString(),
                                    VelocityBrigadierMessage.tooltip(
                                            MiniMessage.miniMessage().deserialize("<rainbow>" + player.getUsername())
                                    )
                            ));
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String player = context.getArgument("player", String.class);


                            try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
                                jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
                                jedis.del("banned:" + player);
                            }

                            return Command.SINGLE_SUCCESS;
                        })).build();
        return new BrigadierCommand(helloNode);
    }
}
