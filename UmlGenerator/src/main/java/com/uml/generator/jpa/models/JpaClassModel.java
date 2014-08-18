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
	private static final String COLUMNS_SEPARATOR = "__COLUMNS__";
	private static final String ID_COLUMNS_SEPARATOR ="__ID COLUMNS__";
	private static final String TABLE_NAME_SEPARATOR = "__TABLE__";
	private static final String ONE_TO_ONE_UML_STR = "--";

	private String tableName;
	private JpaEntityType jpaType;
	private Map<String, JpaDependencyType> jpaDependencies = new HashMap<String, JpaDependencyType>(5);
	private List<String> columns = new ArrayList<String>(5);
	private List<String> idColumns = new ArrayList<String>(5);
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
	
	public void removeJpaDependency(String className) {
		jpaDependencies.remove(className);
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
			uml.append(TABLE_NAME_SEPARATOR).append(NEW_LINE).append(tableName).append(NEW_LINE);
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
		
		getJpaDependenciesUml(uml);
		
		uml.append(NEW_LINE);
		return uml.toString();
	}
	
	private void getJpaDependenciesUml(StringBuffer uml) {
		for (Entry<String, JpaDependencyType>  dependency : jpaDependencies.entrySet()) {
			String dependsUmlString = DEPENDS;
			if (dependency.getValue() == JpaDependencyType.ONE_TO_ONE
					|| dependency.getValue() == JpaDependencyType.MANY_TO_MANY) {
				dependsUmlString = ONE_TO_ONE_UML_STR;
			}
			uml.append(NEW_LINE).append(getName()).append(dependsUmlString).append(dependency.getKey()).append(" : ").append(dependency.getValue().toString()).append(" ");
		}
	}

	protected void getColumnsUml(StringBuffer uml) {
		if (!idColumns.isEmpty()) {
			uml.append(ID_COLUMNS_SEPARATOR).append(NEW_LINE);
			// all id columns
			for (String column : idColumns) {
				uml.append(column);
				uml.append(NEW_LINE);
			}
		}
		
		if (!columns.isEmpty()) {
			uml.append(COLUMNS_SEPARATOR).append(NEW_LINE);
			// all non-id columns
			for (String column : columns) {
				uml.append(column);
				uml.append(NEW_LINE);
			}
		}
	}

}