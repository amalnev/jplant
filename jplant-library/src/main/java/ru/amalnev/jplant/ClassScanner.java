package ru.amalnev.jplant;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementScanner8;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassScanner extends ElementScanner8<Void, Void>
{
    private ProgramModel programModel;

    private String className;

    public ClassScanner(ProgramModel programModel)
    {
        this.programModel = programModel;
    }

    private static Map<Modifier, String> plantUmlModifiers = new HashMap<Modifier, String>()
    {{
        put(Modifier.ABSTRACT, "{abstract}");
        put(Modifier.STATIC, "{static}");
        put(Modifier.PRIVATE, "-");
        put(Modifier.PROTECTED, "#");
        put(Modifier.DEFAULT, "~");
        put(Modifier.PUBLIC, "+");
    }};

    private static String renderModifiers(Set<Modifier> modifiers)
    {
        final StringBuilder modifiersBuilder = new StringBuilder();
        modifiers.forEach(modifier -> {
            modifiersBuilder.append(plantUmlModifiers.getOrDefault(modifier, ""));
            modifiersBuilder.append(" ");
        });

        return modifiersBuilder.toString();
    }

    public Void visitExecutable(final ExecutableElement executable, final Void p)
    {
        final StringBuilder methodDeclarationBuilder = new StringBuilder();
        methodDeclarationBuilder.append(renderModifiers(executable.getModifiers()));
        methodDeclarationBuilder.append(executable.getReturnType().toString());
        programModel.addAssociation(className, executable.getReturnType().toString());
        methodDeclarationBuilder.append(" ");
        methodDeclarationBuilder.append(executable.getSimpleName());
        methodDeclarationBuilder.append("(");

        if (executable.getParameters() != null)
        {
            for (int i = 0; i < executable.getParameters().size(); i++)
            {
                final VariableElement parameter = executable.getParameters().get(i);
                methodDeclarationBuilder.append(parameter.asType().toString());
                methodDeclarationBuilder.append(" ");
                methodDeclarationBuilder.append(parameter.getSimpleName());
                if (i != executable.getParameters().size() - 1)
                {
                    methodDeclarationBuilder.append(", ");
                }

                programModel.addAssociation(className, parameter.asType().toString());
            }
        }

        methodDeclarationBuilder.append(")");
        programModel.addMethod(className, methodDeclarationBuilder.toString());
        return super.visitExecutable(executable, p);
    }

    public Void visitVariable(final VariableElement variable, final Void p)
    {
        if (variable.getEnclosingElement().getKind() == ElementKind.CLASS)
        {
            final StringBuilder fieldDeclarationBuilder = new StringBuilder();
            fieldDeclarationBuilder.append(renderModifiers(variable.getModifiers()));
            fieldDeclarationBuilder.append(variable.asType().toString());
            programModel.addAssociation(className, variable.asType().toString());
            fieldDeclarationBuilder.append(" ");
            fieldDeclarationBuilder.append(variable.getSimpleName());
            programModel.addField(className, fieldDeclarationBuilder.toString());
        }

        return super.visitVariable(variable, p);
    }

    @Override
    public Void visitPackage(PackageElement e, Void aVoid)
    {
        return super.visitPackage(e, aVoid);
    }

    @Override
    public Void visitType(TypeElement e, Void aVoid)
    {
        if (e.getKind() == ElementKind.INTERFACE)
            programModel.addInterface(e.getQualifiedName().toString());

        return super.visitType(e, aVoid);
    }

    @Override
    public Void scan(Element e, Void aVoid)
    {
        if (className == null)
        {
            final TypeElement typeElement = (TypeElement) e;
            className = typeElement.getQualifiedName().toString();
        }

        return super.scan(e, aVoid);
    }
}
