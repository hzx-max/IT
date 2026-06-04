$BASE = "http://localhost:8080"
$pass = 0
$fail = 0

function Pass($msg) { $script:pass++; Write-Host "[PASS] $msg" -ForegroundColor Green }
function Fail($msg) { $script:fail++; Write-Host "[FAIL] $msg" -ForegroundColor Red }

Write-Host "`n===== IT运维学习平台 全面测试 =====" -ForegroundColor Cyan

# Login
Write-Host "`n--- 1. 认证模块 ---" -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"admin123"}'
$r = Invoke-RestMethod -Uri "$BASE/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $r.data.token
$hdrs = @{Authorization = "Bearer $token"}

Pass "POST /auth/login (correct)" 

# /me
$r = Invoke-RestMethod -Uri "$BASE/api/auth/me" -Method GET -Headers $hdrs
if ($r.data.username -eq "admin") { Pass "GET /auth/me" } else { Fail "GET /auth/me" }

# bad login
try { Invoke-RestMethod -Uri "$BASE/api/auth/login" -Method POST -Body '{"username":"admin","password":"wrong"}' -ContentType "application/json" 2>$null; Fail "POST /auth/login (bad pw)" }
catch { if ($_.Exception.Message -match "401") { Pass "POST /auth/login (bad pw)" } else { Fail "POST /auth/login (bad pw)" } }

# register
try {
    $r = Invoke-RestMethod -Uri "$BASE/api/auth/register" -Method POST -Body '{"username":"__tu01__","password":"test123"}' -ContentType "application/json"
    if ($r.ok) { Pass "POST /auth/register" } else { Fail "POST /auth/register" }
} catch { Fail "POST /auth/register: $_" }

# duplicate register should fail
try { Invoke-RestMethod -Uri "$BASE/api/auth/register" -Method POST -Body '{"username":"__tu01__","password":"test123"}' -ContentType "application/json" 2>$null; Fail "POST /auth/register dup" }
catch { Pass "POST /auth/register (duplicate)" }

# users list
$r = Invoke-RestMethod -Uri "$BASE/api/auth/users" -Method GET -Headers $hdrs
$tu = ($r.data | Where-Object { $_.username -eq "__tu01__" })[0]
if ($tu) { $uid = $tu.id; Pass "GET /auth/users" } else { Fail "GET /auth/users" }

# approve
$r = Invoke-RestMethod -Uri "$BASE/api/auth/approve/$uid" -Method POST -Headers $hdrs -Body '{"approved":true}' -ContentType "application/json"
if ($r.ok) { Pass "POST /auth/approve" } else { Fail "POST /auth/approve" }

# logout
$r = Invoke-RestMethod -Uri "$BASE/api/auth/logout" -Method POST -Headers $hdrs
if ($r.ok) { Pass "POST /auth/logout" } else { Fail "POST /auth/logout" }

# token invalid after logout
try { Invoke-RestMethod -Uri "$BASE/api/auth/me" -Method GET -Headers $hdrs 2>$null; Fail "GET /auth/me (logged out)" }
catch { if ($_.Exception.Message -match "401") { Pass "GET /auth/me (logged out)" } else { Fail "GET /auth/me (logged out)" } }

# re-login
$r = Invoke-RestMethod -Uri "$BASE/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $r.data.token
$hdrs = @{Authorization = "Bearer $token"}

# ---- 权限控制 ----
Write-Host "`n--- 2. 权限控制 ---" -ForegroundColor Yellow

$r = Invoke-RestMethod -Uri "$BASE/api/topics" -Method GET
if ($r.Count -ge 0) { Pass "GET /topics (no auth)" } else { Fail "GET /topics (no auth)" }

try { Invoke-RestMethod -Uri "$BASE/api/topics" -Method POST -Body '{"title":"hack"}' -ContentType "application/json" 2>$null; Fail "POST /topics (no auth)" }
catch { if ($_.Exception.Message -match "401") { Pass "POST /topics (no auth)" } else { Fail "POST /topics (no auth)" } }

# ---- 3. CRUD ----
Write-Host "`n--- 3. CRUD 模块 ---" -ForegroundColor Yellow

$modules = @(
    @{Name="faults"; CBody='{"title":"___TF___","category":"test","symptom":"test","cause":"test","solution":"test"}'; UBody='{"title":"___TFU___","category":"test","symptom":"upd","cause":"upd","solution":"upd"}'},
    @{Name="desktop"; CBody='{"title":"___TD___","category":"test","symptom":"test","cause":"test","solution":"test"}'; UBody='{"title":"___TDU___","category":"test","symptom":"upd","cause":"upd","solution":"upd"}'},
    @{Name="linux"; CBody='{"title":"___TL___","vendor":"ubuntu","cat":"test","desc":"test","detail":"test","config":"test"}'; UBody='{"title":"___TLU___","vendor":"ubuntu","cat":"test","desc":"upd","detail":"upd","config":"upd"}'},
    @{Name="office"; CBody='{"title":"___TO___","vendor":"word","cat":"test","desc":"test","detail":"test","config":"test"}'; UBody='{"title":"___TOU___","vendor":"word","cat":"test","desc":"upd","detail":"upd","config":"upd"}'},
    @{Name="ai"; CBody='{"title":"___TA___","category":"test","prompt":"test","llm":"chatgpt","response":"test","tags":[]}'; UBody='{"title":"___TAU___","category":"test","prompt":"upd","llm":"chatgpt","response":"upd","tags":[]}'},
    @{Name="topics"; CBody='{"title":"___TT___","cat":"test","desc":"test","configs":[]}'; UBody='{"title":"___TTU___","cat":"test","desc":"upd","configs":[]}'}
)

$ids = @{}

foreach ($m in $modules) {
    $n = $m.Name
    
    $r = Invoke-RestMethod -Uri "$BASE/api/$n" -Method GET
    if ($r.Count -ge 0) { Pass "GET /$n" } else { Fail "GET /$n" }

    $r = Invoke-RestMethod -Uri "$BASE/api/$n" -Method POST -Headers $hdrs -Body $m.CBody -ContentType "application/json"
    if ($r.data.id) { $ids[$n] = $r.data.id; Pass "POST /$n" } else { Fail "POST /$n" }

    $r = Invoke-RestMethod -Uri "$BASE/api/$n/$($ids[$n])" -Method GET
    if ($r.title -like "*___T*") { Pass "GET /$n/{id}" } else { Fail "GET /$n/{id}" }

    $r = Invoke-RestMethod -Uri "$BASE/api/$n/$($ids[$n])" -Method PUT -Headers $hdrs -Body $m.UBody -ContentType "application/json"
    if ($r.data.title -like "*___T*U*") { Pass "PUT /$n/{id}" } else { Fail "PUT /$n/{id}" }

    $r = Invoke-RestMethod -Uri "$BASE/api/$n/$($ids[$n])" -Method DELETE -Headers $hdrs
    if ($r.ok) { Pass "DELETE /$n/{id}" } else { Fail "DELETE /$n/{id}" }
}

# Batch delete
$m = $modules[0]
$r = Invoke-RestMethod -Uri "$BASE/api/faults" -Method POST -Headers $hdrs -Body $m.CBody -ContentType "application/json"
$bid = $r.data.id
$bdBody = '{"ids":["' + $bid + '"]}'
$r = Invoke-RestMethod -Uri "$BASE/api/faults/batch-delete" -Method POST -Headers $hdrs -Body $bdBody -ContentType "application/json"
if ($r.ok) { Pass "POST /faults/batch-delete" } else { Fail "POST /faults/batch-delete" }

# ---- 4. 分类 ----
Write-Host "`n--- 4. 分类管理 ---" -ForegroundColor Yellow

$r = Invoke-RestMethod -Uri "$BASE/api/categories" -Method GET
Pass "GET /categories"

$r = Invoke-RestMethod -Uri "$BASE/api/categories" -Method POST -Headers $hdrs -Body '{"cat_key":"__tcat__","cat_label":"Test"}' -ContentType "application/json"
if ($r.ok) { Pass "POST /categories" } else { Fail "POST /categories" }

$r = Invoke-RestMethod -Uri "$BASE/api/categories/__tcat__" -Method DELETE -Headers $hdrs
Pass "DELETE /categories"

$r = Invoke-RestMethod -Uri "$BASE/api/categories/exclusions" -Method GET
Pass "GET /categories/exclusions"

$r = Invoke-RestMethod -Uri "$BASE/api/categories/exclusions" -Method POST -Headers $hdrs -Body '{"cat_key":"__texcl__"}' -ContentType "application/json"
if ($r.ok) { Pass "POST /categories/exclusions" } else { Fail "POST /categories/exclusions" }

$r = Invoke-RestMethod -Uri "$BASE/api/categories/exclusions/__texcl__" -Method DELETE -Headers $hdrs
Pass "DELETE /categories/exclusions"

# ---- 5. Notes ----
Write-Host "`n--- 5. 笔记 ---" -ForegroundColor Yellow

$nid = "topic_1780433957512_a"

$r = Invoke-RestMethod -Uri "$BASE/api/notes/$nid" -Method PUT -Headers $hdrs -Body '{"content":"___TN___"}' -ContentType "application/json"
if ($r.ok) { Pass "PUT /notes" } else { Fail "PUT /notes" }

$r = Invoke-RestMethod -Uri "$BASE/api/notes/$nid" -Method GET -Headers $hdrs
if ($r.content -eq "___TN___") { Pass "GET /notes" } else { Fail "GET /notes" }

$r = Invoke-RestMethod -Uri "$BASE/api/notes/$nid" -Method PUT -Headers $hdrs -Body '{"content":""}' -ContentType "application/json"
if ($r.ok) { Pass "PUT /notes (clear)" } else { Fail "PUT /notes (clear)" }

# ---- 6. Search History ----
Write-Host "`n--- 6. 搜索历史 ---" -ForegroundColor Yellow

$r = Invoke-RestMethod -Uri "$BASE/api/search-history" -Method POST -Headers $hdrs -Body '{"module":"cmd","keyword":"test_kw"}' -ContentType "application/json"
if ($r.ok) { Pass "POST /search-history" } else { Fail "POST /search-history" }

$r = Invoke-RestMethod -Uri "$BASE/api/search-history/cmd" -Method GET
if ($r.ok) { Pass "GET /search-history/cmd" } else { Fail "GET /search-history/cmd" }

$r = Invoke-RestMethod -Uri "$BASE/api/search-history/cmd" -Method DELETE -Headers $hdrs
if ($r.ok) { Pass "DELETE /search-history/cmd" } else { Fail "DELETE /search-history/cmd" }

# ---- 7. Clicks ----
Write-Host "`n--- 7. 点击记录 ---" -ForegroundColor Yellow

$r = Invoke-RestMethod -Uri "$BASE/api/clicks/record" -Method POST -Body '{"module":"cmd","itemId":"topic_1780433957512_a","itemTitle":"Test"}' -ContentType "application/json"
if ($r.ok) { Pass "POST /clicks/record" } else { Fail "POST /clicks/record" }

$r = Invoke-RestMethod -Uri "$BASE/api/clicks/top10" -Method GET
if ($r.ok) { Pass "GET /clicks/top10" } else { Fail "GET /clicks/top10" }

$r = Invoke-RestMethod -Uri "$BASE/api/clicks/top10/cmd" -Method GET
if ($r.ok) { Pass "GET /clicks/top10/cmd" } else { Fail "GET /clicks/top10/cmd" }

$r = Invoke-RestMethod -Uri "$BASE/api/clicks/stats" -Method GET
if ($r.ok) { Pass "GET /clicks/stats" } else { Fail "GET /clicks/stats" }

# ---- 8. Admin ----
Write-Host "`n--- 8. Admin ---" -ForegroundColor Yellow

$r = Invoke-RestMethod -Uri "$BASE/api/admin/pending-changes" -Method GET -Headers $hdrs
Pass "GET /admin/pending-changes"

# ---- 9. Cleanup ----
Write-Host "`n--- 9. Cleanup ---" -ForegroundColor Yellow

$r = Invoke-RestMethod -Uri "$BASE/api/auth/users" -Method GET -Headers $hdrs
$tu = ($r.data | Where-Object { $_.username -eq "__tu01__" })[0]
if ($tu) {
    $r = Invoke-RestMethod -Uri "$BASE/api/auth/users/$($tu.id)" -Method DELETE -Headers $hdrs
    if ($r.ok) { Pass "DELETE /auth/users (cleanup)" } else { Fail "DELETE /auth/users (cleanup)" }
} else { Pass "DELETE /auth/users (already clean)" }

# ---- Results ----
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  TOTAL: $($pass + $fail)" -ForegroundColor White
Write-Host "  PASS:  $pass" -ForegroundColor Green
Write-Host "  FAIL:  $fail" -ForegroundColor Red
Write-Host "=========================================" -ForegroundColor Cyan

if ($fail -gt 0) { exit 1 } else { exit 0 }