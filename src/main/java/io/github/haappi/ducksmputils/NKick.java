package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NKick implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        // get all players names connected to proxy
        return CompletableFuture.completedFuture(DuckSMPUtils.getInstance().getProxy().getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toList()));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("duck.transfer");
    }
}
