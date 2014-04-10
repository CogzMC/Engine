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

package net.cogz.punishments.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.tbnr.util.FileUtil;
import net.tbnr.util.TPluginBungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Bungee Punishments Plugin
 */
public class GearzBungeePunishments extends TPluginBungee {
    @Getter private Properties strings;
    @Getter public static GearzBungeePunishments instance;

    @Override
    protected void start() {
        GearzBungeePunishments.instance = this;
        if (!new File(getDataFolder() + File.separator + "strings.properties").exists()) saveStrings();
        this.strings = new Properties();
        reloadStrings();
        PunishmentManager punishmentManager = new PunishmentManager();
        punishmentManager.database = punishmentManager.getDB();
        registerCommandHandler(new UnPunishCommands(punishmentManager));
        registerCommandHandler(new PunishmentCommands(punishmentManager));
        registerEvents(punishmentManager);
        punishmentManager.loadIpBans();
    }

    @Override
    protected void stop() { }

    public void reloadStrings() {
        try {
            this.strings.load(new FileInputStream(getDataFolder() + File.separator + "strings.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveStrings() {
        FileUtil.writeEmbeddedResourceToLocalFile("strings.properties", new File(getDataFolder() + File.separator + "strings.properties"));
    }

    public String getFormat(String key, boolean prefix, boolean color, String[]... datas) {
        if (this.strings.getProperty(key) == null) {
            return key;
        }
        String property = this.strings.getProperty(key);
        if (prefix)
            property = ChatColor.translateAlternateColorCodes('&', this.strings.getProperty("prefix")) + property;
        property = ChatColor.translateAlternateColorCodes('&', property);
        if (datas == null) return property;
        for (String[] data : datas) {
            if (data.length != 2) continue;
            property = property.replaceAll(data[0], data[1]);
        }
        if (color) property = ChatColor.translateAlternateColorCodes('&', property);
        return property;
    }

    public String getFormat(String key, boolean prefix, boolean color) {
        return getFormat(key, prefix, color, null);
    }

}
