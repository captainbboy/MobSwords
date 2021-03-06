package com.captainbboy.mobswords;

import com.captainbboy.mobswords.SQLite.SQLite;
import com.captainbboy.mobswords.commands.MainCommand;
import com.captainbboy.mobswords.events.GUIEvents;
import com.captainbboy.mobswords.events.MobEvents;
import com.captainbboy.mobswords.events.PlayerClickEvent;
import com.captainbboy.mobswords.events.PlayerSessionEvent;
import lombok.Getter;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class MobSwords extends JavaPlugin {

    private PlayerHandler playerHandler = new PlayerHandler();
    private MobSwordExpansion mobSwordExpansion;
    private PlayerClickEvent playerClickEvent = new PlayerClickEvent(this);
    private SQLite sqLite;
    @Getter private static double starPrice;
    public String currVersion = "1.1.1";
    public Economy eco;

    @Override
    public void onEnable() {

        // Check Dependencies

        if (!setupEconomy()) {
            getServer().getConsoleSender().sendMessage(MSUtil.clr("[&9&lMob&2Swords&7&l] &cYou must have Vault and an Economy Plugin installed!"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Default Config
        this.saveDefaultConfig();

        // Database
        sqLite = new SQLite(this);
        sqLite.load();
        sqLite.initialize();

        // Events
        getServer().getPluginManager().registerEvents(new PlayerSessionEvent(this), this);
        getServer().getPluginManager().registerEvents(new MobEvents(this), this);
        getServer().getPluginManager().registerEvents(playerClickEvent, this);
        getServer().getPluginManager().registerEvents(new GUIEvents(this), this);

        // Commands
        getCommand("mobsword").setExecutor(new MainCommand(this));

        getServer().getConsoleSender().sendMessage(MSUtil.clr("&7&l[&9&lMob&2Swords&7&l] &fPlugin has been successfully loaded."));

        startMinuteMessages();

        // PAPI Extension
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            mobSwordExpansion = new MobSwordExpansion(this);
            mobSwordExpansion.register();
        }
        // ShopGuiPlus
        if(Bukkit.getPluginManager().getPlugin("ShopGUIPlus ") != null) {
            this.starPrice = ShopGuiPlusApi.getItemStackPriceSell(new ItemStack(Material.NETHER_STAR));
            getServer().getConsoleSender().sendMessage(MSUtil.clr("&7&l[&9&lMob&2Swords&7&l] &fShopGUIPlus found, using star price from shop"));
        }else{
            this.starPrice = this.getConfig().getInt("price-of-star");
            getServer().getConsoleSender().sendMessage(MSUtil.clr("&7&l[&9&lMob&2Swords&7&l] &fUsing star price from config.yml"));
        }

    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(MSUtil.clr("&7&l[&9&lMob&2Swords&7&l] &fPlugin has been successfully unloaded."));
    }

    public void startMinuteMessages() {

        HashMap<UUID, Double> map = this.playerHandler.getAmountEarned();
        for(UUID uuid : map.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if(p != null) {
                String message = this.getConfig().getString("money-in-last-minute-message");
                message = message.replaceAll("\\{amount}", MSUtil.formatNumber(String.valueOf(map.get(uuid))));
                p.sendMessage(MSUtil.clr(message));
            }
        }
        this.playerHandler.clearAmountEarned();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                startMinuteMessages();
            }
        }, 60 * 20L);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if(economy != null)
            eco = economy.getProvider();

        return (eco != null);
    }

    public SQLite getSQLite() {
        return this.sqLite;
    }

    public PlayerHandler getPlayerHandler() {
        return this.playerHandler;
    }

    public MobSwordExpansion getExpansion() {
        return this.mobSwordExpansion;
    }

    public PlayerClickEvent getPlayerClickEvent() {
        return this.playerClickEvent;
    }

}
