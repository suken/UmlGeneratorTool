/**
 * 
 */
package com.uml.generator.classDiagram.models;

import java.lang.reflect.Modifier;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 *
 */
public class MethodModel extends UmlModel {

	private final String name;
	
	private ModifierType access;
	
	public MethodModel(final String name, final int modifier) {
		this.name = name;
		switch (modifier) {
		case Modifier.PUBLIC:
			access = ModifierType.PUBLIC;
			break;
		case Modifier.PRIVATE:
			access = ModifierType.PRIVATE;
			break;
		case Modifier.PROTECTED:
			access = ModifierType.PROTECTED;
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
