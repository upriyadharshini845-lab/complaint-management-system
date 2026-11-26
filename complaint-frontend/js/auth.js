const Auth={
  user:()=>JSON.parse(localStorage.getItem('cmrs_user')||'null'),
  save(r){localStorage.setItem('cmrs_token',r.token);localStorage.setItem('cmrs_user',JSON.stringify(r.user))},
  dir:r=>({USER:'user',ADMIN:'admin',STAFF:'staff'}[r]),
  home:r=>`${ROOT}${Auth.dir(r)}/dashboard.html`,
  require(role){const u=Auth.user();
    if(!u||!localStorage.getItem('cmrs_token')||u.role!==role){location.href=ROOT+'login.html';return null}return u},
  logout(){localStorage.removeItem('cmrs_token');localStorage.removeItem('cmrs_user');location.href=ROOT+'login.html'}
};
