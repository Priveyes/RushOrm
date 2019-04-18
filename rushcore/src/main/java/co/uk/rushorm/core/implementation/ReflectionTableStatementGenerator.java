package co.uk.rushorm.core.implementation;

import android.annotation.*;
import android.util.*;

import java.lang.reflect.*;
import java.util.*;

import co.uk.rushorm.core.*;
import co.uk.rushorm.core.annotations.*;

/**
 * Created by stuartc on 11/12/14.
 */
public class ReflectionTableStatementGenerator implements RushTableStatementGenerator {

	private List<Join> joins = new ArrayList<>();

	private class Column {
		String name;
		String type;
	}

	private class Join {
		Class<? extends Rush> key;
		Field keyField;
		Class<? extends Rush> child;
	}

	private final RushConfig rushConfig;

	public ReflectionTableStatementGenerator(RushConfig rushConfig) {
		this.rushConfig = rushConfig;
	}

	@SuppressLint("NewApi")
	@Override
	public void generateStatements(List<Class<? extends Rush>> classes, RushColumns rushColumns, StatementCallback statementCallback, Map<Class<? extends Rush>, AnnotationCache> annotationCache) {

		for(Class clazz : classes) {
			String sql = classToStatement(clazz, rushColumns, annotationCache);
			statementCallback.statementCreated(sql);
		}

		for(Join join : joins) {
			String joinTableName = null;
			//TODO Problem avec les Tables à la création
			try {
				joinTableName = ReflectionUtils.joinTableNameForClass(annotationCache.get(join.key).getTableName(), annotationCache.get(join.child).getTableName(), join.keyField.getName());
			} catch (NullPointerException e) {
//				e.printStackTrace();
				Log.e("Error on Join", join.keyField.getName()+" "+join.key.toString()+" "+join.child.toString());
				continue;
			}
			String sql = joinToStatement(join, joinTableName, annotationCache);
			statementCallback.statementCreated(sql);
//			if(!rushConfig.usingMySql()) {
				statementCallback.statementCreated(String.format(RushSqlUtils.CREATE_INDEX, joinTableName, joinTableName));
//			}
		}
	}

	private String classToStatement(Class<? extends Rush> clazz, RushColumns rushColumns, Map<Class<? extends Rush>, AnnotationCache> annotationCache) {

		StringBuilder columnsStatement = new StringBuilder();

		List<Field> fields = new ArrayList<>();
		ReflectionUtils.getAllFields(fields, clazz, rushConfig.orderColumnsAlphabetically());
		for (Field field : fields) {

			if(!annotationCache.get(clazz).getFieldToIgnore().contains(field.getName())) {
				field.setAccessible(true);
				Column column = columnFromField(clazz, field, rushColumns);
				if(column != null) {
					columnsStatement.append(",\n")
							.append(column.name)
							.append(" ")
							.append(column.type);
				}
			}
		}
		return String.format(RushSqlUtils.TABLE_TEMPLATE, annotationCache.get(clazz).getTableName(), columnsStatement.toString());
	}

	private String joinToStatement(Join join, String joinTableName, Map<Class<? extends Rush>, AnnotationCache> annotationCache) {
			return String.format(/*rushConfig.usingMySql() ? RushSqlUtils.JOIN_TEMPLATE_MYSQL :*/ RushSqlUtils.JOIN_TEMPLATE_SQLITE, joinTableName,
					annotationCache.get(join.key).getTableName(),
					annotationCache.get(join.child).getTableName());
	}

	private Column columnFromField(Class<? extends Rush> clazz, Field field, RushColumns rushColumns) {

		if(Rush.class.isAssignableFrom(field.getType())){

			// One to one join table
			Join join = new Join();
			join.key = clazz;
			join.keyField = field;
			join.child = (Class<? extends Rush>) field.getType();
			joins.add(join);
			return null;

		}else if(field.isAnnotationPresent(RushList.class)) {

			// One to many join table
			RushList rushList = field.getAnnotation(RushList.class);
			Class listClass = rushList.classType();

			if (Rush.class.isAssignableFrom(listClass)) {

				Join join = new Join();
				join.key = clazz;
				join.keyField = field;
				join.child = listClass;
				joins.add(join);
				return null;
			}
		}

		if(rushColumns.supportsField(field)) {
			Column column = new Column();
			column.name = field.getName();
			column.type = rushColumns.sqlColumnType(field);
			return column;
		}

		return null;
	}
}
