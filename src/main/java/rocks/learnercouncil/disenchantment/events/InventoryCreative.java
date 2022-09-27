package rocks.learnercouncil.disenchantment.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import rocks.learnercouncil.disenchantment.ItemManager;

public class InventoryCreative implements Listener {

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent e) {
        if(e.getWhoClicked().hasPermission("disenchantment.bypass")) return;
        if(e.getCurrentItem() == null) return;
        ItemStack i = e.getCursor();
        if(e.getWhoClicked() instanceof Player) {
            e.setCursor(ItemManager.modify(i, (Player) e.getWhoClicked(), false));
        }
    }
}
