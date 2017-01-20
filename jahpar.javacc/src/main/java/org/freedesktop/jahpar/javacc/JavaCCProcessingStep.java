package org.freedesktop.jahpar.javacc;


import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.SetMultimap;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

public class JavaCCProcessingStep implements BasicAnnotationProcessor.ProcessingStep{
    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return null;
    }

    @Override
    public Set<Element> process(final SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        return null;
    }
}
