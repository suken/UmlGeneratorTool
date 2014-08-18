/**
 * 
 */
package com.uml.generator.classDiagram.models;

import java.util.ArrayList;
import java.util.List;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 */
public class PackageModel extends UmlModel {

	private String name;
	
	private List<ClassModel> classes = new ArrayList<ClassModel>(3);
	
	public PackageModel(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addClass(ClassModel clazz) {
		this.classes.add(clazz);
	}
	
	public List<ClassModel> getClasses() {
		return this.classes;
	}
	
	/* (non-Javadoc)
	 * @see com.uml.generator.models.UmlModel#getUml()
	 */
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		uml.append(" package ").append(name).append(NEW_LINE).append(OPEN_PARENTHESIS);
		for (ClassModel clazz : classes) {
			uml.append(clazz.getUml());
			uml.append(NEW_LINE);
		}
		uml.append(CLOSE_PARENTHESIS);
		return uml.toString();
	}

}
