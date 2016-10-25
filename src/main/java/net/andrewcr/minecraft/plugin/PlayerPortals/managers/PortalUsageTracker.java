package net.andrewcr.minecraft.plugin.PlayerPortals.managers;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.WeakHashMap;

class PortalUsageTracker {
    private class EntityPortalRecord {
        Location lastPortalContact;
        long lastTime;

        public EntityPortalRecord(Location portalLocation) {
            this.lastPortalContact = portalLocation;
            this.lastTime = System.currentTimeMillis();
        }
    }

    private Map<Entity, EntityPortalRecord> portalRecords;

    public PortalUsageTracker() {
        // Use a WeakHashMap to ensure that our keys don't keep entities from being GC'ed
        this.portalRecords = new WeakHashMap<>();
    }

    public boolean canEnterPortal(Entity entity) {
        if (this.portalRecords.containsKey(entity)) {
            EntityPortalRecord record = this.portalRecords.get(entity);

            // Need to wait a minimum time between teleports
            if (System.currentTimeMillis() - record.lastTime < 1000) {
                return false;
            }

            // Need to break contact with the last portal touched before teleporting again
            if (record.lastPortalContact != null) {
                return false;
            }
        }

        return true;
    }

    public void setReferencePoint(Entity entity, Location portalLocation) {
        if (this.portalRecords.containsKey(entity)) {
            this.portalRecords.get(entity).lastPortalContact = portalLocation;
        } else {
            EntityPortalRecord record = new EntityPortalRecord(portalLocation);
            this.portalRecords.put(entity, record);
        }
    }

    public void updateEntityLocation(Entity entity) {
        if (!this.portalRecords.containsKey(entity)) {
            // Not an entity we care about
            return;
        }

        EntityPortalRecord record = this.portalRecords.get(entity);
        if (record.lastPortalContact == null) {
            // Entity moved away from the portal already
            return;
        }

        if (entity.getLocation().distance(record.lastPortalContact) > 2) {
            // Entity has moved a sufficient distance from the last teleport location, so clear it
            record.lastPortalContact = null;
        }
    }
}
