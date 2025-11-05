import React from 'react'
import { useAuth } from '../auth.jsx'
import { UBIGEO, REGIONES } from './peruUbigeo.js'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function AddressForm({ initial = {}, onSaved, onCancel }){
  const { tokens, fetchWithAuth } = useAuth() || {}
  const [form, setForm] = React.useState({
    label: '', nombre: '', telefono: '', alt_telefono: '',
    direccion: '', direccion_linea2: '',
    region: '', ciudad: '', distrito: '',
    estado: '', codigo_postal: '', referencia: '',
    is_default: false,
    ...initial,
  })
  const [saving, setSaving] = React.useState(false)
  const [errors, setErrors] = React.useState({})

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  const submit = async (e) => {
    e?.preventDefault()
    if (!tokens?.access){
      alert('Inicia sesión para guardar una dirección.')
      return
    }
  const errs = {}
    if (!form.nombre || form.nombre.trim().length < 2) errs.nombre = 'Ingresa el nombre del destinatario'
    if (!/^[a-zA-ZÁÉÍÓÚáéíóúñÑ\s.'-]+$/.test(form.nombre || '')) errs.nombre = 'El nombre solo debe contener letras y espacios'
    if (!form.direccion || form.direccion.trim().length < 5) errs.direccion = 'Ingresa una dirección válida'
    if (form.nombre && form.direccion && form.nombre.trim() === form.direccion.trim()) errs.direccion = 'Nombre y dirección no deben ser iguales'
    if (!form.region) errs.region = 'Selecciona una región (departamento)'
    if (form.region && UBIGEO[form.region]){
      if (!form.ciudad) errs.ciudad = 'Selecciona una provincia'
      if (!form.distrito) errs.distrito = 'Selecciona un distrito'
    }
    setErrors(errs)
    if (Object.keys(errs).length) return
    setSaving(true)
    try{
      const method = form.id ? 'PUT' : 'POST'
      const url = form.id ? `${API_BASE}/api/addresses/${form.id}/` : `${API_BASE}/api/addresses/`
  const headers = { 'Content-Type':'application/json' }
      const payload = { ...form }
      delete payload.pais
      const res = await fetchWithAuth(url, { method, headers, body: JSON.stringify(payload) })
      const j = await res.json().catch(()=>null)
      if (res.ok){ onSaved && onSaved(j) } else if (res.status === 401){ alert('Tu sesión ha expirado. Inicia sesión nuevamente.'); } else { alert(j?.message || 'No se pudo guardar la dirección') }
    } finally{ setSaving(false) }
  }

  return (
    <form onSubmit={submit} style={{display:'grid', gap:8}}>
      <div style={row}><label>Etiqueta</label><input name="label" value={form.label||''} onChange={handleChange} /></div>
      <div style={row}><label>Destinatario*</label>
        <div>
          <input name="nombre" value={form.nombre||''} onChange={handleChange} required />
          {errors.nombre && <small style={errStyle}>{errors.nombre}</small>}
        </div>
      </div>
      <div style={row}><label>Teléfono</label><input name="telefono" value={form.telefono||''} onChange={handleChange} /></div>
      <div style={row}><label>Teléfono alterno</label><input name="alt_telefono" value={form.alt_telefono||''} onChange={handleChange} /></div>
      <div style={row}><label>Dirección*</label>
        <div>
          <input name="direccion" value={form.direccion||''} onChange={handleChange} required />
          {errors.direccion && <small style={errStyle}>{errors.direccion}</small>}
        </div>
      </div>
      <div style={row}><label>Dirección línea 2</label><input name="direccion_linea2" value={form.direccion_linea2||''} onChange={handleChange} /></div>
      <div style={row}><label>Región (Departamento)*</label>
        <div>
          <select name="region" value={form.region||''} onChange={(e)=>{ handleChange(e); setForm(p=>({ ...p, ciudad:'', distrito:'' })) }} required>
            <option value="">Seleccione</option>
            {REGIONES.map(r => <option key={r} value={r}>{r}</option>)}
            <option value="Otro">Otro</option>
          </select>
          {errors.region && <small style={errStyle}>{errors.region}</small>}
        </div>
      </div>
      {form.region && form.region !== 'Otro' && UBIGEO[form.region] ? (
        <>
          <div style={row}><label>Provincia*</label>
            <div>
              <select name="ciudad" value={form.ciudad||''} onChange={(e)=>{ handleChange(e); setForm(p=>({ ...p, distrito:'' })) }} required>
                <option value="">Seleccione</option>
                {Object.keys(UBIGEO[form.region]).map(p => <option key={p} value={p}>{p}</option>)}
              </select>
              {errors.ciudad && <small style={errStyle}>{errors.ciudad}</small>}
            </div>
          </div>
          <div style={row}><label>Distrito*</label>
            <div>
              <select name="distrito" value={form.distrito||''} onChange={handleChange} required>
                <option value="">Seleccione</option>
                {(UBIGEO[form.region][form.ciudad] || []).map(d => <option key={d} value={d}>{d}</option>)}
              </select>
              {errors.distrito && <small style={errStyle}>{errors.distrito}</small>}
            </div>
          </div>
        </>
      ) : (
        <>
          <div style={row}><label>Provincia</label><input name="ciudad" value={form.ciudad||''} onChange={handleChange} /></div>
          <div style={row}><label>Distrito</label><input name="distrito" value={form.distrito||''} onChange={handleChange} /></div>
        </>
      )}
      <div style={row}><label>Estado/Provincia</label><input name="estado" value={form.estado||''} onChange={handleChange} /></div>
      <div style={row}><label>Código Postal</label><input name="codigo_postal" value={form.codigo_postal||''} onChange={handleChange} /></div>
      <div style={row}><label>Referencia</label><input name="referencia" value={form.referencia||''} onChange={handleChange} /></div>
      <div style={{display:'flex', alignItems:'center', gap:8}}>
        <input type="checkbox" id="is_default" name="is_default" checked={!!form.is_default} onChange={handleChange} />
        <label htmlFor="is_default">Usar como dirección predeterminada</label>
      </div>
      <div style={{display:'flex', gap:8, marginTop:8}}>
        <button type="submit" disabled={saving} style={btnPrimary}>{saving?'Guardando...':'Guardar'}</button>
        {onCancel && <button type="button" onClick={onCancel} style={btnSecondary}>Cancelar</button>}
      </div>
    </form>
  )
}

const row = { display:'grid', gridTemplateColumns:'180px 1fr', alignItems:'center', gap:8 }
const btnPrimary = { padding:'8px 14px', border:'none', background:'#111', color:'#fff', borderRadius:8, fontWeight:700, cursor:'pointer' }
const btnSecondary = { padding:'8px 14px', border:'1px solid #ddd', background:'#fff', color:'#111', borderRadius:8, fontWeight:600, cursor:'pointer' }
const errStyle = { color:'#b91c1c', fontSize:12, marginTop:4, display:'block' }
