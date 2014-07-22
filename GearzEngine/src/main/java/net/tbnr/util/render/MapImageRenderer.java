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

package net.tbnr.util.render;

import lombok.extern.java.Log;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

import java.awt.image.BufferedImage;

@Log
public class MapImageRenderer extends MapRenderer {

    private boolean hasRendered;
    private Thread renderImageThread;

    private final BufferedImage image;
    private final String text;

    public MapImageRenderer(BufferedImage image, String text) {
        hasRendered = false;
        this.image = image;
        this.text = text;
    }

    @Override
    public void render(MapView view, final MapCanvas canvas, Player player) {
        if (!hasRendered && image != null && renderImageThread == null) {
            log.info("[DEBUG] Running Map Render");
            renderImageThread = new Thread() {
                @Override
                public void run() {
                    canvas.drawImage(0, 0, MapPalette.resizeImage(image));
                    canvas.drawText(0, 0, MinecraftFont.Font, text);
                }
            };
            renderImageThread.start();
            hasRendered = true;
        }
    }
}