import React from 'react'
import AddressForm from './AddressForm.jsx'
import { useAuth } from '../auth.jsx'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function AddressPicker({ value, onChange }){
  const { tokens, user, fetchWithAuth } = useAuth() || {}
  const [addresses, setAddresses] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [showForm, setShowForm] = React.useState(false)

  const load = async () => {
    setLoading(true)
    try{
  const headers = {}
  const res = await fetchWithAuth(`${API_BASE}/api/addresses/`, { headers })
      const j = await res.json().catch(()=>null)
      if (res.ok && j?.data){
        setAddresses(j.data)
        if (!value){
          const def = j.data.find(a => a.is_default)
          if (def) onChange && onChange(def.id)
        }
      }
    } finally{ setLoading(false) }
  }

  React.useEffect(()=>{ if (tokens?.access) load() }, [tokens?.access])

  const del = async (id) => {
    if (!confirm('¿Eliminar esta dirección?')) return
  const headers = {}
  await fetchWithAuth(`${API_BASE}/api/addresses/${id}/`, { method:'DELETE', headers })
    if (value === id) onChange && onChange(null)
    load()
  }

  const markDefault = async (id) => {
  const headers = {}
  await fetchWithAuth(`${API_BASE}/api/addresses/${id}/default`, { method:'POST', headers })
    load()
  }

  return (
    <div style={{border:'1px solid var(--color-border)', borderRadius:10, padding:12, background:'var(--color-bg)'}}>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
        <strong>Dirección de envío</strong>
        {!!tokens?.access && <button onClick={()=>setShowForm(!showForm)} style={btnSecondary}>{showForm?'Cerrar':'Agregar nueva'}</button>}
      </div>
        {!tokens?.access && (
        <div style={{marginTop:10, fontSize:13, color:'var(--color-text-soft)'}}>
          Inicia sesión para gestionar tus direcciones de envío.
        </div>
      )}
      {tokens?.access && showForm && (
        <div style={{marginTop:10}}>
          <AddressForm onSaved={()=>{ setShowForm(false); load() }} onCancel={()=>setShowForm(false)} />
        </div>
      )}
      <div style={{marginTop:10}}>
        {!tokens?.access ? (
          <div>No has iniciado sesión.</div>
        ) : loading ? 'Cargando direcciones...' : (
          addresses.length ? (
            <div style={{display:'grid', gap:8}}>
              {addresses.map(a => (
                <label key={a.id} style={card}>
                  <div style={{display:'grid', gridTemplateColumns:'24px 1fr auto', alignItems:'center', gap:8}}>
                    <input type="radio" name="address" checked={value===a.id} onChange={()=>onChange && onChange(a.id)} />
                    <div>
                      <div style={{fontWeight:700, color:'var(--color-text)'}}>{a.label || a.nombre}</div>
                      <div style={{fontSize:12, color:'var(--color-text-soft)'}}>
                        {a.nombre} · {a.telefono || 's/tel'}{a.alt_telefono ? ` / ${a.alt_telefono}` : ''}
                      </div>
                      <div style={{fontSize:12, color:'var(--color-text)'}}>
                        {a.direccion}{a.direccion_linea2 ? `, ${a.direccion_linea2}` : ''}, {a.distrito || ''} {a.ciudad || ''}, {a.region}
                        {a.estado ? `, ${a.estado}` : ''}{a.pais ? `, ${a.pais}` : ''}{a.codigo_postal ? `, ${a.codigo_postal}` : ''}
                      </div>
                      {a.referencia && <div style={{fontSize:12, color:'var(--color-text-soft)'}}>Ref: {a.referencia}</div>}
                      {a.is_default && <span style={badge}>Predeterminada</span>}
                    </div>
                    <div style={{display:'flex', gap:6}}>
                      {!a.is_default && <button type="button" onClick={()=>markDefault(a.id)} style={miniBtn}>Hacer predet.</button>}
                      <button type="button" onClick={()=>del(a.id)} style={miniDanger}>Eliminar</button>
                    </div>
                  </div>
                </label>
              ))}
            </div>
          ) : (
            <div>No tienes direcciones guardadas.</div>
          )
        )}
      </div>
    </div>
  )
}

const card = { border:'1px solid var(--color-border)', borderRadius:10, padding:10, cursor:'pointer', background:'var(--color-bg)' }
const badge = { display:'inline-block', marginTop:6, fontSize:10, background:'var(--color-bg-soft)', color:'var(--color-text)', padding:'2px 6px', borderRadius:6 }
const btnSecondary = { padding:'6px 10px', border:'1px solid var(--color-border)', background:'var(--color-bg)', color:'var(--color-text)', borderRadius:8, fontWeight:600, cursor:'pointer' }
const miniBtn = { padding:'6px 8px', border:'1px solid var(--color-border)', background:'var(--color-bg)', color:'var(--color-text)', borderRadius:8, cursor:'pointer', fontSize:12 }
const miniDanger = { padding:'6px 8px', border:'1px solid #fecaca', background:'var(--color-bg)', color:'#b91c1c', borderRadius:8, cursor:'pointer', fontSize:12 }
