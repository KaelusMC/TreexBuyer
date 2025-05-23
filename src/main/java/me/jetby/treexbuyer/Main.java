package me.jetby.treexbuyer;

import me.jetby.treexbuyer.commands.PluginCommands;
import me.jetby.treexbuyer.commands.PluginCompleter;
import me.jetby.treexbuyer.configurations.Config;
import me.jetby.treexbuyer.dataBase.DatabaseUtils;
import me.jetby.treexbuyer.listeners.OnJoin;
import me.jetby.treexbuyer.listeners.OnQuit;
import me.jetby.treexbuyer.menu.*;
import me.jetby.treexbuyer.autoBuy.AutoBuy;
import me.jetby.treexbuyer.configurations.PriceItemCfg;
import me.jetby.treexbuyer.configurations.MenuLoader;
import me.jetby.treexbuyer.configurations.PriseItemLoader;
import me.jetby.treexbuyer.utils.Placeholders;
import me.jetby.treexbuyer.utils.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static me.jetby.treexbuyer.autoBuy.AutoBuy.getAutoBuyStatus;
import static me.jetby.treexbuyer.autoBuy.AutoBuy.savePlayerScoreAsync;
import static me.jetby.treexbuyer.boost.CoefficientManager.*;
import static me.jetby.treexbuyer.boost.CoefficientManager.getCachedScores;
import static me.jetby.treexbuyer.configurations.Config.CFG;
import static me.jetby.treexbuyer.utils.Hex.hex;

public final class Main extends JavaPlugin {
    private static Main instance;
    public Economy economy;
    private Placeholders placeholderExpansion;
    private MenuManager menuManager;
    private Map<String, PriseItemLoader.ItemData> itemPrice;



    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        checkDepends(); // проверка и подключение Vault


        Config config = new Config();
        config.loadYamlFile(this);



        PriceItemCfg priseItemCfg= new PriceItemCfg();
        priseItemCfg.loadYamlFile(this);

        MenuLoader.loadMenus(CFG(), getDataFolder()); // загрузчик
        menuCheck(); // вывод загруженных меню

        menuManager = new MenuManager(MenuLoader.getListMenu());
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new OnJoin(), this);
        getServer().getPluginManager().registerEvents(new OnQuit(), this);

        this.databaseManager = new DatabaseUtils();
        databaseManager.initDatabase();


        createCommand(); // создаём все команды
        getCommand("treexbuyer").setExecutor(new PluginCommands());
        getCommand("treexbuyer").setTabCompleter(new PluginCompleter());

        String path = CFG().getString("priceItem.path", "priceItem.yml");
        File itemFile = new File(getDataFolder(), path);
        itemPrice = PriseItemLoader.loadItemValuesFromFile(itemFile);

        startAutoBuy();

        new Metrics(this, 25141);


    }



    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }

        if (databaseManager != null) {
            for (UUID uuid : getCachedScores().keySet()) {
                if (getCachedScores().containsKey(uuid)) {
                    AutoBuy.savePlayerScore(uuid);
                }
            }
            databaseManager.closeConnection();
        }
        getLogger().warning("[TreexBuyer] Плагин отключён.");
    }

    private void checkDepends(){
        if (!setupEconomy()) {
            getLogger().info("Vault не найден!");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new Placeholders(this);
            placeholderExpansion.register();
        } else {
            getLogger().warning("PlaceholderAPI не обнаружен! Некоторые функции могут быть недоступны.");
        }
    }
    public void menuCheck(){
        MenuLoader.getListMenu().forEach((key, vault) -> {
            getLogger().info("Меню: " + key + " загружен");
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
            registerCommand(commandName, new MenusCommands());
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


    public MenuManager getMenuManager() {
        return menuManager;
    }


    public Map<Material, Double> getMaterialPrise(Map<String, PriseItemLoader.ItemData> itemPrise){
        Map<Material, Double> materialCoal = new HashMap<>();
        itemPrise.forEach((key, vault) -> {
            Material material = Material.valueOf(key.toUpperCase());
            materialCoal.put(material, vault.price());
        });
        return materialCoal;
    }
    public DatabaseUtils getDatabaseManager() {
        return this.databaseManager;
    }
    private DatabaseUtils databaseManager;

    public PriseItemLoader.ItemData getPriseItem(String material) {
        return itemPrice.get(material);
    }

    public Map<String, PriseItemLoader.ItemData> getItemPrice() {
        return itemPrice;
    }
    public void startAutoBuy() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!getAutoBuyStatus(player.getUniqueId())) {
                    return;
                }

                checkForItems(player);
            }
        }, 0L, CFG().getInt("autoBuyDelay", 60));
    }

    private void checkForItems(Player player) {

        List<String> disabled_worlds = CFG().getStringList("autoBuy.disabled-worlds");
        if (!disabled_worlds.isEmpty()) {
            for (String disabled_world : disabled_worlds) {
                World world = player.getWorld();

                if (world.equals(Bukkit.getWorld(disabled_world))) return;
            }
        }


        List<String> autoBuyList = AutoBuy.getAutoBuyItems(player.getUniqueId());
        if (autoBuyList == null || autoBuyList.isEmpty()) {
            return;
        }

        double sumCount = 0d;
        int totalScores = 0;
        ItemStack air = new ItemStack(Material.AIR);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && autoBuyList.contains(item.getType().name())) {
                PriseItemLoader.ItemData itemData = getPriseItem(item.getType().name());

                if (itemData != null) {
                    double price = itemData.price();
                    int scores = itemData.addScores();

                    if (price > 0d) {
                        double totalPrice = price * item.getAmount();
                        economy.depositPlayer(player, totalPrice);
                        sumCount += totalPrice * getPlayerCoefficient(player);
                        totalScores += scores * item.getAmount(); // Учитываем количество предметов
                        if (player.getEquipment().getItemInOffHand()!=null && player.getEquipment().getItemInOffHand().equals(item)) {
                            player.getEquipment().setItemInOffHand(air);
                        }
                        if (player.getEquipment().getHelmet()!=null && player.getEquipment().getHelmet().equals(item)) {
                            player.getEquipment().setHelmet(air);
                        }
                        if (player.getEquipment().getChestplate()!=null && player.getEquipment().getChestplate().equals(item)) {
                            player.getEquipment().setChestplate(air);
                        }
                        if (player.getEquipment().getLeggings()!=null && player.getEquipment().getLeggings().equals(item)) {
                            player.getEquipment().setLeggings(air);
                        }
                        if (player.getEquipment().getBoots()!=null && player.getEquipment().getBoots().equals(item)) {
                            player.getEquipment().setBoots(air);
                        }

                        player.getInventory().removeItem(item);
                    }
                }
            }
        }

        if (sumCount > 0d) {
            player.sendMessage(hex(CFG().getString("autoBuy.message", "&aВы успешно продали все предметы на сумму &f%sum%")
                    .replace("%sum%", String.valueOf(sumCount))
                    .replace("%score%", String.valueOf(totalScores))

            ));
        }

        if (totalScores > 0) {
            addPlayerScores(player, totalScores);
        }
    }

}
