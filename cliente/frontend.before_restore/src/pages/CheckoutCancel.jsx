import React from 'react'
import { Link } from 'react-router-dom'

export default function CheckoutCancel(){
  return (
    <div style={{maxWidth:800, margin:'0 auto', padding:'16px 20px', paddingTop:'calc(var(--nav-height) + 12px)', textAlign:'center'}}>
      <h2>Pago cancelado</h2>
      <p>Tu pago fue cancelado o no se pudo completar. Puedes intentar nuevamente.</p>
      <div style={{display:'flex', gap:10, justifyContent:'center', marginTop:12}}>
        <Link to="/carrito" style={{padding:'10px 16px', border:'1px solid #ddd', borderRadius:10, textDecoration:'none'}}>Volver al carrito</Link>
        <Link to="/catalogo" style={{padding:'10px 16px', border:'1px solid #ddd', borderRadius:10, textDecoration:'none'}}>Seguir comprando</Link>
      </div>
    </div>
  )
}
