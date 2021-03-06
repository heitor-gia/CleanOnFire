package com.cleanonfire.processor;


import com.cleanonfire.processor.core.ProcessingException;
import com.cleanonfire.processor.utils.ProcessingUtils;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CleanOnFireProcessor extends AbstractProcessor {

    ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
        ProcessingUtils.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (SupportedAnnotation supportedAnnotation : SupportedAnnotation.values()) {
            Class<? extends Annotation> annotation = supportedAnnotation.getSupportedAnnotation();
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotation);
            try {
                if (elements.isEmpty()) continue;
                supportedAnnotation.getProcessor().process(elements, roundEnvironment);
            } catch (ProcessingException pe) {
                processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, pe.getCompilerMessage(), pe.getElement());
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return SupportedAnnotation.getSupportedAnnotations();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


}
