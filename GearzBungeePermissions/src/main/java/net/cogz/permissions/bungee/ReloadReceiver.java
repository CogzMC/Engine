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

package net.cogz.permissions.bungee;

import net.tbnr.gearz.command.NetCommandHandler;

import java.util.HashMap;

/**
 * Handles the reloading of
 * permissions when the NetCommand
 * is receieved from the Bukkit
 * end, where the command is initially
 * executed from.
 *
 * <p>
 * Latest Change: Created
 * <p>
 *
 * @author Jake
 * @since Unknown
 */
public class ReloadReceiver {
    @NetCommandHandler(args = {"reload"}, name = "permissions")
    public void permissions(HashMap<String, Object> args) {
        GearzBungeePermissions.getInstance().getPermsManager().reload();
    }
}
