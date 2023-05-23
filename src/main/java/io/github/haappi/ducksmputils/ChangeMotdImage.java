package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.RawCommand;

public class ChangeMotdImage implements RawCommand {

    @Override
    public void execute(final Invocation invocation) {
        String url;
        if (invocation.arguments().length() == 0) {
            url = "http://quack.boo/server_motd.png";
        } else {
            url = invocation.arguments();
        }

        MOTD.setImage(url);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("duck.motd");
    }
}