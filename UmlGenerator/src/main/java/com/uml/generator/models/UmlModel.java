/**
 * 
 */
package com.uml.generator.models;

/**
 * @author sukenshah
 */
public abstract class UmlModel {
	
	public static final String START_UML = "@startuml";
	
	public static final String END_UML = "@enduml";

	public static final String NEW_LINE = "\n";
	
	public static final String OPEN_PARENTHESIS = "{";
	
	public static final String CLOSE_PARENTHESIS = "}";
	
	public static final String OPEN_BRACKET = "(";
	
	public static final String CLOSE_BRACKET = ")";
	
	public static final String SPACE = " ";
	
	public static final String EXTENDS = " --|> ";
	
	public static final String DEPENDS = " --> ";
	
	public static final String PUBLIC = " + ";
	
	public static final String PRIVATE = " - ";
	
	public static final String PROTECTED = " # ";
	
	public static final String METHOD = "()";
	
	public abstract String getUml();
}
