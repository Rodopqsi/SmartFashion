import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './Home.css'
import { useCart } from '../cart.jsx'
import AddressPicker from '../components/AddressPicker.jsx'
import { useAuth } from '../auth.jsx'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function Cart(){
  const { items, increment, decrement, remove, clear, subtotal, igv, total, IGV_RATE } = useCart() || {}
  const { user, tokens, fetchWithAuth } = useAuth() || {}
  const navigate = useNavigate()
  const userEmail = user?.email || null
  const [serverTotals, setServerTotals] = React.useState(null)
  const [checking, setChecking] = React.useState(false)
  const [addressId, setAddressId] = React.useState(null)
  const [addressWarning, setAddressWarning] = React.useState(false)

  const preview = async () => {
    if (!items?.length) return
    setChecking(true)
    try{
      const payload = { items: items.map(i => ({ product_id: i.productId, size_id: i.sizeId, color_id: i.colorId, qty: i.qty })) }
  const headers = { 'Content-Type':'application/json' }
  const res = await fetchWithAuth(`${API_BASE}/api/checkout/preview/`, { method:'POST', headers, body: JSON.stringify(payload) })
      const j = await res.json().catch(()=>null)
      if (res.ok && j?.data){ setServerTotals(j.data) } else { setServerTotals(null) }
    } finally{ setChecking(false) }
  }

  React.useEffect(()=>{ preview() }, [items])

  // Start Stripe Checkout session
  const finalize = async () => {
    if (!items?.length) return
    try{
      const payload = {
        userEmail,
        address_id: addressId || undefined,
        items: items.map(i => ({ product_id: i.productId, size_id: i.sizeId, color_id: i.colorId, qty: i.qty }))
      }
      const headers = { 'Content-Type':'application/json' }
      const res = await fetchWithAuth(`${API_BASE}/api/payments/create_session/`, { method:'POST', headers, body: JSON.stringify(payload) })
      const j = await res.json().catch(()=>null)
      if (res.ok && j?.url){
        // Leave cart for now; we'll clear on webhook/order success
        window.location.href = j.url
        return
      }
      // Fallback: if payments not configured, finish without gateway
      const fallback = await fetchWithAuth(`${API_BASE}/api/checkout/confirm/`, { method:'POST', headers, body: JSON.stringify(payload) })
      const fj = await fallback.json().catch(()=>null)
      clear()
      navigate(`/checkout/success?order=${encodeURIComponent(fj?.order_number || 'LOCAL-'+Date.now())}`)
    }catch{
      clear()
      navigate(`/checkout/success?order=${encodeURIComponent('LOCAL-'+Date.now())}`)
    }
  }

  return (
    <div style={{maxWidth:1200, margin:'0 auto', padding:'16px 20px', paddingTop:'calc(var(--nav-height) + 12px)'}}>
      <h2 style={{marginTop:0}}>Carrito de compras</h2>
      {!items?.length ? (
        <div style={{padding:16, border:'1px solid #eee', borderRadius:10, background:'#fff'}}>
          <p>Tu carrito está vacío.</p>
          <Link to="/catalogo" className="btn" style={btnPrimary}>Ir al catálogo</Link>
        </div>
      ) : (
        <div style={{display:'grid', gridTemplateColumns:'1fr 360px', gap:16}}>
          <div style={{display:'flex', flexDirection:'column', gap:12}}>
            <AddressPicker value={addressId} onChange={id => { setAddressId(id); setAddressWarning(false); }} />
            {items.map(i => (
              <div key={i.key} style={itemCard}>
                <div style={{display:'grid', gridTemplateColumns:'96px 1fr 120px', alignItems:'center', gap:12}}>
                  <div>
                    {i.image ? <img src={i.image} alt={i.name} style={{width:96, height:96, objectFit:'cover', borderRadius:8}}/> : <div style={{width:96, height:96, background:'#f5f5f5', borderRadius:8}}/>}
                  </div>
                  <div>
                    <div style={{fontWeight:700}}>{i.name}</div>
                    <div style={{fontSize:12, color:'#6b7280'}}>Talla: {i.sizeName || i.sizeId || '-'} | Color: {i.colorName || i.colorId || '-'}</div>
                    <div style={{display:'flex', alignItems:'center', gap:8, marginTop:8}}>
                      <button onClick={()=>decrement(i.key)} style={qtyBtn} aria-label="Disminuir">-</button>
                      <input value={i.qty} readOnly style={qtyInput}/>
                      <button onClick={()=>increment(i.key)} style={qtyBtn} aria-label="Incrementar">+</button>
                      <button onClick={()=>remove(i.key)} title="Eliminar" style={trashBtn}>🗑</button>
                    </div>
                  </div>
                  <div style={{textAlign:'right'}}>
                    <div style={{fontWeight:800}}>S/ {(i.price * i.qty).toFixed(2)}</div>
                    <div style={{fontSize:12, color:'#6b7280'}}>S/ {i.price.toFixed(2)} c/u</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
          <aside style={summaryCard}>
            <div style={{fontWeight:700, marginBottom:6}}>Resumen del pedido</div>
            <div style={row}><span>Subtotal</span><span>S/ {(serverTotals?.subtotal ?? subtotal).toFixed(2)}</span></div>
            <div style={{...row, fontSize:12, color:'#6b7280'}}><span>({items.length} producto{items.length>1?'s':''})</span><span>{checking ? 'verificando...' : ''}</span></div>
            <div style={row}><span>IGV ({Math.round(IGV_RATE*100)}%)</span><span>S/ {(serverTotals?.igv ?? igv).toFixed(2)}</span></div>
            <hr style={{margin:'10px 0', border:'none', borderTop:'1px solid #eee'}}/>
            <div style={{...row, fontWeight:800}}><span>Total</span><span>S/ {(serverTotals?.total ?? total).toFixed(2)}</span></div>
            {addressWarning && (
              <div style={{color:'#b91c1c', background:'#fee2e2', border:'1px solid #fca5a5', borderRadius:8, padding:'8px 10px', marginBottom:8, fontSize:13, textAlign:'center'}}>
                Debes seleccionar una dirección de envío antes de finalizar la compra.
              </div>
            )}
            <button
              onClick={() => {
                if (!addressId) {
                  setAddressWarning(true);
                  return;
                }
                setAddressWarning(false);
                finalize();
              }}
              style={{...btnPrimary, width:'100%', marginTop:12}}
            >
              Finalizar compra
            </button>
            <button onClick={()=>navigate('/catalogo')} style={{...btnSecondary, width:'100%', marginTop:8}}>Continuar comprando</button>
            <div style={{marginTop:12, fontSize:11, color:'#6b7280', textAlign:'center'}}>Compra 100% segura. Tus datos están protegidos.</div>
          </aside>
        </div>
      )}
    </div>
  )
}

const itemCard = { border:'1px solid #eee', borderRadius:10, padding:12, background:'#fff' }
const qtyBtn = {border:'1px solid #e5e7eb', width:28, height:28, borderRadius:8, background:'#fff', cursor:'pointer'}
const qtyInput = {width:42, textAlign:'center', border:'1px solid #e5e7eb', height:28, borderRadius:8}
const trashBtn = {marginLeft:8, border:'1px solid #fca5a5', background:'#fff', color:'#b91c1c', borderRadius:8, padding:'5px 8px', cursor:'pointer'}
const summaryCard = { border:'1px solid #eee', borderRadius:10, padding:16, background:'#fff', height:'fit-content', position:'sticky', top:'calc(var(--nav-height) + 16px)' }
const row = { display:'flex', justifyContent:'space-between', marginTop:6 }
const btnPrimary = { padding:'10px 16px', border:'none', background:'#111', color:'#fff', borderRadius:10, fontWeight:700, cursor:'pointer' }
const btnSecondary = { padding:'10px 16px', border:'1px solid #ddd', background:'#fff', color:'#111', borderRadius:10, fontWeight:600, cursor:'pointer' }
