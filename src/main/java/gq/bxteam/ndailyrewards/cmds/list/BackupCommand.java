package gq.bxteam.ndailyrewards.cmds.list;

import gq.bxteam.ndailyrewards.NDailyRewards;
import gq.bxteam.ndailyrewards.cfg.Lang;
import gq.bxteam.ndailyrewards.cmds.ICmd;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BackupCommand extends ICmd
{

    public BackupCommand(final NDailyRewards plugin) {
        super(plugin);
    }

    @Override
    public void perform(final CommandSender sender, final String[] args) {
        String pluginFolder = String.valueOf(plugin.getDataFolder());

        File backupFolder = new File(pluginFolder, "backup");
        backupFolder.mkdir();

        File configFile = new File(pluginFolder, "config.yml");
        File dataFile = new File(pluginFolder, "data.db");
        File backupConfigFile = new File(backupFolder, "config.yml");
        File backupDataFile = new File(backupFolder, "data.db");
        try {
            Files.copy(configFile.toPath(), backupConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(dataFile.toPath(), backupDataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sender.sendMessage(Lang.Prefix.toMsg() + "Successfully backed up files!");
        }
        catch (Exception ex) {
            sender.sendMessage(Lang.Prefix.toMsg() + "Failed to backup files! Please check your console for more information.");
            ex.printStackTrace();
        }
    }

    @Override
    public String getPermission() {
        return "ndailyrewards.admin";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public String label() {
        return "backup";
    }

    @Override
    public String usage() {
        return "";
    }
}
