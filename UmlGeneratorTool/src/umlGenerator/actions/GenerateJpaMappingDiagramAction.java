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
import umlGenerator.windows.GenerateComponentDiagramOptionsDialog;

import com.uml.generator.UmlGenerator;
import com.uml.generator.UmlOptions;

public class GenerateJpaMappingDiagramAction extends MultipleProjectAction {
	
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger("GenerateJpaMappingDiagramAction");
	
	/**
	 * {@inheritDoc}
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	
	@Override
	protected String getNatureId() {
		return JavaCore.NATURE_ID;
	}

	/**
	 * @param javaProject
	 *            The project to deploy
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
		LOGGER.log(Level.ALL, "Starting generation of JPA mapping diagram for project : " + project.getName());
		
		// create a job such that the long running process can run in background.
		Job generationJob = new Job("Generation of JPA mapping diagram") {
			
			@Override
			protected IStatus run(IProgressMonitor progress) {
				boolean success = true;
				String reason = "";
				progress.beginTask("Starting generation of JPA mapping diagram.", 100);
				
				// get user options for uml generation
				progress.subTask("Get user options.");
				final GenerateComponentDiagramOptionsDialog dialog = new GenerateComponentDiagramOptionsDialog(shell);
				// create sync execution to make sure that the UI updates are performed in main UI thread.
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						dialog.open();
					}
				});
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
					
					progress.subTask("Generating JPA mapping diagram.");
					UmlOptions options = new UmlOptions();
					options.setIncludePatterns(dialog.getIncludePattern());
					options.setExcludePatterns(dialog.getExcludePatterns());
					UmlGenerator.generateJPAMappingDiagram(urls.get(0), urls.toArray(new URL[] {}),
							project.getName(), project.getFolder("uml").getLocation().toFile().getPath(), options);
					progress.worked(40);
					LOGGER.log(Level.ALL, "Finished generation of JPA mapping diagram for project : " + project.getName());
				}
				catch (Exception e) {
					e.printStackTrace();
					reason = ExceptionUtils.getStackTrace(e);
					success = false;
				}
				
				showResultDialog(shell, project.getName(), success, reason);
				return Status.OK_STATUS;
			}
		}; 
		generationJob.setPriority(Job.SHORT);
		generationJob.schedule();
	}

}
