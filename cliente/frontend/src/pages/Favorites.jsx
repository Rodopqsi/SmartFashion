import React, { useMemo, useState } from 'react'
import InputFloating from '../components/InputFloating.jsx'
import InputSelectFloating from '../components/InputSelectFloating.jsx'
import { useAuth } from '../auth.jsx'
import { useFavorites } from '../favorites.jsx'
import { Link } from 'react-router-dom'

export default function Favorites(){
  const { user } = useAuth() || {}
  const { items, removeFavorite, clearFavorites } = useFavorites() || { items: [] }

  const [query, setQuery] = useState('')
  const [priceRange, setPriceRange] = useState('all')

  const filtered = useMemo(()=>{
    let arr = items || []
    if (query) arr = arr.filter(p => (p.nombre||'').toLowerCase().includes(query.toLowerCase()))
    if (priceRange !== 'all'){
      const inRange = (price) => {
        if (priceRange === '0-100') return price >= 0 && price <= 100
        if (priceRange === '100-200') return price > 100 && price <= 200
        if (priceRange === '200+') return price > 200
        return true
      }
      arr = arr.filter(p => inRange(Number(p.precio||0)))
    }
    return arr
  }, [items, query, priceRange])

  return (
    <div style={{padding:'2rem', maxWidth:1200, margin:'0 auto'}}>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'baseline'}}>
        <div>
          <h2 style={{margin:'0 0 4px'}}>Mis Favoritos</h2>
          <div style={{opacity:.7}}>{filtered.length} productos guardados</div>
        </div>
        {items?.length>0 && (
          <button onClick={clearFavorites} style={{border:'1px solid #eee', background:'#fff', borderRadius:8, padding:'6px 10px', cursor:'pointer'}}>Vaciar</button>
        )}
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1fr 260px', gap:12, margin:'12px 0'}}>
        <InputFloating label="Buscar en favoritos" value={query} onChange={e=>setQuery(e.target.value)} />
        <InputSelectFloating label="Precio" value={priceRange} onChange={e=>setPriceRange(e.target.value)}>
          <option value="all">Todos los precios</option>
          <option value="0-100">S/ 0 - S/ 100</option>
          <option value="100-200">S/ 100 - S/ 200</option>
          <option value="200+">S/ 200+</option>
        </InputSelectFloating>
      </div>

      {!items?.length && (
        <p style={{opacity:.8}}>Aún no tienes productos favoritos.</p>
      )}

      {!!filtered.length && (
        <div style={{display:'grid', gridTemplateColumns:'repeat(3, 1fr)', gap:16}}>
          {filtered.map(p => (
            <article key={p.id} style={{position:'relative', border:'1px solid #eee', borderRadius:10, overflow:'hidden', background:'#fff'}}>
              <button title="Quitar" onClick={()=>removeFavorite && removeFavorite(p.id)} style={{position:'absolute', right:8, top:8, width:24, height:24, borderRadius:'50%', border:'none', background:'#ef4444', color:'#fff', cursor:'pointer'}}>x</button>
              <Link to={`/producto/${p.id}`} style={{textDecoration:'none', color:'inherit'}}>
                {p.image_preview ? <img src={p.image_preview} alt={p.nombre} style={{width:'100%', height:180, objectFit:'cover'}} /> : <div style={{height:180, background:'#f5f5f5'}}/>}
                <div style={{padding:12}}>
                  <div style={{fontWeight:700}}>{p.nombre}</div>
                  <div style={{fontWeight:800}}>S/ {Number(p.precio||0).toFixed(2)}</div>
                  <div style={{fontSize:12, color:'#6b7280', marginTop:6}}>{p.sizes || 'S,M,L,XL'}</div>
                </div>
              </Link>
              <button title="Agregar al carrito" style={{position:'absolute', right:12, bottom:12, width:32, height:32, borderRadius:8, border:'none', background:'#111', color:'#fff', cursor:'pointer', fontWeight:800}}>+</button>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}
