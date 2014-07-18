package umlGenerator.actions;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.plexus.util.ExceptionUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import umlGenerator.MultipleProjectAction;
import umlGenerator.windows.GenerateComponentDiagramOptionsDialog;

import com.uml.generator.UmlGenerator;

/**
 * Action implementing the deploy project functionality.
 * <p>
 * This action will deploy the selected projects to the correct location under REF_JHMI.
 * If the deploy location of the project cannot be determined an error will be displayed
 * to the user.
 */
public class GenerateComponentDiagramAction extends MultipleProjectAction {
	
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger("GenerateComponentDiagramAction");
	
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
	 * Deploy a project to it's deployed location under REF_JHMI.
	 * 
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
	protected void generateUml(final IProject project, final Shell shell)	throws CoreException, MalformedURLException {
		LOGGER.log(Level.ALL, "Starting generation of component diagram for project : " + project.getName());
		
		// create a job such that the long running process can run in background.
		Job generateJob = new Job("Component Diagram Job") {

			@Override
			protected IStatus run(IProgressMonitor progress) {
				boolean success = true;
				String reason = "";
				
				progress.beginTask("Start generating Component Diagram", 100);
				progress.subTask("Getting user options.");
				final GenerateComponentDiagramOptionsDialog dialog = new GenerateComponentDiagramOptionsDialog(shell);
				// create sync execution to make sure that the UI updates are performed in main UI thread.
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						dialog.open();
					}
				});
				progress.worked(20);

				try {
					progress.subTask("Generating Component Diagram.");
					UmlGenerator.generateComponentDiagram(project.getLocation().toFile().getPath(),
							project.getName(),
							dialog.getIncludePattern(),
							dialog.getExcludePatterns(),
							project.getFolder("uml").getLocation().toFile().getPath());
					progress.worked(80);
					LOGGER.log(Level.ALL, "Finished generation of component diagram for project : " + project.getName());
				}
				catch (Exception e) {
					success = false;
					reason = ExceptionUtils.getStackTrace(e);
				}

				showResultDialog(shell, project.getName(), success, reason);
				return Status.OK_STATUS;
			}
			
		};
		generateJob.setPriority(Job.SHORT);
		generateJob.schedule();
	}
	
}
