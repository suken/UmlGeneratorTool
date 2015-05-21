/**
 *
 */
package com.uml.generator.classDiagram.models;

import lombok.Getter;

/**
 * @author sukenshah
 */
public enum ClassType {
	CLASS(" class "),
	INTERFACE(" interface "),
	ABSTRACT(" abstract "),
	ENUM(" enum "),
	ANNOTATION(" annotation ");

	@Getter
	private final String umlStr;

	ClassType(final String uml) {
		umlStr = uml;
	}

	public static String convert(final ClassType type) {
		return type.getUmlStr();
	}
}
