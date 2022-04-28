package com.lzw.okeventbus_lib;

/**
 * This Event is posted by OKEventBus when no subscriber is found for a posted event.
 * 
 * @author Markus
 */
public final class NoSubscriberEvent {
    /** The {@link OKEventBus} instance to with the original event was posted to. */
    public final OKEventBus mOKEventBus;

    /** The original event that could not be delivered to any subscriber. */
    public final Object originalEvent;

    public NoSubscriberEvent(OKEventBus OKEventBus, Object originalEvent) {
        this.mOKEventBus = OKEventBus;
        this.originalEvent = originalEvent;
    }

}
