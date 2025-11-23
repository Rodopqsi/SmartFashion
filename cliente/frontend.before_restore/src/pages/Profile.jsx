import React, { useEffect, useState } from 'react'
import InputFloating from '../components/InputFloating.jsx'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'

export default function Profile(){
  const { user, tokens, setUser, fetchWithAuth } = useAuth() || {}
  const toast = useToast()
  if (!user) return <div style={{padding:'2rem'}}>Debes iniciar sesión.</div>
  
  const rawBase = (import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000')
  const apiBase = rawBase.endsWith('/api') ? rawBase : `${rawBase}/api`
  const backendRoot = apiBase.replace(/\/?api\/?$/, '')
  const adminSsoUrl = `${backendRoot}/sso/admin/`

  const [loading, setLoading] = useState(true)
  const [profile, setProfile] = useState({ username: user.username, first_name: '', last_name: '', email: user.email })
  const [emails, setEmails] = useState([])
  const [addEmail, setAddEmail] = useState('')
  const [verifyEmail, setVerifyEmail] = useState({ email: '', code: '' })
  const [pwdForm, setPwdForm] = useState({ current_password: '', new_password: '', confirm: '', code: '' })
  const [sessions, setSessions] = useState([])
  const [tab, setTab] = useState('datos') 
  const access = tokens?.access

  const authHeaders = access ? { 'Authorization': `Bearer ${access}`, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' }

  useEffect(()=>{
    const load = async ()=>{
      try{
  const r = await fetchWithAuth?.(`${apiBase}/auth/profile/`, { headers: { 'Content-Type':'application/json' } }) || await fetch(`${apiBase}/auth/profile/`, { headers: authHeaders })
        const j = await r.json().catch(()=>({}))
        if (r.ok){
          setProfile(p=> ({ ...p, ...j }))
          setEmails(j.emails || [])
        }
      } finally { setLoading(false) }
    }
    load()
  }, [])

  const refreshEmails = async ()=>{
  const r = await (fetchWithAuth?.(`${apiBase}/auth/emails/`) || fetch(`${apiBase}/auth/emails/`, { headers: authHeaders }))
    const j = await r.json()
    if (r.ok) setEmails(j.data || [])
  }

  const saveProfile = async ()=>{
    const body = { first_name: profile.first_name || '', last_name: profile.last_name || '', username: (profile.username || '').trim() }
    if (!body.username){ toast?.push('El nombre de usuario es obligatorio','error'); return }
  const r = await (fetchWithAuth?.(`${apiBase}/auth/profile/`, { method:'PATCH', headers: { 'Content-Type':'application/json' }, body: JSON.stringify(body) }) || fetch(`${apiBase}/auth/profile/`, { method:'PATCH', headers: authHeaders, body: JSON.stringify(body) }))
    if (r.ok){ toast?.push('Perfil actualizado','success'); setUser && setUser(u=> ({ ...u, username: body.username || u.username })) }
    else {
      const j=await r.json().catch(()=>({}));
      const msg = (j.detail && typeof j.detail==='string') ? j.detail : (j.detail?.username || null)
      toast?.push(msg || 'No se pudo actualizar (verifica que el username no esté en uso)','error')
    }
  }

  const addNewEmail = async ()=>{
    if (!addEmail) return
  const r = await (fetchWithAuth?.(`${apiBase}/auth/emails/`, { method:'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify({ email: addEmail }) }) || fetch(`${apiBase}/auth/emails/`, { method:'POST', headers: authHeaders, body: JSON.stringify({ email: addEmail }) }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ toast?.push('Hemos enviado un código a ese correo','success'); setVerifyEmail({ email: addEmail, code: '' }); setAddEmail(''); refreshEmails() }
    else { toast?.push(j.detail||'No se pudo agregar','error') }
  }

  const verifyNewEmail = async ()=>{
    const { email, code } = verifyEmail
    if (!email || !code) return
  const r = await (fetchWithAuth?.(`${apiBase}/auth/emails/verify/`, { method:'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify({ email, code }) }) || fetch(`${apiBase}/auth/emails/verify/`, { method:'POST', headers: authHeaders, body: JSON.stringify({ email, code }) }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ toast?.push('Correo verificado','success'); setVerifyEmail({ email:'', code:'' }); refreshEmails() }
    else { toast?.push(j.detail||'Código inválido','error') }
  }

  const setPrimary = async (email)=>{
  const r = await (fetchWithAuth?.(`${apiBase}/auth/emails/set_primary/`, { method:'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify({ email }) }) || fetch(`${apiBase}/auth/emails/set_primary/`, { method:'POST', headers: authHeaders, body: JSON.stringify({ email }) }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ toast?.push('Correo principal actualizado','success'); setUser && setUser(u=> ({ ...u, email: j.email })); refreshEmails() }
    else { toast?.push(j.detail||'No se pudo establecer principal','error') }
  }

  const deleteEmail = async (email)=>{
  const r = await (fetchWithAuth?.(`${apiBase}/auth/emails/?email=${encodeURIComponent(email)}`, { method:'DELETE' }) || fetch(`${apiBase}/auth/emails/?email=${encodeURIComponent(email)}`, { method:'DELETE', headers: authHeaders }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ toast?.push('Correo eliminado','success'); refreshEmails() }
    else { toast?.push(j.detail||'No se pudo eliminar','error') }
  }

  const startPasswordChange = async ()=>{
    const { current_password, new_password, confirm } = pwdForm
    if (!current_password || !new_password || new_password !== confirm){ toast?.push('Revisa las contraseñas','error'); return }
  const r = await (fetchWithAuth?.(`${apiBase}/auth/password_change/`, { method:'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify({ current_password, new_password }) }) || fetch(`${apiBase}/auth/password_change/`, { method:'POST', headers: authHeaders, body: JSON.stringify({ current_password, new_password }) }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ toast?.push('Enviamos un código a tu correo principal','success') }
    else { toast?.push(j.detail||'No se pudo iniciar el cambio','error') }
  }

  const confirmPasswordChange = async ()=>{
    const { code } = pwdForm
    if (!code) return
    const body = { code }
  const r = await (fetchWithAuth?.(`${apiBase}/auth/password_change/verify/`, { method:'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify(body) }) || fetch(`${apiBase}/auth/password_change/verify/`, { method:'POST', headers: authHeaders, body: JSON.stringify(body) }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ toast?.push('Contraseña actualizada','success'); setPwdForm({ current_password:'', new_password:'', confirm:'', code:'' }) }
    else { toast?.push(j.detail||'Código inválido','error') }
  }

  
  const loadSessions = async ()=>{
  const r = await (fetchWithAuth?.(`${apiBase}/auth/sessions/`) || fetch(`${apiBase}/auth/sessions/`, { headers: authHeaders }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ setSessions(Array.isArray(j.data) ? j.data : []) }
    else { toast?.push(j.detail || 'No se pudieron cargar las sesiones','error') }
  }

  const logoutAll = async ()=>{
  const r = await (fetchWithAuth?.(`${apiBase}/auth/sessions/logout_all/`, { method:'POST' }) || fetch(`${apiBase}/auth/sessions/logout_all/`, { method:'POST', headers: authHeaders }))
    const j = await r.json().catch(()=>({}))
    if (r.ok){ toast?.push('Sesiones cerradas. Es posible que debas iniciar sesión de nuevo.','success') }
    else { toast?.push(j.detail || 'No se pudieron cerrar las sesiones','error') }
  }

  return (
    <div className="profile-root">
      <h2>Mi Perfil</h2>
      {loading ? <div>Cargando...</div> : (
        <>
          {}
          <div className="profile-tabs">
            <button onClick={()=>setTab('datos')} className={`tab-btn ${tab==='datos' ? 'active' : ''}`}>Datos</button>
            <button onClick={()=>setTab('correos')} className={`tab-btn ${tab==='correos' ? 'active' : ''}`}>Correos</button>
            <button onClick={()=>setTab('password')} className={`tab-btn ${tab==='password' ? 'active' : ''}`}>Contraseña</button>
            <button onClick={()=>setTab('sesiones')} className={`tab-btn ${tab==='sesiones' ? 'active' : ''}`}>Sesiones</button>
          </div>

          {}
          {tab==='datos' && (
          <section className="profile-card">
            <h3 className="profile-sec-title">Datos personales</h3>
            <div className="grid-2">
              <InputFloating
                id="pf-username"
                name="username"
                label="Usuario"
                value={profile.username || ''}
                onChange={e=>setProfile(p=>({...p, username:e.target.value}))}
                required
              />
              <InputFloating
                id="pf-email"
                name="email"
                type="email"
                label="Email principal"
                value={user.email || ''}
                disabled
              />
              <InputFloating
                id="pf-first"
                name="first_name"
                label="Nombre"
                value={profile.first_name || ''}
                onChange={e=>setProfile(p=>({...p, first_name:e.target.value}))}
              />
              <InputFloating
                id="pf-last"
                name="last_name"
                label="Apellidos"
                value={profile.last_name || ''}
                onChange={e=>setProfile(p=>({...p, last_name:e.target.value}))}
              />
            </div>
            <div className="profile-actions">
              <button onClick={saveProfile} className="btn-primary">Guardar cambios</button>
            </div>
          </section>
          )}

          {}
          {tab==='correos' && (
          <section className="profile-card">
            <h3 className="profile-sec-title">Correos vinculados</h3>
            <div style={{display:'flex', gap:8, flexWrap:'wrap'}}>
              {emails.map(e => (
                <div key={e.email} className={`email-pill ${e.is_primary ? 'primary' : ''}`}>
                  <div>
                    <div style={{fontWeight:600}}>{e.email}</div>
                    <div className="email-state">{e.is_primary ? 'Principal' : e.is_verified ? 'Verificado' : 'Sin verificar'}</div>
                  </div>
                  <div style={{display:'flex', gap:6}}>
                    {!e.is_primary && e.is_verified && <button onClick={()=>setPrimary(e.email)} className="btn-small">Hacer principal</button>}
                    {!e.is_primary && <button onClick={()=>deleteEmail(e.email)} className="btn-small-danger">Eliminar</button>}
                  </div>
                </div>
              ))}
            </div>
            <div className="profile-actions">
              <div style={{flex:1}}>
                <InputFloating id="pf-add-email" name="add_email" type="email" label="nuevo@correo.com" value={addEmail} onChange={e=>setAddEmail(e.target.value)} />
              </div>
              <button onClick={addNewEmail} className="btn-primary">Agregar</button>
            </div>
            {verifyEmail.email && (
              <div className="verify-row">
                <div style={{flex:1, maxWidth:260}}>
                  <InputFloating id="pf-email-code" name="email_code" label="Código de verificación" value={verifyEmail.code} onChange={e=>setVerifyEmail(v=>({...v, code:e.target.value}))} />
                </div>
                <button onClick={verifyNewEmail} className="btn-primary">Verificar</button>
              </div>
            )}
          </section>
          )}

          {}
          {tab==='password' && (
          <section className="profile-card">
            <h3 className="profile-sec-title">Cambiar contraseña</h3>
            <div className="grid-3">
              <InputFloating id="pf-current" name="current_password" type="password" label="Contraseña actual" value={pwdForm.current_password} onChange={e=>setPwdForm(f=>({...f, current_password:e.target.value}))} />
              <InputFloating id="pf-new" name="new_password" type="password" label="Nueva contraseña" value={pwdForm.new_password} onChange={e=>setPwdForm(f=>({...f, new_password:e.target.value}))} />
              <InputFloating id="pf-confirm" name="confirm_password" type="password" label="Confirmar" value={pwdForm.confirm} onChange={e=>setPwdForm(f=>({...f, confirm:e.target.value}))} />
            </div>
            <div style={{marginTop:12, display:'flex', gap:10, alignItems:'center', flexWrap:'wrap'}}>
              <button onClick={startPasswordChange} className="btn-primary">Enviar código</button>
              <div style={{maxWidth:220, flex:'0 0 auto', width:'100%'}}>
                <InputFloating id="pf-code" name="code" label="Código recibido" value={pwdForm.code} onChange={e=>setPwdForm(f=>({...f, code:e.target.value}))} />
              </div>
              <button onClick={confirmPasswordChange} className="btn-primary">Confirmar</button>
            </div>
          </section>
          )}

          {}
          {tab==='sesiones' && (
          <section className="profile-card">
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
              <h3 className="profile-sec-title">Sesiones</h3>
              <div style={{display:'flex', gap:8}}>
                <button onClick={loadSessions} className="btn-small">Refrescar</button>
                <button onClick={logoutAll} className="btn-small-danger">Cerrar todas</button>
              </div>
            </div>
            <div style={{display:'grid', gap:8}}>
              {sessions.map(s => (
                <div key={s.id} className="session-item">
                  <div style={{fontSize:12, color:'var(--color-text-soft)'}}>{s.created_at} · {s.ip}</div>
                  <div style={{fontSize:12}}>{s.ua}</div>
                </div>
              ))}
              {!sessions.length && <div style={{color:'var(--color-text-soft)'}}>No hay sesiones listadas (usa Refrescar).</div>}
            </div>
          </section>
          )}
          
        </>
      )}
    </div>
  )
}

const card = { border:'1px solid #eee', borderRadius:12, padding:20, marginTop:20, background:'#fff' }
const secTitle = { margin:'0 0 12px 0' }
const grid2 = { display:'grid', gridTemplateColumns:'1fr 1fr', gap:16 }
const grid3 = { display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:16 }
const input = { border:'1px solid #e5e7eb', borderRadius:10, padding:'10px 12px', width:'100%' }
const btnPrimary = { padding:'10px 14px', border:'none', background:'#111', color:'#fff', borderRadius:10, fontWeight:700, cursor:'pointer' }
const btnSmall = { padding:'6px 10px', border:'1px solid #e5e7eb', background:'#fff', borderRadius:8, cursor:'pointer' }
const btnSmallDanger = { padding:'6px 10px', border:'1px solid #fecaca', color:'#b91c1c', background:'#fff', borderRadius:8, cursor:'pointer' }
const btnLink = { padding:'8px 12px', background:'#4f46e5', color:'#fff', borderRadius:8, textDecoration:'none', display:'inline-block' }
const emailPill = (primary)=> ({ display:'flex', alignItems:'center', justifyContent:'space-between', gap:12, padding:'10px 12px', border:'1px solid #e5e7eb', borderRadius:10, minWidth:280, background: primary? '#f5f3ff':'#fff' })
const tabBtn = (active)=> ({ padding:'8px 12px', borderRadius:8, border:'1px solid #e5e7eb', background: active? '#111':'#fff', color: active? '#fff':'#111', cursor:'pointer', fontWeight:600 })
