package ru.amalnev.jplant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ProgramModel
{
    private Set<String> interfaces = new HashSet<>();
    private Map<String, Set<String>> fields = new HashMap<>();
    private Map<String, Set<String>> methods = new HashMap<>();
    private Map<String, Set<String>> inheritances = new HashMap<>();
    private Map<String, Set<String>> associations = new HashMap<>();

    private static void addDefinition(Map<String, Set<String>> map, String className, String definition)
    {
        if (map.containsKey(className))
        {
            map.get(className).add(definition);
        }
        else
        {
            Set<String> definitions = new HashSet<>();
            definitions.add(definition);
            map.put(className, definitions);
        }
    }

    private boolean isKnownClass(String className)
    {
        return fields.containsKey(className) || methods.containsKey(className);
    }

    private String renderLinks(Map<String, Set<String>> linkMap, String link)
    {
        final StringBuilder linksBuilder = new StringBuilder();
        linkMap.forEach((className, associatedClasses) -> {
            if (isKnownClass(className))
            {
                associatedClasses.forEach(associatedClass -> {
                    if (isKnownClass(associatedClass))
                    {
                        linksBuilder.append(className);
                        linksBuilder.append(" ");
                        linksBuilder.append(link);
                        linksBuilder.append(" ");
                        linksBuilder.append(associatedClass);
                        linksBuilder.append("\n");
                    }
                });
            }
        });

        return linksBuilder.toString();
    }

    public void addField(String className, String fieldDefinition)
    {
        addDefinition(fields, className, fieldDefinition);
    }

    public void addMethod(String className, String methodDefinition)
    {
        addDefinition(methods, className, methodDefinition);
    }

    public void addAssociation(String className1, String className2)
    {
        addDefinition(associations, className1, className2);
    }

    public void addInheritance(String childType, String parentType)
    {
        addDefinition(inheritances, childType, parentType);
    }

    public void addInterface(String interfaceName)
    {
        interfaces.add(interfaceName);
    }

    public String toString()
    {
        final StringBuilder plantUmlBuilder = new StringBuilder();
        plantUmlBuilder.append("@startuml\n");

        Stream.concat(fields.keySet().stream(), methods.keySet().stream()).distinct().forEach(className -> {
            if (interfaces.contains(className))
            {
                plantUmlBuilder.append("interface ");
            }
            else
            {
                plantUmlBuilder.append("class ");
            }

            plantUmlBuilder.append(className);
            plantUmlBuilder.append(" {\n");

            if (fields.containsKey(className))
            {
                fields.get(className).forEach(fieldDeclaration -> {
                    plantUmlBuilder.append(fieldDeclaration);
                    plantUmlBuilder.append("\n");
                });
            }

            if (methods.containsKey(className))
            {
                methods.get(className).forEach(methodDeclaration -> {
                    plantUmlBuilder.append(methodDeclaration);
                    plantUmlBuilder.append("\n");
                });
            }

            plantUmlBuilder.append("}\n");
        });

        plantUmlBuilder.append(renderLinks(associations, "--"));
        plantUmlBuilder.append(renderLinks(inheritances, "--|>"));
        plantUmlBuilder.append("@enduml");

        return plantUmlBuilder.toString();
    }
}
