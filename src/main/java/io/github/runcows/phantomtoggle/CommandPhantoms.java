package io.github.runcows.phantomtoggle;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CommandPhantoms implements CommandExecutor, TabCompleter {
    PhantomToggle plugin = PhantomToggle.getInstance();
    FileConfiguration config = plugin.getConfig();
    String sleepTimeMode;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            sleepTimeMode = config.getString("statHandlingMode");
            FileConfiguration playerData = plugin.getPlayerData();
            String playerID = player.getUniqueId().toString();
            if (args.length <= 0)
            {
                if(playerData.getBoolean(playerID+".phantomsDisabled"))
                {
                    enablePhantomSpawns(player,playerID,playerData);
                }
                else
                {
                    disablePhantomSpawns(player,playerID,playerData);
                }
            }
            else if (args.length == 1)
            {
                if (args[0].equals("off"))
                {
                    if (!playerData.getBoolean(playerID+".phantomsDisabled"))
                    {
                        disablePhantomSpawns(player,playerID,playerData);
                    }
                    else
                    {
                        String colorMessage = plugin.hex(
                                config.getString("textPhantomsAlreadyDisabled").replaceAll("%textHeader%",config.getString("textHeader"))
                        );
                        player.sendMessage(colorMessage);
                    }
                }
                else if (args[0].equals("on"))
                {
                    if (playerData.getBoolean(playerID+".phantomsDisabled"))
                    {
                        enablePhantomSpawns(player,playerID,playerData);
                    }
                    else
                    {
                        String colorMessage = plugin.hex(
                                config.getString("textPhantomsAlreadyEnabled").replaceAll("%textHeader%",config.getString("textHeader"))
                        );
                        player.sendMessage(colorMessage);
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    private void enablePhantomSpawns(Player player, String playerID, FileConfiguration playerData)
    {
        if(sleepTimeMode.equals("track"))
        {
            playerData.set(
                    playerID + ".prevSleepTime",
                    playerData.getInt(playerID + ".prevSleepTime")
                            + Bukkit.getPlayer(UUID.fromString(playerID)).getStatistic(Statistic.TIME_SINCE_REST)
            );
            plugin.savePlayerData(playerData);
            player.setStatistic(Statistic.TIME_SINCE_REST, playerData.getInt(playerID+".prevSleepTime"));
        }
        else if (sleepTimeMode.equals("pause"))
        {
            player.setStatistic(Statistic.TIME_SINCE_REST, playerData.getInt(playerID+".prevSleepTime"));
        }
        else if (sleepTimeMode.equals("reset"))
        {
            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }
        playerData.set(playerID+".phantomsDisabled", false);
        plugin.savePlayerData(playerData);
        String colorMessage = plugin.hex(
                config.getString("textPhantomsEnabled").replaceAll("%textHeader%",config.getString("textHeader"))
        );
        player.sendMessage(colorMessage);
    }

    private void disablePhantomSpawns(Player player, String playerID, FileConfiguration playerData)
    {
        playerData.set(playerID+".prevSleepTime",player.getStatistic(Statistic.TIME_SINCE_REST)); //just log it anyway
        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        playerData.set(playerID+".phantomsDisabled",true);
        plugin.savePlayerData(playerData);
        String colorMessage = plugin.hex(
                config.getString("textPhantomsDisabled").replaceAll("%textHeader%",config.getString("textHeader"))
        );
        player.sendMessage(colorMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> autoCompleteOptions = new ArrayList<>();
        if (command.getName().equals("phantoms") && args.length >= 0)
        {
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                autoCompleteOptions.add("on");
                autoCompleteOptions.add("off");
            }
        }
        return autoCompleteOptions;
    }
}
