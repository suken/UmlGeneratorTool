/**
 * 
 */
package com.uml.generator.jpa.models;

import java.util.ArrayList;
import java.util.List;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class JpaDependencyDiagramModel extends UmlModel {
	
	private List<JpaClassModel> classes = new ArrayList<JpaClassModel>();

	public void addClass(JpaClassModel classModel) {
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
		for(JpaClassModel clazz : classes) {
			uml.append(NEW_LINE).append(clazz.getUml());
		}
		
		uml.append(NEW_LINE).append(END_UML);
		return uml.toString();
	}

}
