package com.netconfig.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${project.root:}")
    private String projectRoot;

    private String resolveProjectRoot() {
        if (projectRoot != null && !projectRoot.isEmpty()) {
            return projectRoot;
        }
        File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();
        if (dir.getName().equals("springboot-backend") || dir.getName().equals("target")) {
            dir = dir.getParentFile();
        }
        return dir.getAbsolutePath();
    }

    @Override
    public void run(String... args) throws Exception {
        String root = resolveProjectRoot();
        String dbPath = new File(root, "date.db").getAbsolutePath();
        String url = "jdbc:sqlite:" + dbPath;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS command_topics (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    cat TEXT NOT NULL,
                    topo TEXT DEFAULT '[]',
                    desc TEXT DEFAULT '',
                    detail TEXT DEFAULT '',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS command_configs (
                    id TEXT PRIMARY KEY,
                    topic_id TEXT NOT NULL,
                    vendor TEXT NOT NULL,
                    config TEXT DEFAULT '',
                    comment TEXT DEFAULT '',
                    doc TEXT DEFAULT '',
                    verification_cmd TEXT DEFAULT '',
                    verification_images TEXT DEFAULT '[]',
                    UNIQUE(topic_id, vendor)
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

            // 清理废弃表
            try { stmt.execute("DROP TABLE IF EXISTS commands"); } catch (Exception ignored) {}
            try { stmt.execute("DROP TABLE IF EXISTS meta"); } catch (Exception ignored) {}

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS search_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    module TEXT NOT NULL,
                    keyword TEXT NOT NULL,
                    searched_at TEXT NOT NULL
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
                    images TEXT DEFAULT '[]',
                    videos TEXT DEFAULT '[]',
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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ai_topics (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    category TEXT DEFAULT '',
                    scenario TEXT DEFAULT '',
                    prompt TEXT DEFAULT '',
                    config TEXT DEFAULT '',
                    desc TEXT DEFAULT '',
                    detail TEXT DEFAULT '',
                    topo TEXT DEFAULT '[]',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS click_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    module TEXT NOT NULL,
                    item_id TEXT NOT NULL,
                    item_title TEXT DEFAULT '',
                    count INTEGER DEFAULT 1,
                    UNIQUE(module, item_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER',
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    created_at TEXT DEFAULT ''
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_tokens (
                    token TEXT PRIMARY KEY,
                    user_id TEXT NOT NULL,
                    role TEXT NOT NULL,
                    expires_at TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pending_changes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    module TEXT NOT NULL,
                    operation TEXT NOT NULL,
                    entity_id INTEGER,
                    payload TEXT,
                    submitter_id INTEGER NOT NULL,
                    submitter_name TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    created_at TEXT DEFAULT '',
                    approved_at TEXT DEFAULT '',
                    approved_by TEXT DEFAULT ''
                )
            """);

            // 初始化超级管理员账号（默认密码: admin123）
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest("admin123".getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) sb.append(String.format("%02x", b));
                String hashedPwd = sb.toString();
                stmt.execute("INSERT OR IGNORE INTO users (id,username,password,role,status,created_at) VALUES ('user_super_admin','admin','" + hashedPwd + "','SUPER_ADMIN','APPROVED',datetime('now','localtime'))");
            } catch (Exception e) {
                System.out.println("[NetConfig] 超级管理员初始化失败: " + e.getMessage());
            }

            String[] tables = {"command_topics", "faults", "desktop", "linux", "office", "ai_topics"};

            for (String table : tables) {
                try {
                    stmt.execute("ALTER TABLE " + table + " ADD COLUMN images TEXT DEFAULT '[]'");
                } catch (Exception ignored) {}
                try {
                    stmt.execute("ALTER TABLE " + table + " ADD COLUMN videos TEXT DEFAULT '[]'");
                } catch (Exception ignored) {}
            }

            for (String table : tables) {
                try {
                    stmt.execute("UPDATE " + table + " SET created_at=datetime('now','localtime') WHERE created_at IS NULL OR created_at=''");
                } catch (Exception ignored) {}
            }
            for (String table : tables) {
                try {
                    stmt.execute("UPDATE " + table + " SET created_at=substr(datetime(created_at,'localtime'),1,16) WHERE created_at IS NOT NULL AND length(created_at)=19");
                } catch (Exception ignored) {}
            }

            System.out.println("[NetConfig] 数据库初始化完成: " + dbPath);
        }
    }
}
