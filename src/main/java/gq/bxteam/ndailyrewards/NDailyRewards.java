package gq.bxteam.ndailyrewards;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gq.bxteam.ndailyrewards.cfg.Config;
import gq.bxteam.ndailyrewards.cfg.ConfigManager;
import gq.bxteam.ndailyrewards.cmds.CommandManager;
import gq.bxteam.ndailyrewards.manager.UserManager;
import gq.bxteam.ndailyrewards.hooks.external.PlaceholderExpansions;
import gq.bxteam.ndailyrewards.tasks.SaveTask;
import gq.bxteam.ndailyrewards.utils.logs.LogType;
import gq.bxteam.ndailyrewards.utils.logs.LogUtil;
import gq.bxteam.ndailyrewards.utils.metrics.Metrics;
import gq.bxteam.ndailyrewards.data.DataManager;
import gq.bxteam.ndailyrewards.hooks.HookManager;
import gq.bxteam.ndailyrewards.data.IDataV2;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class NDailyRewards extends JavaPlugin {
    public static NDailyRewards instance;
    private CommandManager cmd;
    private ConfigManager cm;
    private PluginManager pm;
    private HookManager hm;
    private DataManager data;
    private UserManager um;

    public static NDailyRewards getInstance() {
        return NDailyRewards.instance;
    }

    @Override
    public void onEnable() {
        NDailyRewards.instance = this;

        onEnableLoad();
        load();
        checkForUpdates().ifPresent(latestVersion -> {
            LogUtil.send("&aAn update is available: " + latestVersion, LogType.INFO);
            LogUtil.send("&aPlease update to the latest version to get bug fixes, security patches and new features!", LogType.INFO);
            LogUtil.send("&aDownload here: https://modrinth.com/plugin/ndailyrewards/version/" + latestVersion, LogType.INFO);
        });

        if (Config.opt_metrics) {
            int pluginId = 13844;
            new Metrics(this, pluginId);
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderExpansions().register();
        }
    }

    public void onDisable() {
        this.unload();
    }

    public void onEnableLoad() {
        (this.cmd = new CommandManager(this)).setup();
        this.getCommand("ndailyrewards").setExecutor(this.cmd);
        this.pm = this.getServer().getPluginManager();
        (this.hm = new HookManager(this)).setup();

        new SaveTask(this).start();
    }

    public void load() {
        (this.cm = new ConfigManager(this)).setup();
        (this.data = new DataManager(this)).setup();
        (this.um = new UserManager(this)).setup();
    }

    public void unload() {
        try {
            this.getServer().getScheduler().cancelTasks(this);
            HandlerList.unregisterAll(this);
            this.um.shutdown();
            this.data.shutdown();
        } catch (Exception ex) {
            LogUtil.send("&cError while saving plugin data: " + ex.getMessage(), LogType.ERROR);
        }
    }

    public void reload() {
        this.unload();
        this.load();
    }

    public static Optional<String> checkForUpdates() {
        final String mcVersion = NDailyRewards.getInstance().getServer().getMinecraftVersion();
        final String pluginName = NDailyRewards.getInstance().getPluginMeta().getName();
        final String pluginVersion = NDailyRewards.getInstance().getPluginMeta().getVersion();
        try {
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.modrinth.com/v2/project/ZiFzQAnz/version?featured=true&game_versions=[%22" + mcVersion + "%22]"))
                    .header("User-Agent",
                            pluginName + "/" + pluginVersion
                    )
                    .GET()
                    .build();
            final HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 400 && res.statusCode() >= 200 && res.body() != null) {
                final JsonObject json = JsonParser.parseString(res.body()).getAsJsonArray().get(0).getAsJsonObject();
                if (json.has("version_number")) {
                    final String latestVersion = json.get("version_number").getAsString();
                    if (!latestVersion.equals(pluginVersion))
                        return Optional.of(latestVersion);
                }
            }
        }
        catch (final Exception e) {
            LogUtil.send("Failed to check for updates: " + e, LogType.ERROR);
        }
        return Optional.empty();
    }

    public PluginManager getPluginManager() {
        return this.pm;
    }

    public IDataV2 getData() {
        return this.data.getData();
    }

    public UserManager getUserManager() {
        return this.um;
    }
}