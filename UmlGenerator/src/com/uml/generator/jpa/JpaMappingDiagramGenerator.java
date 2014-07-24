/**
 * 
 */
package com.uml.generator.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.uml.generator.UmlGeneratorUtility;
import com.uml.generator.UmlOptions;
import com.uml.generator.jpa.models.JpaClassModel;
import com.uml.generator.jpa.models.JpaDependencyDiagramModel;
import com.uml.generator.jpa.models.JpaDependencyType;
import com.uml.generator.jpa.models.JpaEntityType;

/**
 * @author SShah
 */
public class JpaMappingDiagramGenerator {
	
	//TODO: SUKEN TO FIX
	// This is temporary because of issues with loading spring framework jars under the plugin classloader in eclipse.
	// SUKEN to try and package the plugin again with some changes to build.properties and plugin.xml files.
	private static final String ANNOTATION_ENTITY = "javax.persistence.Entity";
	private static final String ANNOTATION_TABLE = "javax.persistence.Table";
	private static final String ANNOTATION_MAPPED_SUPER_CLASS = "javax.persistence.MappedSuperclass";
	private static final String ANNOTATION_COLUMN = "javax.persistence.Column";
	private static final String ANNOTATION_ID_COLUMN = "javax.persistence.Id";
	private static final String ANNOTATION_MANY_TO_ONE = "javax.persistence.ManyToOne";
	private static final String ANNOTATION_ONE_TO_MANY = "javax.persistence.OneToMany";
	private static final String ANNOTATION_ONE_TO_ONE = "javax.persistence.OneToOne";
	private static final String ANNOTATION_MANY_TO_MANY = "javax.persistence.ManyToMany";

	public static String generateJpaDependencies(URLClassLoader classLoader, URL jarUrl, UmlOptions options) {
		JpaDependencyDiagramModel dependencyModel = new JpaDependencyDiagramModel();
		try {
			List<String> classes = new ArrayList<String>();
			final JarFile jarFile = new JarFile(jarUrl.getFile());
			if (jarFile != null) {
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry jarEntry = entries.nextElement();
					String jarEntryName = jarEntry.getName();
					if (jarEntryName.endsWith(".class")	&& !jarEntryName.contains("Test")) {
						String className = jarEntryName.replace('/', '.').replace(".class", "");
						classes.add(className);
					}
				}
				jarFile.close();
			}

			// Parse all the classes for UML
			extractJpaClasses(classLoader, classes, dependencyModel, options.getIncludePatterns(), options.getExcludePatterns());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dependencyModel.getUml();
	}

	static void extractJpaClasses(URLClassLoader classLoader, List<String> classes, JpaDependencyDiagramModel dependencyModel, String includePatternString, String excludePatternString) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String[] includePatterns = includePatternString.split(",");
		String[] excludePatterns = excludePatternString.split(",");
		boolean hasAnyPatterns = !(includePatterns.length == 1 && includePatterns[0].isEmpty()) ||  !(excludePatterns.length == 1 && excludePatterns[0].isEmpty());
		Map<String, Map<String, JpaDependencyType>> jpaDependencyCache = new HashMap<String, Map<String, JpaDependencyType>>();
		for (String className : classes) {
			if (UmlGeneratorUtility.isIncluded(className, hasAnyPatterns, includePatterns, excludePatterns)) {
				Class<?> loadClass = classLoader.loadClass(className);
				// check if its an persistent entity
				if (isPersistentEntity(loadClass)) {
					System.out.println("Parsing persistent class  " + loadClass.getSimpleName());
					// extract class info
					JpaClassModel classModel = new JpaClassModel(loadClass.getName());
					
					// determine class types
					extractJpaClassAnnotations(loadClass, classModel);

					// extract fields
					extractColumnsAndEntityDependencies(dependencyModel, loadClass, classModel, jpaDependencyCache, hasAnyPatterns, includePatterns, excludePatterns);
					
					// extract interfaces
					extractInterfaces(loadClass, classModel, hasAnyPatterns, includePatterns, excludePatterns);
					
					// extract parent class
					Class<?> superclass = loadClass.getSuperclass();
					if (superclass != null && !superclass.equals(Object.class)
							&& UmlGeneratorUtility.isIncluded(superclass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
						classModel.setParent(superclass.getName());
					}
					
					// add prepared class model to class diagram
					dependencyModel.addClass(classModel);
				}
			}
		}
	}
	
	private static boolean isPersistentEntity(Class<?> loadClass) {
		for (Annotation annotation : loadClass.getDeclaredAnnotations()) {
			String name = annotation.annotationType().getName();
			if (ANNOTATION_ENTITY.equals(annotation.annotationType().getName())
					|| ANNOTATION_MAPPED_SUPER_CLASS.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private static void extractJpaClassAnnotations(Class<?> loadClass, JpaClassModel classModel) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		classModel.setType(loadClass);
		classModel.setJpaEntityType(JpaEntityType.ENTITY);
		for (Annotation annotation : loadClass.getAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (ANNOTATION_MAPPED_SUPER_CLASS.equals(annotationType.getName())) {
				classModel.setJpaEntityType(JpaEntityType.MAPPED_SUPER_CLASS);
			}
			else if (ANNOTATION_TABLE.equals(annotationType.getName())) {
				classModel.setJpaEntityType(JpaEntityType.TABLE);
				// extract the table name
				classModel.setTableName(String.valueOf(annotationType.getDeclaredMethod("name").invoke(annotation, (Object[])null)));
			}
		}
	}

	private static void extractInterfaces(Class<?> loadClass, JpaClassModel classModel, boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
		for (Class interfaceClass : loadClass.getInterfaces()) {
			if (UmlGeneratorUtility.isIncluded(interfaceClass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
				classModel.addInterface(interfaceClass.getName());
			}
		}
	}

	private static void extractColumnsAndEntityDependencies(JpaDependencyDiagramModel dependencyModel, Class<?> loadClass, JpaClassModel classModel, Map<String, Map<String, JpaDependencyType>> jpaDependencyCache, boolean hasAnyPatterns, String[] includePatterns, String[] excludePatterns) {
		for (Field field : loadClass.getDeclaredFields()) {
			try {
				Class<?> fieldType = field.getType();
				if (UmlGeneratorUtility.isIncluded(fieldType.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
					// check for spring dependencies
					for (Annotation annotation : field.getDeclaredAnnotations()) {
						String annotationName = annotation.annotationType().getName();
						if (ANNOTATION_COLUMN.equals(annotationName)) {
							extractColumn(classModel, annotation);
						}
						else if (ANNOTATION_ONE_TO_MANY.equals(annotationName)) {
							for (Type type : ((ParameterizedType)field.getGenericType()).getActualTypeArguments()) {
								if (type instanceof Class) {
									updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, ((Class)type).getName(), JpaDependencyType.ONE_TO_MANY);
								}
							}
						}
						else if (ANNOTATION_MANY_TO_ONE.equals(annotationName)) {
							updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, fieldType.getName(), JpaDependencyType.MANY_TO_ONE);
						}
						else if (ANNOTATION_ONE_TO_ONE.equals(annotationName)) {
							updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, fieldType.getName(), JpaDependencyType.ONE_TO_ONE);
						}
						else if (ANNOTATION_MANY_TO_MANY.equals(annotationName)) {
							updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, fieldType.getName(), JpaDependencyType.MANY_TO_MANY);
						}
						else if (ANNOTATION_ID_COLUMN.equals(annotationName)) {
							classModel.addIdColumn(field.getName());
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
	
	private static void updateDependencyCache(JpaDependencyDiagramModel diagramModel, Map<String, Map<String, JpaDependencyType>> jpaDependencyCache, JpaClassModel classModel, String dependentClassName, JpaDependencyType dependencyType) {
		// check if the dependent class already has the mapping
		JpaClassModel dependentClass = diagramModel.getClass(dependentClassName);
		Map<String, JpaDependencyType> cache = jpaDependencyCache.get(dependentClassName);
		if (cache != null && dependentClass != null) {
			JpaDependencyType type = cache.get(classModel.getName());
			if (type != null) {
				int result = type.compareTo(dependencyType);
				// check if higher priority JPA mapping already exists between
				// the classes
				if (result < 0 || result == 0
						&& type != JpaDependencyType.ONE_TO_MANY) {
					return;
				}
				if (result == 0 && type == JpaDependencyType.ONE_TO_MANY) {
					// the one-to-many relationship is present both the sides so
					// convert it to many-to-many
					dependencyType = JpaDependencyType.MANY_TO_MANY;
				}
				// remove entry from the cache and JpaClassModel of the
				// associated class
				cache.remove(classModel.getName());
				dependentClass.removeJpaDependency(classModel.getName());
			}
		}
		
		// no mapping or lower priority mapping exists between the entities so create new entry in cache
		cache = jpaDependencyCache.get(classModel.getName());
		if (cache == null) {
			cache = new HashMap<String, JpaDependencyType>(3);
		}
		cache.put(dependentClass.getName(), dependencyType);
		classModel.addJpaDependency(dependentClassName, dependencyType);
	}

	protected static void extractColumn(JpaClassModel classModel, Annotation annotation) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// get column name from annotation attribute
		Method method = annotation.annotationType().getDeclaredMethod("name");
		Object value = method.invoke(annotation, (Object[])null);
		classModel.addColumn(String.valueOf(value));
	}

}
