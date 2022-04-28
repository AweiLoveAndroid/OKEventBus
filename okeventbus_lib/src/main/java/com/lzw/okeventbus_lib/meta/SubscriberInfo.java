package com.lzw.okeventbus_lib.meta;

import com.lzw.okeventbus_lib.SubscriberMethod;




/** Base class for generated index classes created by annotation processing. */
public interface SubscriberInfo {
    Class<?> getSubscriberClass();

    SubscriberMethod[] getSubscriberMethods();

    SubscriberInfo getSuperSubscriberInfo();

    boolean shouldCheckSuperclass();
}
