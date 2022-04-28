/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzw.okeventbus_lib;

import android.os.Looper;

import com.lzw.okeventbus_lib.log.AndroidLogger;
import com.lzw.okeventbus_lib.log.Logger;
import com.lzw.okeventbus_lib.meta.SubscriberInfoIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates OKEventBus instances with custom parameters and also allows to install a custom default OKEventBus instance.
 * Create a new builder using {@link OKEventBus#builder()}.
 */
public class EventBusBuilder {
    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    boolean logSubscriberExceptions = true;
    boolean logNoSubscriberMessages = true;
    boolean sendSubscriberExceptionEvent = true;
    boolean sendNoSubscriberEvent = true;
    boolean throwSubscriberException;
    boolean eventInheritance = true;
    boolean ignoreGeneratedIndex;
    boolean strictMethodVerification;
    ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;
    List<Class<?>> skipMethodVerificationForClasses;
    List<SubscriberInfoIndex> subscriberInfoIndexes;
    Logger logger;
    MainThreadSupport mainThreadSupport;

    EventBusBuilder() {
    }

    /** Default: true */
    public EventBusBuilder logSubscriberExceptions(boolean logSubscriberExceptions) {
        this.logSubscriberExceptions = logSubscriberExceptions;
        return this;
    }

    /** Default: true */
    public EventBusBuilder logNoSubscriberMessages(boolean logNoSubscriberMessages) {
        this.logNoSubscriberMessages = logNoSubscriberMessages;
        return this;
    }

    /** Default: true */
    public EventBusBuilder sendSubscriberExceptionEvent(boolean sendSubscriberExceptionEvent) {
        this.sendSubscriberExceptionEvent = sendSubscriberExceptionEvent;
        return this;
    }

    /** Default: true */
    public EventBusBuilder sendNoSubscriberEvent(boolean sendNoSubscriberEvent) {
        this.sendNoSubscriberEvent = sendNoSubscriberEvent;
        return this;
    }

    /**
     * Fails if an subscriber throws an exception (default: false).
     * <p/>
     * Tip: Use this with BuildConfig.DEBUG to let the app crash in DEBUG mode (only). This way, you won't miss
     * exceptions during development.
     */
    public EventBusBuilder throwSubscriberException(boolean throwSubscriberException) {
        this.throwSubscriberException = throwSubscriberException;
        return this;
    }

    /**
     * By default, OKEventBus considers the event class hierarchy (subscribers to super classes will be notified).
     * Switching this feature off will improve posting of events. For simple event classes extending Object directly,
     * we measured a speed up of 20% for event posting. For more complex event hierarchies, the speed up should be
     * >20%.
     * <p/>
     * However, keep in mind that event posting usually consumes just a small proportion of CPU time inside an app,
     * unless it is posting at high rates, e.g. hundreds/thousands of events per second.
     */
    public EventBusBuilder eventInheritance(boolean eventInheritance) {
        this.eventInheritance = eventInheritance;
        return this;
    }


    /**
     * Provide a custom thread pool to OKEventBus used for async and background event delivery. This is an advanced
     * setting to that can break things: ensure the given ExecutorService won't get stuck to avoid undefined behavior.
     */
    public EventBusBuilder executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * Method name verification is done for methods starting with onEvent to avoid typos; using this method you can
     * exclude subscriber classes from this check. Also disables checks for method modifiers (public, not static nor
     * abstract).
     */
    public EventBusBuilder skipMethodVerificationFor(Class<?> clazz) {
        if (skipMethodVerificationForClasses == null) {
            skipMethodVerificationForClasses = new ArrayList<>();
        }
        skipMethodVerificationForClasses.add(clazz);
        return this;
    }

    /** Forces the use of reflection even if there's a generated index (default: false). */
    public EventBusBuilder ignoreGeneratedIndex(boolean ignoreGeneratedIndex) {
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
        return this;
    }

    /** Enables strict method verification (default: false). */
    public EventBusBuilder strictMethodVerification(boolean strictMethodVerification) {
        this.strictMethodVerification = strictMethodVerification;
        return this;
    }

    /** Adds an index generated by OKEventBus' annotation preprocessor. */
    public EventBusBuilder addIndex(SubscriberInfoIndex index) {
        if (subscriberInfoIndexes == null) {
            subscriberInfoIndexes = new ArrayList<>();
        }
        subscriberInfoIndexes.add(index);
        return this;
    }

    /**
     * Set a specific log handler for all OKEventBus logging.
     * <p/>
     * By default all logging is via {@link android.util.Log} but if you want to use OKEventBus
     * outside the Android environment then you will need to provide another log target.
     */
    public EventBusBuilder logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    Logger getLogger() {
        if (logger != null) {
            return logger;
        } else {
            // also check main looper to see if we have "good" Android classes (not Stubs etc.)
            return AndroidLogger.isAndroidLogAvailable() && getAndroidMainLooperOrNull() != null
                    ? new AndroidLogger("OKEventBus") :
                    new Logger.SystemOutLogger();
        }
    }


    MainThreadSupport getMainThreadSupport() {
        if (mainThreadSupport != null) {
            return mainThreadSupport;
        } else if (AndroidLogger.isAndroidLogAvailable()) {
            Object looperOrNull = getAndroidMainLooperOrNull();
            return looperOrNull == null ? null :
                    new MainThreadSupport.AndroidHandlerMainThreadSupport((Looper) looperOrNull);
        } else {
            return null;
        }
    }

    Object getAndroidMainLooperOrNull() {
        try {
            return Looper.getMainLooper();
        } catch (RuntimeException e) {
            // Not really a functional Android (e.g. "Stub!" maven dependencies)
            return null;
        }
    }

    /**
     * Installs the default OKEventBus returned by {@link OKEventBus#getDefault()} using this builders' values. Must be
     * done only once before the first usage of the default OKEventBus.
     *
     * @throws EventBusException if there's already a default OKEventBus instance in place
     */
    public OKEventBus installDefaultEventBus() {
        synchronized (OKEventBus.class) {
            if (OKEventBus.defaultInstance != null) {
                throw new EventBusException("Default instance already exists." +
                        " It may be only set once before it's used the first time to ensure consistent behavior.");
            }
            OKEventBus.defaultInstance = build();
            return OKEventBus.defaultInstance;
        }
    }

    /** Builds an OKEventBus based on the current configuration. */
    public OKEventBus build() {
        return new OKEventBus(this);
    }

}
