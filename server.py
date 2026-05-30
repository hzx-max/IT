"""
IT运维学习平台 - 后端服务
Flask + SQLite，提供 REST API + 静态文件服务
启动: python server.py
"""
import json
import sqlite3
import os
from datetime import datetime, timezone, timedelta
from flask import Flask, request, jsonify, g, send_from_directory, redirect

app = Flask(__name__, static_folder='.')
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'netconfig.db')

# ===== 数据库 =====
def get_db():
    if 'db' not in g:
        g.db = sqlite3.connect(DB_PATH)
        g.db.row_factory = sqlite3.Row
        g.db.execute("PRAGMA journal_mode=WAL")
    return g.db

@app.teardown_appcontext
def close_db(exception):
    db = g.pop('db', None)
    if db is not None:
        db.close()

def init_db():
    db = sqlite3.connect(DB_PATH)
    db.execute("PRAGMA journal_mode=WAL")
    db.execute("PRAGMA foreign_keys=ON")
    db.execute('''
        CREATE TABLE IF NOT EXISTS command_topics (
            id TEXT PRIMARY KEY,
            title TEXT NOT NULL,
            cat TEXT NOT NULL,
            topo TEXT DEFAULT '[]',
            desc TEXT DEFAULT '',
            detail TEXT DEFAULT '',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    db.execute('''
        CREATE TABLE IF NOT EXISTS command_configs (
            id TEXT PRIMARY KEY,
            topic_id TEXT NOT NULL,
            vendor TEXT NOT NULL,
            config TEXT DEFAULT '',
            comment TEXT DEFAULT '',
            doc TEXT DEFAULT '',
            verification_cmd TEXT DEFAULT '',
            verification_images TEXT DEFAULT '[]',
            UNIQUE(topic_id, vendor),
            FOREIGN KEY (topic_id) REFERENCES command_topics(id) ON DELETE CASCADE
        )
    ''')
    db.execute('''
        CREATE TABLE IF NOT EXISTS notes (
            cmd_id TEXT PRIMARY KEY,
            content TEXT DEFAULT '',
            FOREIGN KEY (cmd_id) REFERENCES command_topics(id) ON DELETE CASCADE
        )
    ''')
    db.execute('''
        CREATE TABLE IF NOT EXISTS category_labels (
            cat_key TEXT PRIMARY KEY,
            cat_label TEXT NOT NULL
        )
    ''')
    db.execute('''
        CREATE TABLE IF NOT EXISTS category_exclusions (
            cat_key TEXT PRIMARY KEY
        )
    ''')
    db.execute('''
        CREATE TABLE IF NOT EXISTS meta (
            key TEXT PRIMARY KEY,
            value TEXT
        )
    ''')
    db.execute('''
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
    ''')
    db.execute('''
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
    ''')
    db.execute('''
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
    ''')
    db.execute('''
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
    ''')
    # 自动迁移旧 commands 表数据
    try:
        old_count = db.execute("SELECT COUNT(*) FROM commands").fetchone()[0]
    except:
        old_count = 0
    if old_count > 0:
        try:
            _migrate_old_data(db)
        except Exception as e:
            print(f"数据迁移失败（可忽略）: {e}")
    # 迁移：为旧数据补充 created_at
    for table in ('faults', 'desktop', 'linux', 'office'):
        try:
            db.execute(f"UPDATE {table} SET created_at=datetime('now','localtime') WHERE created_at IS NULL OR created_at=''")
        except Exception:
            pass
    db.commit()
    db.close()

def _migrate_old_data(db):
    old_rows = db.execute("SELECT * FROM commands ORDER BY created_at").fetchall()
    for row in old_rows:
        topic_id = 'topic_' + row[0].replace('cmd_', '') if row[0].startswith('cmd_') else 'topic_' + row[0]
        db.execute(
            "INSERT OR IGNORE INTO command_topics (id,title,cat,topo,desc,detail,created_at) VALUES (?,?,?,?,?,?,?)",
            (topic_id, row[1], row[3], row[4], row[5] or '', row[6] or '', row[12] or '')
        )
        configs = _safe_json_load(row[7])
        comments = _safe_json_load(row[8])
        docs = _safe_json_load(row[9])
        verification = _safe_json_load(row[10])
        all_vendors = set(list(configs.keys()) + list(comments.keys()) + list(docs.keys()) + list(verification.keys()))
        for vendor in all_vendors:
            cfg_id = f"cfg_{vendor}_{topic_id}"
            vc = configs.get(vendor, '')
            vcm = comments.get(vendor, '')
            vd = docs.get(vendor, '')
            vv = verification.get(vendor, {})
            if isinstance(vv, dict):
                vv_cmd = vv.get('cmd', '')
                vv_img = json.dumps(vv.get('images', []), ensure_ascii=False)
            else:
                vv_cmd = str(vv) if vv else ''
                vv_img = '[]'
            if not vc and not vcm and not vd and not vv_cmd:
                continue
            db.execute(
                "INSERT OR IGNORE INTO command_configs (id,topic_id,vendor,config,comment,doc,verification_cmd,verification_images) VALUES (?,?,?,?,?,?,?,?)",
                (cfg_id, topic_id, vendor, vc, vcm, vd, vv_cmd, vv_img)
            )
    # 迁移 notes 外键
    old_notes = db.execute("SELECT cmd_id, content FROM notes").fetchall()
    for n in old_notes:
        new_id = 'topic_' + n[0].replace('cmd_', '') if n[0].startswith('cmd_') else 'topic_' + n[0]
        db.execute("INSERT OR IGNORE INTO notes (cmd_id,content) VALUES (?,?)", (new_id, n[1]))

def _safe_json_load(val):
    if not val:
        return {}
    try:
        return json.loads(val)
    except:
        return {}

def convert_time_to_local(utc_str):
    if not utc_str:
        return utc_str
    try:
        dt = datetime.strptime(utc_str, '%Y-%m-%d %H:%M:%S')
        dt = dt.replace(tzinfo=timezone.utc).astimezone()
        return dt.strftime('%Y-%m-%d %H:%M')
    except (ValueError, TypeError):
        return utc_str

def row_to_dict(row):
    d = dict(row)
    # 解析所有 JSON 字符串字段
    _json_list_fields = ('topo', 'verification_images')
    _json_dict_fields = ('docs', 'configs', 'comments', 'verification')
    for key in _json_list_fields:
        if key in d:
            try:
                d[key] = json.loads(d[key])
            except (json.JSONDecodeError, TypeError):
                d[key] = []
    for key in _json_dict_fields:
        if key in d:
            try:
                d[key] = json.loads(d[key])
            except (json.JSONDecodeError, TypeError):
                d[key] = {}
    if 'created_at' in d:
        d['created_at'] = convert_time_to_local(d['created_at'])
    return d

def row_to_summary(row):
    """通用的行转摘要字典：仅转换时间，不解析 JSON 字段"""
    d = dict(row)
    if 'created_at' in d:
        d['created_at'] = convert_time_to_local(d['created_at'])
    return d

def topic_to_summary(row):
    d = dict(row)
    d.pop('topo', None)
    d.pop('detail', None)
    if 'created_at' in d:
        d['created_at'] = convert_time_to_local(d['created_at'])
    return d

def config_to_dict(row):
    d = dict(row)
    if 'verification_images' in d:
        try:
            d['verification_images'] = json.loads(d['verification_images'])
        except:
            d['verification_images'] = []
    return d

# ===== 静态文件 =====
@app.route('/')
def index():
    return redirect('/pages/cmd/cmd_list.html')

@app.route('/<path:path>')
def static_files(path):
    safe_path = os.path.normpath(os.path.join(BASE_DIR, path))
    if not safe_path.startswith(BASE_DIR):
        return jsonify({"error": "Not found"}), 404
    if os.path.isfile(safe_path):
        resp = send_from_directory(BASE_DIR, path)
        if path.endswith('.html') or path.endswith('.css') or path.endswith('.js'):
            resp.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
            resp.headers['Pragma'] = 'no-cache'
            resp.headers['Expires'] = '0'
        return resp
    return jsonify({"error": "Not found"}), 404

# ===== Topics API =====
@app.route('/api/topics', methods=['GET'])
def api_list_topics():
    db = get_db()
    rows = db.execute("SELECT id,title,cat,desc,created_at FROM command_topics ORDER BY created_at DESC, cat, title").fetchall()
    result = []
    for r in rows:
        t = topic_to_summary(r)
        cfgs = db.execute("SELECT id,vendor,config,comment,doc,verification_cmd,verification_images FROM command_configs WHERE topic_id=? ORDER BY vendor", (t['id'],)).fetchall()
        t['configs'] = [config_to_dict(c) for c in cfgs]
        result.append(t)
    return jsonify(result)

@app.route('/api/topics/<topic_id>', methods=['GET'])
def api_get_topic(topic_id):
    db = get_db()
    row = db.execute("SELECT * FROM command_topics WHERE id=?", (topic_id,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    t = row_to_dict(row)
    cfgs = db.execute("SELECT id,vendor,config,comment,doc,verification_cmd,verification_images FROM command_configs WHERE topic_id=? ORDER BY vendor", (topic_id,)).fetchall()
    t['configs'] = [config_to_dict(c) for c in cfgs]
    return jsonify(t)

@app.route('/api/topics', methods=['POST'])
def api_create_topic():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    if 'title' not in data or 'cat' not in data:
        return jsonify({"error": "Missing title or cat"}), 400
    db = get_db()
    tid = data.get('id', 'topic_' + str(int(__import__('time').time() * 1000)) + '_' + __import__('random').choice('abcdefghijklmnopqrstuvwxyz0123456789'))
    try:
        db.execute(
            "INSERT INTO command_topics (id,title,cat,topo,desc,detail) VALUES (?,?,?,?,?,?)",
            (tid, data['title'], data['cat'],
             json.dumps(data.get('topo', []), ensure_ascii=False),
             data.get('desc', ''), data.get('detail', ''))
        )
        for cfg in data.get('configs', []):
            cid = f"cfg_{cfg.get('vendor','')}_{tid}"
            db.execute(
                "INSERT OR IGNORE INTO command_configs (id,topic_id,vendor,config,comment,doc,verification_cmd,verification_images) VALUES (?,?,?,?,?,?,?,?)",
                (cid, tid, cfg.get('vendor', ''),
                 cfg.get('config', ''), cfg.get('comment', ''),
                 cfg.get('doc', ''), cfg.get('verification_cmd', ''),
                 json.dumps(cfg.get('verification_images', []), ensure_ascii=False))
            )
        db.commit()
    except sqlite3.IntegrityError:
        return jsonify({"error": "ID already exists"}), 409
    return jsonify({"ok": True, "id": tid}), 201

@app.route('/api/topics/<topic_id>', methods=['PUT'])
def api_update_topic(topic_id):
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    db = get_db()
    row = db.execute("SELECT id FROM command_topics WHERE id=?", (topic_id,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    db.execute(
        "UPDATE command_topics SET title=?,cat=?,topo=?,desc=?,detail=? WHERE id=?",
        (data.get('title', ''), data.get('cat', ''),
         json.dumps(data.get('topo', []), ensure_ascii=False),
         data.get('desc', ''), data.get('detail', ''),
         topic_id)
    )
    db.execute("DELETE FROM command_configs WHERE topic_id=?", (topic_id,))
    for cfg in data.get('configs', []):
        cid = cfg.get('id') or f"cfg_{cfg.get('vendor','')}_{topic_id}"
        db.execute(
            "INSERT INTO command_configs (id,topic_id,vendor,config,comment,doc,verification_cmd,verification_images) VALUES (?,?,?,?,?,?,?,?)",
            (cid, topic_id, cfg.get('vendor', ''),
             cfg.get('config', ''), cfg.get('comment', ''),
             cfg.get('doc', ''), cfg.get('verification_cmd', ''),
             json.dumps(cfg.get('verification_images', []), ensure_ascii=False))
        )
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/topics/<topic_id>', methods=['DELETE'])
def api_delete_topic(topic_id):
    db = get_db()
    db.execute("DELETE FROM notes WHERE cmd_id=?", (topic_id,))
    db.execute("DELETE FROM command_configs WHERE topic_id=?", (topic_id,))
    db.execute("DELETE FROM command_topics WHERE id=?", (topic_id,))
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/topics/batch-delete', methods=['POST'])
def api_batch_delete_topics():
    data = request.get_json()
    if not data or 'ids' not in data:
        return jsonify({"error": "Missing ids"}), 400
    ids = data['ids']
    if not isinstance(ids, list) or len(ids) == 0:
        return jsonify({"error": "ids must be a non-empty list"}), 400
    db = get_db()
    placeholders = ','.join(['?'] * len(ids))
    db.execute(f"DELETE FROM notes WHERE cmd_id IN ({placeholders})", ids)
    db.execute(f"DELETE FROM command_configs WHERE topic_id IN ({placeholders})", ids)
    db.execute(f"DELETE FROM command_topics WHERE id IN ({placeholders})", ids)
    db.commit()
    return jsonify({"ok": True, "deleted": len(ids)})

# ===== Configs API =====
@app.route('/api/topics/<topic_id>/configs', methods=['POST'])
def api_add_config(topic_id):
    data = request.get_json()
    if not data or 'vendor' not in data:
        return jsonify({"error": "Missing vendor"}), 400
    db = get_db()
    cid = data.get('id', f"cfg_{data['vendor']}_{topic_id}")
    try:
        db.execute(
            "INSERT INTO command_configs (id,topic_id,vendor,config,comment,doc,verification_cmd,verification_images) VALUES (?,?,?,?,?,?,?,?)",
            (cid, topic_id, data['vendor'],
             data.get('config', ''), data.get('comment', ''),
             data.get('doc', ''), data.get('verification_cmd', ''),
             json.dumps(data.get('verification_images', []), ensure_ascii=False))
        )
        db.commit()
    except sqlite3.IntegrityError:
        return jsonify({"error": "Config already exists for this vendor"}), 409
    return jsonify({"ok": True, "id": cid}), 201

@app.route('/api/configs/<config_id>', methods=['PUT'])
def api_update_config(config_id):
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    db = get_db()
    row = db.execute("SELECT id FROM command_configs WHERE id=?", (config_id,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    db.execute(
        "UPDATE command_configs SET vendor=?,config=?,comment=?,doc=?,verification_cmd=?,verification_images=? WHERE id=?",
        (data.get('vendor', ''), data.get('config', ''), data.get('comment', ''),
         data.get('doc', ''), data.get('verification_cmd', ''),
         json.dumps(data.get('verification_images', []), ensure_ascii=False),
         config_id)
    )
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/configs/<config_id>', methods=['DELETE'])
def api_delete_config(config_id):
    db = get_db()
    db.execute("DELETE FROM command_configs WHERE id=?", (config_id,))
    db.commit()
    return jsonify({"ok": True})

# ===== 兼容旧 API =====
@app.route('/api/commands', methods=['GET'])
def api_list_commands():
    return redirect('/api/topics', 301)

@app.route('/api/commands/<cmd_id>', methods=['GET'])
def api_get_command(cmd_id):
    tid = 'topic_' + cmd_id.replace('cmd_', '') if cmd_id.startswith('cmd_') else cmd_id
    return redirect(f'/api/topics/{tid}', 301)

# ===== 备注 API =====
@app.route('/api/notes/<cmd_id>', methods=['GET'])
def api_get_note(cmd_id):
    db = get_db()
    row = db.execute("SELECT content FROM notes WHERE cmd_id=?", (cmd_id,)).fetchone()
    return jsonify({"content": row['content'] if row else ''})

@app.route('/api/notes/<cmd_id>', methods=['PUT'])
def api_save_note(cmd_id):
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    content = data.get('content', '')
    db = get_db()
    db.execute(
        "INSERT INTO notes (cmd_id,content) VALUES (?,?) ON CONFLICT(cmd_id) DO UPDATE SET content=?",
        (cmd_id, content, content)
    )
    db.commit()
    return jsonify({"ok": True})

# ===== 分类标签管理 =====
@app.route('/api/categories', methods=['GET'])
def api_list_categories():
    db = get_db()
    rows = db.execute("SELECT cat_key, cat_label FROM category_labels").fetchall()
    result = {}
    for r in rows:
        result[r['cat_key']] = r['cat_label']
    return jsonify(result)

@app.route('/api/categories', methods=['POST'])
def api_save_category():
    data = request.get_json()
    if not data or 'cat_key' not in data or 'cat_label' not in data:
        return jsonify({"error": "Missing fields"}), 400
    db = get_db()
    db.execute(
        "INSERT INTO category_labels (cat_key,cat_label) VALUES (?,?) ON CONFLICT(cat_key) DO UPDATE SET cat_label=?",
        (data['cat_key'], data['cat_label'], data['cat_label'])
    )
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/categories/<cat_key>', methods=['DELETE'])
def api_delete_category(cat_key):
    db = get_db()
    db.execute("DELETE FROM category_labels WHERE cat_key=?", (cat_key,))
    db.commit()
    return jsonify({"ok": True})

# ===== 分类排除管理（删除下拉框中的分类） =====
@app.route('/api/categories/exclusions', methods=['GET'])
def api_list_exclusions():
    db = get_db()
    rows = db.execute("SELECT cat_key FROM category_exclusions").fetchall()
    return jsonify([r['cat_key'] for r in rows])

@app.route('/api/categories/exclusions', methods=['POST'])
def api_add_exclusion():
    data = request.get_json()
    if not data or 'cat_key' not in data:
        return jsonify({"error": "Missing cat_key"}), 400
    db = get_db()
    db.execute("INSERT OR IGNORE INTO category_exclusions (cat_key) VALUES (?)", (data['cat_key'],))
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/categories/exclusions/<cat_key>', methods=['DELETE'])
def api_remove_exclusion(cat_key):
    db = get_db()
    db.execute("DELETE FROM category_exclusions WHERE cat_key=?", (cat_key,))
    db.commit()
    return jsonify({"ok": True})

# ===== 网络故障 CRUD API =====
@app.route('/api/faults', methods=['GET'])
def api_list_faults():
    db = get_db()
    rows = db.execute("SELECT id,title,category,symptom,created_at FROM faults ORDER BY created_at DESC, category, title").fetchall()
    return jsonify([row_to_summary(r) for r in rows])

@app.route('/api/faults/<fid>', methods=['GET'])
def api_get_fault(fid):
    db = get_db()
    row = db.execute("SELECT * FROM faults WHERE id=?", (fid,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    return jsonify(row_to_dict(row))

@app.route('/api/faults', methods=['POST'])
def api_create_fault():
    data = request.get_json()
    if not data or 'title' not in data:
        return jsonify({"error": "Missing field: title"}), 400
    db = get_db()
    fid = data.get('id', 'fault-' + str(int(__import__('time').time() * 1000)))
    try:
        db.execute(
            "INSERT INTO faults (id,title,category,symptom,cause,solution,topo,docs) VALUES (?,?,?,?,?,?,?,?)",
            (fid, data['title'], data.get('category', ''),
             data.get('symptom', ''), data.get('cause', ''), data.get('solution', ''),
             json.dumps(data.get('topo', []), ensure_ascii=False),
             json.dumps(data.get('docs', {}), ensure_ascii=False))
        )
        db.commit()
    except sqlite3.IntegrityError:
        return jsonify({"error": "ID already exists"}), 409
    return jsonify({"ok": True, "id": fid}), 201

@app.route('/api/faults/<fid>', methods=['PUT'])
def api_update_fault(fid):
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    db = get_db()
    row = db.execute("SELECT id FROM faults WHERE id=?", (fid,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    db.execute(
        "UPDATE faults SET title=?,category=?,symptom=?,cause=?,solution=?,topo=?,docs=? WHERE id=?",
        (data.get('title', ''), data.get('category', ''),
         data.get('symptom', ''), data.get('cause', ''), data.get('solution', ''),
         json.dumps(data.get('topo', []), ensure_ascii=False),
         json.dumps(data.get('docs', {}), ensure_ascii=False), fid)
    )
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/faults/batch-delete', methods=['POST'])
def api_batch_delete_faults():
    data = request.get_json()
    if not data or 'ids' not in data:
        return jsonify({"error": "Missing ids"}), 400
    ids = data['ids']
    if not isinstance(ids, list) or len(ids) == 0:
        return jsonify({"error": "ids must be a non-empty list"}), 400
    db = get_db()
    placeholders = ','.join(['?'] * len(ids))
    db.execute(f"DELETE FROM faults WHERE id IN ({placeholders})", ids)
    db.commit()
    return jsonify({"ok": True, "deleted": len(ids)})

@app.route('/api/faults/<fid>', methods=['DELETE'])
def api_delete_fault(fid):
    db = get_db()
    db.execute("DELETE FROM faults WHERE id=?", (fid,))
    db.commit()
    return jsonify({"ok": True})

# ===== 桌面运维 CRUD API =====
@app.route('/api/desktop', methods=['GET'])
def api_list_desktop():
    db = get_db()
    rows = db.execute("SELECT id,title,category,symptom,created_at FROM desktop ORDER BY created_at DESC, category, title").fetchall()
    return jsonify([row_to_summary(r) for r in rows])

@app.route('/api/desktop/<did>', methods=['GET'])
def api_get_desktop(did):
    db = get_db()
    row = db.execute("SELECT * FROM desktop WHERE id=?", (did,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    return jsonify(row_to_dict(row))

@app.route('/api/desktop', methods=['POST'])
def api_create_desktop():
    data = request.get_json()
    if not data or 'title' not in data:
        return jsonify({"error": "Missing field: title"}), 400
    db = get_db()
    did = data.get('id', 'desktop-' + str(int(__import__('time').time() * 1000)))
    try:
        db.execute(
            "INSERT INTO desktop (id,title,category,symptom,solution,topo,docs) VALUES (?,?,?,?,?,?,?)",
            (did, data['title'], data.get('category', ''),
             data.get('symptom', ''), data.get('solution', ''),
             json.dumps(data.get('topo', []), ensure_ascii=False),
             json.dumps(data.get('docs', {}), ensure_ascii=False))
        )
        db.commit()
    except sqlite3.IntegrityError:
        return jsonify({"error": "ID already exists"}), 409
    return jsonify({"ok": True, "id": did}), 201

@app.route('/api/desktop/<did>', methods=['PUT'])
def api_update_desktop(did):
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    db = get_db()
    row = db.execute("SELECT id FROM desktop WHERE id=?", (did,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    db.execute(
        "UPDATE desktop SET title=?,category=?,symptom=?,solution=?,topo=?,docs=? WHERE id=?",
        (data.get('title', ''), data.get('category', ''),
         data.get('symptom', ''), data.get('solution', ''),
         json.dumps(data.get('topo', []), ensure_ascii=False),
         json.dumps(data.get('docs', {}), ensure_ascii=False), did)
    )
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/desktop/batch-delete', methods=['POST'])
def api_batch_delete_desktop():
    data = request.get_json()
    if not data or 'ids' not in data:
        return jsonify({"error": "Missing ids"}), 400
    ids = data['ids']
    if not isinstance(ids, list) or len(ids) == 0:
        return jsonify({"error": "ids must be a non-empty list"}), 400
    db = get_db()
    placeholders = ','.join(['?'] * len(ids))
    db.execute(f"DELETE FROM desktop WHERE id IN ({placeholders})", ids)
    db.commit()
    return jsonify({"ok": True, "deleted": len(ids)})

@app.route('/api/desktop/<did>', methods=['DELETE'])
def api_delete_desktop(did):
    db = get_db()
    db.execute("DELETE FROM desktop WHERE id=?", (did,))
    db.commit()
    return jsonify({"ok": True})

# ===== Linux运维 CRUD API =====
@app.route('/api/linux', methods=['GET'])
def api_list_linux():
    db = get_db()
    rows = db.execute("SELECT id,title,vendor,cat,desc,created_at FROM linux ORDER BY created_at DESC, vendor, cat, title").fetchall()
    return jsonify([row_to_summary(r) for r in rows])

@app.route('/api/linux/<lid>', methods=['GET'])
def api_get_linux(lid):
    db = get_db()
    row = db.execute("SELECT * FROM linux WHERE id=?", (lid,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    return jsonify(row_to_dict(row))

@app.route('/api/linux', methods=['POST'])
def api_create_linux():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    required = ['id', 'title', 'vendor', 'cat']
    for f in required:
        if f not in data:
            return jsonify({"error": f"Missing field: {f}"}), 400
    db = get_db()
    try:
        db.execute(
            "INSERT INTO linux (id,title,vendor,cat,topo,desc,detail,configs,comments,docs) VALUES (?,?,?,?,?,?,?,?,?,?)",
            (data['id'], data['title'], data['vendor'], data['cat'],
             json.dumps(data.get('topo', []), ensure_ascii=False),
             data.get('desc', ''), data.get('detail', ''),
             json.dumps(data.get('configs', {}), ensure_ascii=False),
             json.dumps(data.get('comments', {}), ensure_ascii=False),
             json.dumps(data.get('docs', {}), ensure_ascii=False))
        )
        db.commit()
    except sqlite3.IntegrityError:
        return jsonify({"error": "ID already exists"}), 409
    return jsonify({"ok": True}), 201

@app.route('/api/linux/<lid>', methods=['PUT'])
def api_update_linux(lid):
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    db = get_db()
    row = db.execute("SELECT id FROM linux WHERE id=?", (lid,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    db.execute(
        "UPDATE linux SET title=?,vendor=?,cat=?,topo=?,desc=?,detail=?,configs=?,comments=?,docs=? WHERE id=?",
        (data.get('title', ''), data.get('vendor', ''), data.get('cat', ''),
         json.dumps(data.get('topo', []), ensure_ascii=False),
         data.get('desc', ''), data.get('detail', ''),
         json.dumps(data.get('configs', {}), ensure_ascii=False),
         json.dumps(data.get('comments', {}), ensure_ascii=False),
         json.dumps(data.get('docs', {}), ensure_ascii=False), lid)
    )
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/linux/batch-delete', methods=['POST'])
def api_batch_delete_linux():
    data = request.get_json()
    if not data or 'ids' not in data:
        return jsonify({"error": "Missing ids"}), 400
    ids = data['ids']
    if not isinstance(ids, list) or len(ids) == 0:
        return jsonify({"error": "ids must be a non-empty list"}), 400
    db = get_db()
    placeholders = ','.join(['?'] * len(ids))
    db.execute(f"DELETE FROM linux WHERE id IN ({placeholders})", ids)
    db.commit()
    return jsonify({"ok": True, "deleted": len(ids)})

@app.route('/api/linux/<lid>', methods=['DELETE'])
def api_delete_linux(lid):
    db = get_db()
    db.execute("DELETE FROM linux WHERE id=?", (lid,))
    db.commit()
    return jsonify({"ok": True})

# ===== Office操作 CRUD API =====
@app.route('/api/office', methods=['GET'])
def api_list_office():
    db = get_db()
    rows = db.execute("SELECT id,title,vendor,cat,desc,created_at FROM office ORDER BY created_at DESC, vendor, cat, title").fetchall()
    return jsonify([row_to_summary(r) for r in rows])

@app.route('/api/office/<oid>', methods=['GET'])
def api_get_office(oid):
    db = get_db()
    row = db.execute("SELECT * FROM office WHERE id=?", (oid,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    return jsonify(row_to_dict(row))

@app.route('/api/office', methods=['POST'])
def api_create_office():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    required = ['id', 'title', 'vendor', 'cat']
    for f in required:
        if f not in data:
            return jsonify({"error": f"Missing field: {f}"}), 400
    db = get_db()
    try:
        db.execute(
            "INSERT INTO office (id,title,vendor,cat,topo,desc,detail,configs,comments,docs) VALUES (?,?,?,?,?,?,?,?,?,?)",
            (data['id'], data['title'], data['vendor'], data['cat'],
             json.dumps(data.get('topo', []), ensure_ascii=False),
             data.get('desc', ''), data.get('detail', ''),
             json.dumps(data.get('configs', {}), ensure_ascii=False),
             json.dumps(data.get('comments', {}), ensure_ascii=False),
             json.dumps(data.get('docs', {}), ensure_ascii=False))
        )
        db.commit()
    except sqlite3.IntegrityError:
        return jsonify({"error": "ID already exists"}), 409
    return jsonify({"ok": True}), 201

@app.route('/api/office/<oid>', methods=['PUT'])
def api_update_office(oid):
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400
    db = get_db()
    row = db.execute("SELECT id FROM office WHERE id=?", (oid,)).fetchone()
    if not row:
        return jsonify({"error": "Not found"}), 404
    db.execute(
        "UPDATE office SET title=?,vendor=?,cat=?,topo=?,desc=?,detail=?,configs=?,comments=?,docs=? WHERE id=?",
        (data.get('title', ''), data.get('vendor', ''), data.get('cat', ''),
         json.dumps(data.get('topo', []), ensure_ascii=False),
         data.get('desc', ''), data.get('detail', ''),
         json.dumps(data.get('configs', {}), ensure_ascii=False),
         json.dumps(data.get('comments', {}), ensure_ascii=False),
         json.dumps(data.get('docs', {}), ensure_ascii=False), oid)
    )
    db.commit()
    return jsonify({"ok": True})

@app.route('/api/office/batch-delete', methods=['POST'])
def api_batch_delete_office():
    data = request.get_json()
    if not data or 'ids' not in data:
        return jsonify({"error": "Missing ids"}), 400
    ids = data['ids']
    if not isinstance(ids, list) or len(ids) == 0:
        return jsonify({"error": "ids must be a non-empty list"}), 400
    db = get_db()
    placeholders = ','.join(['?'] * len(ids))
    db.execute(f"DELETE FROM office WHERE id IN ({placeholders})", ids)
    db.commit()
    return jsonify({"ok": True, "deleted": len(ids)})

@app.route('/api/office/<oid>', methods=['DELETE'])
def api_delete_office(oid):
    db = get_db()
    db.execute("DELETE FROM office WHERE id=?", (oid,))
    db.commit()
    return jsonify({"ok": True})

# ===== 启动 =====
if __name__ == '__main__':
    init_db()
    print("IT运维学习 后端已启动: http://localhost:5000")
    app.run(host='0.0.0.0', port=5000, debug=True)
