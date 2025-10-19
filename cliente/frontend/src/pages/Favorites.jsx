import React from 'react'
import { useAuth } from '../auth.jsx'

export default function Favorites(){
  const { user } = useAuth() || {}
  if (!user) return <div style={{padding:'2rem'}}>Debes iniciar sesión.</div>
  return (
    <div style={{padding:'2rem', maxWidth:800, margin:'0 auto'}}>
      <h2>Mis Favoritos</h2>
      <p style={{opacity:.8}}>Aún no tienes productos favoritos.</p>
    </div>
  )
}
