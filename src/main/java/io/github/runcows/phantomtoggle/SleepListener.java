package io.github.runcows.phantomtoggle;

import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class SleepListener implements Listener {
    PhantomToggle plugin = PhantomToggle.getInstance();
    FileConfiguration config;
    FileConfiguration playerData;
    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event)
    {
        playerData = plugin.getPlayerData();
        config = plugin.getConfig();
        if (config.getString("statHandlingMode").equals("track"))
        {
            Player player = event.getPlayer();
            if (player.getStatistic(Statistic.TIME_SINCE_REST) == 0)
            {// they succesfully slept
                String playerID = player.getUniqueId().toString();
                if (!playerData.contains(playerID))
                {// don't log the sleep time of players who haven't even touched this lol. Keep file sizes down when you can
                    return;
                }
                playerData.set(playerID+".prevSleepTime",player.getStatistic(Statistic.TIME_SINCE_REST));
                plugin.savePlayerData(playerData);
            }
        }
    }
}
