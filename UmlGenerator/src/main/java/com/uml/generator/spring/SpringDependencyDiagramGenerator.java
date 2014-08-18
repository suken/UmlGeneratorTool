/**
 * 
 */
package com.uml.generator.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.uml.generator.UmlGeneratorUtility;
import com.uml.generator.UmlOptions;
import com.uml.generator.classDiagram.models.ClassType;
import com.uml.generator.classDiagram.models.FieldModel;
import com.uml.generator.classDiagram.models.MethodModel;
import com.uml.generator.spring.models.DependencyType;
import com.uml.generator.spring.models.SpringClassModel;
import com.uml.generator.spring.models.SpringClassType;
import com.uml.generator.spring.models.SpringDependencyDiagramModel;

/**
 * @author sukenshah
 */
public class SpringDependencyDiagramGenerator {
	
	//TODO: SUKEN TO FIX
	// This is temporary because of issues with loading spring framework jars under the plugin classloader in eclipse.
	// SUKEN to try and package the plugin again with some changes to build.properties and plugin.xml files.
	private static final String ANNOTATION_CONFIGURATION = "org.springframework.context.annotation.Configuration";
	private static final String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";
	private static final String ANNOTATION_SERVICE = "org.springframework.stereotype.Service";
	private static final String ANNOTATION_COMPONENT = "org.springframework.stereotype.Component";
	private static final String ANNOTATION_CONTROLLER = "org.springframework.stereotype.Controller";
	private static final String ANNOTATION_BEAN = "org.springframework.context.annotation.Bean";
	private static final String ANNOTATION_REQUIRED = "org.springframework.beans.factory.annotation.Required";
	private static final String ANNOTATION_AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
	private static final String ANNOTATION_STR = "@";

	public static String generateSpringDependencies(URLClassLoader classLoader, URL jarUrl, UmlOptions options) {
		SpringDependencyDiagramModel dependencyModel = new SpringDependencyDiagramModel();
		try {
			List<String> classes = new ArrayList<String>();
			final JarFile jarFile = new JarFile(jarUrl.getFile());
			if (jarFile != null) {
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry jarEntry = entries.nextElement();
					String jarEntryName = jarEntry.getName();
					if (jarEntryName.endsWith(".class")
							&& (options.isTestIncluded() || !jarEntryName.contains("Test"))) {
						String className = jarEntryName.replace('/', '.').replace(".class", "");
						classes.add(className);
					}
				}
				jarFile.close();
			}

			// Parse all the classes for UML
			extractClassInformation(classLoader, classes, dependencyModel, options.isPackagesIncluded(), options.isFieldsIncluded(), options.isMethodsIncluded(), options.getIncludePatterns(), options.getExcludePatterns());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dependencyModel.getUml();
	}

	static void extractClassInformation(URLClassLoader classLoader, List<String> classes, SpringDependencyDiagramModel dependencyModel, boolean packagesIncluded, boolean fieldsVisible, boolean methodsVisible, String includePatternString, String excludePatternString) throws ClassNotFoundException {
		String[] includePatterns = includePatternString.split(",");
		String[] excludePatterns = excludePatternString.split(",");
		boolean hasAnyPatterns = !(includePatterns.length == 1 && includePatterns[0].isEmpty()) ||  !(excludePatterns.length == 1 && excludePatterns[0].isEmpty());
		for (String className : classes) {
			if (UmlGeneratorUtility.isIncluded(className, hasAnyPatterns, includePatterns, excludePatterns)) {
				Class<?> loadClass = classLoader.loadClass(className);
				if (!loadClass.getSimpleName().isEmpty()) {
					System.out.println("Parsing class  " + loadClass.getSimpleName());
					// extract class info
					SpringClassModel classModel = new SpringClassModel(packagesIncluded ? loadClass.getName() : loadClass.getSimpleName());
					
					// determine class types
					extractSpringClassAnnotations(loadClass, classModel);

					// do not extract fields and methods for enums.
					if (classModel.getType() != ClassType.ENUM) {
						// extract fields
						extractFields(classes, packagesIncluded, fieldsVisible, loadClass, classModel, hasAnyPatterns, includePatterns, excludePatterns);
						
						// extract methods
						extractMethods(packagesIncluded, methodsVisible, loadClass, classModel, hasAnyPatterns, includePatterns, excludePatterns);
						
						// extract constructors for spring depedencies
						extractConstructorDependencies(packagesIncluded, loadClass, classModel, hasAnyPatterns, includePatterns, excludePatterns);
					}
					
					// extract interfaces
					extractInterfaces(loadClass, classModel, packagesIncluded, hasAnyPatterns, includePatterns, excludePatterns);
					
					// extract parent class
					Class<?> superclass = loadClass.getSuperclass();
					if (superclass != null && !superclass.equals(Object.class)
							&& UmlGeneratorUtility.isIncluded(superclass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
						classModel.setParent(packagesIncluded ? superclass.getName() : superclass.getSimpleName());
					}
					
					extractClassDependencies(loadClass, classModel, packagesIncluded, hasAnyPatterns, includePatterns, excludePatterns);
					
					// add prepared class model to class diagram
					dependencyModel.addClass(classModel);
				}
			}
		}
	}
	
	@SuppressWarnings("finally")
	private static void extractSpringClassAnnotations(Class<?> loadClass,
			SpringClassModel classModel) {
		classModel.setType(loadClass);
		for (Annotation annotation : loadClass.getAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationType.getName().equals(ANNOTATION_CONTROLLER)) {
				classModel.setSpringClassType(SpringClassType.CONTROLLER);
			}
			else if (annotationType.getName().equals(ANNOTATION_COMPONENT)) {
				classModel.setSpringClassType(SpringClassType.COMPONENT);
			}
			else if (annotationType.getName().equals(ANNOTATION_SERVICE)) {
				classModel.setSpringClassType(SpringClassType.SERVICE);
			}
			else if (annotationType.getName().equals(ANNOTATION_REPOSITORY)) {
				classModel.setSpringClassType(SpringClassType.REPOSITORY);
			}
			else if (annotationType.getName().equals(ANNOTATION_CONFIGURATION)) {
				classModel.setSpringClassType(SpringClassType.CONFIGURATION);
			}
			else {
				// adding other annotations as note
				StringBuffer note = new StringBuffer();
				note.append(ANNOTATION_STR + annotationType.getSimpleName()).append(" ( \\n");
				boolean hasAnyAttributes = false;
				for (Method method : annotationType.getDeclaredMethods()) {
					try {
						Object defaultValue = method.getDefaultValue();
						Object value = method.invoke(annotation, (Object[])null);
						if (value != null && !value.equals(defaultValue)) {
							String stringValue = String.valueOf(value);
							if (method.getReturnType().isArray() && ((Object[]) value).length > 0) {
								stringValue = Arrays.toString((Object[])value);
								stringValue = stringValue.substring(1, stringValue.length() - 1);
							}
							if (!stringValue.startsWith("[L")) {
								note.append(" ").append(method.getName()).append(" = ").append(stringValue).append("\\n");
								hasAnyAttributes = true;
							}
						}
					} catch (Exception e) {
						// no need to do anything
					}
					finally {
						continue;
					}
				}
				
				if (hasAnyAttributes) {
					note.append(") \\n");
				}
				else {
					int startIndex = note.indexOf(" ( \\n");
					note.delete(startIndex, startIndex + 5);
				}
				classModel.addNote(note.toString());
			}
		}
	}

	private static void extractClassDependencies(Class<?> loadClass,
			SpringClassModel classModel, boolean packagesIncluded,
			boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
		// extract dependencies
		for (Class dependentClass : loadClass.getDeclaredClasses()) {
			if (UmlGeneratorUtility.isIncluded(dependentClass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
				classModel.addDependency(packagesIncluded ? dependentClass.getName() : dependentClass.getSimpleName());
			}
		}
	}

	private static void extractInterfaces(Class<?> loadClass,
			SpringClassModel classModel, boolean packagesIncluded,
			boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
		for (Class interfaceClass : loadClass.getInterfaces()) {
			if (UmlGeneratorUtility.isIncluded(interfaceClass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
				classModel.addInterface(packagesIncluded ? interfaceClass.getName() : interfaceClass.getSimpleName());
			}
		}
	}

	private static void extractMethods(boolean packagesIncluded, boolean methodsVisible,
			Class<?> loadClass, SpringClassModel classModel,
			boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
		for (Method method : loadClass.getDeclaredMethods()) {
			boolean hasSpringDependency = false;
			for (Annotation annotation : method.getAnnotations()) {
				String annotationName = annotation.annotationType().getName();
				if (annotationName.equals(ANNOTATION_AUTOWIRED)
						&& UmlGeneratorUtility.isIncluded(method.getParameterTypes()[0].getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
					classModel.addDepedency(packagesIncluded ? method.getParameterTypes()[0].getName() : method.getParameterTypes()[0].getSimpleName(), DependencyType.AUTOWIRED);
					hasSpringDependency = true;
				}
				else if (annotationName.equals(ANNOTATION_REQUIRED)
						&& UmlGeneratorUtility.isIncluded(method.getParameterTypes()[0].getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
					classModel.addDepedency(packagesIncluded ? method.getParameterTypes()[0].getName() : method.getParameterTypes()[0].getSimpleName(), DependencyType.REQUIRED);
					hasSpringDependency = true;
				}
				else if (annotationName.equals(ANNOTATION_BEAN)
						&& UmlGeneratorUtility.isIncluded(method.getParameterTypes()[0].getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
					classModel.addDepedency(packagesIncluded ? method.getParameterTypes()[0].getName() : method.getReturnType().getSimpleName(), DependencyType.BEAN);
					hasSpringDependency = true;
				}
			}
			
			if (methodsVisible && !hasSpringDependency && !method.isSynthetic() && !method.isBridge()) {
				classModel.addMethod(new MethodModel(method.getName(), method.getModifiers()));
			}
		}
	}
	
	private static void extractConstructorDependencies(boolean packagesIncluded, Class<?> loadClass, SpringClassModel classModel, boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
		for (Constructor constructor : loadClass.getDeclaredConstructors()) {
			for (Annotation annotation : constructor.getAnnotations()) {
				String annotationName = annotation.annotationType().getName();
				if (annotationName.equals(ANNOTATION_AUTOWIRED)) {
					extractConstructorParameterDependecies(packagesIncluded, classModel,
							hasAnyPatterns, includePatterns, excludePatterns, constructor, DependencyType.AUTOWIRED);
				}
				else if (annotationName.equals(ANNOTATION_REQUIRED)) {
					extractConstructorParameterDependecies(packagesIncluded, classModel,
							hasAnyPatterns, includePatterns, excludePatterns, constructor, DependencyType.REQUIRED);
				}
				else if (annotationName.equals(ANNOTATION_BEAN)) {
					extractConstructorParameterDependecies(packagesIncluded, classModel,
							hasAnyPatterns, includePatterns, excludePatterns, constructor, DependencyType.BEAN);
				}
			}
		}
	}

	protected static void extractConstructorParameterDependecies(boolean packagesIncluded, SpringClassModel classModel,	boolean hasAnyPatterns,
			String[] includePatterns, String[] excludePatterns, Constructor constructor, DependencyType type) {
		for (Class argumentType : constructor.getParameterTypes()) {
			if (UmlGeneratorUtility.isIncluded(argumentType.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
				classModel.addDepedency(packagesIncluded ? argumentType.getName() : argumentType.getSimpleName(), type);
			}
		}
	}
	
	private static void extractFields(List<String> classes, boolean packagesIncluded,
			boolean fieldsVisible, Class<?> loadClass, SpringClassModel classModel,
			boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
		for (Field field : loadClass.getDeclaredFields()) {
			try {
				Class<?> fieldType = field.getType();
				if (UmlGeneratorUtility.isIncluded(fieldType.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
					boolean isSpringDepedency = false;
					// check for spring dependencies
					for (Annotation annotation : field.getDeclaredAnnotations()) {
						String annotationName = annotation.annotationType().getName();
						if (annotationName.equals(ANNOTATION_AUTOWIRED)) {
							classModel.addDepedency(packagesIncluded ? fieldType.getName() : fieldType.getSimpleName(), DependencyType.AUTOWIRED);
							isSpringDepedency = true;
						}
						else if (annotationName.equals(ANNOTATION_REQUIRED)) {
							classModel.addDepedency(packagesIncluded ? fieldType.getName() : fieldType.getSimpleName(), DependencyType.REQUIRED);
							isSpringDepedency = true;
						}
					}
					if (!isSpringDepedency) {
						String type = fieldType.getName().replace("[", "").replace("]", "");
						if (classes.contains(type)) {
							classModel.addDependency(packagesIncluded ? fieldType.getName() : fieldType.getSimpleName());
						}
						else if (fieldsVisible && !field.isSynthetic()) {
							// only add field if visibility is set
							classModel.addField(new FieldModel(field.getName(), fieldType.getSimpleName(), field.getModifiers()));
						}
					}
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
	}
}
