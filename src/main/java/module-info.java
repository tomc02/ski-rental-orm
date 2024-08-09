module db.impl.main {
    requires lombok;
    requires java.sql;
    requires org.apache.logging.log4j;
    requires com.microsoft.sqlserver.jdbc;
    requires com.oracle.database.jdbc;

    exports cs.vsb.ski_rental;
}