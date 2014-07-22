/*
 * Copyright (c) 2014.
 * CogzMC LLC USA
 * All Right reserved
 *
 * This software is the confidential and proprietary information of Cogz Development, LLC.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Cogz LLC.
 */

package net.cogz.permissions.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.tbnr.gearz.GearzBungee;
import net.tbnr.gearz.activerecord.GModel;
import net.tbnr.util.ErrorHandler;
import net.tbnr.util.TPluginBungee;

import java.util.concurrent.TimeUnit;

/**
 * Bungee Plugin implementation of the
 * Gearz permissions API
 * <p/>
 * <p/>
 * Latest Change: Run reload ASync
 * <p/>
 *
 * @author Jake
 * @since Unknown
 */
@SuppressWarnings("FieldCanBeLocal")
public final class GearzBungeePermissions extends TPluginBungee {
    @Getter
    private static GearzBungeePermissions instance;
    @Getter
    private PermissionsManager permsManager;

    @Override
    protected void start() {
        GearzBungeePermissions.instance = this;
        GModel.setDefaultDatabase(GearzBungee.getInstance().getMongoDB());
        this.permsManager = new PermissionsManager();
        registerEvents(permsManager);
        GearzBungee.getInstance().getDispatch().registerNetCommands(new ReloadReceiver());
        ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                try {
                    GearzBungeePermissions.getInstance().getPermsManager().reload();
                } catch (Exception ex) {
                    ErrorHandler.reportError("permissions", ex);
                    ex.printStackTrace();
                    GearzBungeePermissions.getInstance().getLogger().severe(ex.getMessage());
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void stop() {
    }
}
