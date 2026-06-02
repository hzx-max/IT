package com.netconfig.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.*;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${project.root:}")
    private String projectRoot;

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        String dbPath = new File(root, "netconfig.db").getAbsolutePath();
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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS meta (
                    key TEXT PRIMARY KEY,
                    value TEXT
                )
            """);

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

            migrateOldCommandsTable(stmt);

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

    private void migrateOldCommandsTable(Statement stmt) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM commands");
            int oldCount = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            if (oldCount == 0) return;

            System.out.println("[NetConfig] 发现旧 commands 表数据(" + oldCount + "条)，开始迁移...");

            ResultSet rows = stmt.executeQuery("SELECT * FROM commands ORDER BY created_at");
            while (rows.next()) {
                String oldId = rows.getString("id");
                String topicId = oldId.startsWith("cmd_") ? "topic_" + oldId.substring(4) : "topic_" + oldId;

                try {
                    stmt.execute("INSERT OR IGNORE INTO command_topics (id,title,cat,topo,desc,detail,created_at) VALUES ('"
                            + topicId + "','" + escapeSql(rows.getString("title")) + "','"
                            + escapeSql(rows.getString("cat")) + "','"
                            + escapeSql(rows.getString("topo") != null ? rows.getString("topo") : "[]") + "','"
                            + escapeSql(rows.getString("desc") != null ? rows.getString("desc") : "") + "','"
                            + escapeSql(rows.getString("detail") != null ? rows.getString("detail") : "") + "','"
                            + escapeSql(rows.getString("created_at") != null ? rows.getString("created_at") : "") + "')");
                } catch (Exception e) { /* ignore duplicate */ }

                Map<String, String> configs = safeJsonToMap(rows.getString("configs"));
                Map<String, String> comments = safeJsonToMap(rows.getString("comments"));
                Map<String, String> docs = safeJsonToMap(rows.getString("docs"));
                Map<String, Object> verification = safeJsonToObjMap(rows.getString("verification"));

                Set<String> allVendors = new LinkedHashSet<>();
                allVendors.addAll(configs.keySet());
                allVendors.addAll(comments.keySet());
                allVendors.addAll(docs.keySet());
                allVendors.addAll(verification.keySet());

                for (String vendor : allVendors) {
                    String vc = configs.getOrDefault(vendor, "");
                    String vcm = comments.getOrDefault(vendor, "");
                    String vd = docs.getOrDefault(vendor, "");
                    Object vv = verification.get(vendor);
                    String vvCmd = "";
                    String vvImg = "[]";
                    if (vv instanceof Map) {
                        Map<?, ?> vvMap = (Map<?, ?>) vv;
                        Object cmdObj = vvMap.get("cmd");
                        vvCmd = cmdObj != null ? cmdObj.toString() : "";
                        Object imgObj = vvMap.get("images");
                        vvImg = imgObj != null ? safeToJson(imgObj) : "[]";
                    } else if (vv != null && !vv.toString().isEmpty()) {
                        vvCmd = vv.toString();
                    }
                    if (vc.isEmpty() && vcm.isEmpty() && vd.isEmpty() && vvCmd.isEmpty()) continue;

                    String cfgId = "cfg_" + vendor + "_" + topicId;
                    try {
                        stmt.execute("INSERT OR IGNORE INTO command_configs (id,topic_id,vendor,config,comment,doc,verification_cmd,verification_images) VALUES ('"
                                + cfgId + "','" + topicId + "','" + vendor + "','"
                                + escapeSql(vc) + "','" + escapeSql(vcm) + "','" + escapeSql(vd) + "','"
                                + escapeSql(vvCmd) + "','" + escapeSql(vvImg) + "')");
                    } catch (Exception ignored) {}
                }
            }
            rows.close();

            ResultSet oldNotes = stmt.executeQuery("SELECT cmd_id, content FROM notes");
            while (oldNotes.next()) {
                String oldCmdId = oldNotes.getString("cmd_id");
                String newId = oldCmdId.startsWith("cmd_") ? "topic_" + oldCmdId.substring(4) : "topic_" + oldCmdId;
                try {
                    stmt.execute("INSERT OR IGNORE INTO notes (cmd_id,content) VALUES ('" + newId + "','" + escapeSql(oldNotes.getString("content")) + "')");
                } catch (Exception ignored) {}
            }
            oldNotes.close();

            System.out.println("[NetConfig] 旧 commands 表数据迁移完成");
        } catch (Exception e) {
            System.out.println("[NetConfig] 旧 commands 表不存在或迁移失败（可忽略）: " + e.getMessage());
        }
    }

    private String escapeSql(String s) {
        if (s == null) return "";
        return s.replace("'", "''");
    }

    private Map<String, String> safeJsonToMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> safeJsonToObjMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String safeToJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
}
