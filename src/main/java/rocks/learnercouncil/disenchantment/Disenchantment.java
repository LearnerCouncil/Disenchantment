package rocks.learnercouncil.disenchantment;

import org.bukkit.plugin.java.JavaPlugin;
import rocks.learnercouncil.disenchantment.events.InventoryCreative;
import rocks.learnercouncil.disenchantment.events.InventoryClick;

public final class Disenchantment extends JavaPlugin {

    public static Disenchantment instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        getServer().getPluginManager().registerEvents(new InventoryClick(), this);
        getServer().getPluginManager().registerEvents(new InventoryCreative(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Disenchantment getInstance() {
        return instance;
    }
}
