package io.github.haappi.ducksmputils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redis.clients.jedis.Jedis;

public class NetworkBan {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer server) {
        LiteralCommandNode<CommandSource> helloNode = LiteralArgumentBuilder
                .<CommandSource>literal("nban")
                .requires(source -> source.hasPermission("duck.nban"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            server.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUsername(),
                                    VelocityBrigadierMessage.tooltip(
                                            MiniMessage.miniMessage().deserialize("<rainbow>" + player.getUsername())
                                    )
                            ));
                            return builder.buildFuture();
                        }).then(RequiredArgumentBuilder.<CommandSource, String>argument("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String player = context.getArgument("player", String.class);
                                    String reason = context.getArgument("reason", String.class);

                                    server.getPlayer(player).ifPresent(p -> {
                                        p.disconnect(Component.text("You have been banned. Reason: ", NamedTextColor.RED).append(Component.text(reason, NamedTextColor.YELLOW)));

                                        try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
                                            jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
                                            jedis.set("banned:" + p.getUniqueId().toString(), reason);
                                        }
                                    });

                                    return Command.SINGLE_SUCCESS;
                                }))).build();
        return new BrigadierCommand(helloNode);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerJoin(LoginEvent event) {
        try (Jedis jedis = DuckSMPUtils.getInstance().getJedis()) {
            jedis.auth(DuckSMPUtils.getInstance().getJedisPassword());
            String reason = jedis.get("banned:" + event.getPlayer().getUniqueId().toString());
            if (reason != null) {
                event.getPlayer().disconnect(Component.text("You have been banned. Reason: ", NamedTextColor.RED).append(Component.text(reason, NamedTextColor.YELLOW)));
            }
        }
    }

}


