package pw.kaboom.extras.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static io.papermc.paper.command.brigadier.Commands.argument;

public final class CommandBroadcastRainbow implements BrigadierCommand {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public String getLabel() {
        return "broadcastrainbow";
    }

    @Override
    public String getDescription() {
        return "Broadcasts a rainbow message";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .requires(src ->
                        src.getSender().hasPermission("extras.broadcastrainbow")
                )
                .then(argument("message", greedyString())
                        .executes(ctx -> {
                            final String mm = StringArgumentType.getString(ctx, "message");
                            final String strippedTags = MINI_MESSAGE.stripTags(mm);
                            final Component component = MINI_MESSAGE.deserialize("<rainbow>"
                                    + strippedTags);
                            Bukkit.broadcast(component);
                            return 1;
                        })
                );
    }
}