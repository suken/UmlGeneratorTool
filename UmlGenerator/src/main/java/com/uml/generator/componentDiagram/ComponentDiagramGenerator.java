/**
 *
 */
package com.uml.generator.componentDiagram;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.uml.generator.UmlOptions;
import com.uml.generator.componentDiagram.models.ComponentDiagramModel;
import com.uml.generator.componentDiagram.models.ComponentModel;
import com.uml.generator.componentDiagram.models.ComponentType;

/**
 * @author shahs
 */
public class ComponentDiagramGenerator {

	public static String generateComponentDiagram(String srcDir, UmlOptions options) throws FileNotFoundException, IOException, XmlPullParserException {
		String uml = "";
		// extract all POM files from the source dir
		List<File> pomFiles = getAllPOMFiles(new File(srcDir));
		uml = processPomFiles(pomFiles, options.getIncludePatterns(), options.getExcludePatterns());
		return uml;
	}

	private static String processPomFiles(List<File> pomFiles, String includePatternsString, String excludePatternsString) throws FileNotFoundException, IOException, XmlPullParserException {
		ComponentDiagramModel diagramModel = new ComponentDiagramModel();
		MavenXpp3Reader reader = new MavenXpp3Reader();
		String[] includePatterns = includePatternsString.split(",");
		String[] excludePatterns = excludePatternsString.split(",");

		// process all POM files
		for (File pomFile : pomFiles) {
			Model model = reader.read(new FileInputStream(pomFile));
			MavenProject project = new MavenProject(model);

			// check if the packaging is jar
			if (!project.getPackaging().equalsIgnoreCase("pom")) {
				String groupId = project.getGroupId();
				ComponentModel component = new ComponentModel(project.getArtifactId(), ComponentType.SOURCE);
				diagramModel.addComponent(component, groupId);

				// find out dependencies
				for (Dependency dependency : project.getDependencies()) {
					// only add the dependency if it satisfies the include/exclude patterns
					if (isIncluded(dependency.getArtifactId(), includePatterns, excludePatterns)) {
						if (!diagramModel.containsComponent(dependency.getArtifactId(), dependency.getGroupId())) {
							diagramModel.addComponent(new ComponentModel(dependency.getArtifactId(), ComponentType.SOURCE), dependency.getGroupId());
						}
						component.addDepedentComponent(dependency.getArtifactId());
					}
				}
			}
		}
		return diagramModel.getUml();
	}

	private static boolean isIncluded(String artifactId, String[] includePatterns, String[] excludePatterns) {
		if (includePatterns.length == 1 && excludePatterns.length == 1
				&& includePatterns[0].isEmpty() && excludePatterns[0].isEmpty()) {
			return true;
		}

		// check exclude patterns
		for (String patternText : excludePatterns) {
			if (!patternText.isEmpty()) {
				Pattern pattern = Pattern.compile(patternText);
				Matcher matcher = pattern.matcher(artifactId);
				if (matcher.matches()) {
					return false;
				}
			}
		}

		// check for include pattern
		for (String patternText : includePatterns) {
			if (!patternText.isEmpty()) {
				Pattern pattern = Pattern.compile(patternText);
				Matcher matcher = pattern.matcher(artifactId);
				if (matcher.matches()) {
					return true;
				}
			}
		}
		// by default the artifact is included
		return true;
	}

	private static List<File> getAllPOMFiles(File srcDir) {
		List<File> pomFiles = new ArrayList<File>();
		if (srcDir.exists()) {
			// get all POM files at this level
			File[] files = srcDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.isFile()
							&& file.getName().equalsIgnoreCase("pom.xml") ? true
							: false;
				}
			});
			if (files != null) {
				pomFiles.addAll(Arrays.asList(files));
			}

			// inspect the sub directories
			File[] directories = srcDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});

			if (directories != null) {
				// recursive call to get all POM files
				for (File dir : directories) {
					pomFiles.addAll(getAllPOMFiles(dir));
				}
			}
		}
		return pomFiles;
	}

}
