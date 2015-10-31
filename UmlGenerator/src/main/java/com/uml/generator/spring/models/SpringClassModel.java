/**
 *
 */
package com.uml.generator.spring.models;

import java.util.Map;
import java.util.Map.Entry;

import lombok.Setter;

import com.google.common.collect.Maps;
import com.uml.generator.classDiagram.models.ClassModel;
import com.uml.generator.classDiagram.models.ClassType;

/**
 * @author sukenshah
 */
public class SpringClassModel extends ClassModel {

	private static final String CONTROLLER = " << (C,#FF7700) Controller >> ";

	private static final String COMPONENT = " << (C,#AA5337) Component >> ";

	private static final String SERVICE = " << (S,#BB4830) Service >> ";

	private static final String REPOSITORY = " << (R,#CC2200) Repository >> ";

	private static final String CONFIGURATION = " << (C,#DD9900) Configuration >> ";

	private static final String INJECTED_RELATIONSHIP = "<--";

	@Setter
	private SpringClassType springClassType = SpringClassType.NONE;

	private final StringBuffer note = new StringBuffer();

	private final Map<String, DependencyType> springDependencies = Maps.newHashMap();

	public SpringClassModel(final String name) {
		super(name);
	}

	public void addDepedency(final String name, final DependencyType type) {
		springDependencies.put(name, type);
	}

	public void addNote(final String text) {
		note.append(text).append("\\n");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
	    StringBuilder uml = new StringBuilder();
		uml.append(ClassType.convert(getType())).append(getName());

		switch (springClassType) {
		case COMPONENT:
			uml.append(COMPONENT);
			break;
		case CONTROLLER:
			uml.append(CONTROLLER);
			break;
		case REPOSITORY:
			uml.append(REPOSITORY);
			break;
		case SERVICE:
			uml.append(SERVICE);
			break;
		case CONFIGURATION:
			uml.append(CONFIGURATION);
			break;
		case NONE:
		default:
			break;
		}

		uml.append(OPEN_PARENTHESIS).append(NEW_LINE);

		// all fields
		getFieldsUml(uml);

		// all methods
		getMethodsUml(uml);
		uml.append(CLOSE_PARENTHESIS);

		// add notes
		if (note.length() > 0) {
			uml.append(NEW_LINE).append("note left of ").append(getName()).append(" : ")
			.append(note.substring(0, note.length() - 2));
		}
		uml.append(NEW_LINE);

		// parent relationship
		getDepenciesUml(uml);

		// spring dependencies
		for (Entry<String, DependencyType>  dependency : springDependencies.entrySet()) {
			uml.append(NEW_LINE).append(getName()).append(INJECTED_RELATIONSHIP).append(dependency.getKey()).append(" : ").append(dependency.getValue().toString()).append(" ");
		}

		uml.append(NEW_LINE);
		return uml.toString();
	}

}
