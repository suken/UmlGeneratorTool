/**
 * 
 */
package com.uml.generator.classDiagram.models;

/**
 * @author sukenshah
 */
public enum ClassType {
	CLASS,
	INTERFACE,
	ABSTRACT,
	ENUM,
	ANNOTATION;
	
	public static String convert(ClassType type) {
		String typeString = null;
		switch (type) {
		case ABSTRACT:
			typeString = " abstract ";
			break;
		case CLASS:
			typeString = " class ";
			break;
		case INTERFACE:
			typeString = " interface ";
			break;
		case ENUM:
			typeString = " enum ";
			break;
		case ANNOTATION:
			typeString = " annotation ";
			break;
		default:
			break;
		}
		return typeString; 
	}
}
