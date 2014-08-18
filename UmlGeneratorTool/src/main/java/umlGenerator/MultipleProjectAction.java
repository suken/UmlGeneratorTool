package umlGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Abstract action that provides support for operating on multiple actions at the
 * same time.
 * <p>
 * Subclasses of this class can get the projects selected by the user by calling
 * {@link MultipleProjectAction#getSelectedProjects()}
 *
 */
public abstract class MultipleProjectAction implements IObjectActionDelegate {
	
	private static final Logger LOGGER = Logger.getLogger("MultipleProjectAction");

	/**
	 * Projects selected by the user.
	 */
	private List<IProject> selectedProjects;

	/**
	 * Get the list of currently selected projects.
	 * 
	 * @return the list of currently selected projects.
	 */
	protected List<IProject> getSelectedProjects() {
		return selectedProjects;
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		boolean enableAction = true;

		selectedProjects = new LinkedList<IProject>();

		IStructuredSelection selected = (IStructuredSelection) selection;

		for (Object resource : selected.toArray()) {
			try {
				if (resource instanceof IProject
						&& ((IProject)resource).hasNature(JavaCore.NATURE_ID)) {
					selectedProjects.add((IProject) resource);
				} else {
					enableAction = false;
					break;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		action.setEnabled(enableAction);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void run(IAction action) {
		Shell shell = new Shell();

		List<String> failedProjectsMessages = new LinkedList<String>();

		PlatformUI.getWorkbench().saveAllEditors(true);

		for (IProject project : getSelectedProjects()) {
			try {
				generateUml(project, shell);
			} catch (Exception exception) {
				LOGGER.log(Level.SEVERE, "UML not generated for project : " + project.getName());
				exception.printStackTrace();
			}
		}

		if (!failedProjectsMessages.isEmpty()) {
			StringBuilder message = new StringBuilder();

			for (String projectMessage : failedProjectsMessages) {
				message.append(projectMessage);
				message.append("\n");
			}

			String errorTitle = failedProjectsMessages.size() > 1 ? "Unable to generate UML for Projects "
					: "Unable to generate UML for Project";

			MessageDialog
					.openInformation(shell, errorTitle, message.toString());
		}
	}
	
	protected IPath exportProjectJar(IProject project, final Shell shell) throws Exception {
		IPath deployedJarFile = generateProjectJar(project);
		IPath deployedProjectLocation = deployedJarFile.removeLastSegments(1);

		File destinationFile = deployedProjectLocation.toFile();
		// make sure the destination directory exists
		if (!destinationFile.exists()) {
			destinationFile.mkdirs();
		}
		// copy any files in the root of the project directory
		IResource[] resources = project.members();
		for (IResource resource : resources) {
			if (resource.getType() == IResource.FILE
					&& !resource.getName().startsWith(".")) {
				IPath deployedResouceFile = deployedProjectLocation
						.append(resource.getName());
				copyFile(resource.getLocation().toFile(),
						deployedResouceFile.toFile());
			}
		}

		IFile file = project.getFile(".project");
		JarPackageData jarConfig = createJar(deployedJarFile, new IFile[] {file});
		jarConfig.setOverwrite(true);
		final IJarExportRunnable runnable = jarConfig.createJarExportRunnable(shell);

		// create sync execution to make sure that the UI updates are performed in main UI thread. 
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					new ProgressMonitorDialog(shell).run(false, true, runnable);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		return deployedJarFile;
	}
	
	/**
	 * Get the deployed location of a project
	 * 
	 * @param project
	 *            The project
	 * @return The location of the project on the disk
	 */
	private IPath generateProjectJar(IProject project) {
		String jarFileLocation = File.separator + project.getName();
		IPath deployedJarFile = project.getParent().getLocation().append(File.separator + "uml" + File.separator + project.getName() + File.separator).append(jarFileLocation);
		return deployedJarFile.addFileExtension("jar");
	}
	
	/**
	 * Creates a new .jar file at location path containing all of the output
	 * folders of the project which contains
	 * 
	 * @param path
	 *            Output location of the file
	 * @param filestoExport
	 *            File within the project to export.
	 * @return The packaged jar file configuration.
	 */
	private JarPackageData createJar(IPath path, IFile[] filestoExport) {
		JarPackageData description = new JarPackageData();
		description.setJarLocation(path);
		description.setSaveManifest(false);
		description.setElements(filestoExport);
		description.setCompress(true);
		description.setExportClassFiles(true);
		description.setExportJavaFiles(false);
		description.setExportWarnings(true);
		description.setExportOutputFolders(true);
		return description;
	}

	/**
	 * Copies a file from one location to another
	 * 
	 * @param srcFile
	 *            The absolute location of the source file.
	 * @param dstFile
	 *            The absolute location of the destination file.
	 * @throws DeployFailedException
	 *             If any error occurs.
	 */
	@SuppressWarnings("resource")
	protected void copyFile(File srcFile, File dstFile) throws IOException {
		// Create channel for the source
		FileChannel srcChannel = new FileInputStream(srcFile).getChannel();

		// Create channel for the destination
		FileChannel dstChannel = new FileOutputStream(dstFile).getChannel();

		// Copy file contents from source to destination
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		// Close the channels
		srcChannel.close();
		dstChannel.close();
	}
	
	@SuppressWarnings("deprecation")
	protected List<URL> getDepedentJars(IProject project, IJavaProject javaProject, IClasspathEntry[] classpathEntries,	IPath deployedJarFile) throws MalformedURLException, JavaModelException {
		List<URL> urls = new ArrayList<URL>();
		urls.add(deployedJarFile.toFile().toURL());
		for (IClasspathEntry classpathEntry : classpathEntries) {
			urls.addAll(extractClassPathEntry(project, javaProject, classpathEntry, true));
		}
		return urls;
	}
	
	@SuppressWarnings("deprecation")
	private List<URL> extractClassPathEntry(IProject project, IJavaProject javaProject, IClasspathEntry classpathEntry, boolean relativeProjectPath) throws MalformedURLException, JavaModelException {
		List<URL> urls = new ArrayList<URL>();
		if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
			URL url = null;
			if (relativeProjectPath) {
				url = project.getLocation().append(classpathEntry.getPath().removeFirstSegments(1).makeRelative()).toFile().toURL();
			}
			else {
				url = classpathEntry.getPath().toFile().toURL();
			}
			System.err.println("Jar entry added : " + url.toString());
			urls.add(url);
		}
		else if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
			// its some sort of container like JRE or MAVEN so please extract all the entries from container
			IClasspathContainer container = JavaCore.getClasspathContainer(classpathEntry.getPath(), javaProject);
			for (IClasspathEntry entry : container.getClasspathEntries()) {
				urls.addAll(extractClassPathEntry(project, javaProject, entry, false));
			}
		}
		//TODO: To check how to add project dependencies into ClassLoader.
//		else if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
//			urls.add(project.getLocation().append(classpathEntry.getPath().removeFirstSegments(1).makeRelative()).toFile().toURL());
//		}
		return urls;
	}
	
	protected void showResultDialog(final Shell shell, final String projectName, final boolean success, final String reason) {
		// create sync execution to make sure that the UI updates are performed in main UI thread. 
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageBox messageDialog = new MessageBox(shell, SWT.OK);
				messageDialog.setText("UML diagram generation");
				messageDialog.setMessage(success ? "UML digram is generated successfully under " + projectName + "/uml directory." : "Opps some issue generating UML diagram. Contact Suken Shah. \n\n " + reason);
				messageDialog.open();
			}
		});
	}
	
	protected abstract void generateUml(IProject javaProject, Shell shell) throws Exception;
	
	/**
	 * {@inheritDoc}
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
