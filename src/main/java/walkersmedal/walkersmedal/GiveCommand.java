package walkersmedal.walkersmedal;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        ItemStack itemStack = new ItemStack(Material.AIR);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (command.getName().equalsIgnoreCase("test")) {
            AttributeModifier attributeModifier = new AttributeModifier("test",10, AttributeModifier.Operation.ADD_NUMBER);
            itemMeta.addAttributeModifier(Attribute.GENERIC_ARMOR,attributeModifier);
            itemStack.setItemMeta(itemMeta);
            player.getInventory().addItem(itemStack);
        }

        if (command.getName().equalsIgnoreCase("given")) {
            if (args.length != 0) {
                for (int i = 0; i<=args.length; i++) {
                    switch (i) {
                        case 1:
                            Material M = Material.getMaterial(args[i]);
                            assert M != null;
                            itemStack.setType(M);
                        case 2:
                            Enchantment E = Enchantment.getByKey(NamespacedKey.minecraft(args[i]));
                            assert E != null;
                            itemStack.addEnchantment(E, Integer.parseInt(args[3]));
                        case 4:
                        case 6:
                            String lore = args[i];
                            String L = lore.replaceAll("<>"," ");
                        case 7:
                            String CustomName = args[i];
                            String C = CustomName.replaceAll("<>"," ");
                            player.getInventory().addItem(itemStack);
                    }
                }
            }
        }
        return true;
    }
}
