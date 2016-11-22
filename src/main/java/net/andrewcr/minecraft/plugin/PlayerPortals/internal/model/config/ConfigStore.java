package net.andrewcr.minecraft.plugin.PlayerPortals.internal.model.config;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.BasePluginLib.config.ConfigurationFileBase;
import net.andrewcr.minecraft.plugin.PlayerPortals.internal.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigStore extends ConfigurationFileBase {
    //region Private Fields

    private static final String CONFIGURATION_VERSION_KEY = "ConfigurationVersion";
    private static final String IS_MOMENTUM_CONSERVED_KEY = "IsMomentumConserved";
    private static final String DISTRIBUTED_SPAWNS_INTEGRATION_ENABLED_KEY = "DistributedSpawnsIntegrationEnabled";
    private static final String DYNMAP_INTEGRATION_ENABLED_KEY = "DynmapIntegrationEnabled";
    private static final String PORTAL_BLOCK_PHYSICS_ENABLED_KEY = "PortalBlockPhysicsEnabled";

    @Getter private boolean isMomentumConserved = true;
    @Getter private boolean distributedSpawnsIntegrationEnabled = true;
    @Getter private boolean dynmapIntegrationEnabled = false;
    @Getter private boolean portalBlockPhysicsEnabled = true;

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
            String version = configuration.getString(CONFIGURATION_VERSION_KEY);
            switch (version) {
                case "1.0":
                    this.loadV1_0Config(configuration);
                    return;

                default:
                    Plugin.getInstance().getLogger().severe("Unknown portal configuration version '" + version + "'!");
            }

    }

    private void loadV1_0Config(YamlConfiguration config) {
        this.isMomentumConserved = config.getBoolean(IS_MOMENTUM_CONSERVED_KEY, this.isMomentumConserved);
        this.distributedSpawnsIntegrationEnabled = config.getBoolean(DISTRIBUTED_SPAWNS_INTEGRATION_ENABLED_KEY, this.distributedSpawnsIntegrationEnabled);
        this.dynmapIntegrationEnabled = config.getBoolean(DYNMAP_INTEGRATION_ENABLED_KEY, this.dynmapIntegrationEnabled);
        this.portalBlockPhysicsEnabled = config.getBoolean(PORTAL_BLOCK_PHYSICS_ENABLED_KEY, this.portalBlockPhysicsEnabled);
    }

    @Override
    protected void saveCore(YamlConfiguration configuration) {
        configuration.set(CONFIGURATION_VERSION_KEY, "1.0");

        configuration.set(IS_MOMENTUM_CONSERVED_KEY, this.isMomentumConserved);
        configuration.set(DISTRIBUTED_SPAWNS_INTEGRATION_ENABLED_KEY, this.distributedSpawnsIntegrationEnabled);
        configuration.set(DYNMAP_INTEGRATION_ENABLED_KEY, this.dynmapIntegrationEnabled);
        configuration.set(PORTAL_BLOCK_PHYSICS_ENABLED_KEY, this.portalBlockPhysicsEnabled);
    }

    //endregion
}
