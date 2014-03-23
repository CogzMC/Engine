/*
 * Copyright (c) 2014.
 * Cogz Development LLC USA
 * All Right reserved
 *
 * This software is the confidential and proprietary information of Cogz Development, LLC.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Cogz LLC.
 */

package net.tbnr.gearz.chat.channels;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.tbnr.gearz.GearzBungee;
import net.tbnr.gearz.player.bungee.GearzPlayer;
import net.tbnr.gearz.player.bungee.GearzPlayerManager;
import net.tbnr.util.bungee.command.TCommand;
import net.tbnr.util.bungee.command.TCommandHandler;
import net.tbnr.util.bungee.command.TCommandSender;
import net.tbnr.util.bungee.command.TCommandStatus;

/**
 * Created by Jake on 1/18/14.
 *
 * Purpose Of File:
 *
 * Latest Change:
 */
public class ChannelCommand implements TCommandHandler {

    @TCommand(name = "channel", aliases = {"chan"}, usage = "/channel <channel>", permission = "gearz.channels.command.switch", senders = {TCommandSender.Player})
    public TCommandStatus channel(CommandSender sender, TCommandSender type, TCommand meta, String[] args) {
        if (args.length != 1)  return TCommandStatus.INVALID_ARGS;

        Channel channel = GearzBungee.getInstance().getChannelManager().getChannelByName(args[0].toLowerCase());
        if (channel == null) {
            return TCommandStatus.INVALID_ARGS;
        }

        GearzPlayer target = GearzPlayerManager.getGearzPlayer((ProxiedPlayer) sender);
        if (channel.hasPermission() && !target.getProxiedPlayer().hasPermission(channel.getListeningPermission())) {
            return TCommandStatus.PERMISSIONS;
        }
        GearzBungee.getInstance().getChannelManager().setChannel((ProxiedPlayer) sender, channel);
        sender.sendMessage(GearzBungee.getInstance().getFormat("switched", false, false, new String[]{"<channel>", channel.getName()}));
        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(name = "channels", usage = "/channels", permission = "gearz.channels.command.list", senders = {TCommandSender.Player})
    public TCommandStatus channels(CommandSender sender, TCommandSender type, TCommand meta, String[] args) {
        if (args.length != 0) {
            return TCommandStatus.INVALID_ARGS;
        }

        sender.sendMessage(GearzBungee.getInstance().getFormat("channels", false));
        for (Channel channel : GearzBungee.getInstance().getChannelManager().getChannels()) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("channel", false, false, new String[]{"<channel>", channel.getName()}));
        }

        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(name = "modbroadcast", aliases = {"mb"}, usage = "/mb <message>", permission = "gearz.modbroadcast", senders = {TCommandSender.Player})
    public TCommandStatus modbroadcast(CommandSender sender, TCommandSender type, TCommand meta, String[] args) {
        if (args.length == 0) {
            Channel channel = GearzBungee.getInstance().getChannelManager().getChannelByName("staff");
            if (channel == null) {
                return TCommandStatus.INVALID_ARGS;
            }
            try {
                GearzBungee.getInstance().getChannelManager().setChannel((ProxiedPlayer) sender, channel);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(GearzBungee.getInstance().getFormat("already-on-channel"));
            }
            sender.sendMessage(GearzBungee.getInstance().getFormat("switched", false, false, new String[]{"<channel>", channel.getName()}));
        }
        return TCommandStatus.SUCCESSFUL;
    }

    @TCommand(name = "default", aliases = {"def", "d"}, usage = "/default <message>", permission = "", senders = {TCommandSender.Player})
    public TCommandStatus def(CommandSender sender, TCommandSender type, TCommand meta, String[] args) {
        if (args.length == 0) {
            Channel channel = GearzBungee.getInstance().getChannelManager().getChannelByName("default");
            if (channel == null) {
                return TCommandStatus.INVALID_ARGS;
            }
            try {
                GearzBungee.getInstance().getChannelManager().setChannel((ProxiedPlayer) sender, channel);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(GearzBungee.getInstance().getFormat("already-on-channel"));
            }
            sender.sendMessage(GearzBungee.getInstance().getFormat("switched", false, false, new String[]{"<channel>", channel.getName()}));
        }
        return TCommandStatus.SUCCESSFUL;
    }

    @Override
    public void handleCommandStatus(TCommandStatus status, CommandSender sender, TCommandSender senderType) {
        GearzBungee.handleCommandStatus(status, sender);
    }
}
