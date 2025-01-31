package xyz.jpenilla.wanderingtrades.command.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.jpenilla.pluginbase.legacy.itembuilder.ItemBuilder;
import xyz.jpenilla.wanderingtrades.WanderingTrades;
import xyz.jpenilla.wanderingtrades.command.BaseCommand;
import xyz.jpenilla.wanderingtrades.command.Commands;
import xyz.jpenilla.wanderingtrades.command.argument.TradeConfigArgument;
import xyz.jpenilla.wanderingtrades.config.Messages;
import xyz.jpenilla.wanderingtrades.config.TradeConfig;
import xyz.jpenilla.wanderingtrades.gui.ListTradeConfigsInterface;
import xyz.jpenilla.wanderingtrades.gui.ListTradesInterface;
import xyz.jpenilla.wanderingtrades.gui.MainConfigInterface;
import xyz.jpenilla.wanderingtrades.gui.PlayerHeadConfigInterface;
import xyz.jpenilla.wanderingtrades.util.Constants;

import static net.kyori.adventure.text.Component.text;

public final class ConfigCommands extends BaseCommand {
    public ConfigCommands(final WanderingTrades plugin, final Commands commands) {
        super(plugin, commands);
    }

    @Override
    public void register() {
        final Command.Builder<CommandSender> wt = this.commandManager.commandBuilder("wt");

        /* List Trade Configs Command */
        final Command<CommandSender> list = wt
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Messages.COMMAND_LIST_DESCRIPTION.asComponent())
            .literal("list")
            .permission("wanderingtrades.list")
            .handler(this::executeList)
            .build();

        /* Trade Config Edit Command */
        final Command<CommandSender> edit = wt
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Messages.COMMAND_EDIT_DESCRIPTION.asComponent())
            .literal("edit")
            .argument(TradeConfigArgument.optional("trade_config"))
            .permission("wanderingtrades.edit")
            .senderType(Player.class)
            .handler(context -> {
                final TradeConfig config = context.<TradeConfig>getOptional("trade_config").orElse(null);
                if (config == null) {
                    new ListTradeConfigsInterface(this.plugin).open((Player) context.getSender());
                } else {
                    new ListTradesInterface(this.plugin, config).open((Player) context.getSender());
                }
            })
            .build();

        /* Plugin Config Edit Command */
        final Command<CommandSender> editConfig = wt
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Messages.COMMAND_EDITCONFIG_DESCRIPTION.asComponent())
            .literal("editconfig")
            .permission("wanderingtrades.edit")
            .senderType(Player.class)
            .handler(context -> new MainConfigInterface(this.plugin).open((Player) context.getSender()))
            .build();

        /* Player Head Config Edit Command */
        final Command<CommandSender> editPlayerHeadConfig = wt
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Messages.COMMAND_EDITPLAYERHEADS_DESCRIPTION.asComponent())
            .literal("editplayerheads")
            .permission("wanderingtrades.edit")
            .senderType(Player.class)
            .handler(context -> new PlayerHeadConfigInterface(this.plugin).open((Player) context.getSender()))
            .build();

        // Needed for 1.19+ as run_command click events can no longer be used to send chat messages
        this.commandManager.command(wt
            .literal("accept-input")
            .argument(StringArgument.greedy("input"))
            .permission("wanderingtrades.edit")
            .senderType(Player.class)
            .handler(context -> {
                final Player player = (Player) context.getSender();
                if (!player.isConversing()) {
                    this.plugin.audiences().player(player).sendMessage(text("Error. This command is meant for use by click events.", NamedTextColor.RED));
                    return;
                }
                player.acceptConversationInput(context.get("input"));
            }));

        /* Held ItemStack Rename Command */
        final Command<CommandSender> nameHeldItem = this.commandManager.commandBuilder("namehelditem")
            .meta(CommandMeta.DESCRIPTION, "Sets the display name of the held ItemStack.")
            .argument(StringArgument.of("name", StringArgument.StringMode.GREEDY),
                ArgumentDescription.of("The MiniMessage string to use as a name."))
            .permission("wanderingtrades.namehand")
            .senderType(Player.class)
            .handler(context -> {
                final Player player = (Player) context.getSender();
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    player.getInventory().setItemInMainHand(
                        ItemBuilder.create(player.getInventory().getItemInMainHand())
                            .miniMessageContext()
                            .customName(context.get("name"))
                            .exitAndBuild()
                    );
                }
            })
            .build();

        this.commands.register(List.of(list, edit, editConfig, editPlayerHeadConfig, nameHeldItem));
    }

    private void executeList(final CommandContext<CommandSender> context) {
        this.chat.send(
            context.getSender(),
            Component.textOfChildren(Constants.PREFIX_COMPONENT, Messages.COMMAND_LIST_LOADED)
        );
        final List<String> toSort = new ArrayList<>(this.plugin.configManager().tradeConfigs().keySet());
        toSort.sort(null);
        int index = 1;
        for (final String cfg : toSort) {
            this.chat.send(context.getSender(), String.format(" <gray>%s.</gray> <hover:show_text:'<green>Click to edit'><click:run_command:/wanderingtrades edit %s>%s", index++, cfg, cfg));
        }
    }
}
