/**
 *
 */
package com.uml.generator.classDiagram.models;

import java.lang.reflect.Modifier;

import com.uml.generator.models.UmlModel;

/**
 * @author sukenshah
 */
public class FieldModel extends UmlModel {

	private String name;

	private String type;

	private ModifierType access;

	public FieldModel(String name, String type, int modifier) {
		this.name = name;
		this.type = type;
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
			this.access = ModifierType.PRIVATE;
			break;
		}
	}

	public boolean isPublicOrProtected() {
		return !(access == ModifierType.PRIVATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUml() {
		String modifier = PRIVATE;
		if (access == ModifierType.PUBLIC) {
			modifier = PUBLIC;
		}
		else if (access == ModifierType.PROTECTED) {
			modifier = PROTECTED;
		}
		return modifier + type + SPACE + name;
	}

}
