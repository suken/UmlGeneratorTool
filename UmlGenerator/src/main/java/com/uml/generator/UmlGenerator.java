/**
 *
 */
package com.uml.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceFileReader;

import com.uml.generator.classDiagram.ClassDiagramGenerator;
import com.uml.generator.classDiagram.models.ClassDiagramModel;
import com.uml.generator.componentDiagram.ComponentDiagramGenerator;
import com.uml.generator.jpa.JpaMappingDiagramGenerator;
import com.uml.generator.spring.SpringDependencyDiagramGenerator;

/**
 * @author sukenshah
 */
public class UmlGenerator {

	private static final Logger LOGGER = Logger.getLogger("UmlGenerator");

	/**
	 * Generate the Spring class diagram for the given project jar. The method generates following:
	 * <ul>
	 * <li> plant uml text file (*.plantuml)
	 * <li> class diagram UML file (*.png)
	 * </ul>
	 *
	 * The class diagram generates the followings:
	 * <ul>
	 * <li> Fields if {@code fieldsIncluded} set to TRUE
	 * <li> Methods if {@code methodIncluded} set TRUE
	 * <li> Parent class depedencies
	 * <li> Implemented interfaces
	 * <li> Composite class dependencies
	 * </ul>
	 *
	 * <b> Warning </b><br>
	 * If the component diagram is too complicated then the GraphViz may not generate the PNG file. Try opening the plantuml file in plantuml eclipse plugin.
	 * <p>
	 *
	 * @param projectJarUrl URL of the project jar for which the class diagram is to be generated.
	 * @param jarURLs dependent jars
	 * @param packagesIncluded Are packages included?
	 * @param fieldsIncluded Are fields included?
	 * @param methodsIncluded Are methods included?
	 * @param testIncluded Are tests included?
	 * @param projectName name of the project
	 * @param umlDirPath Output directory path
	 * @throws Exception If anything goes wrong just raise it to the caller.
	 */
	public static void generateClassDiagram(URL projectJarUrl, URL[] jarURLs, String projectName, String umlDirPath, UmlOptions options) throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// parse all the classes from the jars
			URLClassLoader classLoader = new URLClassLoader(jarURLs);
			Thread.currentThread().setContextClassLoader(classLoader);
			ClassDiagramModel classDiagramModel = ClassDiagramGenerator.generateClassDependencies(classLoader, projectJarUrl, options);
			String uml = classDiagramModel.getUml().replace("$", "_Inner");

			// generate the UML and plant uml text files
			String sourceFilePath = exportToPlantUMLFile(projectName, umlDirPath, uml, "_ClassDiagram");
			exportToFile(projectName, umlDirPath, sourceFilePath, "_ClassDiagram", options.getFileFormat());
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	/**
	 * Generate the Spring class diagram for the given project jar. The method generates following:
	 * <ul>
	 * <li> plant uml text file (*.plantuml)
	 * <li> class diagram UML file (*.png)
	 * </ul>
	 *
	 * In addition to plain class diagram, the spring class diagram also generates followings:
	 * <ul>
	 * <li> Autowired depedencies
	 * <li> Required depedencies
	 * <li> Resource depedencies
	 * <li> Component classes
	 * <li> Controller classes
	 * <li> Service classes
	 * <li> Repository classes
	 * <li> Bean classes
	 * <li> Configuration classes
	 * <li> Additional comments are provided for class level annotations.
	 * </ul>
	 *
	 * <b> Warning </b><br>
	 * If the component diagram is too complicated then the GraphViz may not generate the PNG file. Try opening the plantuml file in plantuml eclipse plugin.
	 * <p>
	 *
	 * @param projectJarUrl URL of the project jar for which the class diagram is to be generated.
	 * @param jarURLs dependent jars
	 * @param packagesIncluded Are packages included?
	 * @param fieldsIncluded Are fields included?
	 * @param methodsIncluded Are methods included?
	 * @param testIncluded Are tests included?
	 * @param projectName name of the project
	 * @param umlDirPath Output directory path
	 * @throws Exception If anything goes wrong just raise it to the caller.
	 */
	public static void generateSpringClassDiagram(URL projectJarUrl, URL[] jarURLs, String projectName, String umlDirPath, UmlOptions options) throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// parse all the classes from the jars
			URLClassLoader classLoader = new URLClassLoader(jarURLs);
			Thread.currentThread().setContextClassLoader(classLoader);
			String uml = SpringDependencyDiagramGenerator.generateSpringDependencies(classLoader, projectJarUrl, options).replace("$", "_Inner");

			// generate the UML and plant uml text files
			exportToPlantUMLFile(projectName, umlDirPath, uml, "_SpringDependencyDiagram");
			exportToFile(projectName, umlDirPath, uml, "_SpringDependencyDiagram", options.getFileFormat());
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	/**
	 * Generates the component diagram for the given MAVEN project. The method generates the following:
	 * <ul>
	 * <li> plantuml text file (*.plantuml)
	 * <li> component diagram image file (*.png)
	 * </ul>
	 *
	 * <b>NOTE:</b><br>
	 * The method recursively inspects the given source directory to parse all POM files.
	 * <p>
	 *
	 * <b> Warning </b><br>
	 * If the component diagram is too complicated then the GraphViz may not generate the PNG file. Try opening the plantuml file in plantuml eclipse plugin.
	 *
	 * @param srcDir directory which contains the POM files.
	 * @param projectName Name of the project for which the component diagram is to be generated.
	 * @param includePatterns regular expression of included artifact id patterns
	 * @param excludePatterns regular expression of excluded artifact id patterns
	 * @param umlDirPath output directory to write UML
	 * @throws Exception If anything goes wrong then raise it to the caller.
	 */
	public static void generateComponentDiagram(String srcDir, String projectName, String umlDirPath, UmlOptions options) throws Exception {
		String uml = ComponentDiagramGenerator.generateComponentDiagram(srcDir, options);
		// generate the UML and plant uml text files
		exportToPlantUMLFile(projectName, umlDirPath, uml, "_ComponentDiagram");
		exportToFile(projectName, umlDirPath, uml, "_ComponentDiagram", options.getFileFormat());
	}

	/**
	 * Generates the JPA mapping diagram for the given project. The method generates the following:
	 * <ul>
	 * <li> plantuml text file (*.plantuml)
	 * <li> component diagram image file (*.png)
	 * </ul>
	 *
	 * <b> Warning </b><br>
	 * If the component diagram is too complicated then the GraphViz may not generate the PNG file. Try opening the plantuml file in plantuml eclipse plugin.
	 *
	 * @param srcDir directory which contains the POM files.
	 * @param projectName Name of the project for which the component diagram is to be generated.
	 * @param includePatterns regular expression of included artifact id patterns
	 * @param excludePatterns regular expression of excluded artifact id patterns
	 * @param umlDirPath output directory to write UML
	 * @throws Exception If anything goes wrong then raise it to the caller.
	 */
	public static void generateJPAMappingDiagram(URL projectJarUrl, URL[] jarURLs, String projectName, String umlDirPath, UmlOptions options) throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// parse all the classes from the jars
			URLClassLoader classLoader = new URLClassLoader(jarURLs);
			Thread.currentThread().setContextClassLoader(classLoader);
			String uml = JpaMappingDiagramGenerator.generateJpaDependencies(classLoader, projectJarUrl, options).replace("$", "_Inner");

			// generate the UML and plant uml text files
			exportToPlantUMLFile(projectName, umlDirPath, uml, "_JPAMappingDiagram");
			exportToFile(projectName, umlDirPath, uml, "_JPAMappingDiagram", options.getFileFormat());
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	private static String exportToPlantUMLFile(String projectName, String umlDirPath, String uml, String filePostfix) throws IOException {
		LOGGER.log(Level.INFO, "Writing PlantUML string to *.plantuml file");
		String umlStringFile = umlDirPath + File.separator + projectName + filePostfix + ".plantuml";
		// write plant UML text file
		BufferedWriter writer = new BufferedWriter(new FileWriter(umlStringFile));
		writer.write(uml);
		writer.flush();
		writer.close();
		LOGGER.log(Level.INFO, "The UML diagram is generated under " + umlDirPath);
		return umlStringFile;
	}

	private static void exportToFile(String projectName, String umlDirPath, String sourceFilePath, String filePostfix, FileFormat format) throws IOException {
		LOGGER.log(Level.INFO, "Writing PlantUML string to *." + format + " file");
//		String umlFile = umlDirPath + File.separator + projectName + filePostfix + ".png";
//		File umlDir = new File(umlDirPath);
//		if (umlDir.exists()) {
//			umlDir.delete();
//		}

		// write the plant UML image file
		SourceFileReader reader = new SourceFileReader(new File(sourceFilePath), new File(umlDirPath), new FileFormatOption(format));
		reader.getGeneratedImages();
	}

}
