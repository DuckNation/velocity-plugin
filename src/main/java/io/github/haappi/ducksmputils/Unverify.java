package io.github.haappi.ducksmputils;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.github.haappi.ducksmputils.DuckSMPUtils.httpClient;

public class Unverify implements RawCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("You must be a player to use this command.", NamedTextColor.RED));
            return;
        }
        Player player = (Player) invocation.source();
        CustomDelete post = new CustomDelete("https://quack.boo/internal/api/verification/unverify");
        post.setHeader("Authorization", "Bearer " + Config.API_KEY);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("uid", player.getUniqueId().toString()));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
        try {
            JSONObject myObject = new JSONObject(httpClient.execute(post));
            Component deserialized = DuckSMPUtils.miniMessage.deserialize((String) myObject.get("message"));

            player.sendMessage(deserialized);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(Component.text("Something went wrong. Please contact an administrator.", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("duck.verify");
    }
}
