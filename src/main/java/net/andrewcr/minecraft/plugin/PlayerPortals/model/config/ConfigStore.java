package net.andrewcr.minecraft.plugin.PlayerPortals.model.config;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.BasePluginLib.config.ConfigurationFileBase;
import net.andrewcr.minecraft.plugin.PlayerPortals.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigStore extends ConfigurationFileBase {
    //region Private Fields

    @Getter private final boolean isMomentumConserved = true;

    //endregion

    //region Singleton

    public static ConfigStore getInstance() {
        return Plugin.getInstance().getConfigStore();
    }

    //endregion

    // region Constructor

    public ConfigStore() {
        super(Plugin.getInstance());
    }

    //endregion

    //region Serialization


    @Override
    protected String getFileName() {
        return "config.yml";
    }

    @Override
    protected void loadCore(YamlConfiguration configuration) {
            String version = configuration.getString("ConfigurationVersion");
            switch (version) {
                case "1.0":
                    this.loadV1_0Config(configuration);
                    return;

                default:
                    Plugin.getInstance().getLogger().severe("Unknown portal configuration version '" + version + "'!");
            }

    }

    private void loadV1_0Config(YamlConfiguration config) {
        config.getBoolean("IsMomentumConserved", this.isMomentumConserved);
    }

    @Override
    protected void saveCore(YamlConfiguration configuration) {
        configuration.set("ConfigurationVersion", "1.0");
        configuration.set("IsMomentumConserved", this.isMomentumConserved);
    }

    //endregion
}
