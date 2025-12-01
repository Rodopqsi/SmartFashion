import React, { useEffect, useState } from 'react'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'

export default function Shipments(){
  const { tokens, fetchWithAuth } = useAuth() || {}
  const toast = useToast()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)

  const rawBase = (import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000')
  const apiBase = rawBase.endsWith('/api') ? rawBase : `${rawBase}/api`
  const access = tokens?.access
  const authHeaders = access ? { 'Authorization': `Bearer ${access}`, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' }

  const loadOrders = async ()=>{
    setLoading(true)
    try{
      const r = await (fetchWithAuth?.(`${apiBase}/shop/profile/envios/`) || fetch(`${apiBase}/shop/profile/envios/`, { headers: authHeaders }))
      const j = await r.json().catch(()=>({}))
      if (r.ok){ setOrders(Array.isArray(j.data) ? j.data : []) }
      else { toast?.push(j.detail || 'No se pudieron cargar los envíos','error') }
    }catch(e){ toast?.push('Error al cargar envíos','error') }
    finally{ setLoading(false) }
  }

  useEffect(()=>{ loadOrders() }, [])

  const openTracking = async (orderNumber, trackingUrl)=>{
    if (trackingUrl){ window.open(trackingUrl, '_blank', 'noopener'); return }
    try{
      const r = await (fetchWithAuth?.(`${apiBase}/shop/orders/${encodeURIComponent(orderNumber)}/tracking`) || fetch(`${apiBase}/shop/orders/${encodeURIComponent(orderNumber)}/tracking`, { headers: authHeaders }))
      const j = await r.json().catch(()=>({}))
      if (r.ok && j.tracking_url){ window.open(j.tracking_url, '_blank', 'noopener') }
      else { toast?.push('No se pudo abrir el seguimiento','error') }
    }catch(e){ toast?.push('Error al abrir seguimiento','error') }
  }

  return (
    <div className="container" style={{padding:'1.5rem', marginTop:'4rem'}}>
      <h2>Mis Envíos</h2>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:8}}>
        <div style={{color:'var(--color-text-soft)'}}>Pedidos recientes y estado de envío</div>
        <button onClick={loadOrders} className="btn-small">Refrescar</button>
      </div>
      {loading ? <div style={{marginTop:12}}>Cargando...</div> : (
        <div style={{display:'grid', gap:12, marginTop:12}}>
          {!orders.length && <div style={{color:'var(--color-text-soft)'}}>No hay envíos para mostrar.</div>}
          {orders.map(o => (
            <div key={o.order_number} className="order-row" style={{border:'1px solid var(--color-border)', padding:12, borderRadius:10, background:'var(--color-surface)'}}>
              <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                <div>
                  <div style={{fontWeight:700}}>Pedido {o.order_number}</div>
                  <div style={{fontSize:12, color:'var(--color-text-soft)'}}>Creado: {o.created_at || '—'}</div>
                  <div style={{fontSize:13}}>Total: {o.total}</div>
                </div>
                <div style={{display:'flex', gap:8}}>
                  <button className="btn-small" onClick={()=>openTracking(o.order_number, o.tracking_url)}>Seguimiento</button>
                </div>
              </div>
              <div style={{marginTop:8}}>
                <div style={{fontSize:13, fontWeight:600}}>Dirección de envío</div>
                {o.envio ? (
                  <div style={{fontSize:13}}>
                    <div>{o.envio.destinatario}</div>
                    <div>{o.envio.direccion} · {o.envio.region}</div>
                    <div style={{fontSize:12, color:'var(--color-text-soft)'}}>Tel: {o.envio.telefono || '—'}</div>
                    <div style={{fontSize:12, color:'var(--color-text-soft)'}}>Estado: {o.envio.status || '—'}</div>
                    {o.envio.empresa && (
                      <div style={{fontSize:12, color:'var(--color-text-soft)'}}>Empresa: {o.envio.empresa}</div>
                    )}
                    {o.envio.codigo_tracking && (
                      <div style={{fontSize:12, color:'var(--color-text-soft)'}}>Tracking: {o.envio.codigo_tracking}</div>
                    )}
                  </div>
                ) : (
                  <div style={{fontSize:13, color:'var(--color-text-soft)'}}>No hay información de envío disponible.</div>
                )}
              </div>
              <div style={{marginTop:8}}>
                <div style={{fontSize:13, fontWeight:600}}>Items</div>
                <div style={{display:'grid', gap:6, marginTop:6}}>
                  {o.items.map(it => (
                    <div key={`${o.order_number}-${it.product_id}-${it.size_id || 's'}`} style={{display:'flex', gap:10, alignItems:'center'}}>
                      <img src={it.image || '/img/no-image.png'} alt={it.name} style={{width:48, height:48, objectFit:'cover', borderRadius:6}} />
                      <div style={{flex:1}}>
                        <div style={{fontSize:13, fontWeight:600}}>{it.name}</div>
                        <div style={{fontSize:12, color:'var(--color-text-soft)'}}>x{it.qty} · {it.size_id ? `Talla ${it.size_id}` : ''}</div>
                      </div>
                      <div style={{fontWeight:700}}>{it.amount}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
