package io.github.runcows.phantomtoggle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhantomToggle extends JavaPlugin
{
    private static PhantomToggle instance;
    public FileConfiguration config;
    private File playerDataFile;
    private FileConfiguration playerData;
    BukkitRunnable resetTime = createNewResetTimer();

    public PhantomToggle()
    {
        instance = this;
    }
    public static PhantomToggle getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        config = this.getConfig();
        reloadPlayerData();
        this.getCommand("phantoms").setExecutor(new CommandPhantoms());
        this.getCommand("phantomToggle").setExecutor(new CommandPhantomToggle());
        getServer().getPluginManager().registerEvents(new SleepListener(), this);
        long timer = config.getLong("restTimeResetTimer") * 20;
        resetTime.runTaskTimer(this, 0,timer);
    }

    @Override
    public void onDisable()
    {
        savePlayerData(playerData);
        saveConfig();
    }

    public void reloadTimer()
    {
        resetTime.cancel();
        long timer = config.getLong("restTimeResetTimer") * 20;
        resetTime = createNewResetTimer();
        resetTime.runTaskTimer(this,0,timer);
    }

    public FileConfiguration getPlayerData()
    {
        return this.playerData;
    }
    public void reloadPlayerData()
    {
        playerDataFile = new File(getDataFolder(), "playerData.yml");
        if(!playerDataFile.exists())
        {
            playerDataFile.getParentFile().mkdirs();
            saveResource("playerData.yml",false);
        }
        playerData = new YamlConfiguration();
        try
        {
            playerData.load(playerDataFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
    }
    public void savePlayerData(FileConfiguration playerData)
    {
        try
        {
            playerData.save(playerDataFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    //this method's code is from Maxx_Qc on this thread (https://www.spigotmc.org/threads/use-hex-color-codes-in-clickable-message.476327/)
    private static final Pattern HEX_PATTERN = Pattern.compile("&(#\\w{6})");
    public static String hex(String str)
    {
        Matcher matcher = HEX_PATTERN.matcher(ChatColor.translateAlternateColorCodes('&', str));
        StringBuffer buffer = new StringBuffer();

        while (matcher.find())
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of(matcher.group(1)).toString());

        return matcher.appendTail(buffer).toString();
    }

    private BukkitRunnable createNewResetTimer()
    {
        return new BukkitRunnable()
        {// with this amount of indentation, I want Jerma to put me through a meat grinder
            @Override
            public void run()
            {
                boolean trackTimeEnabled = config.getString("statHandlingMode").equals("track");
                for(String playerID : playerData.getKeys(false))
                {
                    if (playerData.getBoolean(playerID + ".phantomsDisabled") && Bukkit.getPlayer(UUID.fromString(playerID)) != null)
                    {
                        if(trackTimeEnabled)
                        {
                            playerData.set(
                                    playerID + ".prevSleepTime",
                                    playerData.getInt(playerID + ".prevSleepTime")
                                            + Bukkit.getPlayer(UUID.fromString(playerID)).getStatistic(Statistic.TIME_SINCE_REST)
                            );
                            savePlayerData(playerData);
                        }
                        Bukkit.getPlayer(UUID.fromString(playerID)).setStatistic(Statistic.TIME_SINCE_REST, 0);
                    }
                }
            }
        };
    }
}
