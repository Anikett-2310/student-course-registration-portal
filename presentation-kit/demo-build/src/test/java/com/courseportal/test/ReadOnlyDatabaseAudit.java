package com.courseportal.test;

import com.courseportal.util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * ReadOnlyDatabaseAudit.java
 * Strictly READ-ONLY tool. Executes only SELECT and metadata queries.
 * Zero mutations, zero schema changes.
 */
public class ReadOnlyDatabaseAudit {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("LIVE DATABASE READ-ONLY AUDIT: student_course_portal");
        System.out.println("============================================================");

        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database Product: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("Driver Version:   " + meta.getDriverName() + " " + meta.getDriverVersion());
            System.out.println("Catalog:          " + conn.getCatalog());
            System.out.println("------------------------------------------------------------");

            // List Tables
            System.out.println("\n[TABLES]");
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    System.out.println("  - " + tableName);
                }
            }

            // Inspect Table Structure & Counts
            String[] tables = {"students", "courses", "registrations", "admin"};
            for (String table : tables) {
                System.out.println("\n============================================================");
                System.out.println("TABLE: " + table);
                System.out.println("------------------------------------------------------------");

                // Columns
                try (ResultSet cols = meta.getColumns(conn.getCatalog(), null, table, "%")) {
                    System.out.println("COLUMNS:");
                    while (cols.next()) {
                        System.out.printf("  %-20s %-15s (nullable: %s)\n",
                                cols.getString("COLUMN_NAME"),
                                cols.getString("TYPE_NAME") + "(" + cols.getInt("COLUMN_SIZE") + ")",
                                cols.getString("IS_NULLABLE"));
                    }
                }

                // Primary Keys
                try (ResultSet pks = meta.getPrimaryKeys(conn.getCatalog(), null, table)) {
                    System.out.print("PRIMARY KEY(S): ");
                    while (pks.next()) {
                        System.out.print(pks.getString("COLUMN_NAME") + " ");
                    }
                    System.out.println();
                }

                // Foreign Keys
                try (ResultSet fks = meta.getImportedKeys(conn.getCatalog(), null, table)) {
                    System.out.println("FOREIGN KEYS:");
                    while (fks.next()) {
                        System.out.printf("  %s -> %s(%s) ON DELETE %s\n",
                                fks.getString("FKCOLUMN_NAME"),
                                fks.getString("PKTABLE_NAME"),
                                fks.getString("PKCOLUMN_NAME"),
                                fks.getString("DELETE_RULE"));
                    }
                }

                // Rows
                String selectSql = "SELECT * FROM " + table;
                try (PreparedStatement ps = conn.prepareStatement(selectSql);
                     ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int colCount = rsmd.getColumnCount();
                    System.out.println("DATA ROWS:");
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        StringBuilder sb = new StringBuilder("  Row " + count + ": { ");
                        for (int i = 1; i <= colCount; i++) {
                            sb.append(rsmd.getColumnName(i)).append("=").append(rs.getString(i));
                            if (i < colCount) sb.append(", ");
                        }
                        sb.append(" }");
                        System.out.println(sb.toString());
                    }
                    System.out.println("TOTAL COUNT: " + count);
                }
            }

            System.out.println("\n============================================================");
            System.out.println("READ-ONLY AUDIT COMPLETE.");
            System.out.println("============================================================");

        } catch (SQLException e) {
            System.err.println("Database audit failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
