/**
 *
 */
package com.uml.generator.spring.models;

import java.util.List;

import com.google.common.collect.Lists;
import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class SpringDependencyDiagramModel extends UmlModel {

	private final List<SpringClassModel> classes = Lists.newArrayList();

	public void addClass(final SpringClassModel classModel) {
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
