/**
 * 
 */
package com.uml.generator;

/**
 * @author Suken Shah
 *
 */
public class UmlOptions {

	private boolean packagesIncluded;
	private boolean fieldsIncluded;
	private boolean methodsIncluded;
	private boolean testIncluded;
	private String includePatterns;
	private String excludePatterns;
	/**
	 * @return the packagesIncluded
	 */
	public boolean isPackagesIncluded() {
		return packagesIncluded;
	}
	/**
	 * @param packagesIncluded the packagesIncluded to set
	 */
	public void setPackagesIncluded(boolean packagesIncluded) {
		this.packagesIncluded = packagesIncluded;
	}
	/**
	 * @return the fieldsIncluded
	 */
	public boolean isFieldsIncluded() {
		return fieldsIncluded;
	}
	/**
	 * @param fieldsIncluded the fieldsIncluded to set
	 */
	public void setFieldsIncluded(boolean fieldsIncluded) {
		this.fieldsIncluded = fieldsIncluded;
	}
	/**
	 * @return the methodsIncluded
	 */
	public boolean isMethodsIncluded() {
		return methodsIncluded;
	}
	/**
	 * @param methodsIncluded the methodsIncluded to set
	 */
	public void setMethodsIncluded(boolean methodsIncluded) {
		this.methodsIncluded = methodsIncluded;
	}
	/**
	 * @return the testIncluded
	 */
	public boolean isTestIncluded() {
		return testIncluded;
	}
	/**
	 * @param testIncluded the testIncluded to set
	 */
	public void setTestIncluded(boolean testIncluded) {
		this.testIncluded = testIncluded;
	}
	/**
	 * @return the includePatterns
	 */
	public String getIncludePatterns() {
		return includePatterns;
	}
	/**
	 * @param includePatterns the includePatterns to set
	 */
	public void setIncludePatterns(String includePatterns) {
		this.includePatterns = includePatterns;
	}
	/**
	 * @return the excludePatterns
	 */
	public String getExcludePatterns() {
		return excludePatterns;
	}
	/**
	 * @param excludePatterns the excludePatterns to set
	 */
	public void setExcludePatterns(String excludePatterns) {
		this.excludePatterns = excludePatterns;
	}
	
	
}
