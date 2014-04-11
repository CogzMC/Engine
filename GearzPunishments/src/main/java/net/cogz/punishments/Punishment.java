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

package net.cogz.punishments;

import com.mongodb.DB;
import com.mongodb.DBObject;
import lombok.ToString;
import net.tbnr.gearz.activerecord.BasicField;
import net.tbnr.gearz.activerecord.GModel;

import java.util.Date;

/**
 * Stores data about a player's Punishment
 */
@ToString
public class Punishment extends GModel {
    public @BasicField String issuer; //uuid of issuer
    public @BasicField String punished; //uuid of punished player
    public @BasicField String reason; //reason for punishment
    public @BasicField Boolean valid; //whether or not the punishment was appealed
    public @BasicField String type; //the type of punishment, tored as the string of an enum
    public @BasicField Date time; //time punishment was given
    public @BasicField Date end; //ending time of punishments, optional

    @SuppressWarnings("unused")
    public Punishment() {
        super();
    }

    public Punishment(DB database) {
        super(database);
    }

    @SuppressWarnings("unused")
    public Punishment(DB database, DBObject dBobject) {
        super(database, dBobject);
    }

    /**
     * @param punished uuid to lookup
     */
    public Punishment(DB database, String punished) {
        this(database);
        this.punished = punished;
    }

    /**
     * Returns the PunishmentType from the string stored in the database
     *
     * @return the PunishmentType of this punishment instance
     */
    public PunishmentType getPunishmentType() {
        return PunishmentType.valueOf(type);
    }
}
