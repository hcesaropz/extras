package pw.kaboom.extras.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pw.kaboom.extras.modules.player.skin.SkinManager;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class CommandSkin implements BrigadierCommand {
    private static final SimpleCommandExceptionType EX_NOT_PLAYER =
            new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    Component.text("This command must be called by a player")));

    @Override
    public String getLabel() {
        return "skin";
    }

    @Override
    public String getDescription() {
        return "Changes your skin";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.requires(src -> src.getSender().hasPermission("extras.skin"));

        for (String alias : List.of("off", "remove", "disable")) {
            builder.then(literal(alias).executes(ctx -> {
                SkinManager.removeSkin(player(ctx), true);
                return 1;
            }));
        }
        for (String alias : List.of("auto", "default", "reset")) {
            builder.then(literal(alias).executes(ctx -> {
                Player player = player(ctx);
                SkinManager.requestSkin(player, player.getName(), true);
                return 1;
            }));
        }

        builder.then(argument("username", greedyString())
                .suggests((ctx, sb) -> {
                    Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .forEach(sb::suggest);
                    return sb.buildFuture();
                })
                .executes(ctx -> {
                    SkinManager.requestSkin(
                            player(ctx), ctx.getArgument("username", String.class), true);
                    return 1;
                }));
    }

    private static Player player(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        if (ctx.getSource().getExecutor() instanceof Player player) {
            return player;
        }
        throw EX_NOT_PLAYER.create();
    }
}
