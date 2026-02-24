package com.backend.onharu.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CustomPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    private static final String CHILDREN_TABLE_LOGICAL = "children";
    private static final String CHILDREN_TABLE_PHYSICAL = "childrens";


    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        if (logicalName != null && CHILDREN_TABLE_LOGICAL.equalsIgnoreCase(logicalName.getText())) {
            return Identifier.toIdentifier(CHILDREN_TABLE_PHYSICAL, logicalName.isQuoted());
        }
        return super.toPhysicalTableName(logicalName, jdbcEnvironment);
    }
}
