package org.grappepie.mimic.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MimicSoul {

    private static final String PDC_KEY = "mimic_soul";

    /**
     * Creates one Mimic Soul item stack.
     * The item carries a PDC tag so it can be identified reliably regardless
     * of display name or lore changes.
     */
    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Mimic Soul", NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true));

        meta.lore(List.of(
                Component.text("Place inside a chest to awaken", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("a terrible hunger...", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, true)
        ));

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, PDC_KEY),
                PersistentDataType.BYTE,
                (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Returns true if the given item is a Mimic Soul (identified by PDC tag).
     */
    public static boolean isMimicSoul(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(new NamespacedKey(plugin, PDC_KEY), PersistentDataType.BYTE);
    }
}
