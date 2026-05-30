package com.netconfig.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${project.root:./}")
    private String projectRoot;

    @Override
    public void run(String... args) throws Exception {
        String dbPath = new File(projectRoot, "netconfig.db").getAbsolutePath();
        String url = "jdbc:sqlite:" + dbPath;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS commands (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    vendor TEXT NOT NULL,
                    cat TEXT NOT NULL,
                    topo TEXT DEFAULT '[]',
                    desc TEXT DEFAULT '',
                    detail TEXT DEFAULT '',
                    configs TEXT DEFAULT '{}',
                    comments TEXT DEFAULT '{}',
                    docs TEXT DEFAULT '{}',
                    verification TEXT DEFAULT '{}',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notes (
                    cmd_id TEXT PRIMARY KEY,
                    content TEXT DEFAULT ''
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS category_labels (
                    cat_key TEXT PRIMARY KEY,
                    cat_label TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS category_exclusions (
                    cat_key TEXT PRIMARY KEY
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS meta (
                    key TEXT PRIMARY KEY,
                    value TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faults (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    category TEXT DEFAULT '',
                    symptom TEXT DEFAULT '',
                    cause TEXT DEFAULT '',
                    solution TEXT DEFAULT '',
                    topo TEXT DEFAULT '[]',
                    docs TEXT DEFAULT '{}',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS desktop (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    category TEXT DEFAULT '',
                    symptom TEXT DEFAULT '',
                    solution TEXT DEFAULT '',
                    topo TEXT DEFAULT '[]',
                    docs TEXT DEFAULT '{}',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS linux (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    vendor TEXT NOT NULL,
                    cat TEXT NOT NULL,
                    topo TEXT DEFAULT '[]',
                    desc TEXT DEFAULT '',
                    detail TEXT DEFAULT '',
                    configs TEXT DEFAULT '{}',
                    comments TEXT DEFAULT '{}',
                    docs TEXT DEFAULT '{}',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS office (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    vendor TEXT NOT NULL,
                    cat TEXT NOT NULL,
                    topo TEXT DEFAULT '[]',
                    desc TEXT DEFAULT '',
                    detail TEXT DEFAULT '',
                    configs TEXT DEFAULT '{}',
                    comments TEXT DEFAULT '{}',
                    docs TEXT DEFAULT '{}',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            try { stmt.execute("ALTER TABLE commands ADD COLUMN verification TEXT DEFAULT '{}'"); } catch (Exception ignored) {}

            String[] tables = {"commands", "faults", "desktop", "linux", "office"};
            for (String table : tables) {
                try {
                    stmt.execute("UPDATE " + table + " SET created_at=datetime('now','localtime') WHERE created_at IS NULL OR created_at=''");
                } catch (Exception ignored) {}
            }

            System.out.println("[NetConfig] 数据库初始化完成: " + dbPath);
        }
    }
}
