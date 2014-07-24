/**
 * 
 */
package com.uml.generator.jpa.models;

import java.util.LinkedHashMap;
import java.util.Map;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class JpaDependencyDiagramModel extends UmlModel {
	
	private Map<String, JpaClassModel> classes = new LinkedHashMap<String, JpaClassModel>();

	public void addClass(JpaClassModel classModel) {
		classes.put(classModel.getName(), classModel);
	}
	
	public JpaClassModel getClass(String name) {
		return classes.get(name);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		uml.append(START_UML);

		// add classes
		for(JpaClassModel clazz : classes.values()) {
			uml.append(NEW_LINE).append(clazz.getUml());
		}
		
		uml.append(NEW_LINE).append(END_UML);
		return uml.toString();
	}

}
