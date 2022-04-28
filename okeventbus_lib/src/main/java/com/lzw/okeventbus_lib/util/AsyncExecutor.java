package com.lzw.okeventbus_lib.util;



import java.lang.reflect.Constructor;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import com.lzw.okeventbus_lib.OKEventBus;

/**
 * Executes an {@link RunnableEx} using a thread pool. Thrown exceptions are propagated by posting failure events of any
 * given type (default is {@link ThrowableFailureEvent}).
 * 
 * @author Markus
 */
public class AsyncExecutor {

    public static class Builder {
        private Executor threadPool;
        private Class<?> failureEventType;
        private OKEventBus mOKEventBus;

        private Builder() {
        }

        public Builder threadPool(Executor threadPool) {
            this.threadPool = threadPool;
            return this;
        }

        public Builder failureEventType(Class<?> failureEventType) {
            this.failureEventType = failureEventType;
            return this;
        }

        public Builder eventBus(OKEventBus OKEventBus) {
            this.mOKEventBus = OKEventBus;
            return this;
        }

        public AsyncExecutor build() {
            return buildForScope(null);
        }

        public AsyncExecutor buildForScope(Object executionContext) {
            if (mOKEventBus == null) {
                mOKEventBus = OKEventBus.getDefault();
            }
            if (threadPool == null) {
                threadPool = Executors.newCachedThreadPool();
            }
            if (failureEventType == null) {
                failureEventType = ThrowableFailureEvent.class;
            }
            return new AsyncExecutor(threadPool, mOKEventBus, failureEventType, executionContext);
        }
    }

    /** Like {@link Runnable}, but the run method may throw an exception. */
    public interface RunnableEx {
        void run() throws Exception;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static AsyncExecutor create() {
        return new Builder().build();
    }

    private final Executor threadPool;
    private final Constructor<?> failureEventConstructor;
    private final OKEventBus mOKEventBus;
    private final Object scope;

    private AsyncExecutor(Executor threadPool, OKEventBus OKEventBus, Class<?> failureEventType, Object scope) {
        this.threadPool = threadPool;
        this.mOKEventBus = OKEventBus;
        this.scope = scope;
        try {
            failureEventConstructor = failureEventType.getConstructor(Throwable.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Failure event class must have a constructor with one parameter of type Throwable", e);
        }
    }

    /** Posts an failure event if the given {@link RunnableEx} throws an Exception. */
    public void execute(final RunnableEx runnable) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Object event;
                    try {
                        event = failureEventConstructor.newInstance(e);
                    } catch (Exception e1) {
                        mOKEventBus.getLogger().log(Level.SEVERE, "Original exception:", e);
                        throw new RuntimeException("Could not create failure event", e1);
                    }
                    if (event instanceof HasExecutionScope) {
                        ((HasExecutionScope) event).setExecutionScope(scope);
                    }
                    mOKEventBus.post(event);
                }
            }
        });
    }

}
