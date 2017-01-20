package org.freedesktop.jahpar.api;

import org.freedesktop.jahpar.javacc.api.JavaCC;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@JavaCC("C.jj")
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.SOURCE)
public @interface Headers {
    Header[] value();
}
