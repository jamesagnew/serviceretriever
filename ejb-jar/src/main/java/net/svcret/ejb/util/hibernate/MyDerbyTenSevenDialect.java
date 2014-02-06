package net.svcret.ejb.util.hibernate;

import java.sql.Types;

public class MyDerbyTenSevenDialect extends org.hibernate.dialect.DerbyTenSevenDialect {

	public MyDerbyTenSevenDialect() {
		super();
		registerColumnType(Types.BLOB, "blob");
		registerColumnType(Types.CLOB, "clob");
	}
}