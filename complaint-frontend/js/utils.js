const U={
  fmt:d=>d?new Date(d).toLocaleString('en-IN',{day:'2-digit',month:'short',year:'numeric',hour:'2-digit',minute:'2-digit'}):'-',
  fmtDate:d=>d?new Date(d).toLocaleDateString('en-IN',{day:'2-digit',month:'short',year:'numeric'}):'-',
  esc:s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])),
  qs:k=>new URLSearchParams(location.search).get(k),
  statusBadge:s=>`<span class="badge st-${s}">${s.replace('_',' ')}</span>`,
  prioBadge:p=>`<span class="badge pr-${p}">${p}</span>`,
  isOpen:c=>!['RESOLVED','CLOSED','REJECTED'].includes(c.status),
  deadline:c=>c.deadline?new Date(c.deadline):new Date(new Date(c.createdAt).getTime()+(CONFIG.SLA_HOURS[c.priority]||120)*36e5),
  isOverdue:c=>U.isOpen(c)&&U.deadline(c)<new Date(),
  toast(msg,type='success'){
    let box=document.getElementById('toasts');
    if(!box){box=document.createElement('div');box.id='toasts';box.className='toast-container position-fixed top-0 end-0 p-3';document.body.appendChild(box)}
    const t=document.createElement('div');t.className=`toast align-items-center text-bg-${type} border-0`;
    t.innerHTML=`<div class="d-flex"><div class="toast-body">${U.esc(msg)}</div><button class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>`;
    box.appendChild(t);const s=new bootstrap.Toast(t,{delay:3500});s.show();t.addEventListener('hidden.bs.toast',()=>t.remove());
  },
  spinner:()=>'<div class="text-center py-5"><div class="spinner-border text-primary"></div></div>',
  empty:(m,i='inbox')=>`<div class="text-center text-muted py-5"><i class="bi bi-${i} fs-1"></i><div>${m}</div></div>`,
  table(el,{rows,cols,filters=[],pageSize=8,bar=true,rowClass}){
    let page=1,q='',fv={};
    el.innerHTML=`${bar?`<div class="row g-2 mb-3"><div class="col-md-4"><input class="form-control" placeholder="Search..." data-q></div>${filters.map(f=>`<div class="col-6 col-md-2"><select class="form-select" data-f="${f.key}"><option value="">${f.label}: All</option>${f.opts.map(o=>`<option>${o}</option>`).join('')}</select></div>`).join('')}</div>`:''}
    <div class="table-responsive"><table class="table table-hover align-middle mb-2"><thead><tr>${cols.map(c=>`<th>${c.h}</th>`).join('')}</tr></thead><tbody></tbody></table></div>
    <div class="d-flex justify-content-between align-items-center"><small class="text-muted" data-info></small><ul class="pagination pagination-sm mb-0"></ul></div>`;
    const draw=()=>{
      const r=rows.filter(x=>(!q||JSON.stringify(x).toLowerCase().includes(q))&&Object.entries(fv).every(([k,v])=>!v||String(x[k])===v));
      const pages=Math.max(1,Math.ceil(r.length/pageSize));page=Math.min(page,pages);
      const s=r.slice((page-1)*pageSize,page*pageSize);
      el.querySelector('tbody').innerHTML=s.length?s.map(x=>`<tr class="${rowClass?rowClass(x):''}">${cols.map(c=>`<td>${c.f(x)}</td>`).join('')}</tr>`).join(''):`<tr><td colspan="${cols.length}" class="text-center text-muted py-4">No records found</td></tr>`;
      el.querySelector('[data-info]').textContent=`${r.length} record(s)`;
      el.querySelector('.pagination').innerHTML=pages>1?Array.from({length:pages},(_,i)=>`<li class="page-item ${i+1===page?'active':''}"><a class="page-link" href="#" data-p="${i+1}">${i+1}</a></li>`).join(''):'';
    };
    el.addEventListener('input',e=>{if(e.target.matches('[data-q]')){q=e.target.value.toLowerCase();page=1;draw()}});
    el.addEventListener('change',e=>{if(e.target.matches('[data-f]')){fv[e.target.dataset.f]=e.target.value;page=1;draw()}});
    el.addEventListener('click',e=>{const a=e.target.closest('[data-p]');if(a){e.preventDefault();page=+a.dataset.p;draw()}});
    draw();
  },
  /* Server-side table: every page, sort, filter and search is fetched from the backend. */
  serverTable(el,{load,cols,filters=[],pageSize=10,rowClass,sorts=[['createdAt,desc','Newest first'],['createdAt,asc','Oldest first'],['deadline,asc','Deadline soonest'],['title,asc','Title A-Z']]}){
    let page=0,q='',fv={},sort=sorts[0][0],timer;
    el.innerHTML=`<div class="row g-2 mb-3"><div class="col-md-4"><input class="form-control" placeholder="Search ID, title, location..." data-q></div>${filters.map(f=>`<div class="col-6 col-md-2"><select class="form-select" data-f="${f.key}"><option value="">${f.label}: All</option>${f.opts.map(o=>{const v=typeof o==='string'?o:o.v,l=typeof o==='string'?o:o.l;return `<option value="${v}">${l}</option>`}).join('')}</select></div>`).join('')}<div class="col-6 col-md-2"><select class="form-select" data-sort>${sorts.map(s=>`<option value="${s[0]}">${s[1]}</option>`).join('')}</select></div></div>
    <div class="table-responsive"><table class="table table-hover align-middle mb-2"><thead><tr>${cols.map(c=>`<th>${c.h}</th>`).join('')}</tr></thead><tbody></tbody></table></div>
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2"><small class="text-muted" data-info></small><ul class="pagination pagination-sm mb-0"></ul></div>`;
    const tbody=el.querySelector('tbody'),n=cols.length;
    const draw=async()=>{
      tbody.innerHTML=`<tr><td colspan="${n}" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary"></div></td></tr>`;
      try{
        const [sf,dir]=sort.split(',');
        const res=await load({page,size:pageSize,q,sort:sf,dir,...fv});
        const rows=res.content||[];
        tbody.innerHTML=rows.length?rows.map(x=>`<tr class="${rowClass?rowClass(x):''}">${cols.map(c=>`<td>${c.f(x)}</td>`).join('')}</tr>`).join(''):`<tr><td colspan="${n}" class="text-center text-muted py-4">No records found</td></tr>`;
        const total=res.totalElements,from=total?res.page*res.size+1:0,to=Math.min((res.page+1)*res.size,total);
        el.querySelector('[data-info]').textContent=total?`Showing ${from}-${to} of ${total}`:'0 record(s)';
        const tp=res.totalPages,start=Math.max(0,Math.min(res.page-2,tp-5)),end=Math.min(tp,start+5);let h='';
        if(tp>1){
          h+=`<li class="page-item ${res.page===0?'disabled':''}"><a class="page-link" href="#" data-p="${res.page-1}">&laquo;</a></li>`;
          for(let i=start;i<end;i++)h+=`<li class="page-item ${i===res.page?'active':''}"><a class="page-link" href="#" data-p="${i}">${i+1}</a></li>`;
          h+=`<li class="page-item ${res.page>=tp-1?'disabled':''}"><a class="page-link" href="#" data-p="${res.page+1}">&raquo;</a></li>`;
        }
        el.querySelector('.pagination').innerHTML=h;
      }catch(e){tbody.innerHTML=`<tr><td colspan="${n}" class="text-center text-danger py-4">${U.esc(e.message)}</td></tr>`}
    };
    el.addEventListener('input',e=>{if(e.target.matches('[data-q]')){q=e.target.value.trim();page=0;clearTimeout(timer);timer=setTimeout(draw,400)}});
    el.addEventListener('change',e=>{
      if(e.target.matches('[data-f]')){fv[e.target.dataset.f]=e.target.value;page=0;draw()}
      else if(e.target.matches('[data-sort]')){sort=e.target.value;page=0;draw()}});
    el.addEventListener('click',e=>{const a=e.target.closest('[data-p]');if(a){e.preventDefault();if(a.parentElement.classList.contains('disabled'))return;page=+a.dataset.p;draw()}});
    draw();
  },
  chart(id,type,labels,data,label,colors){
    const pal=colors||['#4f46e5','#10b981','#f59e0b','#ef4444','#0ea5e9','#8b5cf6','#ec4899'];const round=['pie','doughnut'].includes(type);
    return new Chart(document.getElementById(id),{type,data:{labels,datasets:[{label,data,backgroundColor:pal,borderRadius:round?0:6}]},
      options:{responsive:true,plugins:{legend:{display:round||false}},scales:round?{}:{y:{beginAtZero:true,ticks:{precision:0}}}}});
  },
  timeline:h=>`<ul class="timeline">${[...h].reverse().map((x,i)=>`<li class="${i===0?'current':''}"><span class="dot st-${x.status}"></span><div><strong>${x.status.replace('_',' ')}</strong><small class="text-muted d-block">${U.fmt(x.at)} · ${U.esc(x.by)}</small>${x.note?`<div>${U.esc(x.note)}</div>`:''}</div></li>`).join('')}</ul>`,
  stepper(status){
    if(status==='REJECTED')return '<div class="alert alert-danger py-2 my-3">This complaint was rejected.</div>';
    const st=['SUBMITTED','UNDER_REVIEW','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'],i=st.indexOf(status);
    return `<div class="stepper">${st.map((s,k)=>`<div class="step ${k<=i?'done':''}">${s.replace('_',' ')}</div>`).join('')}</div>`;
  }
};
