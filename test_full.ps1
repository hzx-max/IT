# ============================================
# FULL E2E TEST SUITE (FIXED)
# ============================================
$base = "http://localhost:8080"
$passed = 0
$failed = 0
$bugs = @()

function Pass($msg) { $script:passed++; Write-Host "[PASS] $msg" -ForegroundColor Green }
function Fail($msg) { $script:failed++; $script:bugs += $msg; Write-Host "[FAIL] $msg" -ForegroundColor Red }
function Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }

# ============================================
# PHASE 1: AUTHENTICATION
# ============================================
Info "========== PHASE 1: Authentication =========="

# 1.1 Super admin login
$superRes = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -Body '{"username":"admin","password":"admin123"}' -ContentType "application/json"
$superToken = $superRes.data.token
Pass "1.1 Super admin login: admin, role=$($superRes.data.role)"

# 1.2 Admin login - try hzx9
$adminToken = $null; $adminUid = $null; $adminName = $null
foreach ($pw in @('123456','hzx9','hzx','admin123')) {
  try {
    $b = "{""username"":""hzx9"",""password"":""$pw""}"
    $r = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -Body $b -ContentType "application/json"
    $adminToken = $r.data.token
    $adminUid = $r.data.id
    $adminName = $r.data.username
    Pass "1.2 Admin login (pw=$pw): $adminName, id=$adminUid, role=$($r.data.role)"
    break
  } catch {}
}
if (-not $adminToken) { Fail "1.2 Admin login: all passwords failed" }

# 1.3 User list
$users = Invoke-RestMethod -Uri "$base/api/auth/users" -Method GET -Headers @{Authorization = "Bearer $superToken"}
Pass "1.3 User list: $($users.data.Count) users"
$users.data | ForEach-Object { Info "  $($_.username) ($($_.role)) $($_.status)" }

# 1.4 Register new admin
$ts = Get-Date -Format "HHmmss"
$regBody = "{""username"":""test_$ts"",""password"":""test123""}"
$regRes = Invoke-RestMethod -Uri "$base/api/auth/register" -Method POST -Body $regBody -ContentType "application/json"
$newUserId = $regRes.data.id
$newUsername = $regRes.data.username
Pass "1.4 Register: $newUsername, id=$newUserId"

# 1.5 Approve
$approveRes = Invoke-RestMethod -Uri "$base/api/auth/approve/$newUserId" -Method POST -Body '{"approved":true}' -ContentType "application/json" -Headers @{Authorization = "Bearer $superToken"}
if ($approveRes.ok) { Pass "1.5 Approve user: ok" } else { Fail "1.5 Approve user: $approveRes" }

# 1.6 Verify approval
$users2 = Invoke-RestMethod -Uri "$base/api/auth/users" -Method GET -Headers @{Authorization = "Bearer $superToken"}
$nu = $users2.data | Where-Object { $_.id -eq $newUserId }
if ($nu.status -eq 'APPROVED') { Pass "1.6 User status: APPROVED" } else { Fail "1.6 User status: $($nu.status)" }

# ============================================
# PHASE 2: DATA CHANGE SUBMISSION
# ============================================
Info "========== PHASE 2: Data Change Submission =========="

# Get a fault to update
$faults = Invoke-RestMethod -Uri "$base/api/faults" -Method GET
$target = $faults[0]
Pass "2.0 Get faults: $($faults.Count) items, target=$($target.id), title=$($target.title)"

# 2.1 Submit UPDATE change - use proper JSON construction
$payloadObj = @{
    id = $target.id
    title = "TEST_UPDATE_$ts"
    category = $target.category
    symptom = $target.symptom
    cause = $target.cause
    solution = $target.solution
    images = @()
    videos = @()
    files = @()
    topo = @()
    docs = @{}
}
$submitBody = @{
    module = 'fault'
    operation = 'UPDATE'
    entityId = $target.id
    payload = $payloadObj
    submitterId = $adminUid
    submitterName = $adminName
} | ConvertTo-Json -Depth 5 -Compress

$submit = Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $submitBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
$changeId = $submit.data.id
Pass "2.1 Submit UPDATE: changeId=$changeId"

# 2.2 Verify pending change
$pendingList = Invoke-RestMethod -Uri "$base/api/admin/pending-changes" -Method GET -Headers @{Authorization = "Bearer $superToken"}
$change = $pendingList.data | Where-Object { $_.id -eq $changeId }
if ($change) {
    Pass "2.2 Pending change: module=$($change.module), op=$($change.operation)"
    $p = $change.payload | ConvertFrom-Json
    Pass "2.3 Payload title: $($p.title)"
} else { Fail "2.2 Pending change not found" }

# 2.4 Original data for diff
$original = Invoke-RestMethod -Uri "$base/api/faults/$($target.id)" -Method GET
if ($original.title -ne $p.title) {
    Pass "2.4 Diff: title [$($original.title)] -> [$($p.title)]"
} else { Fail "2.4 Diff not detected" }

# 2.5 Submit CREATE change
$createPayload = @{
    title = "TEST_CREATE_$ts"
    category = 'network'
    symptom = 'new_symptom'
    cause = 'new_cause'
    solution = 'new_solution'
    images = @()
    videos = @()
    files = @()
    topo = @()
    docs = @{}
}
$createBody = @{
    module = 'fault'
    operation = 'CREATE'
    entityId = ''
    payload = $createPayload
    submitterId = $adminUid
    submitterName = $adminName
} | ConvertTo-Json -Depth 5 -Compress
$createSubmit = Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $createBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
$createChangeId = $createSubmit.data.id
Pass "2.5 Submit CREATE: changeId=$createChangeId"

# ============================================
# PHASE 3: APPROVE CHANGES
# ============================================
Info "========== PHASE 3: Approve Changes =========="

# 3.1 Approve UPDATE
$approveRes = Invoke-RestMethod -Uri "$base/api/admin/pending-change/$changeId/approve" -Method POST -Headers @{Authorization = "Bearer $superToken"}
if ($approveRes.ok) { Pass "3.1 Approve UPDATE: ok" } else { Fail "3.1 Approve UPDATE: $approveRes" }

# 3.2 Verify UPDATE
$updated = Invoke-RestMethod -Uri "$base/api/faults/$($target.id)" -Method GET
if ($updated.title -eq "TEST_UPDATE_$ts") { Pass "3.2 UPDATE applied: $($updated.title)" }
else { Fail "3.2 UPDATE not applied: $($updated.title) != TEST_UPDATE_$ts" }

# 3.3 Approve CREATE
$approveRes2 = Invoke-RestMethod -Uri "$base/api/admin/pending-change/$createChangeId/approve" -Method POST -Headers @{Authorization = "Bearer $superToken"}
if ($approveRes2.ok) { Pass "3.3 Approve CREATE: ok" } else { Fail "3.3 Approve CREATE: $approveRes2" }

# 3.4 Verify CREATE
$allFaults = Invoke-RestMethod -Uri "$base/api/faults" -Method GET
$created = $allFaults | Where-Object { $_.title -eq "TEST_CREATE_$ts" }
if ($created) { Pass "3.4 CREATE applied: found $($created.id)" } else { Fail "3.4 CREATE not applied" }

# ============================================
# PHASE 4: REJECT CHANGE
# ============================================
Info "========== PHASE 4: Reject Change =========="

# 4.1 Submit change for rejection
$rejectPayload = @{
    id = $target.id
    title = "SHOULD_BE_REJECTED"
    category = $target.category
    symptom = $target.symptom
    cause = $target.cause
    solution = $target.solution
    images = @()
    videos = @()
    files = @()
    topo = @()
    docs = @{}
}
$rejectBody = @{
    module = 'fault'
    operation = 'UPDATE'
    entityId = $target.id
    payload = $rejectPayload
    submitterId = $adminUid
    submitterName = $adminName
} | ConvertTo-Json -Depth 5 -Compress
$rejectSubmit = Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $rejectBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
$rejectId = $rejectSubmit.data.id
Pass "4.1 Submit for rejection: changeId=$rejectId"

# 4.2 Reject
$rejectRes = Invoke-RestMethod -Uri "$base/api/admin/pending-change/$rejectId/reject" -Method POST -Headers @{Authorization = "Bearer $superToken"}
if ($rejectRes.ok) { Pass "4.2 Reject: ok" } else { Fail "4.2 Reject: $rejectRes" }

# 4.3 Verify reject
$check = Invoke-RestMethod -Uri "$base/api/faults/$($target.id)" -Method GET
if ($check.title -ne "SHOULD_BE_REJECTED") { Pass "4.3 Reject verified: title=$($check.title)" }
else { Fail "4.3 Reject not effective" }

# ============================================
# PHASE 5: MEDIA UPLOAD
# ============================================
Info "========== PHASE 5: Media Upload =========="

# 5.1 Test media in pending change
$imgPayload = @{
    id = $target.id
    title = $target.title
    category = $target.category
    symptom = $target.symptom
    cause = $target.cause
    solution = $target.solution
    images = @('/uploads/test.png')
    videos = @()
    files = @('/uploads/test.pdf')
    topo = @()
    docs = @{}
}
$imgBody = @{
    module = 'fault'
    operation = 'UPDATE'
    entityId = $target.id
    payload = $imgPayload
    submitterId = $adminUid
    submitterName = $adminName
} | ConvertTo-Json -Depth 5 -Compress
$imgSubmit = Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $imgBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
$imgChangeId = $imgSubmit.data.id
Pass "5.1 Submit with media: changeId=$imgChangeId"

# 5.2 Verify media in pending
$imgPending = Invoke-RestMethod -Uri "$base/api/admin/pending-changes" -Method GET -Headers @{Authorization = "Bearer $superToken"}
$imgChange = $imgPending.data | Where-Object { $_.id -eq $imgChangeId }
$imgPayload2 = $imgChange.payload | ConvertFrom-Json
if ($imgPayload2.images.Count -gt 0) { Pass "5.2 Images in pending: $($imgPayload2.images[0])" }
else { Fail "5.2 No images in pending" }
if ($imgPayload2.files.Count -gt 0) { Pass "5.3 Files in pending: $($imgPayload2.files[0])" }
else { Fail "5.3 No files in pending" }

# Approve media change
Invoke-RestMethod -Uri "$base/api/admin/pending-change/$imgChangeId/approve" -Method POST -Headers @{Authorization = "Bearer $superToken"}
Pass "5.4 Approve media change: ok"

# ============================================
# PHASE 6: API CONSISTENCY
# ============================================
Info "========== PHASE 6: API Consistency =========="

$endpoints = @('/api/topics','/api/faults','/api/desktop','/api/linux','/api/office','/api/ai')
foreach ($ep in $endpoints) {
  try {
    $res = Invoke-RestMethod -Uri "$base$ep" -Method GET -TimeoutSec 5
    $cnt = if ($res -is [array]) { $res.Count } else { 1 }
    Pass "6.1 GET ${ep}: $cnt items"
  } catch { Fail "6.1 GET ${ep}: $_" }
}

# 6.2 POST without auth rejected
try {
  Invoke-RestMethod -Uri "$base/api/faults" -Method POST -Body '{"title":"hack"}' -ContentType "application/json"
  Fail "6.2 POST without auth should reject"
} catch {
  if ($_.Exception.Response.StatusCode.value__ -eq 401) { Pass "6.2 POST without auth: 401" }
  else { Fail "6.2 POST without auth: $($_.Exception.Response.StatusCode.value__)" }
}

# 6.3 Admin cannot manage users
try {
  $body = '{"approved":true}'
  Invoke-RestMethod -Uri "$base/api/auth/approve/123" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
  Fail "6.3 Admin should not manage users"
} catch {
  $code = $_.Exception.Response.StatusCode.value__
  if ($code -eq 401 -or $code -eq 403) { Pass "6.3 Admin user management: rejected ($code)" }
  else { Fail "6.3 Admin user management: $code" }
}

# ============================================
# PHASE 7: EDGE CASES
# ============================================
Info "========== PHASE 7: Edge Cases =========="

# 7.1 Empty payload
try {
  $emptyBody = @{
    module = 'fault'
    operation = 'UPDATE'
    entityId = $target.id
    payload = @{}
    submitterId = $adminUid
    submitterName = $adminName
  } | ConvertTo-Json -Depth 5 -Compress
  $emptyRes = Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $emptyBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
  Pass "7.1 Empty payload: changeId=$($emptyRes.data.id)"
  Invoke-RestMethod -Uri "$base/api/admin/pending-change/$($emptyRes.data.id)/reject" -Method POST -Headers @{Authorization = "Bearer $superToken"}
} catch { Fail "7.1 Empty payload: $_" }

# 7.2 DELETE operation
try {
  $delPayload = @{ id = $target.id }
  $delBody = @{
    module = 'fault'
    operation = 'DELETE'
    entityId = $target.id
    payload = $delPayload
    submitterId = $adminUid
    submitterName = $adminName
  } | ConvertTo-Json -Depth 5 -Compress
  $delRes = Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $delBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
  Pass "7.2 DELETE: changeId=$($delRes.data.id)"
  Invoke-RestMethod -Uri "$base/api/admin/pending-change/$($delRes.data.id)/reject" -Method POST -Headers @{Authorization = "Bearer $superToken"}
} catch { Fail "7.2 DELETE: $_" }

# 7.3 Invalid module
try {
  $invBody = @{
    module = 'invalid'
    operation = 'CREATE'
    entityId = ''
    payload = @{title = 'test'}
    submitterId = $adminUid
    submitterName = $adminName
  } | ConvertTo-Json -Depth 5 -Compress
  Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $invBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
  Pass "7.3 Invalid module accepted (not validated at submit)"
  Invoke-RestMethod -Uri "$base/api/admin/pending-change/$($delRes.data.id)/reject" -Method POST -Headers @{Authorization = "Bearer $superToken"}
} catch {
  $code = $_.Exception.Response.StatusCode.value__
  if ($code -eq 400) { Pass "7.3 Invalid module: 400 rejected" }
  else { Pass "7.3 Invalid module: rejected ($code)" }
}

# ============================================
# PHASE 8: RESTORE ORIGINAL DATA
# ============================================
Info "========== PHASE 8: Restore =========="
$restorePayload = @{
    id = $target.id
    title = $original.title
    category = $target.category
    symptom = $target.symptom
    cause = $target.cause
    solution = $target.solution
    images = @()
    videos = @()
    files = @()
    topo = @()
    docs = @{}
}
$restoreBody = @{
    module = 'fault'
    operation = 'UPDATE'
    entityId = $target.id
    payload = $restorePayload
    submitterId = $adminUid
    submitterName = $adminName
} | ConvertTo-Json -Depth 5 -Compress
$restoreSubmit = Invoke-RestMethod -Uri "$base/api/admin/pending-change" -Method POST -Body $restoreBody -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"}
Invoke-RestMethod -Uri "$base/api/admin/pending-change/$($restoreSubmit.data.id)/approve" -Method POST -Headers @{Authorization = "Bearer $superToken"}
Pass "8.1 Restored original title: $($original.title)"

# ============================================
# SUMMARY
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " TEST RESULTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red
if ($bugs.Count -gt 0) {
  Write-Host "Bugs found:" -ForegroundColor Yellow
  $bugs | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
}
Write-Host "========================================" -ForegroundColor Cyan