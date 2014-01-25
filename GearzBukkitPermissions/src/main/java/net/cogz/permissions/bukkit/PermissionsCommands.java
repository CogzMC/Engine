package net.cogz.permissions.bukkit;

import net.cogz.permissions.PermGroup;
import net.cogz.permissions.PermPlayer;
import net.tbnr.gearz.Gearz;
import net.tbnr.util.command.TCommand;
import net.tbnr.util.command.TCommandHandler;
import net.tbnr.util.command.TCommandSender;
import net.tbnr.util.command.TCommandStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by Jake on 1/24/14.
 */
public class PermissionsCommands implements TCommandHandler {

    @TCommand(
            name = "player",
            usage = "/player",
            permission = "gearz.permissions.player",
            senders = {TCommandSender.Player, TCommandSender.Console})
    @SuppressWarnings("unused")
    public TCommandStatus player(CommandSender sender, TCommandSender type, TCommand meta, Command command, String[] args) {
        PermissionsManager permsManager = GearzBukkitPermissions.getInstance().getPermsManager();
        PermPlayer player = permsManager.getPlayer(args[0]);
        switch (args[1]) {
            case "set":
                if (args.length < 3 || args.length > 4) return TCommandStatus.INVALID_ARGS;
                if (player == null) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("null-player", false));
                    return TCommandStatus.SUCCESSFUL;
                }
                boolean value = true;
                if (args.length == 4) {
                    value = Boolean.parseBoolean(args[3]);
                }
                String permission = args[2];
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-player-perm", false, new String[]{"<permission>", permission}, new String[]{"<player>", args[0]}, new String[]{"<value>", value + ""}));
                player.addPermission(permission, value);
                break;
            case "remove":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                if (player == null) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("null-player", false));
                    return TCommandStatus.SUCCESSFUL;
                }
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("remove-player-perm", false, new String[]{"<oermission>", args[2]}, new String[]{"<player>", args[0]}));
                player.removePermission(args[2]);
                return TCommandStatus.SUCCESSFUL;
            case "check":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                if (player == null) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("null-player", false));
                    return TCommandStatus.SUCCESSFUL;
                }
                if (player.hasPermission(args[2])) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("check-player-perm-value", false, new String[]{"<player>", args[0]}, new String[]{"<permission>", args[2]}, new String[]{"<value>", true + ""}));
                } else {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("check-player-perm-invalid", false, new String[]{"<player>", args[0]}, new String[]{"<permission>", args[2]}));
                }
                return TCommandStatus.SUCCESSFUL;
            case "perms":
            case "show":
            case "permissions":
                if (args.length != 2) return TCommandStatus.INVALID_ARGS;
                for (String perm : player.getPermissions()) {
                    String[] s = perm.split(",");
                    sender.sendMessage(s[0] + " : " + s[1]);
                }
                return TCommandStatus.SUCCESSFUL;
            case "addgroup":
            case "setgroup":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                PermGroup group = permsManager.getGroup(args[2]);
                if (group == null) {
                    sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("null-group", false));
                    return TCommandStatus.SUCCESSFUL;
                }
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("added-player-group", false, new String[]{"<player>", args[0]}, new String[]{"<group>", args[2]}));
                player.addPlayerToGroup(group);
                return TCommandStatus.SUCCESSFUL;
            case "prefix":
                if (args.length < 3) return TCommandStatus.INVALID_ARGS;
                String prefix;
                prefix = Gearz.getInstance().compile(args, 2, args.length);
                player.prefix = prefix;
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-prefix", true, new String[]{"<prefix>", prefix}));
                player.save();
                return TCommandStatus.SUCCESSFUL;
            case "suffix":
                if (args.length < 3) return TCommandStatus.INVALID_ARGS;
                String suffix;
                suffix = Gearz.getInstance().compile(args, 2, args.length);
                player.suffix = suffix;
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-suffix", true, new String[]{"<prefix>", suffix}));
                player.save();
                return TCommandStatus.SUCCESSFUL;
            case "tabcolor":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                player.tabColor = args[2];
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-tab-color", true, new String[]{"<color>", args[2]}));
                player.save();
                return TCommandStatus.SUCCESSFUL;
            case "namecolor":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                player.nameColor = args[2];
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-tab-prefix", true, new String[]{"<color>", args[2]}));
                player.save();
                return TCommandStatus.SUCCESSFUL;
            default:
                return TCommandStatus.INVALID_ARGS;
        }
        return TCommandStatus.SUCCESSFUL;
    }

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
            case "perms":
            case "show":
            case "permissions":
                if (args.length != 2) return TCommandStatus.INVALID_ARGS;
                for (String perm : group.getPermissions()) {
                    String[] s = perm.split(",");
                    sender.sendMessage(s[0] + " : " + s[1]);
                }
                return TCommandStatus.SUCCESSFUL;
            case "prefix":
                if (args.length < 3) return TCommandStatus.INVALID_ARGS;
                String prefix;
                prefix = Gearz.getInstance().compile(args, 2, args.length);
                group.prefix = prefix;
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-prefix", true, new String[]{"<prefix>", prefix}));
                group.save();
                return TCommandStatus.SUCCESSFUL;
            case "suffix":
                if (args.length < 3) return TCommandStatus.INVALID_ARGS;
                String suffix;
                suffix = Gearz.getInstance().compile(args, 2, args.length);
                group.suffix = suffix;
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-suffix", true, new String[]{"<prefix>", suffix}));
                group.save();
                return TCommandStatus.SUCCESSFUL;
            case "tabcolor":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                group.tabColor = args[2];
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-tab-color", true, new String[]{"<color>", args[2]}));
                group.save();
                return TCommandStatus.SUCCESSFUL;
            case "namecolor":
                if (args.length != 3) return TCommandStatus.INVALID_ARGS;
                group.nameColor = args[2];
                sender.sendMessage(GearzBukkitPermissions.getInstance().getFormat("set-tab-prefix", true, new String[]{"<color>", args[2]}));
                group.save();
                return TCommandStatus.SUCCESSFUL;
            default:
                return TCommandStatus.INVALID_ARGS;
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
