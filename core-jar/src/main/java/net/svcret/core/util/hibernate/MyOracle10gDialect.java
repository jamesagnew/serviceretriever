package net.svcret.core.util.hibernate;

import java.sql.Types;

import org.hibernate.dialect.Oracle10gDialect;

/**
 * Workaround for 
 * https://hibernate.atlassian.net/browse/HHH-2315
 * 
 * As suggested by
 * http://stackoverflow.com/questions/2625600/what-are-the-best-workarounds-for-known-problems-with-hibernates-schema-validat
 *
 */
public class MyOracle10gDialect extends Oracle10gDialect {
    public MyOracle10gDialect() {
        super();
    }
    protected void registerNumericTypeMappings() {
        super.registerNumericTypeMappings();
        registerColumnType( Types.DOUBLE, "float" );
    }
}