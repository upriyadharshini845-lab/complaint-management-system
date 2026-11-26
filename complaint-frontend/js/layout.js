const Layout={
  menus:{
    USER:[['dashboard','speedometer2','Dashboard'],['submit-complaint','plus-circle','New Complaint'],['my-complaints','list-check','My Complaints'],['track','geo-alt','Track Complaint'],['notifications','bell','Notifications'],['feedback','star','Feedback'],['profile','person-circle','Profile']],
    ADMIN:[['dashboard','speedometer2','Dashboard'],['complaints','inbox','All Complaints'],['users','people','Users'],['departments','building','Departments'],['analytics','bar-chart-line','Analytics'],['escalations','exclamation-triangle','Escalations']],
    STAFF:[['dashboard','speedometer2','Dashboard'],['assigned','clipboard-check','Assigned Complaints'],['history','clock-history','History']]},
  async mount(role,title){
    const u=Auth.require(role);if(!u)return null;
    const page=location.pathname.split('/').pop()||'dashboard.html';
    const act=page==='complaint-detail.html'?(role==='ADMIN'?'complaints.html':'assigned.html'):page;
    document.getElementById('shell').innerHTML=`
    <div class="d-flex">
      <aside class="sidebar offcanvas-lg offcanvas-start" id="sb" tabindex="-1">
        <div class="offcanvas-header"><span class="brand"><i class="bi bi-headset me-2"></i>CMRS</span><button class="btn-close btn-close-white" data-bs-dismiss="offcanvas" data-bs-target="#sb"></button></div>
        <div class="offcanvas-body flex-column align-items-stretch p-3">
          <div class="brand mb-4 d-none d-lg-block"><i class="bi bi-headset me-2"></i>CMRS <small class="d-block fw-normal text-secondary fs-6">${role}</small></div>
          <nav class="nav flex-column">${Layout.menus[role].map(([f,i,l])=>`<a class="nav-link ${act===f+'.html'?'active':''}" href="${f}.html"><i class="bi bi-${i}"></i>${l}</a>`).join('')}</nav>
          <a class="nav-link mt-auto" href="#" id="lo" style="color:#fca5a5"><i class="bi bi-box-arrow-left"></i>Logout</a>
        </div>
      </aside>
      <div class="main flex-grow-1">
        <header class="topbar d-flex align-items-center gap-2 px-3 py-2">
          <button class="btn btn-light d-lg-none" data-bs-toggle="offcanvas" data-bs-target="#sb"><i class="bi bi-list fs-5"></i></button>
          <h5 class="mb-0 me-auto">${title}</h5>
          <div class="dropdown"><button class="btn btn-light position-relative" data-bs-toggle="dropdown" data-bs-auto-close="outside"><i class="bi bi-bell"></i><span id="nb" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none"></span></button>
            <div class="dropdown-menu dropdown-menu-end p-0" style="width:320px;max-width:90vw"><div class="p-2 fw-semibold border-bottom">Notifications</div><div id="nl" style="max-height:300px;overflow:auto"></div></div></div>
          <div class="dropdown"><button class="btn btn-light" data-bs-toggle="dropdown"><i class="bi bi-person-circle me-1"></i><span class="d-none d-sm-inline">${U.esc(u.name)}</span></button>
            <ul class="dropdown-menu dropdown-menu-end">${role==='USER'?'<li><a class="dropdown-item" href="profile.html">Profile</a></li>':''}<li><a class="dropdown-item" href="#" id="lo2">Logout</a></li></ul></div>
        </header>
        <main id="content" class="p-3 p-md-4"></main>
      </div>
    </div>`;
    document.getElementById('lo').onclick=document.getElementById('lo2').onclick=e=>{e.preventDefault();Auth.logout()};
    API.notifications().then(ns=>{const un=ns.filter(n=>!n.read).length,b=document.getElementById('nb');
      if(un){b.textContent=un;b.classList.remove('d-none')}
      document.getElementById('nl').innerHTML=ns.slice(0,6).map(n=>`<div class="px-3 py-2 border-bottom small ${n.read?'':'bg-light fw-semibold'}">${U.esc(n.message)}<div class="text-muted fw-normal">${U.fmt(n.createdAt)}</div></div>`).join('')||'<div class="p-3 text-muted small">No notifications</div>'}).catch(()=>{});
    return document.getElementById('content');
  }
};
