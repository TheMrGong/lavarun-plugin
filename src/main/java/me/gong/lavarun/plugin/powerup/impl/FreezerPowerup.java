package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.DeathEvent;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.TimeUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class FreezerPowerup extends Powerup {

    public static int FREEZE_TIME = 4;//seconds

    @Override
    public ItemStack getItem(Team team) {
        ItemStack ret = new ItemStack(Material.ICE);
        ItemMeta m = ret.getItemMeta();
        m.setDisplayName(ChatColor.AQUA+getName());
        ret.setItemMeta(m);
        return ret;
    }

    @Override
    public int getMaxUses() {
        return 1;
    }

    @Override
    public String getName() {
        return "Freezer";
    }

    @Override
    public int getCost() {
        return 60;
    }

    @Override
    public String[] getHelp() {

        return new String[] {"Hit a player on the enemy side to freeze them",
        "The player will be frozen for &e"+ TimeUtils.convertToString(FREEZE_TIME * 1000),
        "Only works on players not on their teams side."};
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if(ev.getPlayer().hasPotionEffect(PotionEffectType.SLOW)) ev.setCancelled(true);
    }

    @EventHandler
    public void onDeath(DeathEvent ev) {
        ev.getPlayer().removePotionEffect(PotionEffectType.SLOW);
        ev.getPlayer().removePotionEffect(PotionEffectType.JUMP);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent ev) {
        if(ev.getDamager() instanceof Player && ev.getEntity() instanceof Player) {
            Player abuser = (Player) ev.getDamager(), victim = (Player) ev.getEntity();
            GameManager gm = InManager.get().getInstance(GameManager.class);
            if(abuser.hasPotionEffect(PotionEffectType.SLOW)) {
                ev.setCancelled(true);
                return;
            }
            if(isSelected(abuser) && gm.getCurrentArena().isPlaying(abuser, false) && gm.getCurrentArena().isPlaying(victim, false) &&
                    gm.getCurrentArena().canBeDamaged(victim, abuser) && !victim.hasPotionEffect(PotionEffectType.SLOW)) {
                freeze(victim, abuser);
                onUse(abuser);
                ev.setCancelled(true);
            }
        }
    }

    public void freeze(Player player, Player by) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * FREEZE_TIME, 6, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * FREEZE_TIME, 128));
        generateTitle(by).sendTo(player);
        Location pos1 = player.getLocation().clone(), pos2 = pos1.clone().add(0, 1, 0);
        playEffect(pos1);
        playEffect(pos2);
    }

    private void playEffect(Location location) {
        location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.ICE);
    }

    private BukkitUtils.Title generateTitle(Player by) {
        return new BukkitUtils.Title(ChatColor.AQUA+"You have been frozen by "+by.getName(), true, 0, 20, 3);
    }
}
