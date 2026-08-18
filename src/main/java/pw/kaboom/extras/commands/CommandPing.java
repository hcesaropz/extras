package pw.kaboom.extras.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.player;

public final class CommandPing implements BrigadierCommand {
	private static final SimpleCommandExceptionType EX_NOT_PLAYER =
			new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
					Component.text("This command must be called by a player")));

	@Override
	public String getLabel() {
		return "ping";
	}

	@Override
	public String getDescription() {
		return "Gets your ping";
	}

	@Override
	public List<String> getAliases() {
		return List.of("delay", "ms");
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder
				.requires(src -> src.getSender().hasPermission("extras.ping"))
				.executes(ctx -> {
					if (!(ctx.getSource() instanceof Player player)) {
						throw EX_NOT_PLAYER.create();
					}
					int ping = player.getPing();
					ctx.getSource().getSender().sendMessage(
							Component.text(player.getName())
									.append(Component.text("Your ping is "))
									.append(Component.text(ping, getColor(ping)))
									.append(Component.text(" ms."))
					);
					return 1;
				})
				.then(argument("player", player())
						.executes(ctx -> {
							PlayerSelectorArgumentResolver resolver = ctx.getArgument(
									"player",
									PlayerSelectorArgumentResolver.class
							);
							Player player = resolver.resolve(ctx.getSource()).getFirst();
							int ping = player.getPing();
							ctx.getSource().getSender().sendMessage(
									Component.text(player.getName())
											.append(Component.text("'s ping is "))
											.append(Component.text(ping + "ms.",
													getColor(ping)))
							);
							return 1;
						})
				);
	}

	private TextColor getColor(int ping) {
		int d = ping / 100;
		return switch (d) {
			case 0 -> NamedTextColor.GREEN;
			case 1, 2, 3, 4 -> NamedTextColor.YELLOW;
			case 5 -> NamedTextColor.RED;
			default -> NamedTextColor.DARK_RED;
		};
	}
}
