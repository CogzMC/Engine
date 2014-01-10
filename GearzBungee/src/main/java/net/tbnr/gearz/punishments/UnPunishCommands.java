package net.tbnr.gearz.punishments;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.tbnr.gearz.GearzBungee;
import net.tbnr.gearz.player.bungee.GearzPlayer;
import net.tbnr.util.bungee.command.TCommand;
import net.tbnr.util.bungee.command.TCommandHandler;
import net.tbnr.util.bungee.command.TCommandSender;
import net.tbnr.util.bungee.command.TCommandStatus;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by jake on 1/4/14.
 */
public class UnPunishCommands implements TCommandHandler {
    public SimpleDateFormat readable = new SimpleDateFormat("MM/dd/yyyy");
    @TCommand(
            aliases = {"l", "search", "check"},
            name = "lookup",
            usage = "/lookup <player>",
            senders = {TCommandSender.Player, TCommandSender.Console},
            permission = "gearz.punish.lookup")
    @SuppressWarnings("unused")
    public TCommandStatus lookup(CommandSender sender, TCommandSender type, TCommand command, String[] args) {
        if (args.length != 1) {
            return TCommandStatus.INVALID_ARGS;
        }

        if (args[0].matches(".*([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}).*")) {
            DBObject punishment = GearzBungee.getInstance().getIpBanHandler().getBanObject(args[0]);
            if (punishment == null) {
                sender.sendMessage(GearzBungee.getInstance().getFormat("no-punishment", false, false));
                return TCommandStatus.SUCCESSFUL;
            }
            Date date = (Date) punishment.get("time");
            String issuer;
            if (punishment.get("issuer") instanceof String) {
                issuer = "CONSOLE";
            } else {
                GearzPlayer staff;
                try {
                    staff = GearzPlayer.getById((ObjectId) punishment.get("issuer"));
                    issuer = staff.getName();
                } catch (GearzPlayer.PlayerNotFoundException e) {
                    issuer = "null";
                }
            }
            String action = "ip banned";
            sender.sendMessage(GearzBungee.getInstance().getFormat("lookup-format", false, false, new String[]{"<date>", readable.format(date)}, new String[]{"<reason>", (String) punishment.get("reason")}, new String[]{"<action>", action}, new String[]{"<issuer>", issuer}));
        }

        GearzPlayer gearzTarget;
        try {
            gearzTarget = new GearzPlayer(args[0]);
        } catch (GearzPlayer.PlayerNotFoundException e) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("null-player", false, false));
            return TCommandStatus.SUCCESSFUL;
        }

        List<BasicDBObject> punishments = gearzTarget.getPunishments();
        if (punishments == null) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("no-punishment", false, false));
            return TCommandStatus.SUCCESSFUL;
        }
        sender.sendMessage(GearzBungee.getInstance().getFormat("lookup-header", false, false, new String[]{"<player>", gearzTarget.getName()}));
        int x = 0;
        for (BasicDBObject punishment : punishments) {
            Date date = punishment.getDate("time");
            String issuer;
            if (punishment.get("issuer") instanceof String) {
                issuer = "CONSOLE";
            } else {
                GearzPlayer staff;
                try {
                    staff = GearzPlayer.getById(punishment.getObjectId("issuer"));
                    issuer = staff.getName();
                } catch (GearzPlayer.PlayerNotFoundException e) {
                    issuer = "null";
                }
            }
            String action = PunishmentType.valueOf(punishment.getString("type")).getAction();
            sender.sendMessage(GearzBungee.getInstance().getFormat("lookup-format", false, false, new String[]{"<date>", readable.format(date)}, new String[]{"<reason>", punishment.getString("reason")}, new String[]{"<action>", action}, new String[]{"<issuer>", issuer}, new String[]{"<id>", x + ""}));
            x++;
        }
        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(
            aliases = {"gunban"},
            name = "unban",
            usage = "/unban <player>",
            senders = {TCommandSender.Player, TCommandSender.Console},
            permission = "gearz.punish.unban")
    @SuppressWarnings("unused")
    public TCommandStatus unban(CommandSender sender, TCommandSender type, TCommand command, String[] args) {
        if (args.length != 1) {
            return TCommandStatus.INVALID_ARGS;
        }

        String target = args[0];
        GearzPlayer gearzPlayer;
        try {
            gearzPlayer = new GearzPlayer(target);
        } catch (GearzPlayer.PlayerNotFoundException e) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("null-player", false, false));
            return TCommandStatus.SUCCESSFUL;
        }

        if (gearzPlayer.getActiveBan() == null) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("not-banned", false, false));
            return TCommandStatus.SUCCESSFUL;
        }

        gearzPlayer.unban();
        sender.sendMessage(GearzBungee.getInstance().getFormat("unbanned-player", false, false, new String[]{"<player>", target}));
        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(
            aliases = {"gunmute"},
            name = "unmute",
            usage = "/unmute <player>",
            senders = {TCommandSender.Player, TCommandSender.Console},
            permission = "gearz.punish.unmute")
    @SuppressWarnings("unused")
    public TCommandStatus unmute(CommandSender sender, TCommandSender type, TCommand command, String[] args) {
        if (args.length != 1) {
            return TCommandStatus.INVALID_ARGS;
        }

        String target = args[0];
        GearzPlayer gearzPlayer;
        try {
            gearzPlayer = new GearzPlayer(target);
        } catch (GearzPlayer.PlayerNotFoundException e) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("null-player", false, false));
            return TCommandStatus.SUCCESSFUL;
        }

        if (gearzPlayer.getActiveMute() == null) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("not-muted", false, false));
            return TCommandStatus.SUCCESSFUL;
        }

        gearzPlayer.unMute();
        sender.sendMessage(GearzBungee.getInstance().getFormat("unmuted-player", false, false, new String[]{"<player>", target}));

        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(
            aliases = {"ipunban", "unipban"},
            name = "unbanip",
            usage = "/unbanip <ip>",
            senders = {TCommandSender.Player, TCommandSender.Console},
            permission = "gearz.punish.unbanip")
    @SuppressWarnings("unused")
    public TCommandStatus unbanip(CommandSender sender, TCommandSender type, TCommand command, String[] args) {
        if (args.length != 1) {
            return TCommandStatus.INVALID_ARGS;
        }

        String ip = args[0];
        DBObject ipBan = GearzBungee.getInstance().getIpBanHandler().getBanObject(ip);

        if (ipBan != null) {
            GearzBungee.getInstance().getIpBanHandler().remove(ip);
        } else {
            sender.sendMessage(GearzBungee.getInstance().getFormat("not-ipbanned", false, false));
            return TCommandStatus.SUCCESSFUL;
        }

        sender.sendMessage(GearzBungee.getInstance().getFormat("unbanned-ip", false, false, new String[]{"<target>", ip}));

        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(
            aliases = {"a"},
            name = "appeal",
            usage = "/appeal <player> <id>",
            senders = {TCommandSender.Player, TCommandSender.Console},
            permission = "gearz.punish.appeal")
    @SuppressWarnings("unused")
    public TCommandStatus appeal(CommandSender sender, TCommandSender type, TCommand command, String[] args) {
        if (args.length != 2) {
            return TCommandStatus.INVALID_ARGS;
        }

        String target = args[0];
        Integer id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return TCommandStatus.INVALID_ARGS;
        }

        GearzPlayer gearzTarget;
        try {
            gearzTarget = new GearzPlayer(target);
        } catch (GearzPlayer.PlayerNotFoundException e) {
            return TCommandStatus.INVALID_ARGS;
        }

        List<BasicDBObject> punishments = gearzTarget.getPunishments();
        if (punishments == null) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("null-punishment", false, false));
            return TCommandStatus.SUCCESSFUL;
        }
        BasicDBObject toAppeal = punishments.get(id);
        if (toAppeal == null) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("null-punishment", false, false));
            return TCommandStatus.SUCCESSFUL;
        }
        gearzTarget.appealPunishment(toAppeal);
        ProxyServer.getInstance().getLogger().info(sender.getName() + " appealed " + target + "'s " + id + " punishment.");
        sender.sendMessage(GearzBungee.getInstance().getFormat("appeal-punishment", false, false, new String[]{"<target>", target}));
        return TCommandStatus.SUCCESSFUL;
    }

    @Override
    public void handleCommandStatus(TCommandStatus status, CommandSender sender, TCommandSender senderType) {
        GearzBungee.handleCommandStatus(status, sender);
    }
}
