/**
 *
 */
package com.uml.generator.classDiagram.models;

import java.util.List;

import lombok.Getter;

import com.google.common.collect.Lists;
import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 */
public class PackageModel extends UmlModel {

	@Getter
	private final String name;

	@Getter
	private final List<ClassModel> classes = Lists.newArrayList();

	public PackageModel(final String name) {
		this.name = name;
	}

	public void addClass(final ClassModel clazz) {
		classes.add(clazz);
	}

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
