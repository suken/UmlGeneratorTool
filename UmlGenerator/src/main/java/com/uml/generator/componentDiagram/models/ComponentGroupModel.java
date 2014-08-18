/**
 * 
 */
package com.uml.generator.componentDiagram.models;

import java.util.HashMap;
import java.util.Map;

import com.uml.generator.models.UmlModel;

/**
 * @author shahs
 */
public class ComponentGroupModel extends UmlModel {
	
	private static final String GROUP = " frame ";
	private static final String GROUP_NAME_PRE_POST_FIX = "\" ";

	private String name;
	
	private Map<String, ComponentModel> components = new HashMap<String, ComponentModel>();
	
	public ComponentGroupModel(String name) {
		this.name = name;
	}
	
	public void addComponent(ComponentModel model) {
		this.components.put(model.getName(), model);
	}
	
	public boolean containsComponent(String artifactId) {
		return components.containsKey(artifactId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		uml.append(GROUP).append(GROUP_NAME_PRE_POST_FIX).append(name)
				.append(GROUP_NAME_PRE_POST_FIX).append(OPEN_PARENTHESIS);
		uml.append(NEW_LINE);

		// add all components
		for (ComponentModel component : components.values()) {
			uml.append(component.getUml()).append(NEW_LINE);
		}

		uml.append(CLOSE_PARENTHESIS).append(NEW_LINE);
		return uml.toString();
	}
	
	public String getDependencyUML() {
		StringBuffer uml = new StringBuffer();
		for (ComponentModel component : components.values()) {
			uml.append(component.getDependencyUML()).append(NEW_LINE);
		}
		return uml.toString();
	}

}
