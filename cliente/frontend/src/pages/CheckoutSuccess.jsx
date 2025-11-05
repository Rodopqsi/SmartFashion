import React from 'react'
import { Link, useLocation } from 'react-router-dom'

export default function CheckoutSuccess(){
  const { search } = useLocation()
  const order = new URLSearchParams(search).get('order') || 'N/A'
  return (
    <div style={{maxWidth:800, margin:'0 auto', padding:'16px 20px', paddingTop:'calc(var(--nav-height) + 12px)', textAlign:'center'}}>
      <h2>¡Gracias por tu compra!</h2>
      <p>Tu pedido fue registrado correctamente.</p>
      <div style={{margin:'12px 0', fontWeight:800, fontSize:18}}>N° de pedido: {order}</div>
      <Link to="/catalogo" style={{display:'inline-block', marginTop:12, padding:'10px 16px', borderRadius:10, border:'1px solid #ddd', textDecoration:'none'}}>Seguir comprando</Link>
    </div>
  )
}
