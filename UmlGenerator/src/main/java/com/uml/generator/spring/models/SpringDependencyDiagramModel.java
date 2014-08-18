/**
 * 
 */
package com.uml.generator.spring.models;

import java.util.ArrayList;
import java.util.List;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class SpringDependencyDiagramModel extends UmlModel {
	
	private List<SpringClassModel> classes = new ArrayList<SpringClassModel>();

	public void addClass(SpringClassModel classModel) {
		classes.add(classModel);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		uml.append(START_UML);
		
		// add classes
		for(SpringClassModel clazz : classes) {
			uml.append(NEW_LINE).append(clazz.getUml());
		}
		
		uml.append(NEW_LINE).append(END_UML);
		return uml.toString();
	}

}
