/**
 *
 */
package com.uml.generator.componentDiagram.models;

import java.util.Map;

import com.google.common.collect.Maps;
import com.uml.generator.models.UmlModel;

/**
 * @author shahs
 */
public class ComponentGroupModel extends UmlModel {

	private static final String GROUP = " frame ";
	private static final String GROUP_NAME_PRE_POST_FIX = "\" ";

	private final String name;

	private final Map<String, ComponentModel> components = Maps.newHashMap();

	public ComponentGroupModel(final String name) {
		this.name = name;
	}

	public void addComponent(final ComponentModel model) {
		components.put(model.getName(), model);
	}

	public boolean containsComponent(final String artifactId) {
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
