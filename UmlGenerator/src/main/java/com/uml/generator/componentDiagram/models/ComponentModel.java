/**
 *
 */
package com.uml.generator.componentDiagram.models;

import java.util.List;

import lombok.Getter;

import com.google.common.collect.Lists;
import com.uml.generator.models.UmlModel;

/**
 * @author shahs
 *
 */
public class ComponentModel extends UmlModel {

	private static final String DATABASE = " database ";

	private static final String SOURCE_START = " [";

	private static final String END_SOURCE = "] ";

	@Getter
	private final String name;

	private final ComponentType type;

	private final List<String> dependentComponents = Lists.newArrayList();

	public ComponentModel(final String name, final ComponentType type) {
		this.name = name;
		this.type = type;
	}

	public void addDepedentComponent(final String name) {
		dependentComponents.add(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		switch (type) {
		case DATABASE:
			uml.append(DATABASE).append("db").append(SPACE).append(OPEN_PARENTHESIS).append(NEW_LINE);
			uml.append(SOURCE_START).append(name).append(END_SOURCE).append(NEW_LINE).append(CLOSE_PARENTHESIS);
			break;
		case SOURCE:
			uml.append(SOURCE_START).append(name).append(END_SOURCE);
			break;
		case INTERFACE:
			uml.append(name);
			break;
		case LIB:
			uml.append(SOURCE_START).append(name).append(END_SOURCE).append(" <<" + ComponentType.LIB.toString() + ">> ");
			break;
		default:
			break;
		}
		uml.append(NEW_LINE);

		return uml.toString();
	}

	public String getDependencyUML() {
		StringBuffer uml = new StringBuffer();
		// add dependencies
		String componentName = SOURCE_START + name + END_SOURCE;
		for (String dependentComponent : dependentComponents) {
			uml.append(componentName).append(" --> ").append(SOURCE_START)
			.append(dependentComponent).append(END_SOURCE)
			.append(NEW_LINE);
		}
		return uml.toString();
	}

}
