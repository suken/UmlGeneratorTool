/**
 *
 */
package com.uml.generator.classDiagram.models;

import java.util.List;

import com.google.common.collect.Lists;
import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 */
public class ClassDiagramModel extends UmlModel {

	private final List<ClassModel> classes = Lists.newArrayList();

	private final List<PackageModel> packages = Lists.newArrayList();

	public void addClass(final ClassModel classModel) {
		classes.add(classModel);
	}

	public void addPackage(final PackageModel packageModel) {
		packages.add(packageModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		StringBuilder uml = new StringBuilder();
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
