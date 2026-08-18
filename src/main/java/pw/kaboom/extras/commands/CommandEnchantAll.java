package pw.kaboom.extras.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CommandEnchantAll implements BrigadierCommand {
    @Override
    public String getLabel() {
        return "enchantall";
    }

    @Override
    public String getDescription() {
        return "Adds every enchantment to a held item";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .requires(src ->
                        src.getSender() instanceof Player
                                && src.getSender().hasPermission("extras.enchantall")
                )
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) {
                        // should be impossible, see above
                        throw new IllegalStateException("Command has to be run by a player");
                    }

                    final ItemStack item = player.getInventory().getItemInMainHand();

                    if (Material.AIR.equals(item.getType())) {
                        // TODO: perhaps should be red to signify error? idk
                        player.sendMessage(Component
                                .text("Please hold an item in your hand to enchant it"));
                        return 0;
                    }

                    final Registry<Enchantment> registry = RegistryAccess.registryAccess()
                            .getRegistry(RegistryKey.ENCHANTMENT);
                    for (Enchantment enchantment : registry) {
                        item.addUnsafeEnchantment(enchantment, Short.MAX_VALUE);
                    }
                    player.sendMessage(Component
                            .text("I killed Martin."));
                    return 1;
                });
    }
}
