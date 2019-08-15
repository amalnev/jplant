package ru.amalnev.jplant;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleTypeVisitor8;

public class InheritanceScanner extends SimpleTypeVisitor8<Void, Void>
{
    private ProgramModel programModel;

    public InheritanceScanner(ProgramModel programModel)
    {
        this.programModel = programModel;
    }

    @Override
    public Void visitDeclared(DeclaredType declaredType, Void aVoid)
    {
        final Element element = declaredType.asElement();
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE)
        {
            final TypeElement typeElement = (TypeElement) element;
            final String currentTypeName = typeElement.getQualifiedName().toString();
            typeElement.getInterfaces().forEach(iface -> {
                programModel.addInheritance(currentTypeName, iface.toString());
                final InheritanceScanner inheritanceScanner = new InheritanceScanner(programModel);
                inheritanceScanner.visit(iface);
            });

            programModel.addInheritance(currentTypeName, typeElement.getSuperclass().toString());
            final InheritanceScanner inheritanceScanner = new InheritanceScanner(programModel);
            inheritanceScanner.visit(typeElement.getSuperclass());
        }

        return super.visitDeclared(declaredType, aVoid);
    }
}
