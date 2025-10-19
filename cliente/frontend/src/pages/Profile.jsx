import React from 'react'
import { useAuth } from '../auth.jsx'

export default function Profile(){
  const { user } = useAuth() || {}
  if (!user) return <div style={{padding:'2rem'}}>Debes iniciar sesión.</div>
  return (
    <div style={{padding:'2rem', maxWidth:800, margin:'0 auto'}}>
      <h2>Mi Perfil</h2>
      <div style={{marginTop:'1rem'}}>
        <div><strong>Usuario:</strong> {user.username || '-'}</div>
        <div><strong>Email:</strong> {user.email || '-'}</div>
      </div>
    </div>
  )
}
