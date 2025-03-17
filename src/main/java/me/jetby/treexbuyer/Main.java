package me.jetby.treexbuyer;

import me.jetby.treexbuyer.Menu.Menu;
import me.jetby.treexbuyer.MenuListener.MenuListener;
import me.jetby.treexbuyer.MenuManager.MenuManager;
import me.jetby.treexbuyer.autoBuy.AutoBuy;
import me.jetby.treexbuyer.commands.Seller;
import me.jetby.treexbuyer.createDefaultYml.ConfigManager;
import me.jetby.treexbuyer.createDefaultYml.PriseItemFileCreator;
import me.jetby.treexbuyer.dataBase.DatabaseManager;
import me.jetby.treexbuyer.events.Listeners;
import me.jetby.treexbuyer.loader.MenuLoader;
import me.jetby.treexbuyer.loader.PriseItemLoader;
import me.jetby.treexbuyer.utils.ASellerPlaceholder;
import me.jetby.treexbuyer.utils.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static me.jetby.treexbuyer.utils.Hex.hex;

public final class Main extends JavaPlugin {
    private static Main instance;
    public Economy economy;
    private static FileConfiguration cfg;
    private ASellerPlaceholder placeholderExpansion;
    private MenuManager menuManager;
    private Map<String, Double> itemPrise;


    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        vaultCheck(); // проверка и подключение Vault

        saveDefaultConfig();
        cfg = getConfig();
        new ConfigManager(this);
        new PriseItemFileCreator(this);

        MenuLoader.loadMenus(cfg, getDataFolder()); // загрузчик
        menuCheck(); // вывод загруженных меню

        menuManager = new MenuManager(MenuLoader.getListMenu());
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        this.databaseManager = new DatabaseManager();
        databaseManager.initDatabase();


        createCommand(); // создаём все команды

        String path = cfg.getString("priseItem.path");
        File itemFile = new File(getDataFolder(), path);
        itemPrise = PriseItemLoader.loadItemValuesFromFile(itemFile);

        startAutoBuy();



        new Metrics(this, 23115);
    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        System.out.println("[TreexBuyer] Плагин отключён.");
    }

    private void vaultCheck(){
        if (!setupEconomy()) {
            getLogger().info("Vault не найден!");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new ASellerPlaceholder(this);
            placeholderExpansion.register();
        } else {
            getLogger().warning("PlaceholderAPI не обнаружен! Некоторые функции могут быть недоступны.");
        }
    }
    public void menuCheck(){
        MenuLoader.getListMenu().forEach((key, vault) -> {
            getLogger().info("Меню: " + key + " загружен");
            getLogger().info("Имя: " + vault.getTitleMenu());
            vault.getButtons().forEach(edit -> {

            });
        });
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> registeredServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredServiceProvider == null) {
            return false;
        }
        economy = registeredServiceProvider.getProvider();
        return true;
    }
    private void createCommand(){
        // ниже регистрация всех команд
        List<String> commandNames = new ArrayList<>();
        Map<String, Menu> listMenu = MenuLoader.getListMenu();
        listMenu.forEach((key, item) -> {
            commandNames.addAll(item.getCommandOpenMenu());
        });
        for (String commandName : commandNames) {
            registerCommand(commandName, new Seller());
        }
    }
    private void registerCommand(String commandName, CommandExecutor executor) {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(getServer());

            Command command = new BukkitCommand(commandName) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return executor.onCommand(sender, this, label, args);
                }
            };

            command.setAliases(Collections.emptyList());

            commandMap.register(getDescription().getName(), command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getCfg() {
        return cfg;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public Map<String, Double> getItemPrise() {
        return itemPrise;
    }
    public Map<Material, Double> getMaterialPrise(Map<String, Double> itemPrise){
        Map<Material, Double> materialCoal = new HashMap<>();
        itemPrise.forEach((key, vault) -> {
            Material material = Material.valueOf(key.toUpperCase());
            materialCoal.put(material, vault);
        });
        return materialCoal;
    }
    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }
    private DatabaseManager databaseManager;

    public Double getPriseItem(String material){
        itemPrise.forEach((key, vault) -> {
//            Main.getInstance().getLogger().info("Название предмета: " + key + "цена: " + vault);
        });
        return itemPrise.get(material);
    }

    public void startAutoBuy() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                if (!AutoBuy.getAutoBuyPlayersMap().containsKey(playerId)) {
                    AutoBuy.checkPlayer(playerId);
                }
                List<String> autoBuyList = AutoBuy.getAutoBuyPlayers(playerId);

                // если список autoBuy пуст, скип игрока
                if (autoBuyList == null || autoBuyList.isEmpty()) {
                    continue;
                }
                double sumCount = 0d;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && autoBuyList.contains(item.getType().name())) {
                        Double count = getPriseItem(item.getType().name());

                        if (count != null) {
                            double priseItem = count * item.getAmount();
                            economy.depositPlayer(player, priseItem);
                            sumCount += priseItem;
                            player.getInventory().remove(item);

                        }
                    }
                }

                if (sumCount!=0d) {
                    player.sendMessage(hex(cfg.getString("completeSaleMessage", "&aВы успешно продали все предметы на сумму &f%sum%").replace("%sum%", String.valueOf(sumCount))));
                }

            }
        }, 0L, cfg.getInt("autoBuyDelay", 60)); // Задержка перед первым запуском (0 тиков), периодичность (20 тиков = 1 секунда)
    }
}
