package org.freedesktop.jahpar.javacc;


import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import java.util.Collections;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"org.freedesktop.jahpar.javacc.api.JavaCC"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JavaCCProcessor extends BasicAnnotationProcessor {
    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        return Collections.singleton(new JavaCCProcessingStep(this.processingEnv));
    }
}
