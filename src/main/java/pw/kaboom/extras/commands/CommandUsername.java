package pw.kaboom.extras.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pw.kaboom.extras.util.Utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class CommandUsername implements BrigadierCommand {
    private static final SimpleCommandExceptionType EX_NOT_PLAYER =
            new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    Component.text("This command must be called by a player")));
    private static final SimpleCommandExceptionType EX_RATELIMIT =
            new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    Component.text("Please wait a few seconds before changing your username.")));
    private static final SimpleCommandExceptionType EX_TAKEN =
            new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    Component.text("A player with that username is already logged in.")));
    private static final long RATELIMIT_MILLIS = 2000;
    private final Map<Player, Long> lastUsedMillis = new HashMap<>();
    private final Map<Player, String> originalNames = new HashMap<>();

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
        builder.requires(src ->
                src.getSender().hasPermission("extras.username")
                        && src.getSender() instanceof Player
        );

        for (String alias : List.of("auto", "default", "reset")) {
            builder.then(literal(alias).executes(ctx -> {
                Player player = player(ctx);
                String original = originalNames.get(player);

                if (original == null || original.equals(player.getName())) {
                    player.sendMessage(
                            Component.text("You already have your default username"));
                    return 0;
                }

                setUsername(player, original);
                originalNames.remove(player);
                return 1;
            }));
        }

        builder.then(argument("username", greedyString())
                .executes(ctx -> {
                    Player player = player(ctx);

                    String nameColor =
                            Utility.translateLegacyColors(
                                    StringArgumentType.getString(
                                            ctx,
                                            "username"
                                    )
                            );
                    String name = nameColor.substring(0, Math.min(16,
                            nameColor.length()));

                    if (name.equals(player.getName())) {
                        player.sendMessage(Component
                                .text("You already have the username \"" + name + "\""));
                        return 0;
                    }

                    originalNames.putIfAbsent(player, player.getName());
                    setUsername(player, name);
                    return 1;
                }));
    }

    private void setUsername(Player player, String name)
            throws CommandSyntaxException {
        long millis = lastUsedMillis.getOrDefault(player, 0L);
        long millisDifference = System.currentTimeMillis() - millis;

        if (millisDifference <= RATELIMIT_MILLIS) {
            throw EX_RATELIMIT.create();
        }

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player) || !other.getName().equalsIgnoreCase(name)) {
                continue;
            }
            throw EX_TAKEN.create();
        }

        // Preserve UUIDs, as changing them breaks clients
        PlayerProfile newProfile = Bukkit.createProfileExact(player.getUniqueId(), name);

        player.setPlayerProfile(newProfile);
        lastUsedMillis.put(player, System.currentTimeMillis());

        player.sendMessage(
                Component.text("Successfully set your username to \"")
                        .append(Component.text(name))
                        .append(Component.text("\""))
        );
    }

    private static Player player(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        if (ctx.getSource().getSender() instanceof Player player) {
            return player;
        }
        throw EX_NOT_PLAYER.create();
    }
}
