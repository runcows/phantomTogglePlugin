package io.github.runcows.phantomtoggle;

import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class CommandPhantoms implements CommandExecutor, TabCompleter {
    PhantomToggle plugin = PhantomToggle.getInstance();
    FileConfiguration config = plugin.getConfig();
    String sleepTimeMode;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            sleepTimeMode = config.getString("oldSleepTimeMode");
            FileConfiguration playerData = plugin.getPlayerData();
            String playerID = player.getUniqueId().toString();
            if (args.length <= 0)
            {
                if(playerData.getBoolean(playerID+".enabled"))
                {//disable it
                    disable(player,playerID,playerData);
                }
                else
                {//disabled OR not there, enable it
                    enable(player,playerID,playerData);
                }
            }
            else if (args.length == 1)
            {
                if (args[0].equals("off"))
                {
                    if (!playerData.getBoolean(playerID+".enabled"))
                    {
                        enable(player,playerID,playerData);
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
                    if (playerData.getBoolean(playerID+".enabled"))
                    {
                        disable(player,playerID,playerData);
                    }
                    else
                    {
                        String colorMessage = plugin.hex(
                                config.getString("textPhantomsAlreadyEnabled").replaceAll("%textHeader%",config.getString("textHeader"))
                        );
                        player.sendMessage(colorMessage);
                    }
                }
                else if (player.hasPermission("phantomtoggle.reload") && args[0].equals("reload"))
                {
                    plugin.reloadConfig();
                    plugin.config = plugin.getConfig();
                    this.config = plugin.getConfig();
                    plugin.reloadTimer();
                    String colorMessage = plugin.hex(
                            config.getString("textReload").replaceAll("%textHeader%",config.getString("textHeader"))
                    );
                    player.sendMessage(colorMessage);
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

    private void disable(Player player, String playerID, FileConfiguration playerData)
    {
        if(sleepTimeMode.equals("track") || sleepTimeMode.equals("pause"))
        {
            player.setStatistic(Statistic.TIME_SINCE_REST, playerData.getInt(playerID+".prevSleepTime"));
        }
        else if (sleepTimeMode.equals("reset"))
        {
            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }
        playerData.set(playerID+".enabled", false);
        plugin.savePlayerData(playerData);
        String colorMessage = plugin.hex(
                config.getString("textPhantomsEnabled").replaceAll("%textHeader%",config.getString("textHeader"))
        );
        player.sendMessage(colorMessage);
    }

    private void enable(Player player, String playerID, FileConfiguration playerData)
    {
        //don't need a config if statement here, just log it to file anyway, not worth the computation
        playerData.set(playerID+".prevSleepTime",player.getStatistic(Statistic.TIME_SINCE_REST));
        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        playerData.set(playerID+".enabled",true);
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
                if(player.hasPermission("phantomtoggle.reload"))
                {
                    autoCompleteOptions.add("reload");
                }
            }
        }
        return autoCompleteOptions;
    }
}
