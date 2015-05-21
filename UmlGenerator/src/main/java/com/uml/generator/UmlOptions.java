/**
 *
 */
package com.uml.generator;

import lombok.Getter;
import lombok.Setter;
import net.sourceforge.plantuml.FileFormat;

/**
 * @author Suken Shah
 */
@Getter
@Setter
public class UmlOptions {
	private boolean packagesIncluded;
	private boolean fieldsIncluded;
	private boolean methodsIncluded;
	private boolean testIncluded;
	private String includePatterns;
	private String excludePatterns;
	private FileFormat fileFormat;
}
