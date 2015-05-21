/**
 *
 */
package com.uml.generator.classDiagram.models;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 */
public class ClassModel extends UmlModel {

	@Getter
	private final String name;

	@Getter
	private ClassType type;

	@Getter
	@Setter
	private String parent;

	private final List<FieldModel> fields = new ArrayList<FieldModel>(5);

	private final List<MethodModel> methods = new ArrayList<MethodModel>(3);

	private final List<String> interfaces = new ArrayList<String>(3);

	private final List<String> dependencies = new ArrayList<String>(5);

	public ClassModel(final String name) {
		this.name = name;
	}

	public void addField(final FieldModel field) {
		fields.add(field);
	}

	public void addInterface(final String interfaze) {
		interfaces.add(interfaze);
	}

	public void addMethod(final MethodModel method) {
		methods.add(method);
	}

	public void addDependency(final String clazz) {
		dependencies.add(clazz);
	}

	public void setType(final Class clazz) {
		type = ClassType.CLASS;
		if (clazz.isInterface()) {
			type = ClassType.INTERFACE;
		}
		else if (clazz.isEnum()) {
			type = ClassType.ENUM;
		}
		else if (clazz.isAnnotation()) {
			type = ClassType.ANNOTATION;
		}
		else if (clazz.getModifiers() == Modifier.ABSTRACT) {
			type = ClassType.ABSTRACT;
		}
	}

	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		uml.append(ClassType.convert(type)).append(name).append(OPEN_PARENTHESIS).append(NEW_LINE);
		getFieldsUml(uml);

		// all methods
		getMethodsUml(uml);
		uml.append(CLOSE_PARENTHESIS);

		// parent relationship
		getDepenciesUml(uml);

		uml.append(NEW_LINE);
		return uml.toString();
	}

	protected void getDepenciesUml(final StringBuffer uml) {
		if (parent != null) {
			uml.append(NEW_LINE).append(name).append(EXTENDS).append(parent);
		}

		// interfaces
		for (String interfaze : interfaces) {
			uml.append(NEW_LINE).append(name).append(EXTENDS).append(interfaze);
		}

		// dependencies
		for (String clazz : dependencies) {
			uml.append(NEW_LINE).append(name).append(DEPENDS).append(clazz);
		}
	}

	protected void getMethodsUml(final StringBuffer uml) {
		for (MethodModel method : methods) {
			if (method.isPublicOrProtected()) {
				uml.append(method.getUml());
				uml.append(NEW_LINE);
			}
		}
	}

	protected void getFieldsUml(final StringBuffer uml) {
		// all public fields
		for (FieldModel field : fields) {
			if (field.isPublicOrProtected()) {
				uml.append(field.getUml());
				uml.append(NEW_LINE);
			}
		}
	}
}
