package dev.qixils.quasicolon;

import de.huxhorn.sulky.ulid.ULID;
import net.dv8tion.jda.api.events.GenericEvent;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A temporary listener for Discord events. The first matching event (defined by the {@link #getPredicate() predicate}
 * returning true) will be passed into the {@link #getCallback() callback}, and then the listener will be discarded.
 * @param <T> event type that will be listened for
 */
public class TemporaryListener<T extends GenericEvent> {
    private static final ULID ULID = new ULID();

    private final Class<T> eventClass;
    private final Predicate<T> predicate;
    private final Consumer<T> callback;
    private final Runnable onTimeout;
    private final long length;
    private final String ID = ULID.nextULID();

    /**
     * Constructs a temporary listener which listens for the first event specified by the {@code eventClass}, checks it
     * against the optional {@code predicate}, then calls the {@code callback} if the predicate passes.
     * @param eventClass class of event that this object is listening for
     * @param callback method that will be called with the event
     * @param length how long the temporary listener will exist until it is discarded, in milliseconds
     * @param predicate optional predicate which the event must pass
     */
    @CheckReturnValue
    public TemporaryListener(@NotNull Class<T> eventClass, @Nullable Predicate<T> predicate, @NotNull Consumer<T> callback, @Nullable Runnable onTimeout, long length) {
        this.eventClass = Objects.requireNonNull(eventClass, "eventClass cannot be null");
        this.callback = Objects.requireNonNull(callback, "callback cannot be null");
        this.length = length;
        this.predicate = predicate == null ? $ -> true : predicate;
        this.onTimeout = onTimeout == null ? () -> {} : onTimeout;
    }

    /**
     * Gets the event class being listening for.
     * @return event class
     */
    @CheckReturnValue
    public @NotNull Class<T> getEventClass() {
        return eventClass;
    }

    /**
     * Gets the callback which will be executed once upon a matching event being found.
     * @return event consumer callback
     */
    @CheckReturnValue
    public @NotNull Consumer<T> getCallback() {
        return callback;
    }

    /**
     * Gets the function which will be executed if the listener times out.
     * @return timeout callback
     */
    @CheckReturnValue
    public @NotNull Runnable getTimeoutCallback() {
        return onTimeout;
    }

    /**
     * Gets the predicate which must return {@code true} for an event to be considered matching.
     * @return predicate
     */
    @CheckReturnValue
    public @NotNull Predicate<T> getPredicate() {
        return predicate;
    }

    /**
     * Gets the milliseconds until the listener expires.
     * @return expiry time
     */
    @CheckReturnValue
    public long expiresAfter() {
        return length;
    }

    /**
     * Gets the unique identifier for this temporary listener.
     * @return unique identifier
     */
    @CheckReturnValue
    public @NotNull String getID() {
        return ID;
    }

    /**
     * Creates a new builder representing this temporary listener.
     * @return new builder
     */
    @CheckReturnValue
    public @NotNull Builder<T> toBuilder() {
        return new Builder<T>().eventClass(eventClass).predicate(predicate).callback(callback).onTimeout(onTimeout).length(length);
    }

    /**
     * Registers this temporary listener.
     * @param bot bot to register for
     */
    public void register(@NotNull QuasicolonBot bot) {
        Objects.requireNonNull(bot, "bot cannot be null").register(this);
    }

    /**
     * Builder for {@link TemporaryListener}.
     * @param <T> event to listen for
     */
    public static class Builder<T extends GenericEvent> {
        private Class<T> eventClass = null;
        private Predicate<T> predicate = null;
        private Consumer<T> callback = null;
        private Runnable onTimeout = null;
        private long length = 0;

        public Builder(){}
        public Builder(Class<T> eventClass) {
            this.eventClass = eventClass;
        }

        /**
         * Sets the event to listen for.
         * @param eventClass class of the event to listen for
         * @return this builder
         */
        @CheckReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder<T> eventClass(@NotNull Class<T> eventClass) {
            this.eventClass = eventClass;
            return this;
        }

        /**
         * Sets the filter which an event must pass (return {@code true}) for the {@link #callback(Consumer) callback}
         * to be called.
         * @param predicate event filter
         * @return this builder
         */
        @CheckReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder<T> predicate(@NotNull Predicate<T> predicate) {
            this.predicate = predicate;
            return this;
        }

        /**
         * Sets the callback which will be executed only once when a matching event is received.
         * A "matching event" is defined by the event being the same type as the {@link #eventClass(Class) eventClass}
         * and passing the {@link #predicate(Predicate) predicate}.
         * @param callback callback to execute
         * @return this builder
         */
        @CheckReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder<T> callback(@NotNull Consumer<T> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Sets the method which will be run if the temporary listener expires without receiving an event.
         * @param onTimeout method to execute on timeout
         * @return this builder
         */
        @CheckReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder<T> onTimeout(@NotNull Runnable onTimeout) {
            this.onTimeout = onTimeout;
            return this;
        }

        /**
         * Sets how many milliseconds until the temporary listener expires.
         * @param length time in milliseconds
         * @return this builder
         */
        @CheckReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder<T> length(@IntRange(from=1L) long length) {
            this.length = length;
            return this;
        }

        /**
         * Sets the duration until the temporary listener expires.
         * @param length duration
         * @return this builder
         */
        @CheckReturnValue
        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder<T> length(@NotNull Duration length) {
            return length(Objects.requireNonNull(length, "length cannot be null").toMillis());
        }

        /**
         * Creates a new {@link TemporaryListener}.
         * @return the built temporary listener
         * @throws IllegalStateException a required parameter was not set
         */
        @CheckReturnValue
        public @NotNull TemporaryListener<T> build() throws IllegalStateException {
            if (eventClass == null) throw new IllegalStateException("eventClass must be set");
            if (callback == null) throw new IllegalStateException("callback must be set");
            if (length <= 0) throw new IllegalStateException("length must be positive");
            return new TemporaryListener<>(eventClass, predicate, callback, onTimeout, length);
        }

        /**
         * Creates a new {@link TemporaryListener} and registers it.
         * @param bot bot to register for
         */
        public void register(@NotNull QuasicolonBot bot) {
            Objects.requireNonNull(bot, "bot cannot be null").register(build());
        }
    }
}
