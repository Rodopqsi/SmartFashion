import React from 'react'
import { useAuth } from '../auth.jsx'

export default function Profile(){
  const { user } = useAuth() || {}
  if (!user) return <div style={{padding:'2rem'}}>Debes iniciar sesión.</div>
  const apiBase = import.meta.env.VITE_API_BASE || 'http://localhost:8000/api'
  const backendRoot = apiBase.replace(/\/?api\/?$/, '')
  const adminSsoUrl = `${backendRoot}/sso/admin/`
  return (
    <div style={{padding:'2rem', maxWidth:800, margin:'0 auto'}}>
      <h2>Mi Perfil</h2>
      <div style={{marginTop:'1rem'}}>
        <div><strong>Usuario:</strong> {user.username || '-'}</div>
        <div><strong>Email:</strong> {user.email || '-'}</div>
      </div>
      <div style={{marginTop:'2rem'}}>
        <a className="btn" href={adminSsoUrl} style={{padding:'0.5rem 1rem', background:'#4f46e5', color:'#fff', borderRadius:8, textDecoration:'none'}}>Ir al Panel Administrativo</a>
        <div style={{fontSize:12, color:'#666', marginTop:8}}>Requiere permisos de administrador. Serás redirigido al inicio si no los tienes.</div>
      </div>
    </div>
  )
}
