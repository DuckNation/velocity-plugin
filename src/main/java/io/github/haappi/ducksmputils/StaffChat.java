package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StaffChat implements RawCommand {

    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player) {
            Player player = (Player) invocation.source();
            if (DiscordChat.staffChatEnabled.contains(player.getUniqueId())) {
                DiscordChat.staffChatEnabled.remove(player.getUniqueId());
                player.sendMessage(Component.text("Staff chat disabled", NamedTextColor.RED));
            } else {
                DiscordChat.staffChatEnabled.add(player.getUniqueId());
                player.sendMessage(Component.text("Staff chat enabled", NamedTextColor.GREEN));
            }

        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("duck." + Enums.STAFF_CHAT);
    }
}
