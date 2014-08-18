package umlGenerator.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.plexus.util.ExceptionUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import umlGenerator.MultipleProjectAction;
import umlGenerator.windows.GenerateUMLOptionsDialog;

import com.uml.generator.UmlGenerator;
import com.uml.generator.UmlOptions;

public class GenerateSpringClassDiagramAction extends MultipleProjectAction {
	
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger("GenerateSpringClassDiagramAction");
	
	/**
	 * Generate UML for the given project.
	 * 
	 * @param javaProject
	 *            The project for which UML is to be generated.
	 * @param shell
	 *            The current shell used to display progress monitors.
	 * @throws CoreException
	 *             If unable to deploy the project.
	 * @throws MalformedURLException 
	 * @throws DeployFailedException
	 *             If unable to deploy the project.
	 */
	@Override
	protected void generateUml(final IProject project, final Shell shell) throws CoreException, MalformedURLException {
		LOGGER.log(Level.ALL, "Starting generation of spring class diagram for project : " + project.getName());
		
		// create a job such that the long running process can run in background.
		Job generationJob = new Job("Generation of Spring class diagram") {
			
			@Override
			protected IStatus run(IProgressMonitor progress) {
				boolean success = true;
				String reason = "";
				progress.beginTask("Starting generation of spring class diagram.", 100);
				
				// get user options for uml generation
				progress.subTask("Get user options.");
				final GenerateUMLOptionsDialog dialog = new GenerateUMLOptionsDialog(shell);
				// create sync execution to make sure that the UI updates are performed in main UI thread.
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						dialog.open();
					}
				});
				
				// only proceed with UML generation if user pressed OK
				if (dialog.getReturnCode() == 0) {
					progress.worked(10);
					
					try {
						IJavaProject javaProject = JavaCore.create(project);
						progress.subTask("Export the project " + project.getName() + " jar file.");
						IPath deployedJarFile = exportProjectJar(project, shell);
						progress.worked(40);
						
						progress.subTask("Extracting dependent jars for project " + project.getName());
						List<URL> urls = getDepedentJars(project, javaProject, javaProject.getRawClasspath(), deployedJarFile);
						progress.worked(10);
						
						// create output directory
						File umlDir = project.getFolder("uml").getLocation().toFile();
						if (!umlDir.exists()) {
							umlDir.mkdir();
						}
						
						progress.subTask("Generating Spring class diagram.");
						UmlOptions options = new UmlOptions();
						options.setPackagesIncluded(dialog.arePackagesIncluded());
						options.setTestIncluded(dialog.areTestsIncluded());
						options.setFieldsIncluded(dialog.areFieldsIncluded());
						options.setMethodsIncluded(dialog.areMethodsIncluded());
						options.setIncludePatterns(dialog.getIncludePattern());
						options.setExcludePatterns(dialog.getExcludePatterns());
						UmlGenerator.generateSpringClassDiagram(urls.get(0), urls.toArray(new URL[] {}), project.getName(), project.getFolder("uml").getLocation().toFile().getPath(), options);
						progress.worked(40);
						LOGGER.log(Level.ALL, "Finished generation of spring class diagram for project : " + project.getName());
					}
					catch (Exception e) {
						e.printStackTrace();
						reason = ExceptionUtils.getStackTrace(e);
						success = false;
					}
					
					showResultDialog(shell, project.getName(), success, reason);
				}
				return Status.OK_STATUS;
			}
		}; 
		generationJob.setPriority(Job.SHORT);
		generationJob.schedule();
	}

}
