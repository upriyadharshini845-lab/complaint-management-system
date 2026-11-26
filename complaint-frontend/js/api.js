/* API layer. Every call goes to the Spring Boot backend (API_BASE_URL in config.js). */
async function req(path,method='GET',body){
  const h={'Content-Type':'application/json'},t=localStorage.getItem('cmrs_token');
  if(t)h.Authorization='Bearer '+t;
  let r;
  try{r=await fetch(CONFIG.API_BASE_URL+path,{method,headers:h,body:body?JSON.stringify(body):undefined})}
  catch(e){throw new Error('Cannot reach the server. Please check that the backend is running.')}
  if(r.status===401&&!path.startsWith('/auth')){Auth.logout();return}
  const j=await r.json().catch(()=>({}));
  if(!r.ok)throw new Error(j.message||'Request failed ('+r.status+')');
  return j;
}
const qs=p=>new URLSearchParams(Object.entries(p).filter(([k,v])=>v!==''&&v!==undefined&&v!==null)).toString();

const API={
  /* auth */
  login:(email,password)=>req('/auth/login','POST',{email,password}),
  register:b=>req('/auth/register','POST',b),
  forgot:email=>req('/auth/forgot-password','POST',{email}),
  resetPassword:(token,newPassword)=>req('/auth/reset-password','POST',{token,newPassword}),
  updateProfile:b=>req('/users/me','PUT',b),
  /* complaints */
  complaints:()=>req('/complaints'),
  complaintsPage:p=>req('/complaints/search?'+qs(p)),
  complaint:tid=>req('/complaints/track/'+encodeURIComponent(tid)),
  createComplaint:b=>req('/complaints','POST',b),
  updateComplaint:(tid,p)=>req('/complaints/'+encodeURIComponent(tid),'PATCH',p),
  /* admin: users and departments */
  users:()=>req('/users'),
  addUser:b=>req('/users','POST',b),
  toggleUser:id=>req(`/users/${id}/toggle`,'PATCH'),
  departments:()=>req('/departments'),
  addDepartment:name=>req('/departments','POST',{name}),
  deleteDepartment:id=>req('/departments/'+id,'DELETE'),
  /* notifications, feedback, escalations, analytics */
  notifications:()=>req('/notifications'),
  readAll:()=>req('/notifications/read-all','PATCH'),
  feedbackList:()=>req('/feedback'),
  addFeedback:b=>req('/feedback','POST',b),
  escalations:()=>req('/escalations'),
  runEscalation:()=>req('/escalations/run','POST'),
  analytics:()=>req('/analytics')
};
