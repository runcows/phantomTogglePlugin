package io.github.runcows.phantomtoggle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CommandPhantomToggle implements CommandExecutor, TabCompleter {
    PhantomToggle plugin = PhantomToggle.getInstance();
    FileConfiguration config = plugin.getConfig();

    //NEXT STEPS WITH THIS]
        //do we want a help command? idk maybe. Don't care too much, but sure I guess

    // oh i think i understand why people dont do this
        // editing config options from commands, that is
    // the only people who have access are the admins anyway
    // admins can just (often times) edit the config file directly
    // but it is definitely a nice QOL thing to be able to do it

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1)
        {
            //one argument, must be reload
            if (!args[0].equals("reload"))
            {
                return false;
            }
            // reload config from file
            plugin.reloadConfig();
            plugin.config = plugin.getConfig();
            this.config = plugin.getConfig();
            plugin.reloadTimer();
            String colorMessage = plugin.hex(
                    config.getString("textReload").replaceAll("%textHeader%",config.getString("textHeader"))
            );
            sender.sendMessage(colorMessage);
            return true;
        }
        if (args.length == 3)
        {
            //three arguments, must be a config
            if (!args[0].equals("config"))
            {
                return false;
            }
            if (args[1].equals("statHandlingMode"))
            {
                if (!args[2].equals("track") && !args[2].equals("reset") && !args[2].equals("pause"))
                {// check to make sure it matches the modes
                    return false;
                }
                //set the config
                config.set("statHandlingMode", args[2]);
                plugin.saveConfig();
                String colorMessage = plugin.hex(
                        config.getString("textConfigChanged")
                                .replaceAll("%textHeader%",config.getString("textHeader"))
                                .replaceAll("%option%","statHandlingMode")
                                .replaceAll("%value%",args[2])
                );
                sender.sendMessage(colorMessage);
                return true;
            }
            if (args[1].equals("restTimeResetTimer"))
            {
                // check to make sure its a long
                if(Integer.valueOf(args[2]) <= 0)
                {
                    return false;
                }
                //set the config
                config.set("restTimeResetTimer", Integer.valueOf(args[2]));
                plugin.saveConfig();
                plugin.reloadTimer();
                String colorMessage = plugin.hex(
                        config.getString("textConfigChanged")
                                .replaceAll("%textHeader%",config.getString("textHeader"))
                                .replaceAll("%option%","restTimeResetTimer")
                                .replaceAll("%value%",args[2])
                );
                sender.sendMessage(colorMessage);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1)
        {
            options.add("reload");
            options.add("config");
        }
        else if (args.length == 2)
        {
            options.add("statHandlingMode");
            options.add("restTimeResetTimer");
        }
        else if (args.length == 3)
        {
            if (args[1].equals("statHandlingMode"))
            {
                options.add("track");
                options.add("pause");
                options.add("reset");
            }
            else if (args[1].equals("restTimeResetTimer"))
            {
                options.add("1200");
            }
        }
        return options;
    }
}
