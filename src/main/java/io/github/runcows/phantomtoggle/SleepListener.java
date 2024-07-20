package io.github.runcows.phantomtoggle;

import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class SleepListener implements Listener {
    PhantomToggle plugin = PhantomToggle.getInstance();
    FileConfiguration playerData = plugin.getPlayerData();
    FileConfiguration config;
    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event)
    {
        config = plugin.getConfig();
        if (config.getString("statHandlingMode").equals("track"))
        {
            Player player = event.getPlayer();
            if (player.getStatistic(Statistic.TIME_SINCE_REST) == 0)
            {// they succesfully slept
                String playerID = player.getUniqueId().toString();
                playerData.set(playerID+".prevSleepTime",player.getStatistic(Statistic.TIME_SINCE_REST));
                plugin.savePlayerData(playerData);
            }
        }
    }
}
