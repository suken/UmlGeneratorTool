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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

	public static String generateJpaDependencies(final URLClassLoader classLoader, final URL jarUrl, final UmlOptions options) {
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

	static void extractJpaClasses(final URLClassLoader classLoader, final List<String> classes, final JpaDependencyDiagramModel dependencyModel, final String includePatternString, final String excludePatternString) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
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

	private static boolean isPersistentEntity(final Class<?> loadClass) {
		return loadClass.isAnnotationPresent(Entity.class)
				|| loadClass.isAnnotationPresent(MappedSuperclass.class);
	}

	private static void extractJpaClassAnnotations(final Class<?> loadClass, final JpaClassModel classModel) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		classModel.setType(loadClass);
		classModel.setJpaEntityType(JpaEntityType.ENTITY);
		if (loadClass.isAnnotationPresent(MappedSuperclass.class)) {
			classModel.setJpaEntityType(JpaEntityType.MAPPED_SUPER_CLASS);
		}
		else if (loadClass.isAnnotationPresent(Table.class)) {
			classModel.setJpaEntityType(JpaEntityType.TABLE);
			// extract the table name
			Annotation annotation = loadClass.getAnnotation(Table.class);
			classModel.setTableName(String.valueOf(annotation.annotationType()
					.getDeclaredMethod("name")
					.invoke(annotation, (Object[]) null)));
		}
	}

	private static void extractInterfaces(final Class<?> loadClass, final JpaClassModel classModel, final boolean hasAnyPatterns, final String[] includePatterns, final String[] excludePatterns) {
		for (Class interfaceClass : loadClass.getInterfaces()) {
			if (UmlGeneratorUtility.isIncluded(interfaceClass.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
				classModel.addInterface(interfaceClass.getName());
			}
		}
	}

	private static void extractColumnsAndEntityDependencies(final JpaDependencyDiagramModel dependencyModel, final Class<?> loadClass, final JpaClassModel classModel, final Map<String, Map<String, JpaDependencyType>> jpaDependencyCache, final boolean hasAnyPatterns, final String[] includePatterns, final String[] excludePatterns) {
		for (Field field : loadClass.getDeclaredFields()) {
			try {
				Class<?> fieldType = field.getType();
				if (UmlGeneratorUtility.isIncluded(fieldType.getName(), hasAnyPatterns, includePatterns, excludePatterns)) {
					// check for spring dependencies
					if (field.isAnnotationPresent(Column.class)) {
						extractColumn(classModel,
								field.getAnnotation(Column.class));
					}
					else if (field.isAnnotationPresent(OneToMany.class)) {
						for (Type type : ((ParameterizedType)field.getGenericType()).getActualTypeArguments()) {
							if (type instanceof Class) {
								updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, ((Class<?>) type).getName(), JpaDependencyType.ONE_TO_MANY);
							}
						}
					}
					else if (field.isAnnotationPresent(ManyToOne.class)) {
						updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, fieldType.getName(), JpaDependencyType.MANY_TO_ONE);
					}
					else if (field.isAnnotationPresent(OneToOne.class)) {
						updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, fieldType.getName(), JpaDependencyType.ONE_TO_ONE);
					}
					else if (field.isAnnotationPresent(ManyToMany.class)) {
						updateDependencyCache(dependencyModel, jpaDependencyCache, classModel, fieldType.getName(), JpaDependencyType.MANY_TO_MANY);
					}
					else if (field.isAnnotationPresent(Id.class)) {
						classModel.addIdColumn(field.getName());
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

	private static void updateDependencyCache(final JpaDependencyDiagramModel diagramModel, final Map<String, Map<String, JpaDependencyType>> jpaDependencyCache, final JpaClassModel classModel, final String dependentClassName, JpaDependencyType dependencyType) {
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

	protected static void extractColumn(final JpaClassModel classModel, final Annotation annotation) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// get column name from annotation attribute
		Method method = annotation.annotationType().getDeclaredMethod("name");
		Object value = method.invoke(annotation, (Object[])null);
		classModel.addColumn(String.valueOf(value));
	}

}
