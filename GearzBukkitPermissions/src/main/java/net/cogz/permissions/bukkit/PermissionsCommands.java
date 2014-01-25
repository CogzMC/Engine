package net.cogz.permissions.bukkit;

import net.cogz.permissions.PermGroup;
import net.tbnr.gearz.Gearz;
import net.tbnr.util.command.TCommand;
import net.tbnr.util.command.TCommandHandler;
import net.tbnr.util.command.TCommandSender;
import net.tbnr.util.command.TCommandStatus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Jake on 1/24/14.
 */
public class PermissionsCommands implements TCommandHandler {

    /**
     * group admin remove Derpsd.lol
     */
    @TCommand(
            name = "group",
            usage = "/group <args...>",
            permission = "gearz.permissions.group",
            senders = {TCommandSender.Player, TCommandSender.Console})
    @SuppressWarnings("unused")
    public TCommandStatus group(CommandSender sender, TCommandSender type, TCommand meta, Command command, String[] args) {
        if (args.length < 1) {
            return TCommandStatus.FEW_ARGS;
        }
        PermissionsManager permsManager = GearzBukkitPermissions.getInstance().getPermsManager();
        PermGroup group = permsManager.getGroup(args[0]);
        switch (args[1]) {
            case "create":
                if (args.length < 2 || args.length > 3) return TCommandStatus.INVALID_ARGS;
                boolean defau = false;
                if (args.length == 3) {
                    defau = Boolean.parseBoolean(args[2]);
                }
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("created-group", false, new String[]{"<group>", args[0]}, new String[]{"<default>", defau + ""}));
                permsManager.createGroup(args[0], defau);
                return TCommandStatus.SUCCESSFUL;
            case "delete":
                if (args.length != 2) return TCommandStatus.INVALID_ARGS;
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("deleted-group", false, new String[]{"<group>", args[0]}));
                permsManager.deleteGroup(args[0]);
                return TCommandStatus.SUCCESSFUL;
            case "set":
                if (args.length < 3 || args.length > 4) return TCommandStatus.INVALID_ARGS;
                if (group == null) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("null-group", false));
                    return TCommandStatus.SUCCESSFUL;
                }
                boolean value = true;
                if (args.length == 4) {
                    value = Boolean.parseBoolean(args[3]);
                }
                String permission = args[2];
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-group-perm", false, new String[]{"<permission>", permission}, new String[]{"<group>", args[0]}, new String[]{"<value>", value + ""}));
                group.addPermission(permission, value);
                break;
            case "remove":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                if (group == null) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("null-group", false));
                    return TCommandStatus.SUCCESSFUL;
                }
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("remove-group-perm", false, new String[]{"<oermission>", args[2]}, new String[]{"<group>", args[0]}));
                group.removePermission(args[2]);
                return TCommandStatus.SUCCESSFUL;
            case "check":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                if (group == null) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("null-group", false));
                    return TCommandStatus.SUCCESSFUL;
                }
                if (group.hasPermission(args[2])) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("check-group-perm-value", false, new String[]{"<group>", args[0]}, new String[]{"<permission>", args[2]}, new String[]{"<value>", true + ""}));
                } else {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("check-group-perm-invalid", false, new String[]{"<group>", args[0]}, new String[]{"<permission>", args[2]}));
                }
                return TCommandStatus.SUCCESSFUL;
        }
        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(
            name = "permissions",
            usage = "/permissions <args...>",
            permission = "gearz.permissions",
            senders = {TCommandSender.Player, TCommandSender.Console})
    @SuppressWarnings("unused")
    public TCommandStatus command(CommandSender sender, TCommandSender type, TCommand meta, Command command, String[] args) {
        if (args.length < 2) {
            return TCommandStatus.INVALID_ARGS;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("reload"));
            GearzBukkitPermissions.getInstance().getPermsManager().reload();
            return TCommandStatus.SUCCESSFUL;
        }
        return TCommandStatus.SUCCESSFUL;
    }

    @Override
    public void handleCommandStatus(TCommandStatus status, CommandSender sender, TCommandSender senderType) {
        Gearz.getInstance().handleCommandStatus(status, sender, senderType);
    }


}
