/**
 * 
 */
package umlGenerator.windows;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author sukenshah
 */
public class GenerateComponentDiagramOptionsDialog extends Dialog {
	
	private Text includePatternsArea;
	private Text excludePatternsArea;
	private String includePatterns;
	private String excludePatterns;

	public GenerateComponentDiagramOptionsDialog(Shell parentShell) {
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
	    
	    getShell().setText("Generate Component Diagram options");
	    return container;
	}
	
	/**
	 * Save the state of the checkboxes
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		includePatterns = this.includePatternsArea.getText();
		excludePatterns = this.excludePatternsArea.getText();
		super.okPressed();
	}
	
	public String getIncludePattern() {
		return includePatterns.trim();
	}
	
	public String getExcludePatterns() {
		return excludePatterns.trim();
	}
	

}
