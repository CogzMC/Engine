package net.tbnr.gearz.modules;

import com.mongodb.BasicDBList;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.tbnr.gearz.GearzBungee;
import net.tbnr.util.bungee.command.TCommand;
import net.tbnr.util.bungee.command.TCommandHandler;
import net.tbnr.util.bungee.command.TCommandSender;
import net.tbnr.util.bungee.command.TCommandStatus;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Joey
 * Date: 10/28/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unused")
public class AnnouncerModule implements Runnable, TCommandHandler {
    @Getter
    private List<Announcement> announcements;
    private boolean running;
    private ScheduledTask thisSchedule = null;
    private int interval_seconds = 0;
    private int current;

    public AnnouncerModule(List<Announcement> announcementList, int interval) {
        this.announcements = announcementList;
        this.interval_seconds = interval;
    }

    @TCommand(aliases = {"announcer"}, usage = "/announcer", senders = {TCommandSender.Player, TCommandSender.Console}, permission = "gearz.announcer", name = "announcer")
    @SuppressWarnings("unused")
    public TCommandStatus announcer(CommandSender sender, TCommandSender type, TCommand meta, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-help"));
            return TCommandStatus.INVALID_ARGS;
        }
        if (args[0].equalsIgnoreCase("list")) {
            for (int x = 0; x < announcements.size(); x++) {
                sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-list", false, false, new String[]{"<num>", x + ""}, new String[]{"<announcement>", announcements.get(x).getColoredText()}));
            }
            return TCommandStatus.SUCCESSFUL;
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-badargs"));
                return TCommandStatus.INVALID_ARGS;
            }

            BasicDBList basicDBList = (BasicDBList) GearzBungee.getInstance().getConfig().get("announcements");
            if (basicDBList == null) {
                basicDBList = new BasicDBList();
            }
            basicDBList.add(compile(args, 1, args.length));
            announcements.add(new Announcement(compile(args, 1, args.length)));

            GearzBungee.getInstance().getConfig().put("announcements", basicDBList);
            sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-add"));
        } else if (args[0].equalsIgnoreCase("remove")) {
            int num;
            try {
                num = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-notanum"));
                return TCommandStatus.INVALID_ARGS;
            }

            BasicDBList basicDBList = (BasicDBList) GearzBungee.getInstance().getConfig().get("announcements");
            if (basicDBList == null) {
                basicDBList = new BasicDBList();
            } else {
                basicDBList.remove(num);
            }

            announcements.remove(num);
            GearzBungee.getInstance().getConfig().put("announcements", basicDBList);
            sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-remove", false, false, new String[]{"<num>", num + ""}));

            return TCommandStatus.SUCCESSFUL;
        } else if (args[0].equalsIgnoreCase("interval")) {
            if (args[1] != null) {
                int num;
                try {
                    num = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-notanum"));
                    return TCommandStatus.INVALID_ARGS;
                }

                GearzBungee.getInstance().getConfig().put("announcements_interval", num);

                interval_seconds = num;

                sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-interval-set", false, false, new String[]{"<num>", num + ""}));
                return TCommandStatus.SUCCESSFUL;
            } else {
                Integer seconds = (Integer) GearzBungee.getInstance().getConfig().get("announcements_interval");

                sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-interval-get", false, false, new String[]{"<num>", seconds + ""}));
                return TCommandStatus.SUCCESSFUL;

            }
        } else if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-help"));
            return TCommandStatus.SUCCESSFUL;
        } else if (args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("start")) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-restart"));
            reschedule();
        } else if (args[0].equalsIgnoreCase("stop")) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("announcer-stop"));
            cancel();
        }

        return TCommandStatus.SUCCESSFUL;
    }

    public static String compile(String[] args, int min, int max) {

        StringBuilder builder = new StringBuilder();

        for (int i = min; i < args.length; i++) {
            builder.append(args[i]);
            if (i == max) return builder.toString();
            builder.append(" ");
        }

        return builder.toString();

    }

    @Override
    public void handleCommandStatus(TCommandStatus status, CommandSender sender, TCommandSender senderType) {
        GearzBungee.handleCommandStatus(status, sender);
    }

    public static class Announcement {
        private String rawText;

        public Announcement(String text) {
            this.rawText = text;
        }

        public String getColoredText() {
            return ChatColor.translateAlternateColorCodes('&', rawText);
        }

        public String getStringFor(ProxiedPlayer player) {
            return this.rawText.replaceAll("%player%", player.getName());
        }
    }

    public void start() {
        this.thisSchedule = ProxyServer.getInstance().getScheduler().schedule(GearzBungee.getInstance(), this, interval_seconds, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
            announce(proxiedPlayer);
        }
    }

    private void reschedule() {
        cancel();
        start();
    }

    private void cancel() {
        if (this.thisSchedule != null) ProxyServer.getInstance().getScheduler().cancel(thisSchedule);

    }


    public void announce(ProxiedPlayer proxiedPlayer) {
        proxiedPlayer.sendMessage(GearzBungee.getInstance().getFormat("prefix", false, true) + ChatColor.translateAlternateColorCodes('&', announcements.get(this.current).getStringFor(proxiedPlayer)));
        this.current++;
        if (this.current == announcements.size() - 1) this.current = 0;
    }
}
