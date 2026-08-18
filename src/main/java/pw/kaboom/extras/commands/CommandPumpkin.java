package pw.kaboom.extras.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.player;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.players;

public final class CommandPumpkin implements BrigadierCommand {

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .requires(src -> src.getSender().hasPermission("extras.pumpkin"))
                .then(argument("players", players())
                        .executes(ctx -> {
                            PlayerSelectorArgumentResolver selector = ctx.getArgument(
                                    "players",
                                    PlayerSelectorArgumentResolver.class
                            );
                            List<Player> targets = selector.resolve(ctx.getSource());
                            for (Player target : targets) {
                                placePumpkin(target);
                                if (targets.size() == 1) {
                                    ctx.getSource().getSender().sendMessage(
                                            Component.text("\"")
                                                    .append(Component.text(target.getName()))
                                                    .append(Component.text("\" is now a pumpkin"))
                                    );
                                } else {
                                    ctx.getSource().getSender().sendMessage(
                                            Component.text(targets.size()
                                                    + " players are now pumpkins")
                                    );
                                }
                            }
                            return 1;
                        })
                );
    }

    private void placePumpkin(final Player player) {
        player.getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
    }
}
