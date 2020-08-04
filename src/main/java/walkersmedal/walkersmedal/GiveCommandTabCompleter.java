package walkersmedal.walkersmedal;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GiveCommandTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("given")) {

            List<String> result = new ArrayList<String>();
            if (args.length == 1) {
                result.add("0アイテム名");
                for (Material a : Material.values()) {
                    result.add(a.name());
                }
                return result;
            }
            if (args.length == 2) {
                result.add("0エンチャントリスト");
                for (Enchantment enchantment : Enchantment.values()) {
                    result.add(enchantment.getKey().toString());
                }
                return result;
            }
            if (args.length == 3) {
                result.add("0");
                return result;
            }
            if (args.length == 4) {
                result.add("0属性");
                for (Attribute attribute : Attribute.values()) {
                    result.add(attribute.name());
                }
                return result;
            }
            if (args.length == 5) {
                result.add("0");
                return result;
            }
            if (args.length == 6) {
                result.add("custom<>name<>⇚isSpace");
                return result;
            }
            if (args.length == 7) {
                result.add("lore:Write<>to<>Word<>⇚isSpace");
                return result;
            }


        }
        return null;
    }

}
