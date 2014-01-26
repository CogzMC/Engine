package net.tbnr.gearz;

import net.tbnr.util.ZipUtil;
import net.tbnr.util.command.TCommand;
import net.tbnr.util.command.TCommandHandler;
import net.tbnr.util.command.TCommandSender;
import net.tbnr.util.command.TCommandStatus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;

/**
 * Created by Jake on 1/26/14.
 */
public class PackageCommands implements TCommandHandler {
    @TCommand(
            name = "package",
            usage = "/package",
            permission = "gearz.mapdev.package",
            senders = {TCommandSender.Player, TCommandSender.Console})
    @SuppressWarnings("unused")
    public TCommandStatus command(CommandSender sender, TCommandSender type, TCommand meta, Command command, String[] args) {
        if (args.length != 1) return TCommandStatus.INVALID_ARGS;
        String world = args[0];
        String zip = args[0].replace(" ", "_");
        try {
            ZipUtil.zipFolder(Bukkit.getWorldContainer() + File.separator + world, zip);
        } catch (Exception e) {
            sender.sendMessage(GearzMapDev.getInstance().getFormat("formats.error-zipping"));
            return TCommandStatus.SUCCESSFUL;
        }
        sender.sendMessage(GearzMapDev.getInstance().getFormat("formats.zipping-successful"));
        return TCommandStatus.SUCCESSFUL;
    }

    @Override
    public void handleCommandStatus(TCommandStatus status, CommandSender sender, TCommandSender senderType) {
        Gearz.getInstance().handleCommandStatus(status, sender, senderType);
    }
}