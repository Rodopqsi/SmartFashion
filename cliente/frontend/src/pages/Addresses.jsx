import React from 'react'
import { useAuth } from '../auth.jsx'
import AddressForm from '../components/AddressForm.jsx'
import './Addresses.css'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function Addresses(){
  const { tokens, user, fetchWithAuth } = useAuth() || {}
  const [addresses, setAddresses] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [editing, setEditing] = React.useState(null)

  const load = async () => {
    setLoading(true)
    try{
  const headers = {}
  const res = await fetchWithAuth(`${API_BASE}/api/addresses/`, { headers })
      const j = await res.json().catch(()=>null)
      if (res.ok && j?.data) setAddresses(j.data)
    } finally{ setLoading(false) }
  }
  React.useEffect(()=>{ if (tokens?.access) load() }, [tokens?.access])

  const del = async (id) => {
    if (!confirm('¿Eliminar esta dirección?')) return
  const headers = {}
  await fetchWithAuth(`${API_BASE}/api/addresses/${id}/`, { method:'DELETE', headers })
    setEditing(null)
    load()
  }

  const markDefault = async (id) => {
  const headers = {}
  await fetchWithAuth(`${API_BASE}/api/addresses/${id}/default`, { method:'POST', headers })
    load()
  }

  return (
    <div className="addresses-page">
      <h2>Mis direcciones</h2>
      {!tokens?.access && (
        <div style={card}>Debes iniciar sesión para ver y gestionar tus direcciones.</div>
      )}
      <div className="addresses-grid">
        <aside className="addresses-form" style={card}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
            <strong>{editing ? 'Editar dirección' : 'Agregar nueva dirección'}</strong>
            {tokens?.access && editing && <button style={miniBtn} onClick={()=>setEditing(null)}>Nueva</button>}
          </div>
          <div style={{marginTop:10}}>
            {tokens?.access ? (
              <AddressForm initial={editing||{}} onSaved={()=>{ setEditing(null); load() }} />
            ) : (
              <div style={{fontSize:13, color:'#6b7280'}}>Inicia sesión para agregar una dirección.</div>
            )}
          </div>
        </aside>
        <div className="addresses-list">
          {loading ? 'Cargando...' : (
            !tokens?.access ? null : addresses.length ? (
              <div style={{display:'grid', gap:10}}>
                {addresses.map(a => (
                  <div key={a.id} style={card}>
                    <div style={{display:'grid', gridTemplateColumns:'1fr auto', gap:8, alignItems:'start'}}>
                      <div>
                        <div style={{fontWeight:700}}>{a.label || a.nombre}</div>
                        <div style={{fontSize:12, color:'#6b7280'}}>
                          {a.nombre} · {a.telefono || 's/tel'}{a.alt_telefono ? ` / ${a.alt_telefono}` : ''}
                        </div>
                        <div style={{fontSize:12}}>
                          {a.direccion}{a.direccion_linea2 ? `, ${a.direccion_linea2}` : ''}, {a.distrito || ''} {a.ciudad || ''}, {a.region}
                          {a.estado ? `, ${a.estado}` : ''}{a.pais ? `, ${a.pais}` : ''}{a.codigo_postal ? `, ${a.codigo_postal}` : ''}
                        </div>
                        {a.referencia && <div style={{fontSize:12, color:'#6b7280'}}>Ref: {a.referencia}</div>}
                        {a.is_default && <span style={badge}>Predeterminada</span>}
                      </div>
                      <div style={{display:'flex', flexDirection:'column', gap:6, alignSelf:'start'}}>
                        {!a.is_default && <button onClick={()=>markDefault(a.id)} style={miniBtn}>Hacer predet.</button>}
                        <button onClick={()=>setEditing(a)} style={miniBtn}>Editar</button>
                        <button onClick={()=>del(a.id)} style={miniDanger}>Eliminar</button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div style={card}>No tienes direcciones guardadas.</div>
            )
          )}
        </div>
      </div>
    </div>
  )
}

const card = { border:'1px solid #eee', borderRadius:10, padding:16, background:'#fff' }
const badge = { display:'inline-block', marginTop:6, fontSize:10, background:'#eef2ff', color:'#3730a3', padding:'2px 6px', borderRadius:6 }
const miniBtn = { padding:'6px 8px', border:'1px solid #ddd', background:'#fff', color:'#111', borderRadius:8, cursor:'pointer', fontSize:12 }
const miniDanger = { padding:'6px 8px', border:'1px solid #fecaca', background:'#fff', color:'#b91c1c', borderRadius:8, cursor:'pointer', fontSize:12 }
