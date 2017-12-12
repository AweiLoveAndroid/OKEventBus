package com.lzw.okeventbus.meta;

import com.lzw.okeventbus.SubscriberMethod;




/** Base class for generated index classes created by annotation processing. */
public interface SubscriberInfo {
    Class<?> getSubscriberClass();

    SubscriberMethod[] getSubscriberMethods();

    SubscriberInfo getSuperSubscriberInfo();

    boolean shouldCheckSuperclass();
}
