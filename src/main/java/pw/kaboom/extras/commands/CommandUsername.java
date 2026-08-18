package pw.kaboom.extras.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pw.kaboom.extras.util.Utility;

import java.util.HashMap;
import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static io.papermc.paper.command.brigadier.Commands.argument;

public final class CommandUsername implements BrigadierCommand {
    private static final SimpleCommandExceptionType EX_NOT_PLAYER =
            new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    Component.text("This command must be called by a player")));
    private static final SimpleCommandExceptionType EX_RATELIMIT =
            new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    Component.text("Please wait a few seconds before changing your username.")));
    private static final SimpleCommandExceptionType EX_TAKEN =
            new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    Component.text("Please wait a few seconds before changing your username.")));
    private final Map<Player, Long> lastUsedMillis = new HashMap<>();

    @Override
    public String getLabel() {
        return "username";
    }

    @Override
    public String getDescription() {
        return "Changes your username on the server";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .requires(src ->
                        src.getSender().hasPermission("extras.username")
                                && src.getSender() instanceof Player
                )
                // TODO: perhaps "off" lit here to match /skin
                .then(argument("username", greedyString())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                throw EX_NOT_PLAYER.create();
                            }

                            final String nameColor =
                                    Utility.translateLegacyColors(
                                            StringArgumentType.getString(
                                                    ctx,
                                                    "username"
                                            )
                                    );
                            final String name = nameColor.substring(0, Math.min(16,
                                    nameColor.length()));
                            final long millis = lastUsedMillis.getOrDefault(player, 0L);
                            final long millisDifference = System.currentTimeMillis() - millis;

                            if (name.equals(player.getName())) {
                                player.sendMessage(Component
                                        .text("You already have the username \"" + name + "\""));
                                return 0;
                            }

                            if (millisDifference <= 2000) {
                                throw EX_RATELIMIT.create();
                            }

                            for (Player other : Bukkit.getOnlinePlayers()) {
                                if (!other.getName().equalsIgnoreCase(name)) continue;
                                throw EX_TAKEN.create();
                            }

                            // Preserve UUIDs, as changing them breaks clients
                            final PlayerProfile newProfile =
                                    Bukkit.createProfileExact(player.getUniqueId(), name);

                            player.setPlayerProfile(newProfile);
                            lastUsedMillis.put(player, System.currentTimeMillis());

                            player.sendMessage(
                                    Component.text("Successfully set your username to \"")
                                            .append(Component.text(name))
                                            .append(Component.text("\""))
                            );
                            return 1;
                        }));
    }
}
