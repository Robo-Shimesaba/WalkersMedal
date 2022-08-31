package walkersmedal.walkersmedal;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WalkersMedal extends JavaPlugin implements Listener {

    private FileConfiguration Trader;
    private File TraderFile;
    private Path workDirectory = Paths.get(this.getDataFolder().getPath()+"/PlayerData");

    @Override
    public void onEnable() {
        // Plugin startup logic
        PluginManager pl = Bukkit.getServer().getPluginManager();
        Objects.requireNonNull(getCommand("WalkersMedalPermission")).setTabCompleter(this);

        pl.registerEvents(this, this);
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        try {
            Files.createDirectory(workDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TraderFile = new File(this.getDataFolder(), "Trader.yml");

        if (!TraderFile.exists()) {
            try {
                TraderFile.createNewFile();
                Bukkit.getServer().getConsoleSender().sendMessage("Traders.yml を生成しました");
            } catch (IOException e) {
                Bukkit.getServer().getConsoleSender().sendMessage("Traders.yml の生成に失敗しました。");
            }
        }
        Trader = YamlConfiguration.loadConfiguration(TraderFile);
        Trader.set("Traders", true);
        try {
            Trader.save(TraderFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onjoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        //FileConfiguration config = this.getConfig();

        if (!Files.exists(Path.of(workDirectory +"/"+ player.getUniqueId()+".scw"))) {
            try {
                Files.createFile(Path.of(workDirectory +"/"+ player.getUniqueId()+".scw"));
                File file = new File(String.valueOf(workDirectory),player.getUniqueId()+".scw");
                if (!file.canWrite()) {
                    file.setWritable(true);
                }

                Map<String,Integer> userdata = new HashMap<>();
                userdata.put("walk",0);
                userdata.put("combat",0);
                userdata.put("mining",0);
                userdata.put("lumberjack",0);
                userdata.put("fishing",0);
                userdata.put("build",0);
                userdata.put("bossBattle",0);
                userdata.put("trade",0);

                ObjectOutputStream export = new ObjectOutputStream(new FileOutputStream(file));
                export.writeObject(userdata);


            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        //if (this.getConfig().contains(String.valueOf(player.getUniqueId()))) {
        //    return;
        //} else {
        //    config.set(String.valueOf(player.getUniqueId()), 0);
        //    config.set(player.getUniqueId() + "medal", 0);
        //    this.saveConfig();
        //}
    }

    @EventHandler
    public void onWalk(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Configuration config = this.getConfig();
        int counter = config.getInt(player.getUniqueId() + "medal");
        int count = config.getInt(String.valueOf(player.getUniqueId()));

        if (!player.isFlying()) {
            if (!player.isGliding()) {
                if (!player.isJumping()) {
                    Location from = e.getFrom();
                    Location to = e.getTo();

                    if (from.getBlockX() + from.getBlockZ() == to.getBlockX() + to.getBlockZ()) {
                        return;
                    }

                    double xz = Math.sqrt((from.getBlockX() - to.getBlockX()) ^ 2 + (from.getBlockZ() - to.getBlockZ()) ^ 2);
                    if (Double.isNaN(xz)) {
                        xz = 1;
                    }
                    if (xz > 1) {
                        xz = 1;
                    }

                    double i = count + xz;

                    this.saveConfig();
                    config.set(String.valueOf(player.getUniqueId()), i);

                    if (count >= 1250) {
                        count = 0;
                        int ic = counter + 1;
                        config.set(String.valueOf(player.getUniqueId()), count);
                        config.set(player.getUniqueId() + "medal", ic);
                        this.saveConfig();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("filePreview")) {
            File file = new File(String.valueOf(workDirectory), player.getUniqueId() + ".scw");
            Map<String, Integer> map  = new HashMap<>();
            try {
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
                map = (Map<String, Integer>) input.readObject();
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }

            player.sendMessage("*現在の歩数: "+map.get("walk"));
            player.sendMessage("*討伐数: "+map.get("combat"));
            player.sendMessage("*採掘数: "+map.get("mining"));
            player.sendMessage("*伐採数: "+map.get("lumberjack"));
            player.sendMessage("*釣り回数: "+map.get("fishing"));
            player.sendMessage("*ブロック設置回数: "+map.get("build"));
            player.sendMessage("*ボス討伐数: "+map.get("bossBattle"));
            player.sendMessage("*取引回数: "+map.get("trade"));
        }

            if (command.getName().equalsIgnoreCase("WalkersMedalPermission")) {
            HashMap<UUID, PermissionAttachment> perms = new HashMap<>();
            PermissionAttachment attachment = player.addAttachment(this);
            perms.put(player.getUniqueId(), attachment);
            if (args.length != 0) {
                Player p = Bukkit.getPlayer(args[2]);
                if (args[0].equalsIgnoreCase("allow")) {
                    if (args[1].equalsIgnoreCase("WalkersMedalTraderCommandAllowUse")) {
                        assert p != null;
                        PermissionAttachment pperms = perms.get(p.getUniqueId());
                        pperms.setPermission("WalkersMedalTraderCommandAllowUse", true);
                        player.sendMessage("permission set allow");
                    }
                    if (args[1].equalsIgnoreCase("WalkCountCommandAllowUse")) {
                        assert p != null;
                        PermissionAttachment pperms = perms.get(p.getUniqueId());
                        pperms.setPermission("WalkCountCommandAllowUse", true);
                        player.sendMessage("permission set allow");
                    }
                }
                if (args[0].equalsIgnoreCase("deny")) {
                    if (args[1].equalsIgnoreCase("WalkersMedalTraderCommandAllowUse")) {
                        assert p != null;
                        PermissionAttachment pperms = perms.get(p.getUniqueId());
                        pperms.setPermission("WalkersMedalTraderCommandAllowUse", false);
                        player.sendMessage("permission set deny");
                    }
                    if (args[1].equalsIgnoreCase("WalkCountCommandAllowUse")) {
                        assert p != null;
                        PermissionAttachment pperms = perms.get(p.getUniqueId());
                        pperms.setPermission("WalkCountCommandAllowUse", false);
                        player.sendMessage("permission set deny");
                    }
                }
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("WalkCount")) {
            if (player.hasPermission("WalkCountCommandAllowUse")) {
                if (args.length != 0) {
                    Player p = Bukkit.getPlayer(args[0]);
                    assert p != null;
                    Configuration config = this.getConfig();
                    int s = config.getInt(String.valueOf(p.getUniqueId()));
                    p.sendMessage("現在の次のメダル獲得までの歩数" + (1250 - s));
                    int ss = config.getInt(p.getUniqueId() + "medal");
                    p.sendMessage("現在の獲得可能なメダルの枚数" + ss);
                    return true;
                }
            } else return true;
        }

        if (command.getName().equalsIgnoreCase("WalkersMedalTrader")) {

            if (player.hasPermission("WalkersMedalTraderCommandAllowUse")) {

                Location loc = player.getLocation();
                World w = player.getWorld();
                double y = loc.getY();
                double x = loc.getX();
                double z = loc.getZ();
                float yaw = loc.getYaw();
                float pitch = loc.getPitch();
                Location sentry = new Location(w, x, y, z, yaw, pitch);
                Creature villager = (Creature) w.spawnEntity(sentry, EntityType.VILLAGER);
                villager.setAI(false);
                villager.setCustomNameVisible(true);
                villager.setSilent(true);
                villager.setCustomName("WalkersMedalTrader");
                villager.setCustomNameVisible(true);
                Trader.set(villager.getUniqueId() + "Trader", sentry);
                try {
                    Trader.save(TraderFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        boolean uuid = Trader.contains(e.getRightClicked().getUniqueId() + "Trader");
        if (uuid) {
            if (player.isSneaking()) {
                Inventory inv = Bukkit.createInventory(null, 9, ChatColor.AQUA + "Walkers Medal Trader");

                //SLOT0
                ItemStack Close = new ItemStack(Material.RED_WOOL);
                ItemMeta CloseMeta = Close.getItemMeta();
                CloseMeta.setDisplayName(ChatColor.UNDERLINE + "GUIを閉じます。");
                Close.setItemMeta(CloseMeta);
                inv.setItem(0, Close);

                //SLOT4
                ItemStack WalkersMedal = new ItemStack(Material.EMERALD);
                ItemMeta WalkersMedalMeta = WalkersMedal.getItemMeta();
                WalkersMedalMeta.addEnchant(Enchantment.PROTECTION_FIRE, 1, true);
                WalkersMedalMeta.setDisplayName(ChatColor.AQUA + "ウォーカーズメダル ");
                WalkersMedalMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                ArrayList<String> lore = new ArrayList<String>();
                lore.add("1250歩マイに贈られるメダル");
                lore.add("通常の通貨としての価値もある");
                lore.add("");
                lore.add(ChatColor.GREEN + "クリックすると取得可能個数分メダルがもらえます");
                WalkersMedalMeta.setLore(lore);
                WalkersMedal.setItemMeta(WalkersMedalMeta);
                inv.setItem(4, WalkersMedal);

                //SLOT8
                ItemStack count = new ItemStack(Material.PAPER);
                ItemMeta countMeta = count.getItemMeta();
                Configuration config = this.getConfig();
                int counter = config.getInt(player.getUniqueId() + "medal");
                countMeta.setDisplayName(ChatColor.UNDERLINE + "現在の取得可能個数:" + counter);
                count.setItemMeta(countMeta);
                inv.setItem(8, count);

                player.openInventory(inv);

            }

            Inventory inv = Bukkit.createInventory(null, 9, ChatColor.AQUA + "Walkers Medal Trader");

            //SLOT0
            ItemStack Close = new ItemStack(Material.RED_WOOL);
            ItemMeta CloseMeta = Close.getItemMeta();
            CloseMeta.setDisplayName(ChatColor.UNDERLINE + "GUIを閉じます。");
            Close.setItemMeta(CloseMeta);
            inv.setItem(0, Close);

            //SLOT4
            ItemStack WalkersMedal = new ItemStack(Material.EMERALD);
            ItemMeta WalkersMedalMeta = WalkersMedal.getItemMeta();
            WalkersMedalMeta.addEnchant(Enchantment.PROTECTION_FIRE, 1, true);
            WalkersMedalMeta.setDisplayName(ChatColor.AQUA + "ウォーカーズメダル ");
            WalkersMedalMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("1250歩マイに贈られるメダル");
            lore.add("通常の通貨としての価値もある");
            lore.add("");
            lore.add(ChatColor.GREEN + "クリックすると取得可能個数分メダルがもらえます");
            WalkersMedalMeta.setLore(lore);
            WalkersMedal.setItemMeta(WalkersMedalMeta);
            inv.setItem(4, WalkersMedal);

            //SLOT8
            ItemStack count = new ItemStack(Material.PAPER);
            ItemMeta countMeta = count.getItemMeta();
            Configuration config = this.getConfig();
            int counter = config.getInt(player.getUniqueId() + "medal");
            countMeta.setDisplayName(ChatColor.UNDERLINE + "現在の取得可能個数:" + counter);
            count.setItemMeta(countMeta);
            inv.setItem(8, count);

            player.openInventory(inv);
        }
    }

    @EventHandler
    public void onINVClicked(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        Configuration config = this.getConfig();
        int counter = config.getInt(player.getUniqueId() + "medal");

        InventoryView open = e.getView();
        ItemStack item = e.getCurrentItem();

        if (open.getTitle().equals(ChatColor.AQUA + "Walkers Medal Trader")) {

            if (item == null || !item.hasItemMeta() || item.getType() == Material.AIR) {
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(ChatColor.UNDERLINE + "GUIを閉じます。")) {
                player.closeInventory();
            }

            if (item.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "ウォーカーズメダル ")) {
                player.closeInventory();
                ItemStack WalkersMedal = new ItemStack(Material.EMERALD);
                ItemMeta WalkersMedalMeta = WalkersMedal.getItemMeta();
                WalkersMedalMeta.addEnchant(Enchantment.PROTECTION_FIRE, 1, true);
                WalkersMedalMeta.setDisplayName(ChatColor.AQUA + "ウォーカーズメダル");
                WalkersMedalMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                ArrayList<String> lore = new ArrayList<String>();
                lore.add("1250歩マイに贈られるメダル");
                lore.add("通常の通貨としての価値もある");
                WalkersMedalMeta.setLore(lore);
                WalkersMedal.setItemMeta(WalkersMedalMeta);
                Inventory inv = player.getInventory();

                WalkersMedal.setAmount(counter);
                HashMap<Integer, ItemStack> amount = inv.addItem(WalkersMedal);
                if (!amount.containsKey(0)) {
                    config.set(player.getUniqueId() + "medal", 0);
                    this.saveConfig();
                    return;
                }
                config.set(player.getUniqueId() + "medal", amount.get(0).getAmount());
                this.saveConfig();
            }

            if (item.getType().equals(Material.PAPER)) {
                e.setCancelled(true);
            }
        }
    }
}