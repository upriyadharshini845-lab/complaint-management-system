const $=(s,r=document)=>r.querySelector(s);
const OPT=(a,sel)=>a.map(x=>`<option ${x===sel?'selected':''}>${U.esc(x)}</option>`).join('');
const Pages={
  /* ---------- shared helpers ---------- */
  stat:(label,val,icon,color)=>`<div class="col-6 col-xl-3"><div class="card stat-card h-100"><div class="card-body d-flex align-items-center gap-3"><div class="icon bg-${color}-subtle text-${color}"><i class="bi bi-${icon}"></i></div><div><div class="fs-4 fw-bold">${val}</div><div class="text-muted small">${label}</div></div></div></div></div>`,
  card:(title,body,extra='')=>`<div class="card mb-3 ${extra}"><div class="card-header bg-white fw-semibold">${title}</div><div class="card-body">${body}</div></div>`,
  cols:(link,admin)=>[
    {h:'ID',f:r=>`<a class="fw-semibold" href="${link}${r.trackingId}">${r.trackingId}</a>`},
    {h:'Title',f:r=>U.esc(r.title)},{h:'Category',f:r=>U.esc(r.category)},
    ...(admin?[{h:'Department',f:r=>U.esc(r.department||'-')}]:[]),
    {h:'Priority',f:r=>U.prioBadge(r.priority)},
    {h:'Status',f:r=>U.statusBadge(r.status)+(U.isOverdue(r)?' <span class="badge bg-danger">OVERDUE</span>':'')},
    {h:'Date',f:r=>U.fmtDate(r.createdAt)}],
  filters:()=>[{key:'status',label:'Status',opts:CONFIG.STATUSES},{key:'priority',label:'Priority',opts:CONFIG.PRIORITIES},{key:'category',label:'Category',opts:CONFIG.CATEGORIES}],
  counts(cs){return{total:cs.length,pending:cs.filter(c=>['SUBMITTED','UNDER_REVIEW','ASSIGNED'].includes(c.status)).length,prog:cs.filter(c=>c.status==='IN_PROGRESS').length,done:cs.filter(c=>['RESOLVED','CLOSED'].includes(c.status)).length}},
  detailView(c){
    return `<div class="card mb-3"><div class="card-body">
      <div class="d-flex flex-wrap justify-content-between gap-2"><div><h5 class="mb-1">${U.esc(c.title)}</h5><div class="text-muted small">${c.trackingId}</div></div><div>${U.prioBadge(c.priority)} ${U.statusBadge(c.status)} ${U.isOverdue(c)?'<span class="badge bg-danger">OVERDUE</span>':''}</div></div>
      ${U.stepper(c.status)}
      <div class="row g-2 small mb-3"><div class="col-sm-6"><b>Category:</b> ${U.esc(c.category)}</div><div class="col-sm-6"><b>Location:</b> ${U.esc(c.location||'-')}</div>
      <div class="col-sm-6"><b>Department:</b> ${U.esc(c.department||'Not assigned')}</div><div class="col-sm-6"><b>Staff:</b> ${U.esc(c.staffName||'Not assigned')}</div>
      <div class="col-sm-6"><b>Submitted:</b> ${U.fmt(c.createdAt)}</div><div class="col-sm-6"><b>Deadline:</b> ${U.fmt(U.deadline(c))}</div><div class="col-sm-6"><b>Submitted by:</b> ${U.esc(c.userName||'-')}</div></div>
      <p>${U.esc(c.description)}</p>${c.imageData?`<img src="${c.imageData}" class="preview mb-2" alt="attachment">`:''}
      ${c.resolutionNotes?`<div class="alert alert-success py-2"><b>Resolution notes:</b> ${U.esc(c.resolutionNotes)}</div>`:''}</div></div>
      ${Pages.card('Status History',U.timeline(c.history))}`;
  },
  authShell(title,sub,body){document.getElementById('shell').innerHTML=`<div class="auth-wrap"><div class="auth-card card shadow-lg p-4"><div class="text-center mb-4"><div class="logo-circle mx-auto mb-2"><i class="bi bi-headset"></i></div><h4 class="fw-bold mb-0">${title}</h4><small class="text-muted">${sub}</small></div>${body}</div></div>`},
  field:(label,name,type='text',extra='',fb='This field is required')=>`<div class="mb-3"><label class="form-label">${label}</label><input type="${type}" name="${name}" class="form-control" ${extra}><div class="invalid-feedback">${fb}</div></div>`,
  async guard(role,title,fn){const el=await Layout.mount(role,title);if(!el)return;el.innerHTML=U.spinner();
    try{await fn(el)}catch(e){el.innerHTML=`<div class="alert alert-danger">${U.esc(e.message)}</div>`}},

  /* ---------- auth pages ---------- */
  login(){
    Pages.authShell('Welcome back','Smart Complaint Management System',`<form id="f" novalidate>${Pages.field('Email','email','email','required','Enter a valid email')}${Pages.field('Password','password','password','required')}
      <div class="text-end mb-3"><a href="forgot-password.html" class="small">Forgot password?</a></div><button class="btn btn-primary w-100">Login</button></form>
      <p class="text-center mt-3 mb-0 small">New user? <a href="register.html">Register</a></p>
      ${CONFIG.USE_MOCK?'<div class="alert alert-info small mt-3 mb-0"><b>Demo logins</b> (mock mode)<br>User: user@cms.com / User@123<br>Admin: admin@cms.com / Admin@123<br>Staff: staff@cms.com / Staff@123</div>':''}`);
    $('#f').onsubmit=async e=>{e.preventDefault();const f=e.target;f.classList.add('was-validated');if(!f.checkValidity())return;
      try{const r=await API.login(f.email.value.trim(),f.password.value);Auth.save(r);location.href=Auth.home(r.user.role)}catch(x){U.toast(x.message,'danger')}};
  },
  register(){
    Pages.authShell('Create account','Register to submit and track complaints',`<form id="f" novalidate>${Pages.field('Full name','name','text','required minlength=3')}${Pages.field('Email','email','email','required','Enter a valid email')}
      ${Pages.field('Phone','phone','tel','pattern="[0-9]{10}"','Enter 10 digit number')}${Pages.field('Password','password','password','required minlength=8','Minimum 8 characters')}${Pages.field('Confirm password','confirm','password','required','Passwords must match')}
      <button class="btn btn-primary w-100">Register</button></form><p class="text-center mt-3 mb-0 small">Already registered? <a href="login.html">Login</a></p>`);
    $('#f').onsubmit=async e=>{e.preventDefault();const f=e.target;f.confirm.setCustomValidity(f.confirm.value===f.password.value?'':'x');f.classList.add('was-validated');if(!f.checkValidity())return;
      try{await API.register({name:f.name.value.trim(),email:f.email.value.trim(),phone:f.phone.value,password:f.password.value});U.toast('Registered! Please login.');setTimeout(()=>location.href='login.html',1200)}catch(x){U.toast(x.message,'danger')}};
  },
  forgot(){
    Pages.authShell('Forgot password','Enter your email to receive a reset link',`<form id="f" novalidate>${Pages.field('Email','email','email','required','Enter a valid email')}<button class="btn btn-primary w-100">Send reset link</button></form><p class="text-center mt-3 mb-0 small"><a href="login.html">Back to login</a></p>`);
    $('#f').onsubmit=async e=>{e.preventDefault();const f=e.target;f.classList.add('was-validated');if(!f.checkValidity())return;try{const r=await API.forgot(f.email.value);U.toast(r.message)}catch(x){U.toast(x.message,'danger')}};
  },

  resetPassword(){
    const token=U.qs('token');
    if(!token){Pages.authShell('Reset password','Link problem',`<div class="alert alert-danger">This reset link is invalid. Please request a new one.</div><a href="forgot-password.html" class="btn btn-primary w-100">Request new link</a>`);return}
    Pages.authShell('Reset password','Choose a new password',`<form id="f" novalidate>${Pages.field('New password','password','password','required minlength=8','Minimum 8 characters')}${Pages.field('Confirm password','confirm','password','required','Passwords must match')}<button class="btn btn-primary w-100">Update password</button></form><p class="text-center mt-3 mb-0 small"><a href="login.html">Back to login</a></p>`);
    $('#f').onsubmit=async e=>{e.preventDefault();const f=e.target;f.confirm.setCustomValidity(f.confirm.value===f.password.value?'':'x');f.classList.add('was-validated');if(!f.checkValidity())return;
      try{await API.resetPassword(token,f.password.value);U.toast('Password updated! Please login.');setTimeout(()=>location.href='login.html',1500)}catch(x){U.toast(x.message,'danger')}};
  },

  /* ---------- USER ---------- */
  userDashboard:()=>Pages.guard('USER','Dashboard',async el=>{
    const [cs,ns]=await Promise.all([API.complaints(),API.notifications()]),c=Pages.counts(cs);
    el.innerHTML=`<div class="row g-3 mb-3">${Pages.stat('Total Complaints',c.total,'collection','primary')}${Pages.stat('Pending',c.pending,'hourglass-split','secondary')}${Pages.stat('In Progress',c.prog,'gear','warning')}${Pages.stat('Resolved',c.done,'check-circle','success')}</div>
    <div class="row g-3"><div class="col-lg-8">${Pages.card('Recent Complaints <a class="float-end small" href="my-complaints.html">View all</a>','<div id="t"></div>')}</div>
    <div class="col-lg-4">${Pages.card('Status Overview','<canvas id="ch"></canvas>')}${Pages.card('Notifications',ns.slice(0,4).map(n=>`<div class="small border-bottom py-1">${U.esc(n.message)}</div>`).join('')||'<span class="text-muted small">No notifications</span>')}</div></div>`;
    U.table($('#t'),{rows:cs.slice(0,5),cols:Pages.cols('track.html?id='),bar:false});
    U.chart('ch','doughnut',['Pending','In Progress','Resolved'],[c.pending,c.prog,c.done],'Complaints',['#6b7280','#f59e0b','#10b981']);
  }),
  submitComplaint:()=>Pages.guard('USER','New Complaint',async el=>{
    const depts=(await API.departments()).map(d=>d.name);let img='';
    el.innerHTML=`<div id="box" class="row justify-content-center"><div class="col-lg-8">${Pages.card('Submit a Complaint',`<form id="f" novalidate class="row g-3">
      <div class="col-12"><label class="form-label">Title</label><input name="title" class="form-control" required minlength="5" maxlength="120"><div class="invalid-feedback">Min 5 characters</div></div>
      <div class="col-md-6"><label class="form-label">Category</label><select name="category" class="form-select" required><option value="">Select</option>${OPT(CONFIG.CATEGORIES)}</select><div class="invalid-feedback">Select a category</div></div>
      <div class="col-md-6"><label class="form-label">Urgency</label><select name="urgency" class="form-select">${OPT(['Low','Medium','High','Critical'],'Medium')}</select></div>
      <div class="col-md-6"><label class="form-label">Department</label><select name="department" class="form-select"><option value="">Not sure</option>${OPT(depts)}</select></div>
      <div class="col-md-6"><label class="form-label">Location</label><input name="location" class="form-control" required><div class="invalid-feedback">Enter location</div></div>
      <div class="col-12"><label class="form-label">Description</label><textarea name="description" rows="4" class="form-control" required minlength="20"></textarea><div class="invalid-feedback">Min 20 characters</div></div>
      <div class="col-12"><label class="form-label">Supporting image (optional, max 2MB)</label><input type="file" id="img" accept="image/*" class="form-control"><img id="pv" class="preview mt-2 d-none"></div>
      <div class="col-12"><button class="btn btn-primary">Submit Complaint</button></div></form>`)}</div></div>`;
    $('#img').onchange=e=>{const f=e.target.files[0];if(!f)return;if(f.size>2097152){U.toast('Image must be under 2MB','danger');e.target.value='';return}
      const r=new FileReader();r.onload=()=>{img=r.result;$('#pv').src=img;$('#pv').classList.remove('d-none')};r.readAsDataURL(f)};
    $('#f').onsubmit=async e=>{e.preventDefault();const f=e.target;f.classList.add('was-validated');if(!f.checkValidity())return;
      try{const c=await API.createComplaint({title:f.title.value.trim(),category:f.category.value,urgency:f.urgency.value,department:f.department.value,location:f.location.value.trim(),description:f.description.value.trim(),imageData:img});
        $('#box').innerHTML=`<div class="col-lg-6"><div class="card text-center p-4"><i class="bi bi-check-circle-fill text-success display-3"></i><h4 class="mt-2">Complaint Submitted</h4><p class="mb-1">Your tracking ID</p><h3 class="text-primary">${c.trackingId}</h3><p>Priority: ${U.prioBadge(c.priority)}</p><div><a class="btn btn-primary" href="track.html?id=${c.trackingId}">Track Now</a> <a class="btn btn-outline-secondary" href="submit-complaint.html">Submit Another</a></div></div></div>`}
      catch(x){U.toast(x.message,'danger')}};
  }),
  myComplaints:()=>Pages.guard('USER','My Complaints',async el=>{
    el.innerHTML=Pages.card('All My Complaints','<div id="t"></div>');
    U.serverTable($('#t'),{load:API.complaintsPage,cols:Pages.cols('track.html?id='),filters:Pages.filters()})}),
  track:()=>Pages.guard('USER','Track Complaint',async el=>{
    el.innerHTML=`<div class="card mb-3"><div class="card-body"><form id="f" class="input-group"><input class="form-control" id="id" placeholder="Enter tracking ID e.g. CMP-2026-1001" required><button class="btn btn-primary"><i class="bi bi-search"></i> Track</button></form></div></div><div id="res"></div>`;
    const go=async id=>{$('#res').innerHTML=U.spinner();try{const c=await API.complaint(id);
      $('#res').innerHTML=Pages.detailView(c)+(['RESOLVED','CLOSED'].includes(c.status)?'<a class="btn btn-warning" href="feedback.html">Give Feedback</a>':'')}catch(x){$('#res').innerHTML=`<div class="alert alert-warning">${U.esc(x.message)}</div>`}};
    $('#f').onsubmit=e=>{e.preventDefault();go($('#id').value)};
    const q=U.qs('id');if(q){$('#id').value=q;go(q)}else $('#res').innerHTML='';
  }),
  notifications:()=>Pages.guard('USER','Notifications',async el=>{const ns=await API.notifications();
    el.innerHTML=Pages.card('All Notifications <button class="btn btn-sm btn-outline-primary float-end" id="ra">Mark all read</button>',ns.length?`<ul class="list-group list-group-flush">${ns.map(n=>`<li class="list-group-item ${n.read?'':'fw-semibold bg-light'}">${U.esc(n.message)}<div class="small text-muted fw-normal">${U.fmt(n.createdAt)}</div></li>`).join('')}</ul>`:U.empty('No notifications','bell-slash'));
    $('#ra').onclick=async()=>{await API.readAll();location.reload()}}),
  feedback:()=>Pages.guard('USER','Feedback',async el=>{
    const [cs,fb]=await Promise.all([API.complaints(),API.feedbackList()]),done=new Set(fb.map(f=>f.trackingId)),elig=cs.filter(c=>['RESOLVED','CLOSED'].includes(c.status)&&!done.has(c.trackingId)),avg=fb.length?(fb.reduce((a,f)=>a+f.rating,0)/fb.length).toFixed(1):'-';let rating=0;
    const stars=n=>'<i class="bi bi-star-fill text-warning"></i>'.repeat(n)+'<i class="bi bi-star text-muted"></i>'.repeat(5-n);
    el.innerHTML=`<div class="row g-3"><div class="col-lg-5">${Pages.card('Rate a Resolved Complaint',elig.length?`<form id="f"><select id="tid" class="form-select mb-3">${elig.map(c=>`<option value="${c.trackingId}">${c.trackingId} - ${U.esc(c.title)}</option>`).join('')}</select>
      <div class="star-rate mb-3">${[1,2,3,4,5].map(n=>`<i class="bi bi-star" data-n="${n}"></i>`).join('')}</div><textarea id="cm" rows="3" class="form-control mb-3" placeholder="Your feedback"></textarea><button class="btn btn-primary">Submit Feedback</button></form>`:U.empty('No resolved complaints awaiting feedback','star'))}</div>
      <div class="col-lg-7">${Pages.card(`Feedback History <span class="badge bg-warning text-dark float-end">Average: ${avg}/5</span>`,fb.length?fb.map(f=>`<div class="border-bottom py-2"><b>${f.trackingId}</b> ${stars(f.rating)}<div class="small">${U.esc(f.comment||'')}</div><small class="text-muted">${U.fmtDate(f.createdAt)}</small></div>`).join(''):U.empty('No feedback yet'))}</div></div>`;
    if(elig.length){const st=()=>document.querySelectorAll('.star-rate i').forEach(i=>{i.className=`bi bi-star${+i.dataset.n<=rating?'-fill on':''}`});
      document.querySelectorAll('.star-rate i').forEach(i=>i.onclick=()=>{rating=+i.dataset.n;st()});
      $('#f').onsubmit=async e=>{e.preventDefault();if(!rating)return U.toast('Please select a rating','danger');await API.addFeedback({trackingId:$('#tid').value,rating,comment:$('#cm').value});U.toast('Thank you for your feedback!');setTimeout(()=>location.reload(),800)}}
  }),
  profile:()=>Pages.guard('USER','Profile',async el=>{const u=Auth.user();
    el.innerHTML=`<div class="row"><div class="col-lg-6">${Pages.card('My Profile',`<form id="f" novalidate><div class="mb-3"><label class="form-label">Email</label><input class="form-control" value="${U.esc(u.email)}" disabled></div>
      ${Pages.field('Name','name','text','required minlength=3')}${Pages.field('Phone','phone','tel','pattern="[0-9]{10}"','10 digits')}${Pages.field('New password (optional)','password','password','minlength=8','Min 8 characters')}<button class="btn btn-primary">Save</button></form>`)}</div></div>`;
    const f=$('#f');f.name.value=u.name;f.phone.value=u.phone||'';
    f.onsubmit=async e=>{e.preventDefault();f.classList.add('was-validated');if(!f.checkValidity())return;try{const r=await API.updateProfile({name:f.name.value.trim(),phone:f.phone.value,password:f.password.value||undefined});
      localStorage.setItem('cmrs_user',JSON.stringify({...u,...r,password:undefined}));U.toast('Profile updated')}catch(x){U.toast(x.message,'danger')}}}),

  /* ---------- ADMIN ---------- */
  adminDashboard:()=>Pages.guard('ADMIN','Admin Dashboard',async el=>{
    const [cs,an]=await Promise.all([API.complaints(),API.analytics()]),od=cs.filter(U.isOverdue),c=Pages.counts(cs);
    el.innerHTML=`<div class="row g-3 mb-3">${Pages.stat('Total Complaints',c.total,'collection','primary')}${Pages.stat('Pending',an.pending,'hourglass-split','warning')}${Pages.stat('Overdue',od.length,'exclamation-triangle','danger')}${Pages.stat('Avg Resolution (hrs)',an.avgResolutionHours,'stopwatch','success')}</div>
    <div class="row g-3"><div class="col-lg-8">${Pages.card('Recent Complaints','<div id="t"></div>')}</div><div class="col-lg-4">${Pages.card('Overdue Alerts',od.map(x=>`<a class="d-block border-bottom py-2 small" href="complaint-detail.html?id=${x.trackingId}"><b>${x.trackingId}</b> ${U.prioBadge(x.priority)}<br>${U.esc(x.title)}</a>`).join('')||'<span class="text-muted small">No overdue complaints</span>')}</div></div>`;
    U.table($('#t'),{rows:cs.slice(0,6),cols:Pages.cols('complaint-detail.html?id=',true),bar:false})}),
  adminComplaints:()=>Pages.guard('ADMIN','All Complaints',async el=>{const ds=await API.departments();
    el.innerHTML=Pages.card('Complaints','<div id="t"></div>');
    U.serverTable($('#t'),{load:API.complaintsPage,cols:Pages.cols('complaint-detail.html?id=',true),
      filters:[...Pages.filters(),{key:'department',label:'Dept',opts:ds.map(d=>d.name)},{key:'overdue',label:'Overdue',opts:[{v:'true',l:'Overdue only'}]}],
      rowClass:r=>U.isOverdue(r)?'overdue-row':''})}),
  complaintDetail:role=>Pages.guard(role,'Complaint Details',async el=>{
    const id=U.qs('id'),c=await API.complaint(id),admin=role==='ADMIN';let ds=[],staff=[];
    if(admin){[ds,staff]=await Promise.all([API.departments(),API.users()]);staff=staff.filter(u=>u.role==='STAFF'&&u.active)}
    const draw=c=>{
      let ctl='';
      if(admin)ctl=`<form id="f"><label class="form-label">Department</label><select name="department" class="form-select mb-2"><option value="">Select</option>${OPT(ds.map(d=>d.name),c.department)}</select>
        <label class="form-label">Assign staff</label><select name="staffId" class="form-select mb-2"><option value="">Select</option>${staff.map(s=>`<option value="${s.id}" ${s.id==c.staffId?'selected':''}>${U.esc(s.name)} (${U.esc(s.department||'-')})</option>`).join('')}</select>
        <label class="form-label">Priority (override)</label><select name="priority" class="form-select mb-2">${OPT(CONFIG.PRIORITIES,c.priority)}</select>
        <label class="form-label">Status</label><select name="status" class="form-select mb-2">${OPT(CONFIG.STATUSES,c.status)}</select>
        <label class="form-label">Note</label><textarea name="note" class="form-control mb-3" rows="2"></textarea><button class="btn btn-primary w-100">Save Changes</button></form>`;
      else if(c.status==='ASSIGNED')ctl='<button class="btn btn-success w-100" id="acc">Accept Complaint</button>';
      else if(c.status==='IN_PROGRESS')ctl=`<label class="form-label">Progress update</label><textarea id="pn" class="form-control mb-2" rows="2"></textarea><button class="btn btn-outline-primary w-100 mb-3" id="up">Update Progress</button>
        <label class="form-label">Resolution notes</label><textarea id="rn" class="form-control mb-2" rows="3"></textarea><button class="btn btn-success w-100" id="rs">Mark as Resolved</button>`;
      else ctl='<div class="text-muted">No actions available for this status.</div>';
      el.innerHTML=`<div class="row g-3"><div class="col-lg-8">${Pages.detailView(c)}</div><div class="col-lg-4">${Pages.card('Actions',ctl)}</div></div>`;
      const run=async p=>{try{draw(await API.updateComplaint(id,p));U.toast('Updated successfully')}catch(x){U.toast(x.message,'danger')}};
      if(admin)$('#f').onsubmit=e=>{e.preventDefault();const f=e.target;run({department:f.department.value||undefined,staffId:f.staffId.value||undefined,priority:f.priority.value,status:f.status.value,note:f.note.value.trim()||undefined})};
      if($('#acc'))$('#acc').onclick=()=>run({status:'IN_PROGRESS',note:'Complaint accepted by staff'});
      if($('#up'))$('#up').onclick=()=>{const n=$('#pn').value.trim();if(!n)return U.toast('Enter a progress note','danger');run({note:n})};
      if($('#rs'))$('#rs').onclick=()=>{const n=$('#rn').value.trim();if(n.length<5)return U.toast('Enter resolution notes','danger');run({status:'RESOLVED',resolutionNotes:n,note:'Marked as resolved'})};
    };draw(c)}),
  adminUsers:()=>Pages.guard('ADMIN','Manage Users',async el=>{const [us,ds]=await Promise.all([API.users(),API.departments()]);
    el.innerHTML=Pages.card('Add User',`<form id="f" class="row g-2" novalidate><div class="col-md-3"><input name="name" class="form-control" placeholder="Name" required></div><div class="col-md-3"><input name="email" type="email" class="form-control" placeholder="Email" required></div>
      <div class="col-md-2"><input name="password" type="password" class="form-control" placeholder="Password" minlength="8" required></div><div class="col-md-2"><select name="role" class="form-select">${OPT(['USER','STAFF','ADMIN'])}</select></div>
      <div class="col-md-2"><select name="department" class="form-select"><option value="">Dept (staff)</option>${OPT(ds.map(d=>d.name))}</select></div><div class="col-12"><button class="btn btn-primary">Add User</button></div></form>`)+Pages.card('All Users','<div id="t"></div>');
    U.table($('#t'),{rows:us,filters:[{key:'role',label:'Role',opts:['USER','STAFF','ADMIN']}],cols:[{h:'Name',f:r=>U.esc(r.name)},{h:'Email',f:r=>U.esc(r.email)},{h:'Role',f:r=>`<span class="badge text-bg-secondary">${r.role}</span>`},{h:'Department',f:r=>U.esc(r.department||'-')},
      {h:'Status',f:r=>r.active?'<span class="badge bg-success">Active</span>':'<span class="badge bg-secondary">Disabled</span>'},{h:'',f:r=>r.id===Auth.user().id?'':`<button class="btn btn-sm btn-outline-secondary" data-t="${r.id}">${r.active?'Disable':'Enable'}</button>`}]});
    $('#t').addEventListener('click',async e=>{const b=e.target.closest('[data-t]');if(b){await API.toggleUser(+b.dataset.t);location.reload()}});
    $('#f').onsubmit=async e=>{e.preventDefault();const f=e.target;f.classList.add('was-validated');if(!f.checkValidity())return;
      try{await API.addUser({name:f.name.value.trim(),email:f.email.value.trim(),password:f.password.value,role:f.role.value,department:f.department.value||undefined});U.toast('User added');setTimeout(()=>location.reload(),700)}catch(x){U.toast(x.message,'danger')}}}),
  adminDepartments:()=>Pages.guard('ADMIN','Departments',async el=>{const ds=await API.departments();
    el.innerHTML=Pages.card('Add Department','<form id="f" class="input-group" style="max-width:420px"><input id="n" class="form-control" placeholder="Department name" required><button class="btn btn-primary">Add</button></form>')+Pages.card('Departments','<div id="t"></div>');
    U.table($('#t'),{rows:ds,bar:false,cols:[{h:'#',f:r=>r.id},{h:'Name',f:r=>U.esc(r.name)},{h:'',f:r=>`<button class="btn btn-sm btn-outline-danger" data-d="${r.id}">Delete</button>`}]});
    $('#t').addEventListener('click',async e=>{const b=e.target.closest('[data-d]');if(b&&confirm('Delete this department?')){await API.deleteDepartment(+b.dataset.d);location.reload()}});
    $('#f').onsubmit=async e=>{e.preventDefault();const n=$('#n').value.trim();if(!n)return;await API.addDepartment(n);location.reload()}}),
  adminAnalytics:()=>Pages.guard('ADMIN','Analytics & Reports',async el=>{const a=await API.analytics(),dn=Object.keys(a.byDepartment),ms=Object.keys(a.byMonth).sort();
    el.innerHTML=`<div class="row g-3 mb-3">${Pages.stat('Pending',a.pending,'hourglass-split','warning')}${Pages.stat('Resolved',a.resolved,'check-circle','success')}${Pages.stat('Avg Resolution (hrs)',a.avgResolutionHours,'stopwatch','primary')}${Pages.stat('Avg Satisfaction',a.avgRating+'/5','star','danger')}</div>
    <div class="row g-3"><div class="col-md-6">${Pages.card('Category-wise Complaints','<canvas id="c1"></canvas>')}</div><div class="col-md-6">${Pages.card('Monthly Complaints','<canvas id="c2"></canvas>')}</div>
    <div class="col-md-6">${Pages.card('Pending vs Resolved','<canvas id="c3"></canvas>')}</div><div class="col-md-6">${Pages.card('Department Performance (avg hrs to resolve)','<canvas id="c4"></canvas>')}</div>
    <div class="col-md-6">${Pages.card('User Satisfaction (ratings)','<canvas id="c5"></canvas>')}</div></div>`;
    U.chart('c1','pie',Object.keys(a.byCategory),Object.values(a.byCategory),'Complaints');U.chart('c2','bar',ms,ms.map(m=>a.byMonth[m]),'Complaints');
    U.chart('c3','doughnut',['Pending','Resolved'],[a.pending,a.resolved],'Count',['#f59e0b','#10b981']);U.chart('c4','bar',dn,dn.map(d=>a.byDepartment[d].avgHours),'Avg hours');
    U.chart('c5','bar',['1★','2★','3★','4★','5★'],a.ratingDist,'Responses',['#ef4444','#f97316','#eab308','#84cc16','#22c55e'])}),
  adminEscalations:()=>Pages.guard('ADMIN','Escalations',async el=>{const [cs,es]=await Promise.all([API.complaints(),API.escalations()]),od=cs.filter(U.isOverdue);
    el.innerHTML=Pages.card('Overdue Complaints <button class="btn btn-sm btn-danger float-end" id="run">Run escalation check</button>',od.length?'<div id="t1"></div>':U.empty('No overdue complaints','check2-circle'))+Pages.card('Escalation History','<div id="t2"></div>');
    if(od.length)U.table($('#t1'),{rows:od,bar:false,cols:[...Pages.cols('complaint-detail.html?id=',true).slice(0,5),{h:'Deadline',f:r=>U.fmt(U.deadline(r))}]});
    U.table($('#t2'),{rows:es,bar:false,cols:[{h:'ID',f:r=>`<a href="complaint-detail.html?id=${r.trackingId}">${r.trackingId}</a>`},{h:'Title',f:r=>U.esc(r.title)},{h:'Priority',f:r=>U.prioBadge(r.priority)},{h:'Reason',f:r=>U.esc(r.reason)},{h:'Escalated',f:r=>U.fmt(r.escalatedAt)}]});
    $('#run').onclick=async()=>{const r=await API.runEscalation();U.toast(`${r.escalated} complaint(s) escalated`);setTimeout(()=>location.reload(),900)}}),

  /* ---------- STAFF ---------- */
  staffDashboard:()=>Pages.guard('STAFF','Staff Dashboard',async el=>{const cs=await API.complaints(),new_=cs.filter(c=>c.status==='ASSIGNED');
    el.innerHTML=`<div class="row g-3 mb-3">${Pages.stat('Total Assigned',cs.length,'collection','primary')}${Pages.stat('Awaiting Acceptance',new_.length,'bell','warning')}${Pages.stat('In Progress',cs.filter(c=>c.status==='IN_PROGRESS').length,'gear','info')}${Pages.stat('Resolved',cs.filter(c=>['RESOLVED','CLOSED'].includes(c.status)).length,'check-circle','success')}</div>${Pages.card('New Assignments','<div id="t"></div>')}`;
    U.table($('#t'),{rows:new_,bar:false,cols:Pages.cols('complaint-detail.html?id=')})}),
  staffAssigned:()=>Pages.guard('STAFF','Assigned Complaints',async el=>{const cs=(await API.complaints()).filter(U.isOpen);
    el.innerHTML=Pages.card('Active Complaints','<div id="t"></div>');U.table($('#t'),{rows:cs,cols:Pages.cols('complaint-detail.html?id='),filters:Pages.filters().slice(0,2)})}),
  staffHistory:()=>Pages.guard('STAFF','Complaint History',async el=>{const cs=(await API.complaints()).filter(c=>!U.isOpen(c));
    el.innerHTML=Pages.card('Completed Complaints','<div id="t"></div>');U.table($('#t'),{rows:cs,cols:Pages.cols('complaint-detail.html?id='),filters:Pages.filters().slice(0,1)})})
};
