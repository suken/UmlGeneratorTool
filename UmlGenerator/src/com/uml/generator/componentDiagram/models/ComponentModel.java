/**
 * 
 */
package com.uml.generator.componentDiagram.models;

import java.util.ArrayList;
import java.util.List;

import com.uml.generator.models.UmlModel;

/**
 * @author shahs
 *
 */
public class ComponentModel extends UmlModel {
	
	private static final String DATABASE = " database ";
	
	private static final String SOURCE_START = " [";
	
	private static final String END_SOURCE = "] ";

	private String name;
	
	private ComponentType type;
	
	private List<String> dependentComponents = new ArrayList<String>(5);
	
	public ComponentModel(String name, ComponentType type) {
		this.name = name;
		this.type = type;
	}
	
	public void addDepedentComponent(String name) {
		dependentComponents.add(name);
	}
	
	public String getName() {
		return name;
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
 