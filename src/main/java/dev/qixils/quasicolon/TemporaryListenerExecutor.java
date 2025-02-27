package dev.qixils.quasicolon;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class TemporaryListenerExecutor {
    private final ConcurrentHashMap<String, TemporaryListener<?>> listeners = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Registers a temporary listener.
     * @param listener temporary listener to register
     */
    public void register(TemporaryListener<?> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");

        final String ID = listener.getID();
        listeners.put(ID, listener);
        executor.schedule(() -> listeners.remove(ID), listener.expiresAfter(), TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onEvent(GenericEvent event) {
        Iterator<TemporaryListener<?>> iterator = listeners.values().iterator();
        while (iterator.hasNext()) {
            TemporaryListener<?> listener = iterator.next();
            Class<?> eventClass = listener.getEventClass();
            if (eventClass.isInstance(event)) {
                try {
                    ((Consumer) listener.getCallback()).accept(event);
                } catch (Throwable throwable) {
                    logger.error("Temporary listener for '" + eventClass.getSimpleName() + "' threw an exception", throwable);
                }
                iterator.remove();
            }
        }
    }
}
