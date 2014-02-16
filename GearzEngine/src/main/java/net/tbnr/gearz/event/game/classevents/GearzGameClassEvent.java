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

package net.tbnr.gearz.event.game.classevents;

import lombok.Getter;
import net.tbnr.gearz.game.GearzGame;

/**
 * Created by George on 16/02/14.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
public abstract class GearzGameClassEvent {
	@Getter
	private final Class<? extends GearzGame> game;

	public GearzGameClassEvent(final Class<? extends GearzGame> game) {
		this.game = game;
	}
}
