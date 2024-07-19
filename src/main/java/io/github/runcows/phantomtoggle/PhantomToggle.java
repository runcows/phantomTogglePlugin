package io.github.runcows.phantomtoggle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
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
        //getLogger().info("onEnable is called!");
        saveDefaultConfig();
        config = this.getConfig();

        this.getCommand("phantoms").setExecutor(new CommandPhantoms());
        createPlayerData();
        long timer = config.getLong("timeSinceLastRestResetTimer") * 20;
        resetTime.runTaskTimer(this, 0,timer);
    }

    @Override
    public void onDisable()
    {
        //getLogger().info("onDisable is called!");
        savePlayerData(playerData);
        saveConfig();
    }

    public void reloadTimer()
    {
        resetTime.cancel();
        long timer = config.getLong("timeSinceLastRestResetTimer") * 20;
        resetTime = createNewResetTimer();
        resetTime.runTaskTimer(this,0,timer);
    }

    public FileConfiguration getPlayerData()
    {
        return this.playerData;
    }
    private void createPlayerData()
    {
        playerDataFile = new File(getDataFolder(), "playerData.yml");
        if(!playerDataFile.exists())
        {
            playerDataFile.getParentFile().mkdirs();
            saveResource("playerData.yml",false);
        }
        playerData = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(playerDataFile);
    }
    public void savePlayerData(FileConfiguration playerData)
    {
        try
        {
            playerData.save("playerData.yml");
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
        {
            @Override
            public void run()
            {
                boolean trackTimeEnabled = config.getString("oldSleepTimeMode").equals("track");
                for(String playerID : playerData.getKeys(false))
                {
                    if (playerData.getBoolean(playerID + ".enabled"))
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
