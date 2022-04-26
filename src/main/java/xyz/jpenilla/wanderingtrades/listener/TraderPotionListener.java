package xyz.jpenilla.wanderingtrades.listener;

import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.persistence.PersistentDataType;
import xyz.jpenilla.wanderingtrades.WanderingTrades;
import xyz.jpenilla.wanderingtrades.util.Constants;

public final class TraderPotionListener implements Listener {
    private final WanderingTrades plugin;

    public TraderPotionListener(final WanderingTrades plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionEffect(final EntityPotionEffectEvent e) {
        if (!(e.getEntity() instanceof WanderingTrader wanderingTrader)) {
            return;
        }
        if (e.getCause() == EntityPotionEffectEvent.Cause.POTION_DRINK && e.getAction() == EntityPotionEffectEvent.Action.ADDED) {
            final boolean stopDrink =
                wanderingTrader.getPersistentDataContainer().has(Constants.PREVENT_INVISIBILITY, PersistentDataType.STRING) // summonnatural with --noinvisibilty flag
                    || (wanderingTrader.getPersistentDataContainer().has(Constants.CONFIG_NAME, PersistentDataType.STRING) && this.plugin.config().preventNightInvisibility()); // config traders
            if (stopDrink) {
                wanderingTrader.setCanDrinkPotion(false);
                wanderingTrader.clearActiveItem();
                e.setCancelled(true);
            }
        }
    }
}
