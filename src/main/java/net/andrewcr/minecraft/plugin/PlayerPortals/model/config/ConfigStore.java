package net.andrewcr.minecraft.plugin.PlayerPortals.model.config;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.PlayerPortals.Plugin;
import net.andrewcr.minecraft.plugin.BasePluginLib.config.ConfigurationFileBase;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigStore extends ConfigurationFileBase {
    @Getter
    private final boolean isMomentumConserved = true;

    //region Singleton

    public static ConfigStore getInstance() {
        return Plugin.getInstance().getConfigStore();
    }

    //endregion

    public ConfigStore() {
        super(Plugin.getInstance());
    }

    @Override
    protected void saveCore() {
        File portalFile = new File(Plugin.getInstance().getDataFolder(), "portals.yml");

        YamlConfiguration config = new YamlConfiguration();
        config.set("ConfigurationVersion", "1.0");

        config.set("IsMomentumConserved", this.isMomentumConserved);

        try {
            config.save(portalFile);
        } catch (IOException e) {
            Plugin.getInstance().getLogger().severe("Failed to save portal configuration: " + e.toString());
        }
    }

    @Override
    protected void loadCore() {
        File portalFile = new File(Plugin.getInstance().getDataFolder(), "config.yml");
        if (portalFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(portalFile);
            String version = config.getString("ConfigurationVersion");

            switch (version) {
                case "1.0":
                    this.loadV1_0Config(config);
                    return;

                default:
                    Plugin.getInstance().getLogger().severe("Unknown portal configuration version '" + version + "'!");
            }
        }
    }

    private void loadV1_0Config(YamlConfiguration config) {
        config.getBoolean("IsMomentumConserved", this.isMomentumConserved);
    }
}
