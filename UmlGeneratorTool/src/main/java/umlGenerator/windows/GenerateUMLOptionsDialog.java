/**
 * 
 */
package umlGenerator.windows;

import net.sourceforge.plantuml.FileFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author sukenshah
 */
public class GenerateUMLOptionsDialog extends Dialog {
	
	private boolean packagesVisible = true;
	private Button packagesCheckbox;
	private boolean methodsVisible = true;
	private Button methodsCheckbox;
	private boolean fieldsVisible = false;
	private Button fieldsCheckbox;
	private boolean testsVisible = false;
	private Button testsCheckbox;
	private Text includePatternsArea;
	private Text excludePatternsArea;
	private String includePatterns;
	private String excludePatterns;
	private FileFormat fileFormat;
	private Combo fileFormatChoice;
	
	public GenerateUMLOptionsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 * @param parent
	 * @return
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		packagesCheckbox = new Button(container, SWT.CHECK);
	    packagesCheckbox.setLayoutData(new GridData(SWT.LEFT_TO_RIGHT, SWT.CENTER, false, false));
	    packagesCheckbox.setText("Packages");
	    packagesCheckbox.setSelection(true);
	    
		methodsCheckbox = new Button(container, SWT.CHECK);
	    methodsCheckbox.setLayoutData(new GridData(SWT.LEFT_TO_RIGHT, SWT.CENTER, false, false));
	    methodsCheckbox.setText("Methods");
	    methodsCheckbox.setSelection(true);
	    
	    fieldsCheckbox = new Button(container, SWT.CHECK);
	    fieldsCheckbox.setLayoutData(new GridData(SWT.LEFT_TO_RIGHT, SWT.CENTER, false, false));
	    fieldsCheckbox.setText("Fields");
	    fieldsCheckbox.setSelection(false);
	    
	    testsCheckbox = new Button(container, SWT.CHECK);
	    testsCheckbox.setLayoutData(new GridData(SWT.LEFT_TO_RIGHT, SWT.CENTER, false, false));
	    testsCheckbox.setText("Tests");
	    testsCheckbox.setSelection(false);
	    
	    Label includeLabel = new Label(container, SWT.NONE);
		includeLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false));
		includeLabel.setText("Include Patterns (comma separated)");
	    includePatternsArea = new Text(container, SWT.BORDER);
	    includePatternsArea.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, false, false));
	    includePatternsArea.setMessage("No pattern.");
	    
	    Label excludeLabel = new Label(container, SWT.NONE);
	    excludeLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false));
	    excludeLabel.setText("Exclude Patterns (command separated)");
	    excludePatternsArea = new Text(container, SWT.BORDER);
	    excludePatternsArea.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, false, false));
	    excludePatternsArea.setMessage("No pattern.");
	    
	    Label fileFormatLabel = new Label(container, SWT.None);
	    fileFormatLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false));
	    fileFormatLabel.setText("Export File Format");
	    fileFormatChoice = new Combo(container, SWT.READ_ONLY);
	    fileFormatChoice.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, false, false));
	    fileFormatChoice.setItems(new String[] {FileFormat.PNG.toString(),
	    		FileFormat.PDF.toString(),
	    		FileFormat.HTML.toString(),
	    		FileFormat.SVG.toString(),
	    		FileFormat.MJPEG.toString()});
	    fileFormatChoice.select(0);
	    
	    getShell().setText("Generate UML options");
	    return container;
	}
	
	/**
	 * Save the state of the checkboxes
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fieldsVisible = this.fieldsCheckbox.getSelection();
		methodsVisible = this.methodsCheckbox.getSelection();
		testsVisible = this.testsCheckbox.getSelection();
		packagesVisible = this.packagesCheckbox.getSelection();
		includePatterns = this.includePatternsArea.getText();
		excludePatterns = this.excludePatternsArea.getText();
		fileFormat = FileFormat.valueOf(this.fileFormatChoice.getItem(this.fileFormatChoice.getSelectionIndex()));
		super.okPressed();
	}
	
	public boolean areFieldsIncluded() {
		return fieldsVisible;
	}
	
	public boolean areMethodsIncluded() {
		return methodsVisible;
	}
	
	public boolean areTestsIncluded() {
		return testsVisible;
	}
	
	public boolean arePackagesIncluded() {
		return packagesVisible;
	}
	
	public String getIncludePattern() {
		return includePatterns.trim();
	}
	
	public String getExcludePatterns() {
		return excludePatterns.trim();
	}
	
	public FileFormat getFileFormat() {
		return fileFormat;
	}

}
