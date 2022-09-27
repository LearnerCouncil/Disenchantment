package rocks.learnercouncil.disenchantment;

import com.google.common.collect.HashMultimap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.bukkit.Material.*;

public class ItemManager {

    private static final Disenchantment plugin = Disenchantment.getInstance();

    public static ItemStack modify(ItemStack i, Player p, boolean shulker) {
        if(i.getType() == POTION || i.getType() == SPLASH_POTION || i.getType() == LINGERING_POTION || i.getType() == TIPPED_ARROW) {
            return handlePotions(i, p, shulker);
        } else if(SPAWN_EGGS.contains(i.getType())){
            return handleSpawnEggs(i, p, shulker);
        } else if(i.getType() == BUNDLE) {
            return handleBundles(i, p, shulker);
        } else if(Tag.SHULKER_BOXES.isTagged(i.getType())) {
            return handleShulkers(i, p);
        } else if(i.getItemMeta() != null) {
            ItemMeta im = i.getItemMeta();
            if(im.hasAttributeModifiers()) {
                return handleAttributeModifiers(i, p, shulker);
            } else if(im.hasEnchants()) {
                return handleEnchantments(i, p, shulker);
            }
        }
        return i;
    }

    private static ItemStack handleShulkers(ItemStack item, Player p) {
        ItemMeta im = item.getItemMeta();
        if(im == null) return item;
        if(im instanceof BlockStateMeta) {
            BlockStateMeta bm = (BlockStateMeta) im;
            if (bm.getBlockState() instanceof ShulkerBox) {
                ShulkerBox sb = (ShulkerBox) bm.getBlockState();
                ItemStack[] items = sb.getInventory().getContents();
                boolean modified = false;
                for (int i = 0; i < items.length; i++) {
                    if(items[i] == null) continue;
                    ItemStack old = items[i].clone();
                    items[i] = modify(items[i], p, true);
                    if(!items[i].equals(old)) modified = true;
                }

                sb.getInventory().setContents(items);
                bm.setBlockState(sb);
                if(modified) {
                    displayMessage(item, p, false);
                    bm.setLore(Collections.singletonList(ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + "Disenchanted"));
                }

                item.setItemMeta(bm);
            }
        }
        return item;
    }


    private static ItemStack handlePotions(ItemStack i, Player p, boolean shulker) {
        ItemMeta im = i.getItemMeta();
        if(im == null) return i;
        if (im instanceof PotionMeta) {
            PotionMeta pm = (PotionMeta) im;
            if(pm.hasCustomEffects()) {
                displayMessage(i, p, shulker);
                pm.clearCustomEffects();
                pm.setLore(Collections.singletonList(ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + "Disenchanted"));
                i.setItemMeta(pm);
                return i;
            }
        }
        return i;
    }

    private static ItemStack handleSpawnEggs(ItemStack i, Player p, boolean shulker) {
        net.minecraft.world.item.ItemStack nmsItem = getNMSItemStack(i);
        if(nmsItem == null) return i;
        NBTTagCompound nbt = nmsItem.getOrCreateTag();
        if(nbt.hasKey("EntityTag")) {
            displayMessage(i, p, shulker);
            nbt.set("EntityTag", new NBTTagCompound());
            nmsItem.setTag(nbt);
            ItemStack i2 = getBukkitItemStack(nmsItem);
            if(i2 == null) return i;
            ItemMeta im = i2.getItemMeta();
            if(im == null) return i;
            im.setLore(Collections.singletonList(ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + "Disenchanted"));
            i2.setItemMeta(im);
            return i2;
        }
        return i;
    }

    private static ItemStack handleBundles(ItemStack i, Player p, boolean shulker) {
        displayMessage(i, p, shulker);
        i.setType(FLOWER_POT);
        ItemMeta im = i.getItemMeta();
        if(im == null) return i;
        if(!im.hasDisplayName())
            im.setDisplayName("§rBundle");
        im.setLore(Collections.singletonList(ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + "Disenchanted"));
        i.setItemMeta(im);
        return i;
    }

    private static ItemStack handleAttributeModifiers(ItemStack i, Player p, boolean shulker) {
        ItemMeta im = i.getItemMeta();
        if(im == null) return i;
        if(im.hasAttributeModifiers()) {
            displayMessage(i, p, shulker);
            im.setAttributeModifiers(HashMultimap.create());
            im.setLore(Collections.singletonList(ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + "Disenchanted"));
            i.setItemMeta(im);
        }
        return i;
    }

    private static ItemStack handleEnchantments(ItemStack i, Player p, boolean shulker) {
        ItemMeta im = i.getItemMeta();
        if(im == null) return i;
        if(im.hasEnchants()) {
            boolean message = false;
            Map<Enchantment, Integer> enchants = im.getEnchants();
            for(Enchantment e : enchants.keySet()) {
                if(enchants.get(e) > e.getMaxLevel()) {
                    if(!message) displayMessage(i, p, shulker);
                    message = true;
                    im.removeEnchant(e);
                    im.addEnchant(e, e.getMaxLevel(), false);
                }
            }
            if(message) {
                im.setLore(Collections.singletonList(ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + "Disenchanted"));
                i.setItemMeta(im);
            }
        }
        return i;
    }


    private static final List<Player> onCooldown = new ArrayList<>();

    private static void displayMessage(ItemStack i, Player player, boolean shulker) {
        if(onCooldown.contains(player)) return;

        if(shulker) return;

        ItemMeta im = i.getItemMeta();

        plugin.getLogger().info("[Disenchantment] " + player.getName() + "Was caught with [" + (im != null && im.hasDisplayName() ? im.getDisplayName() : i.getType().toString().toLowerCase()) + "], Disenchanting.");

        TextComponent prefix = new TextComponent("[Disenchantment] ");
        prefix.setColor(ChatColor.DARK_PURPLE);
        TextComponent mid = new TextComponent(player.getName() + " was caught with [");
        mid.setColor(ChatColor.LIGHT_PURPLE);

        BaseComponent name;
        if(im != null && im.hasDisplayName()) {
            name = new TextComponent(im.getDisplayName());
        } else {
            name = new TranslatableComponent("item.minecraft." + i.getType().toString().toLowerCase());
        }
        net.minecraft.world.item.ItemStack nmsItem = getNMSItemStack(i);
        if(nmsItem == null) return;
        NBTTagCompound nbt = nmsItem.getOrCreateTag();
        name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(i.getType().toString().toLowerCase(), 1, ItemTag.ofNbt(nbt.toString()))));
        name.setColor(ChatColor.LIGHT_PURPLE);

        TextComponent end = new TextComponent("], Disenchanting.");
        end.setColor(ChatColor.LIGHT_PURPLE);

        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("disenchantment.bypass")).forEach(p -> p.spigot().sendMessage(prefix, mid, name, end));
        onCooldown.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                onCooldown.remove(player);
            }
        }.runTaskLater(plugin, 2);
    }

    private static net.minecraft.world.item.ItemStack getNMSItemStack(ItemStack i) {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf(".") + 1);

        try {
            Object nmsItem = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, i);
            if(nmsItem instanceof net.minecraft.world.item.ItemStack) {
                return (net.minecraft.world.item.ItemStack) nmsItem;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ItemStack getBukkitItemStack(net.minecraft.world.item.ItemStack i) {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf(".") + 1);

        try {
            Object nmsItem = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack").getMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class).invoke(null, i);
            if(nmsItem instanceof ItemStack) {
                return (ItemStack) nmsItem;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static final List<Material> SPAWN_EGGS = Arrays.asList(
            AXOLOTL_SPAWN_EGG,
            BAT_SPAWN_EGG,
            BEE_SPAWN_EGG,
            BLAZE_SPAWN_EGG,
            CAVE_SPIDER_SPAWN_EGG,
            CAT_SPAWN_EGG,
            CHICKEN_SPAWN_EGG,
            COD_SPAWN_EGG,
            COW_SPAWN_EGG,
            CREEPER_SPAWN_EGG,
            DOLPHIN_SPAWN_EGG,
            DONKEY_SPAWN_EGG,
            DROWNED_SPAWN_EGG,
            ELDER_GUARDIAN_SPAWN_EGG,
            ENDERMAN_SPAWN_EGG,
            ENDERMITE_SPAWN_EGG,
            EVOKER_SPAWN_EGG,
            FOX_SPAWN_EGG,
            GHAST_SPAWN_EGG,
            GLOW_SQUID_SPAWN_EGG,
            GUARDIAN_SPAWN_EGG,
            HOGLIN_SPAWN_EGG,
            HORSE_SPAWN_EGG,
            HUSK_SPAWN_EGG,
            LLAMA_SPAWN_EGG,
            MAGMA_CUBE_SPAWN_EGG,
            MOOSHROOM_SPAWN_EGG,
            MULE_SPAWN_EGG,
            OCELOT_SPAWN_EGG,
            PANDA_SPAWN_EGG,
            PARROT_SPAWN_EGG,
            PHANTOM_SPAWN_EGG,
            PIG_SPAWN_EGG,
            PIGLIN_SPAWN_EGG,
            PIGLIN_BRUTE_SPAWN_EGG,
            PILLAGER_SPAWN_EGG,
            POLAR_BEAR_SPAWN_EGG,
            PUFFERFISH_SPAWN_EGG,
            RABBIT_SPAWN_EGG,
            RAVAGER_SPAWN_EGG,
            SALMON_SPAWN_EGG,
            SHEEP_SPAWN_EGG,
            SHULKER_SPAWN_EGG,
            SILVERFISH_SPAWN_EGG,
            SKELETON_SPAWN_EGG,
            SKELETON_HORSE_SPAWN_EGG,
            SLIME_SPAWN_EGG,
            SPIDER_SPAWN_EGG,
            SQUID_SPAWN_EGG,
            STRAY_SPAWN_EGG,
            STRIDER_SPAWN_EGG,
            TRADER_LLAMA_SPAWN_EGG,
            TROPICAL_FISH_SPAWN_EGG,
            TURTLE_SPAWN_EGG,
            VEX_SPAWN_EGG,
            VILLAGER_SPAWN_EGG,
            VINDICATOR_SPAWN_EGG,
            WANDERING_TRADER_SPAWN_EGG,
            WITCH_SPAWN_EGG,
            WITHER_SKELETON_SPAWN_EGG,
            WOLF_SPAWN_EGG,
            ZOMBIE_SPAWN_EGG,
            ZOMBIE_HORSE_SPAWN_EGG,
            ZOMBIE_VILLAGER_SPAWN_EGG,
            ZOMBIFIED_PIGLIN_SPAWN_EGG,
            ARMOR_STAND
    );
}
