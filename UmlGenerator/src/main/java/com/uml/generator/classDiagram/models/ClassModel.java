/**
 * 
 */
package com.uml.generator.classDiagram.models;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class ClassModel extends UmlModel {
	
	private String name;
	
	private ClassType type;
	
	private List<FieldModel> fields = new ArrayList<FieldModel>(5);
	
	private List<MethodModel> methods = new ArrayList<MethodModel>(3);
	
	private List<String> interfaces = new ArrayList<String>(3);
	
	private String parent;
	
	private List<String> dependencies = new ArrayList<String>(5);
	
	public ClassModel(String name) {
		this.name = name;
	}
	
	public void addField(FieldModel field) {
		this.fields.add(field);
	}
	
	public void addInterface(String interfaze) {
		this.interfaces.add(interfaze);
	}
	
	public void addMethod(MethodModel method) {
		this.methods.add(method);
	}
	
	public void addDependency(String clazz) {
		this.dependencies.add(clazz);
	}
	
	public void setParent(String parentClass) {
		this.parent = parentClass;
	}
	
	public void setType(Class clazz) {
		this.type = ClassType.CLASS;
		if (clazz.isInterface()) {
			this.type = ClassType.INTERFACE;
		}
		else if (clazz.isEnum()) {
			this.type = ClassType.ENUM;
		}
		else if (clazz.isAnnotation()) {
			this.type = ClassType.ANNOTATION;
		}
		else if (clazz.getModifiers() == Modifier.ABSTRACT) {
			this.type = ClassType.ABSTRACT;
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public ClassType getType() {
		return this.type;
	}
	
	public String getParent() {
		return this.parent;
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

	protected void getDepenciesUml(StringBuffer uml) {
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

	protected void getMethodsUml(StringBuffer uml) {
		for (MethodModel method : methods) {
			if (method.isPublicOrProtected()) {
				uml.append(method.getUml());
				uml.append(NEW_LINE);
			}
		}
	}

	protected void getFieldsUml(StringBuffer uml) {
		// all public fields
		for (FieldModel field : fields) {
			if (field.isPublicOrProtected()) {
				uml.append(field.getUml());
				uml.append(NEW_LINE);
			}
		}
	}
}
