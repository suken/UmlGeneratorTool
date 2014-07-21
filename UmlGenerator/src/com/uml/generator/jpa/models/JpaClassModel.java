/**
 * 
 */
package com.uml.generator.jpa.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.uml.generator.classDiagram.models.ClassModel;
import com.uml.generator.classDiagram.models.ClassType;

/**
 * @author SShah
 */
public class JpaClassModel extends ClassModel {
	
	private static final String TABEL = " << (T,#BB3255) TABLE >> ";
	private static final String MAPPED_SUPER_CLASS = " << (M,#CD45FF) MAPPED SUPER CLASS >> ";
	private static final String ENTITY_CLASS = " << (E,#ACFFFF) ENTITY >> ";

	private String tableName;
	private JpaEntityType jpaType;
	private Map<String, JpaDependencyType> jpaDependencies = new HashMap<String, JpaDependencyType>();
	private List<String> columns = new ArrayList<String>();
	private List<String> idColumns = new ArrayList<String>();
	private StringBuffer note = new StringBuffer();

	public JpaClassModel(String name) {
		super(name);
	}
	
	public void setTableName(String table) {
		this.tableName = table;
	}
	
	public void setJpaEntityType(JpaEntityType type) {
		this.jpaType = type;
	}
	
	public void addJpaDependency(String className, JpaDependencyType type) {
		jpaDependencies.put(className, type);
	}
	
	public void addColumn(String columnName) {
		this.columns.add(columnName);
	}
	
	public void addIdColumn(String columnName) {
		this.idColumns.add(columnName);
	}
	
	public void addNote(String text) {
		note.append(text).append("\\n");
	}
	
	@Override
	public String getUml() {
		StringBuffer uml = new StringBuffer();
		uml.append(ClassType.convert(getType())).append(getName());
		
		switch (jpaType) {
		case TABLE:
			uml.append(TABEL);
			break;
		case MAPPED_SUPER_CLASS:
			uml.append(MAPPED_SUPER_CLASS);
			break;
		case ENTITY:
			uml.append(ENTITY_CLASS);
		default:
			break;
		}
		
		uml.append(OPEN_PARENTHESIS).append(NEW_LINE);
		
		// add table name
		if (tableName != null) {
			uml.append("__ID COLUMNS__").append(NEW_LINE).append(tableName).append(NEW_LINE);
		}
		
		// all columns
		getColumnsUml(uml);
		
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
		for (Entry<String, JpaDependencyType>  dependency : jpaDependencies.entrySet()) {
			uml.append(NEW_LINE).append(getName()).append(DEPENDS).append(dependency.getKey()).append(" : ").append(dependency.getValue().toString()).append(" ");
		}
		
		uml.append(NEW_LINE);
		return uml.toString();
	}
	
	protected void getColumnsUml(StringBuffer uml) {
		if (!idColumns.isEmpty()) {
			uml.append("__ID COLUMNS__").append(NEW_LINE);
			// all id columns
			for (String column : idColumns) {
				uml.append(column);
				uml.append(NEW_LINE);
			}
		}
		
		if (!columns.isEmpty()) {
			uml.append("__COLUMNS__").append(NEW_LINE);
			// all non-id columns
			for (String column : columns) {
				uml.append(column);
				uml.append(NEW_LINE);
			}
		}
	}

}
