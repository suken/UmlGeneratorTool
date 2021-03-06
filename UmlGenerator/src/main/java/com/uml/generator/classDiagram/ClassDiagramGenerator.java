/**
 *
 */
package com.uml.generator.classDiagram;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.uml.generator.UmlGeneratorUtility;
import com.uml.generator.UmlOptions;
import com.uml.generator.classDiagram.models.ClassDiagramModel;
import com.uml.generator.classDiagram.models.ClassModel;
import com.uml.generator.classDiagram.models.ClassType;
import com.uml.generator.classDiagram.models.FieldModel;
import com.uml.generator.classDiagram.models.MethodModel;

/**
 * @author suken shah
 */
public class ClassDiagramGenerator {

    private static Logger LOGGER = Logger.getLogger(ClassDiagramGenerator.class.getSimpleName());

    public static ClassDiagramModel generateClassDependencies(URLClassLoader classLoader, URL jarUrl, UmlOptions options) throws Exception {
        ClassDiagramModel classDiagramModel = new ClassDiagramModel();
        LOGGER.log(Level.INFO, "Loading classes");
        JarFile jarFile = null;
        try {
            List<String> classes = new ArrayList<String>();
            jarFile = new JarFile(jarUrl.getFile());
            if (jarFile != null) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String jarEntryName = jarEntry.getName();
                    // exclude test classes if testIncluded is set to FALSE
                    if (jarEntryName.endsWith(".class") && (options.isTestIncluded() || !jarEntryName.contains("Test"))) {
                        String className = jarEntryName.replace('/', '.').replace(".class", "");
                        classes.add(className);
                    }
                }
            }

            // Parse all the classes for UML
            extractClassInformation(classLoader, classes, classDiagramModel, options.isPackagesIncluded(), options.isFieldsIncluded(), options.isMethodsIncluded(), options.getIncludePatterns(),
                options.getExcludePatterns());

        }
        catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO, "exception while loading and parsing class information." + e.getMessage());
        }
        finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
        return classDiagramModel;
    }

    private static void extractClassInformation(URLClassLoader classLoader, List<String> classes, ClassDiagramModel classDiagramModel, boolean packagesIncluded, //
        boolean fieldsVisible, boolean methodsVisible, String includePatternString, String excludePatternString) //
        throws ClassNotFoundException, IntrospectionException {
        String[] includePatterns = includePatternString.split(",");
        String[] excludePatterns = excludePatternString.split(",");
        boolean hasAnyPatterns = !(includePatterns.length == 1 && includePatterns[0].isEmpty()) //
            || !(excludePatterns.length == 1 && excludePatterns[0].isEmpty());
        for (String className : classes) {
            if (UmlGeneratorUtility.isIncluded(className, hasAnyPatterns, includePatterns, excludePatterns)) {
                Class<?> loadClass = classLoader.loadClass(className);
                if (!loadClass.getSimpleName().isEmpty()) {
                    // extract class info
                    ClassModel classModel = new ClassModel(packagesIncluded ? loadClass.getName() : loadClass.getSimpleName());
                    classModel.setType(loadClass);

                    // do not extract field and method information for ENUM
                    if (classModel.getType() != ClassType.ENUM) {
                        extractFieldsAndMethods(classes, packagesIncluded, fieldsVisible, methodsVisible, includePatterns, //
                            excludePatterns, hasAnyPatterns, loadClass, classModel);
                    }

                    // extract interfaces
                    for (Class<?> interfaceClass : loadClass.getInterfaces()) {
                        if (UmlGeneratorUtility.isIncluded(interfaceClass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
                            classModel.addInterface(packagesIncluded ? interfaceClass.getName() : interfaceClass.getSimpleName());
                        }
                    }

                    // extract parent class
                    Class<?> superclass = loadClass.getSuperclass();
                    if (superclass != null && !superclass.equals(Object.class)) {
                        if (UmlGeneratorUtility.isIncluded(superclass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
                            classModel.setParent(packagesIncluded ? superclass.getName() : superclass.getSimpleName());
                        }
                    }

                    // extract dependencies
                    for (Class<?> dependentClass : loadClass.getDeclaredClasses()) {
                        if (UmlGeneratorUtility.isIncluded(dependentClass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
                            classModel.addDependency(packagesIncluded ? dependentClass.getName() : dependentClass.getSimpleName());
                        }
                    }

                    // add prepared class model to class diagram
                    classDiagramModel.addClass(classModel);
                }
            }
        }
    }

    private static void extractFieldsAndMethods(List<String> classes, boolean packagesIncluded, boolean fieldsVisible, boolean methodsVisible, //
        String[] includePatterns, String[] excludePatterns,
        boolean hasAnyPatterns, Class<?> loadClass, ClassModel classModel) {
        // extract fields
        for (Field field : loadClass.getDeclaredFields()) {
            try {
                Class<?> fieldType = field.getType();
                String type = fieldType.getName().replace("[", "").replace("]", "");
                if (classes.contains(type) && UmlGeneratorUtility.isIncluded(fieldType.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
                    classModel.addDependency(packagesIncluded ? fieldType.getName() : fieldType.getSimpleName());
                }
                else if (fieldsVisible && !field.isSynthetic()) {
                    // only add field if visibility is set
                    classModel.addField(new FieldModel(field.getName(), fieldType.getSimpleName(), field.getModifiers()));
                }
            }
            catch (Exception e) {
                // no need to do anything
                continue;
            }
            catch (NoClassDefFoundError error) {
                // no need to do anything
                continue;
            }
        }

        // extract methods
        if (methodsVisible) {
            for (Method method : loadClass.getDeclaredMethods()) {
                if (!method.isSynthetic() && !method.isBridge()) {
                    classModel.addMethod(new MethodModel(method.getName(), method.getModifiers()));
                }
            }
        }
    }

}
