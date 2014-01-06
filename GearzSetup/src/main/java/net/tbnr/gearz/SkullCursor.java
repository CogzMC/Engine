package net.tbnr.gearz;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Created by Joey on 12/19/13.
 */
@RequiredArgsConstructor
@ToString
public class SkullCursor {
    @NonNull
    private Integer minimum;
    @NonNull
    private Integer maximum;
    private Integer cursor = -1;
    private Integer thisSession = 0;
    @NonNull
    private Integer maxSession;

    public boolean shouldContinue() {
        return (thisSession < maxSession && cursor + minimum <= maximum);
    }

    public Integer getNext() {
        cursor++;
        thisSession++;
        return minimum + cursor;
    }

    public void nextSession() {
        this.thisSession = 0;
    }

    public void reset() {
        nextSession();
        this.cursor = 0;
    }

    public boolean isDone() {
        return this.minimum + cursor >= maximum;
    }
}
