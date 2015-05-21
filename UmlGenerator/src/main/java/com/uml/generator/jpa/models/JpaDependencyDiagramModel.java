/**
 *
 */
package com.uml.generator.jpa.models;

import java.util.Map;

import com.google.common.collect.Maps;
import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class JpaDependencyDiagramModel extends UmlModel {

	private final Map<String, JpaClassModel> classes = Maps.newLinkedHashMap();

	public void addClass(final JpaClassModel classModel) {
		classes.put(classModel.getName(), classModel);
	}

	public JpaClassModel getClass(final String name) {
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
