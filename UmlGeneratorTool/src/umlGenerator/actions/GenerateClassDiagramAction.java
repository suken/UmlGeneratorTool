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
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import umlGenerator.MultipleProjectAction;
import umlGenerator.windows.GenerateUMLOptionsDialog;

import com.uml.generator.UmlGenerator;
import com.uml.generator.UmlOptions;

public class GenerateClassDiagramAction extends MultipleProjectAction {
	
	private static final Logger LOGGER = Logger.getLogger("GenerateClassDiagramAction");
	
	/**
	 * {@inheritDoc}
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * Generate UML diagram for the given project.
	 * 
	 * @param javaProject
	 *            The project for which the UML is to be generated.
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
		LOGGER.log(Level.INFO, "Starting generation of class diagram for project : " + project.getName());
		
		// create a job such that the long running process can run in background.
		Job generateJob = new Job("Class Diagram Job") {
			
			@Override
			protected IStatus run(IProgressMonitor progress) {
				boolean success = true;
				String reason = "";
				
				progress.beginTask("Starting the task.", 100);
				progress.subTask("Getting user UML generation options.");
				
				// get user options for uml generation
				final GenerateUMLOptionsDialog dialog = new GenerateUMLOptionsDialog(shell);
				// create sync execution to make sure that the UI updates are performed in main UI thread.
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						dialog.open();
					}
				});
				IJavaProject javaProject = JavaCore.create(project);
				progress.worked(10);
				
				try {
					progress.subTask("Exporting project " + project.getName() + " to jar file.");
					final IPath deployedJarFile = exportProjectJar(project, shell);
					progress.worked(40);
					
					progress.subTask("Extracting dependent jars for " + project.getName());
					List<URL> urls = getDepedentJars(project, javaProject, javaProject.getRawClasspath(), deployedJarFile);
					progress.worked(10);
					
					// create output directory
					File umlDir = project.getFolder("uml").getLocation().toFile();
					if (!umlDir.exists()) {
						umlDir.mkdir();
					}
					
					progress.subTask("Generating class diagram for " + project.getName());
					UmlOptions options = new UmlOptions();
					options.setPackagesIncluded(dialog.arePackagesIncluded());
					options.setTestIncluded(dialog.areTestsIncluded());
					options.setFieldsIncluded(dialog.areFieldsIncluded());
					options.setMethodsIncluded(dialog.areMethodsIncluded());
					options.setIncludePatterns(dialog.getIncludePattern());
					options.setExcludePatterns(dialog.getExcludePatterns());
					UmlGenerator.generateClassDiagram(urls.get(0), urls.toArray(new URL[] {}), project.getName(), umlDir.getPath(), options);
					LOGGER.log(Level.INFO, "Finished generation of class diagram for project : " + project.getName());
					progress.worked(40);
				}
				catch (Exception e) {
					// capture exception and show it to user
					e.printStackTrace();
					reason = ExceptionUtils.getStackTrace(e);
					success = false;
				}
				
				showResultDialog(shell, project.getName(), success, reason);
				return Status.OK_STATUS;
			}
		};
		generateJob.setPriority(Job.SHORT);
		generateJob.schedule();
	}

}
