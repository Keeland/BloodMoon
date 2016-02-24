package uk.co.jacekk.bukkit.bloodmoon.feature.world;

import net.minecraft.server.v1_8_R3.EnumDifficulty;
//import net.minecraft.server.v1_8_R2.EnumGamemode;
import net.minecraft.server.v1_8_R3.PacketPlayOutRespawn;
import net.minecraft.server.v1_8_R3.WorldSettings;
import net.minecraft.server.v1_8_R3.WorldType;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import uk.co.jacekk.bukkit.baseplugin.event.BaseListener;
import uk.co.jacekk.bukkit.bloodmoon.BloodMoon;
import uk.co.jacekk.bukkit.bloodmoon.Feature;
import uk.co.jacekk.bukkit.bloodmoon.event.BloodMoonEndEvent;
import uk.co.jacekk.bukkit.bloodmoon.event.BloodMoonStartEvent;

public class NetherSkyListener extends BaseListener<BloodMoon> {

    public NetherSkyListener(BloodMoon plugin) {
        super(plugin);
    }

    private void sendWorldEnvironment(Player player, Environment environment) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        CraftWorld world = (CraftWorld) player.getWorld();
        Location location = player.getLocation();

        PacketPlayOutRespawn packet = new PacketPlayOutRespawn(environment.getId(), EnumDifficulty.getById(world.getDifficulty().getValue()), WorldType.NORMAL, WorldSettings.EnumGamemode.getById(player.getGameMode().getValue()));

        craftPlayer.getHandle().playerConnection.sendPacket(packet);

        int viewDistance = plugin.getServer().getViewDistance();

        int xMin = location.getChunk().getX() - viewDistance;
        int xMax = location.getChunk().getX() + viewDistance;
        int zMin = location.getChunk().getZ() - viewDistance;
        int zMax = location.getChunk().getZ() + viewDistance;

        for (int x = xMin; x < xMax; ++x) {
            for (int z = zMin; z < zMax; ++z) {
                world.refreshChunk(x, z);
            }
        }

        player.updateInventory(); Needed to fix cilent empty inventory glitch
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onStart(BloodMoonStartEvent event) {
        World world = event.getWorld();

        if (plugin.isFeatureEnabled(world, Feature.NETHER_SKY)) {
            for (Player player : world.getPlayers()) {
                this.sendWorldEnvironment(player, Environment.NETHER);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onStop(BloodMoonEndEvent event) {
        World world = event.getWorld();
        Environment environment = world.getEnvironment();

        if (plugin.isFeatureEnabled(world, Feature.NETHER_SKY)) {
            for (Player player : event.getWorld().getPlayers()) {
                this.sendWorldEnvironment(player, environment);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        World world = event.getPlayer().getWorld();

        if (plugin.isActive(world) && plugin.isFeatureEnabled(world, Feature.NETHER_SKY)) {
            this.sendWorldEnvironment(event.getPlayer(), Environment.NETHER);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRespawn(final PlayerRespawnEvent event) {
        World world = event.getPlayer().getWorld();

        if (plugin.isActive(world) && plugin.isFeatureEnabled(world, Feature.NETHER_SKY)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

                @Override
                public void run() {
                    sendWorldEnvironment(event.getPlayer(), Environment.NETHER);
                }

            }, 10);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChangeWorlds(PlayerChangedWorldEvent event) {
        World world = event.getPlayer().getWorld();

        if (plugin.isActive(world) && plugin.isFeatureEnabled(world, Feature.NETHER_SKY)) {
            this.sendWorldEnvironment(event.getPlayer(), Environment.NETHER);
        }
    }

}
