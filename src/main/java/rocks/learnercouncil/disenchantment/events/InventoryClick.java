package rocks.learnercouncil.disenchantment.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import rocks.learnercouncil.disenchantment.ItemManager;


public class InventoryClick implements Listener {


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getClick() == ClickType.CREATIVE) return;
        if(e.getWhoClicked().hasPermission("disenchantment.bypass")) return;
        if(e.getCurrentItem() == null) return;
        ItemStack i = e.getCurrentItem();
        if(e.getWhoClicked() instanceof Player) {
            e.setCurrentItem(ItemManager.modify(i, (Player) e.getWhoClicked(), false));
        }
    }

}
