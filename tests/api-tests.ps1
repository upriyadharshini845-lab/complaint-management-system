# Smart Complaint Management System: API regression tests (55 checks)
# Start the backend first, then run from the cms folder:
#   powershell -ExecutionPolicy Bypass -File .\tests\api-tests.ps1
# Each run creates a few test records (a test user and some complaints) in the database.
param([string]$Base = "http://localhost:8080/api")
$base = $Base
$global:pass = 0; $global:fail = 0

function Api($method, $path, $token, $body) {
  $h = @{}; if ($token) { $h["Authorization"] = "Bearer $token" }
  $p = @{ Method = $method; Uri = "$base$path"; Headers = $h }
  if ($body) { $p["ContentType"] = "application/json"; $p["Body"] = ($body | ConvertTo-Json -Depth 5) }
  Invoke-RestMethod @p
}
function Code($method, $path, $token, $body) {
  try { Api $method $path $token $body | Out-Null; return 200 } catch { return [int]$_.Exception.Response.StatusCode }
}
function Safe($sb) { try { return [bool](& $sb) } catch { return $false } }
# Counts items safely. Windows PowerShell 5.1 returns a JSON array as ONE object, so @(...).Count would give 1.
function Cnt($x) { if ($null -eq $x) { return 0 }; return @($x | ForEach-Object { $_ }).Count }
function Check($name, $ok) {
  if ($ok) { Write-Host "PASS  $name" -ForegroundColor Green; $global:pass++ }
  else { Write-Host "FAIL  $name" -ForegroundColor Red; $global:fail++ }
}

# ---------- logins ----------
$admin = $null; try { $admin = Api POST "/auth/login" $null @{email="admin@cms.com";password="Admin@123"} } catch {}
$user  = $null; try { $user  = Api POST "/auth/login" $null @{email="user@cms.com";password="User@123"} } catch {}
$staff = $null; try { $staff = Api POST "/auth/login" $null @{email="staff2@cms.com";password="Staff@123"} } catch {}
if (-not ($admin -and $user -and $staff)) {
  Write-Host "Login failed. Is the backend running with the demo accounts (app.seed.demo-users=true)?" -ForegroundColor Red
  return
}
$rand = Get-Random -Minimum 1000 -Maximum 9999

# ---------- authentication and security ----------
Check "1. Server and database health" (Safe { (Api GET "/health" $null $null).status -eq "UP" })
Check "2. Admin login gives token and ADMIN role" ($admin.token -and $admin.user.role -eq "ADMIN")
Check "3. User and staff login give tokens" ($user.token -and $staff.token)
Check "4. Wrong password rejected (401)" ((Code POST "/auth/login" $null @{email="admin@cms.com";password="wrong"}) -eq 401)
Check "5. No token rejected (401)" ((Code GET "/auth/me" $null $null) -eq 401)
Check "6. /auth/me returns the logged in user" (Safe { (Api GET "/auth/me" $admin.token $null).email -eq "admin@cms.com" })
Check "7. Normal user cannot list users (403)" ((Code GET "/users" $user.token $null) -eq 403)
Check "8. Admin can list users" (Safe { (Cnt (Api GET "/users" $admin.token $null)) -ge 4 })
$reg = @{name="Test User";email="test$rand@cms.com";phone="9876543210";password="Test@1234"}
Check "9. New user can register" ((Code POST "/auth/register" $null $reg) -eq 200)
Check "10. Duplicate email rejected (409)" ((Code POST "/auth/register" $null $reg) -eq 409)
Check "11. Weak password rejected (400)" ((Code POST "/auth/register" $null @{name="Weak Pass";email="weak$rand@cms.com";phone="";password="123"}) -eq 400)

# ---------- complaints ----------
$cb = @{title="Fire in test lab $rand";category="Technical Issues";urgency="Low";department="";location="Test Block";description="There is fire near the electrical panel in the test lab."}
$c = $null; try { $c = Api POST "/complaints" $user.token $cb } catch {}
Check "12. Complaint submitted with tracking ID" ($c.trackingId -match '^CMP-\d{4}-\d+$')
Check "13. Smart priority: 'fire' gives Critical" ($c.priority -eq "Critical")
Check "14. Admin cannot submit complaint (403)" ((Code POST "/complaints" $admin.token $cb) -eq 403)
Check "15. User sees own complaint in list" (Safe { $l = Api GET "/complaints" $user.token $null; (Cnt ($l | Where-Object { $_.trackingId -eq $c.trackingId })) -eq 1 })
Check "16. Track shows status history" (Safe { (Api GET "/complaints/track/$($c.trackingId)" $user.token $null).history.Count -ge 1 })
Check "17. Staff cannot view unassigned complaint (403)" ((Code GET "/complaints/track/$($c.trackingId)" $staff.token $null) -eq 403)

# ---------- admin and staff actions ----------
$a = $null; try { $a = Api PATCH "/complaints/$($c.trackingId)" $admin.token @{department="IT Support";staffId=$staff.user.id} } catch {}
Check "18. Admin assigns staff, status becomes ASSIGNED" ($a.status -eq "ASSIGNED" -and $a.staffName -eq "Arun M")
Check "19. Staff sees the assigned complaint" (Safe { $l = Api GET "/complaints" $staff.token $null; (Cnt ($l | Where-Object { $_.trackingId -eq $c.trackingId })) -eq 1 })
Check "20. Staff cannot change priority (403)" ((Code PATCH "/complaints/$($c.trackingId)" $staff.token @{priority="Low"}) -eq 403)
$s1 = $null; try { $s1 = Api PATCH "/complaints/$($c.trackingId)" $staff.token @{status="IN_PROGRESS";note="Accepted"} } catch {}
Check "21. Staff accepts: IN_PROGRESS" ($s1.status -eq "IN_PROGRESS")
Check "22. Resolve without notes rejected (400)" ((Code PATCH "/complaints/$($c.trackingId)" $staff.token @{status="RESOLVED"}) -eq 400)
$s2 = $null; try { $s2 = Api PATCH "/complaints/$($c.trackingId)" $staff.token @{status="RESOLVED";resolutionNotes="Cable replaced and tested";note="Done"} } catch {}
Check "23. Staff resolves: RESOLVED" ($s2.status -eq "RESOLVED")
$t = $null; try { $t = Api GET "/complaints/track/$($c.trackingId)" $user.token $null } catch {}
Check "24. User sees RESOLVED with full history" ($t.status -eq "RESOLVED" -and $t.history.Count -ge 4)
Check "25. User has notifications" (Safe { (Cnt (Api GET "/notifications" $user.token $null)) -ge 1 })
Check "26. Departments list" (Safe { (Cnt (Api GET "/departments" $user.token $null)) -ge 4 })
$d = $null; try { $d = Api POST "/departments" $admin.token @{name="Test Dept $rand"} } catch {}
Check "27. Admin creates department" ($d.id)
Check "28. User cannot create department (403)" ((Code POST "/departments" $user.token @{name="Hack $rand"}) -eq 403)
Check "29. Admin deletes department" (Safe { Api DELETE "/departments/$($d.id)" $admin.token $null | Out-Null; $true })

# ---------- feedback, analytics, escalation ----------
$body = @{title="Projector issue $rand";category="Technical Issues";urgency="Medium";department="";location="Lab 1";description="The projector shows no display during classes."}
$f1 = Api POST "/complaints" $user.token $body
$f2 = Api POST "/complaints" $user.token $body
Api PATCH "/complaints/$($f1.trackingId)" $admin.token @{department="IT Support";staffId=$staff.user.id} | Out-Null
Api PATCH "/complaints/$($f1.trackingId)" $staff.token @{status="IN_PROGRESS"} | Out-Null
Api PATCH "/complaints/$($f1.trackingId)" $staff.token @{status="RESOLVED";resolutionNotes="Fixed the projector cable"} | Out-Null
Check "30. Feedback on unresolved complaint rejected (400)" ((Code POST "/feedback" $user.token @{trackingId=$f2.trackingId;rating=5;comment="x"}) -eq 400)
Check "31. Rating above 5 rejected (400)" ((Code POST "/feedback" $user.token @{trackingId=$f1.trackingId;rating=9;comment="x"}) -eq 400)
Check "32. Staff cannot give feedback (403)" ((Code POST "/feedback" $staff.token @{trackingId=$f1.trackingId;rating=5;comment="x"}) -eq 403)
Check "33. User gives feedback on resolved complaint" ((Code POST "/feedback" $user.token @{trackingId=$f1.trackingId;rating=4;comment="Good service"}) -eq 200)
Check "34. Duplicate feedback rejected (409)" ((Code POST "/feedback" $user.token @{trackingId=$f1.trackingId;rating=5;comment="again"}) -eq 409)
Check "35. User sees own feedback" (Safe { (Cnt (Api GET "/feedback" $user.token $null)) -ge 1 })
Check "36. Admin sees all feedback" (Safe { (Cnt (Api GET "/feedback" $admin.token $null)) -ge 1 })
$an = $null; try { $an = Api GET "/analytics" $admin.token $null } catch {}
Check "37. Analytics: totals and category data" ($an.byCategory -and $an.resolved -ge 1 -and $an.pending -ge 1)
Check "38. Analytics: avg rating and rating distribution" ($an.avgRating -gt 0 -and $an.ratingDist.Count -eq 5)
Check "39. User cannot open analytics (403)" ((Code GET "/analytics" $user.token $null) -eq 403)
Check "40. Admin runs escalation check" (Safe { (Api POST "/escalations/run" $admin.token $null).escalated -ge 0 })
Check "41. Admin reads escalation history" (Safe { Api GET "/escalations" $admin.token $null | Out-Null; $true })
Check "42. User cannot run escalation (403)" ((Code POST "/escalations/run" $user.token $null) -eq 403)

# ---------- server-side pagination, sorting, filters ----------
$all = Api GET "/complaints" $admin.token $null
$n = Cnt $all
$p = Api GET "/complaints/search?page=0&size=2" $admin.token $null
Check "43. Page has at most 2 items" (@($p.content).Count -le 2 -and $p.size -eq 2)
Check "44. Total count matches full list" ($p.totalElements -eq $n)
Check "45. Total pages is correct" ($p.totalPages -eq [math]::Ceiling($n / 2))
$asc  = Api GET "/complaints/search?size=50&sort=createdAt&dir=asc" $admin.token $null
$desc = Api GET "/complaints/search?size=50&sort=createdAt&dir=desc" $admin.token $null
Check "46. Sort asc and desc give opposite order" ($n -lt 2 -or $asc.content[0].trackingId -ne $desc.content[0].trackingId)
$s = Api GET "/complaints/search?size=50&status=SUBMITTED" $admin.token $null
Check "47. Status filter works" (@($s.content | Where-Object { $_.status -ne "SUBMITTED" }).Count -eq 0)
$cr = Api GET "/complaints/search?size=50&priority=critical" $admin.token $null
Check "48. Priority filter works (any case)" (@($cr.content).Count -ge 1 -and @($cr.content | Where-Object { $_.priority -ne "Critical" }).Count -eq 0)
$id = $all[0].trackingId
$q = Api GET "/complaints/search?q=$id" $admin.token $null
Check "49. Search by tracking ID" (@($q.content).Count -eq 1 -and $q.content[0].trackingId -eq $id)
$big = Api GET "/complaints/search?size=1000" $admin.token $null
Check "50. Page size limited to 50" ($big.size -eq 50)
$far = Api GET "/complaints/search?page=999" $admin.token $null
Check "51. Page beyond the end gives empty list" (@($far.content).Count -eq 0)
$u = Api GET "/complaints/search?size=50" $user.token $null
Check "52. User sees only own complaints" (@($u.content).Count -ge 1 -and @($u.content | Where-Object { $_.userId -ne $user.user.id }).Count -eq 0)
$st = Api GET "/complaints/search?size=50" $staff.token $null
Check "53. Staff sees only assigned complaints" (@($st.content | Where-Object { $_.staffId -ne $staff.user.id }).Count -eq 0)
Check "54. Invalid status rejected (400)" ((Code GET "/complaints/search?status=WRONG" $admin.token $null) -eq 400)
Check "55. No token rejected (401)" ((Code GET "/complaints/search" $null $null) -eq 401)

Write-Host ""
Write-Host "Passed: $global:pass   Failed: $global:fail" -ForegroundColor Cyan
