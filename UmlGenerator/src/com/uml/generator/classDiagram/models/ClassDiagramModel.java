/**
 * 
 */
package com.uml.generator.classDiagram.models;

import java.util.ArrayList;
import java.util.List;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class ClassDiagramModel extends UmlModel {

	private List<ClassModel> classes = new ArrayList<ClassModel>();
	
	private List<PackageModel> packages = new ArrayList<PackageModel>();
	
	public void addClass(ClassModel classModel) {
		this.classes.add(classModel);
	}
	
	public void addPackage(PackageModel packageModel) {
		this.packages.add(packageModel);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		uml.append(START_UML);
		
		// add number of pages
		uml.append(NEW_LINE).append("page 2x2").append(NEW_LINE);
		
		// add packages
		for(PackageModel packaze : packages) {
			uml.append(NEW_LINE).append(packaze.getUml());
		}
		
		// add classes
		for(ClassModel clazz : classes) {
			uml.append(NEW_LINE).append(clazz.getUml());
		}
		
		uml.append(NEW_LINE).append(END_UML);
		return uml.toString();
	}

}
