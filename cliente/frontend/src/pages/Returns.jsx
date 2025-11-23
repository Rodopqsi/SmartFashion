import React, { useEffect, useState } from 'react'
import InputFloating from '../components/InputFloating.jsx'
import InputSelectFloating from '../components/InputSelectFloating.jsx'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'

export default function Returns(){
  const { user } = useAuth() || {}
  const { push } = useToast()
  const apiBase = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({ order_number:'', motivo:'talla_incorrecta', descripcion:'', metodo:'cambio', telefono:'' })

  useEffect(()=>{
    let ignore = false
    async function load(){
      setLoading(true)
      try{
        const res = await fetch(`${apiBase}/returns/`, { credentials:'include' })
        const j = await res.json()
        if(!ignore) setItems(j.data || [])
      }catch(e){}
      if(!ignore) setLoading(false)
    }
    load()
    return ()=>{ ignore = true }
  },[apiBase])

  async function onSubmit(e){
    e.preventDefault()
    if(!form.order_number || !form.motivo || !form.metodo){
      push({ type:'error', message:'Completa orden, motivo y método' }); return
    }
    try{
      const res = await fetch(`${apiBase}/returns/`, {
        method:'POST',
        headers:{ 'Content-Type':'application/json' },
        credentials:'include',
        body: JSON.stringify({ ...form })
      })
      const j = await res.json()
      if(res.ok){
        push({ type:'success', message:'Solicitud de devolución enviada' })
        setForm({ order_number:'', motivo:'talla_incorrecta', descripcion:'', metodo:'cambio', telefono:'' })
        setItems(prev=>[{ id:j.id, order_number:form.order_number, motivo:form.motivo, metodo:form.metodo, estado:'solicitado', created_at:new Date().toISOString() }, ...prev])
      }else{
        push({ type:'error', message:j.message || 'Error al enviar' })
      }
    }catch(e){ push({ type:'error', message:String(e) }) }
  }

  if(!user) return <div style={{padding:'2rem'}}>Debes iniciar sesión.</div>

  return (
    <div style={{padding:'2rem', maxWidth:900, margin:'0 auto'}}>
      <h2>Mis Devoluciones</h2>
      <form onSubmit={onSubmit} style={{margin:'1rem 0', padding:'1rem', border:'1px solid #eee', borderRadius:8}}>
        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
          <InputFloating id="ret-order" name="order" label="Orden" value={form.order_number} onChange={e=>setForm(f=>({ ...f, order_number:e.target.value }))} />
          <InputFloating id="ret-phone" name="telefono" label="Teléfono (opcional)" value={form.telefono} onChange={e=>setForm(f=>({ ...f, telefono:e.target.value }))} />
          <InputSelectFloating id="ret-motivo" name="motivo" label="Motivo" value={form.motivo} onChange={e=>setForm(f=>({ ...f, motivo:e.target.value }))}>
            <option value="talla_incorrecta">Talla incorrecta</option>
            <option value="defectuoso">Defectuoso</option>
            <option value="no_satisfecho">No satisfecho</option>
            <option value="otro">Otro</option>
          </InputSelectFloating>
          <InputSelectFloating id="ret-metodo" name="metodo" label="Método" value={form.metodo} onChange={e=>setForm(f=>({ ...f, metodo:e.target.value }))}>
            <option value="cambio">Cambio</option>
            <option value="reembolso">Reembolso</option>
          </InputSelectFloating>
          <div style={{gridColumn:'1 / -1'}}>
            <label style={{display:'block', marginBottom:6, fontWeight:600}}>Descripción (opcional)</label>
            <textarea rows={4} value={form.descripcion} onChange={e=>setForm(f=>({ ...f, descripcion:e.target.value }))} style={{width:'100%', padding:'10px 12px', border:'1px solid #e5e7eb', borderRadius:10}} />
          </div>
        </div>
        <button className="btn" type="submit">Enviar</button>
      </form>

      {loading ? <div>Cargando...</div> : (
        <table className="table">
          <thead>
            <tr><th>ID</th><th>Orden</th><th>Motivo</th><th>Método</th><th>Estado</th><th>Fecha</th></tr>
          </thead>
          <tbody>
            {items.map(it => (
              <tr key={it.id}>
                <td>{it.id}</td>
                <td>{it.order_number}</td>
                <td>{it.motivo}</td>
                <td>{it.metodo}</td>
                <td>{it.estado}</td>
                <td>{it.created_at?.slice(0,19).replace('T',' ')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
