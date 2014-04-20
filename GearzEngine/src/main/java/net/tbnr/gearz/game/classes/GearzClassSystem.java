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

package net.tbnr.gearz.game.classes;

import lombok.Data;
import net.tbnr.gearz.player.GearzPlayer;

@Data
public abstract class GearzClassSystem<PlayerType extends GearzPlayer, AbstractClassType extends GearzAbstractClass<PlayerType>> {
    private final Class<? extends AbstractClassType>[] classes;
    private final GearzClassResolver<PlayerType, AbstractClassType> classResolver;
    private final Class<? extends AbstractClassType> defaultClass;
}
