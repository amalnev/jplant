package ru.amalnev.jplant;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("ru.amalnev.jplant.PlantUml")
public class PlantUmlAnnotationProcessor extends AbstractProcessor
{
    private final ProgramModel programModel = new ProgramModel();

    private String outputFilePath;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (annotations.size() == 0) return true;

        roundEnv.getElementsAnnotatedWith(PlantUml.class).forEach(element -> {
            if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.INTERFACE) return;

            if(outputFilePath == null || outputFilePath.length() == 0)
                outputFilePath = ((TypeElement)element).getAnnotation(PlantUml.class).value();

            final ClassScanner classScanner = new ClassScanner(programModel);
            classScanner.scan(element);


            final InheritanceScanner inheritanceScanner = new InheritanceScanner(programModel);
            inheritanceScanner.visit(element.asType());
        });

        try (final FileOutputStream outputStream = new FileOutputStream(outputFilePath))
        {
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.append(programModel.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return true;
    }
}
