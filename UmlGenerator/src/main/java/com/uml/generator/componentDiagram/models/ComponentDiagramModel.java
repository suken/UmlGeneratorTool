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
public class ComponentDiagramModel extends UmlModel {

	private final Map<String, ComponentGroupModel> groups = Maps.newHashMap();

	private final Map<String, ComponentModel> components = Maps.newHashMap();

	private ComponentGroupModel getGroup(final String name) {
		ComponentGroupModel group = groups.get(name);
		if (group == null) {
			group = new ComponentGroupModel(name);
			groups.put(name, group);
		}
		return group;
	}

	public void addComponent(final ComponentModel component, final String group) {
		if (group != null && !group.isEmpty()) {
			getGroup(group).addComponent(component);
		}
		else {
			components.put(component.getName(), component);
		}
	}

	public boolean containsComponent(final String artifactId, final String groupId) {
		boolean exists = false;
		if (groupId != null && !groupId.isEmpty()) {
			exists = groups.containsKey(groupId);
			if (exists) {
				exists = groups.get(groupId).containsComponent(artifactId);
			}
		}
		else {
			exists = components.containsKey(artifactId);
		}
		return exists;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer(START_UML + NEW_LINE);

		// add all components without groups
		for (ComponentModel component : components.values()) {
			uml.append(component.getUml());
			uml.append(NEW_LINE);
		}

		// add all groups
		for (ComponentGroupModel groupModel : groups.values()) {
			uml.append(groupModel.getUml());
			uml.append(NEW_LINE);
		}

		// add component dependencies
		for (ComponentModel component : components.values()) {
			uml.append(component.getDependencyUML());
			uml.append(NEW_LINE);
		}

		// add component dependencies
		for (ComponentGroupModel groupModel : groups.values()) {
			uml.append(groupModel.getDependencyUML());
			uml.append(NEW_LINE);
		}

		uml.append(END_UML);
		return uml.toString();
	}

}
