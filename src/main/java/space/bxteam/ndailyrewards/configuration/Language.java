package space.bxteam.ndailyrewards.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import space.bxteam.ndailyrewards.NDailyRewards;
import space.bxteam.ndailyrewards.utils.LogUtil;

import java.io.File;
import java.util.List;

public class Language {
    private static final String languagesPath = "language" + File.separator;
    private static final String[] languageFiles = {"en.yml"};
    private final File langFile;
    private final YamlConfiguration langConfig;

    public Language(File langFile) {
        this.langFile = langFile;
        this.langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getString(String path) {
        return langConfig.getString(path);
    }

    public List<String> getStringList(String path) {
        return langConfig.getStringList(path);
    }

    public String getPrefix() {
        return langConfig.getString("prefix");
    }

    public static File getLangFile() {
        String configLang = NDailyRewards.getInstance().config().language();
        String langPath = NDailyRewards.getInstance().getDataFolder() + File.separator + languagesPath + configLang + ".yml";
        File langFile = new File(langPath);

        try {
            if (!langFile.exists()) {
                LogUtil.log("&cFailed to load language file. Check your config.yml file!", LogUtil.LogLevel.ERROR);
                return null;
            }
        } catch (SecurityException e) {
            return null;
        }

        return langFile;
    }

    public static void saveLanguages() {
        File langDir = new File(NDailyRewards.getInstance().getDataFolder() + File.separator + languagesPath);
        if (!langDir.exists()) {
            langDir.mkdir();
            for (String langFile : languageFiles) {
                NDailyRewards.getInstance().saveResource(languagesPath + langFile, false);
            }
        }
    }
}
