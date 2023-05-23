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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class NetworkUnban {
        public static BrigadierCommand createBrigadierCommand(final ProxyServer server) {
        LiteralCommandNode<CommandSource> helloNode = LiteralArgumentBuilder
                .<CommandSource>literal("nunban")
                .requires(source -> source.hasPermission("duck.nban"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> {
                            // Here we provide the names of the players along with a tooltip,
                            // which can be used as an explanation of a specific argument or as a simple decoration
                            server.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUniqueId().toString(),
                                    // A VelocityBrigadierMessage takes a component.
                                    // In this case, the player's name is provided with a rainbow
                                    // gradient created by MiniMessage (Library available since Velocity 3.1.2+)
                                    VelocityBrigadierMessage.tooltip(
                                            MiniMessage.miniMessage().deserialize("<rainbow>" + player.getUsername())
                                    )
                            ));
                            return builder.buildFuture();
                        }).then(RequiredArgumentBuilder.<CommandSource, String>argument("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String player = context.getArgument("argument", String.class);


                                    try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
                                        jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
                                        jedis.del("banned:" + player);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))).build();
        return new BrigadierCommand(helloNode);
    }
}
