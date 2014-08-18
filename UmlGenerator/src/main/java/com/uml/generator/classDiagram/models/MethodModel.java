/**
 * 
 */
package com.uml.generator.classDiagram.models;

import java.lang.reflect.Modifier;
import java.util.List;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class MethodModel extends UmlModel {

	private String name;
	
	private List<FieldModel> args;
	
	private String returnType;
	
	private ModifierType access;
	
	public MethodModel(String name, int modifier) {
		this.name = name;
		switch (modifier) {
		case Modifier.PUBLIC:
			this.access = ModifierType.PUBLIC;
			break;
		case Modifier.PRIVATE:
			this.access = ModifierType.PRIVATE;
			break;
		case Modifier.PROTECTED:
			this.access = ModifierType.PROTECTED;
			break;
		default:
			access = ModifierType.PROTECTED;
			break;
		}
	}
	
	public boolean isPublicOrProtected() {
		return !(access == ModifierType.PRIVATE);
	}

	@Override
	public String getUml() {
		String modifier = PRIVATE;
		if (access == ModifierType.PUBLIC) {
			modifier = PUBLIC;
		}
		else if (access == ModifierType.PROTECTED) {
			modifier = PROTECTED;
		}
		return modifier + name + METHOD;
	}

}
