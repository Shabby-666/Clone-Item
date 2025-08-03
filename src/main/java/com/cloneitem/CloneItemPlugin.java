package com.cloneitem;

// 移除未使用的导入语句
// 已移除未使用的导入语句：import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class CloneItemPlugin extends JavaPlugin implements CommandExecutor {

    // 存储玩家最后使用命令的时间
    private final Map<Player, Long> cooldowns = new HashMap<>();
    // 冷却时间（毫秒）10分钟 = 600000毫秒
    private static final long COOLDOWN_TIME = 600000;

    @Override
    public void onEnable() {
        // 注册命令
        getCommand("dupe").setExecutor(this);
        getLogger().info("CloneItem已被启用!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CloneItem已被禁用!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 检查命令发送者是否为玩家
        if (!(sender instanceof Player player)) {
            sender.sendMessage("该命令只能由玩家执行!");
            return true;
        }

        // 检查玩家是否有权限
        if (!player.hasPermission("cloneitem.allow")) {
            player.sendMessage("你没有权限使用该命令!");
            return true;
        }

        // 检查是否有冷却豁免权限
        boolean hasCooldownBypass = player.hasPermission("cloneitem.allow.cooldown");

        // 检查冷却
        if (!hasCooldownBypass) {
            long currentTime = System.currentTimeMillis();
            if (cooldowns.containsKey(player)) {
                long lastUseTime = cooldowns.get(player);
                if (currentTime - lastUseTime < COOLDOWN_TIME) {
                    long remainingCooldown = (COOLDOWN_TIME - (currentTime - lastUseTime)) / 1000;
                    player.sendMessage("命令冷却中! 剩余 " + remainingCooldown + " 秒.");
                    return true;
                }
            }
            // 更新冷却时间
            cooldowns.put(player, currentTime);
        }

        // 获取玩家手持的物品
        ItemStack handItem = player.getInventory().getItemInMainHand();

        // 检查是否手持物品
        if (handItem.isEmpty()) {
            player.sendMessage("你没有手持任何物品!");
            return true;
        }

        // 获取手持物品的数量
        int amount = handItem.getAmount();

        // 创建物品副本
        ItemStack clonedItem = handItem.clone();
        clonedItem.setAmount(amount);

        // 尝试将副本添加到玩家背包
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(clonedItem);
            player.sendMessage("成功克隆 " + amount + " 个 " + handItem.getType().name() + "!");
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), clonedItem);
            player.sendMessage("背包已满! 克隆物品已被扔到地面上.");
        }

        return true;
    }
}