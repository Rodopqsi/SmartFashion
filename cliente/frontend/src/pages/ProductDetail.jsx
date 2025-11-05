import React, { useEffect, useMemo, useState } from 'react'
import { useLocation, useParams, Link } from 'react-router-dom'
import './Home.css'
import { useAuth } from '../auth.jsx'
import { useCart } from '../cart.jsx'
import { useToast } from '../toast.jsx'
import { useFavorites } from '../favorites.jsx'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function ProductDetail(){
  const { id } = useParams()
  const location = useLocation()
  const { user, tokens } = useAuth() || {}
  const cart = useCart()
  const toast = useToast()
  const [product, setProduct] = useState(location.state?.product || null)
  const [catalog, setCatalog] = useState(null)
  const [sizes, setSizes] = useState([])
  const [colors, setColors] = useState([])
  const [selectedSize, setSelectedSize] = useState(null)
  const [selectedColor, setSelectedColor] = useState(null)
  const [images, setImages] = useState([])
  const [imagesByColor, setImagesByColor] = useState({})
  const [imagesByVariant, setImagesByVariant] = useState({})
  const [related, setRelated] = useState([])
  const [reviews, setReviews] = useState([])
  const [loadingReviews, setLoadingReviews] = useState(false)
  const [submittingReview, setSubmittingReview] = useState(false)
  const { isFavorite, toggleFavorite } = useFavorites() || {}
  const [qty, setQty] = useState(1)

  // Load from dedicated endpoint; fall back to /api/home only if needed
  useEffect(()=>{
    const load = async ()=>{
      try{
        const res = await fetch(`${API_BASE}/api/products/${id}/`)
        if (res.ok){
          const json = await res.json()
          const d = json.data || {}
          if (d.product) setProduct({ ...d.product, variants: d.variants || [] })
          setImages(Array.isArray(d.images)? d.images: [])
          setImagesByColor(d.imagesByColor || {})
          setImagesByVariant(d.imagesByVariant || {})
          setRelated(Array.isArray(d.related)? d.related: [])
        } else {
          // fallback minimal
          const j = await fetch(`${API_BASE}/api/home/`).then(r=>r.json()).catch(()=>null)
          if (j){
            setCatalog(j.data || {})
            const all = j?.data?.featured_products || []
            const found = all.find(p => String(p.id) === String(id))
            if (found) setProduct(found)
          }
        }
      }catch(_e){ /* ignore */ }

      fetch(`${API_BASE}/api/sizes/`).then(r=>r.json()).then(j=>setSizes(j.data||[])).catch(()=>{})
      fetch(`${API_BASE}/api/colors/`).then(r=>r.json()).then(j=>setColors(j.data||[])).catch(()=>{})
      // load reviews
      setLoadingReviews(true)
      fetch(`${API_BASE}/api/products/${id}/reviews/`).then(r=>r.json()).then(j=>setReviews(j.data||[])).catch(()=>{}).finally(()=>setLoadingReviews(false))
    }
    load()
  },[id])

  const gallery = useMemo(()=>{
    // Prefer images for the specific size+color if available
    if (selectedSize && selectedColor){
      const key = `${selectedSize}-${selectedColor}`
      const byVar = imagesByVariant[key]
      if (byVar && byVar.length) return byVar
    }
    if (selectedColor && imagesByColor && imagesByColor[String(selectedColor)]){
      return imagesByColor[String(selectedColor)]
    }
    if (images && images.length) return images
    return product?.image_preview ? [product.image_preview] : []
  }, [images, imagesByColor, imagesByVariant, selectedSize, selectedColor, product])

  const activeImage = gallery?.[0]

  // Build maps for sizes/colors and available combinations
  const sizeMap = useMemo(()=>{
    const m = new Map()
    for (const s of sizes) m.set(String(s.id), s)
    return m
  }, [sizes])
  const colorMap = useMemo(()=>{
    const m = new Map()
    for (const c of colors) m.set(String(c.id), c)
    return m
  }, [colors])

  const variants = useMemo(()=> product?.variants || [], [product])
  const availableSizeIds = useMemo(()=> Array.from(new Set(variants.map(v => String(v.size_id)))), [variants])
  const availableColorIds = useMemo(()=> Array.from(new Set(variants.map(v => String(v.color_id)))), [variants])

  const filteredColorsForSize = useMemo(()=>{
    if (!selectedSize) return availableColorIds
    const ids = variants.filter(v => String(v.size_id) === String(selectedSize)).map(v => String(v.color_id))
    return Array.from(new Set(ids))
  }, [variants, selectedSize, availableColorIds])

  const filteredSizesForColor = useMemo(()=>{
    if (!selectedColor) return availableSizeIds
    const ids = variants.filter(v => String(v.color_id) === String(selectedColor)).map(v => String(v.size_id))
    return Array.from(new Set(ids))
  }, [variants, selectedColor, availableSizeIds])

  const selectedStock = useMemo(()=>{
    if (!selectedSize || !selectedColor) return null
    const found = variants.find(v => String(v.size_id)===String(selectedSize) && String(v.color_id)===String(selectedColor))
    return found ? Number(found.stock) : 0
  }, [variants, selectedSize, selectedColor])

  if (!product){
    return <div style={{padding:'2rem'}}>Cargando producto...</div>
  }

  return (
    <div style={{maxWidth:1200, margin:'0 auto', padding:'16px 20px', paddingTop:'calc(var(--nav-height) + 12px)'}}>
      <nav style={{fontSize:14, marginBottom:12}}>
        <Link to="/catalogo">← Volver al catálogo</Link>
      </nav>

      <section style={{display:'grid', gridTemplateColumns:'520px 1fr', gap:24}}>
        {/* Left: images */}
        <div>
          <div style={{border:'1px solid #eee', borderRadius:12, overflow:'hidden'}}>
            {activeImage ? (
              <img src={activeImage} alt={product.nombre} style={{width:'100%', height:520, objectFit:'cover'}} />
            ) : (
              <div style={{width:'100%', height:520, background:'#f4f4f5'}} />
            )}
          </div>
          {/* Thumbs */}
          <div style={{display:'flex', gap:8, marginTop:8, flexWrap:'wrap'}}>
            {gallery.map((img, idx) => (
              <button key={idx} onClick={()=>{
                // Swap active gallery's first image to clicked thumb
                if (selectedSize && selectedColor){
                  const k = `${selectedSize}-${selectedColor}`
                  const src = imagesByVariant[k] ? [...imagesByVariant[k]] : []
                  const sel = src[idx]; src[idx] = src[0]; src[0] = sel
                  setImagesByVariant(prev => ({ ...prev, [k]: src }))
                } else if (selectedColor && imagesByColor[String(selectedColor)]){
                  const key = String(selectedColor)
                  const src = imagesByColor[key] ? [...imagesByColor[key]] : []
                  const sel = src[idx]; src[idx] = src[0]; src[0] = sel
                  setImagesByColor(prev => ({ ...prev, [key]: src }))
                } else {
                  const src = [...images]
                  const sel = src[idx]; src[idx] = src[0]; src[0] = sel
                  setImages(src)
                }
              }} style={{border:'1px solid #eee', borderRadius:8, overflow:'hidden', padding:0, width:80, height:80, background:'#fff'}}>
                <img src={img} alt={product.nombre+idx} style={{width:'100%', height:'100%', objectFit:'cover'}} />
              </button>
            ))}
          </div>
        </div>

        {/* Right: details */}
        <div>
          <h1 style={{marginTop:0, marginBottom:4}}>{product.nombre}</h1>
          <div style={{display:'flex', alignItems:'center', gap:8, color:'#666', fontSize:14, marginBottom:8}}>
            <Stars rating={product.rating || 4.6} />
            <span>({product.reviews_count || 24} reseñas)</span>
          </div>
          <p style={{color:'#374151'}}>{product.descripcion || 'Descripción no disponible.'}</p>

          <div style={{display:'flex', alignItems:'baseline', gap:8, margin:'12px 0'}}>
            <div style={{fontSize:24, fontWeight:800}}>S/ {Number(product.precio).toFixed(2)}</div>
            {product.precio_descuento && (
              <div style={{fontSize:16, color:'#16a34a'}}>S/ {Number(product.precio_descuento).toFixed(2)}</div>
            )}
          </div>

          {/* Size */}
          <div style={{marginTop:14}}>
            <div style={{fontWeight:600, marginBottom:6}}>Talla</div>
            <div style={{display:'flex', gap:8, flexWrap:'wrap'}}>
              {availableSizeIds.map(sid => {
                const s = sizeMap.get(String(sid))
                const disabled = selectedColor && !filteredSizesForColor.includes(String(sid))
                return (
                  <button key={sid} onClick={()=>!disabled && setSelectedSize(String(sid))} style={chip(selectedSize===String(sid), disabled)} disabled={disabled}>
                    {s?.nombre || sid}
                  </button>
                )
              })}
            </div>
          </div>

          {/* Color */}
          <div style={{marginTop:14}}>
            <div style={{fontWeight:600, marginBottom:6}}>Color {selectedColor && (
              <span style={{marginLeft:8, display:'inline-flex', alignItems:'center', gap:6, fontWeight:500}}>
                <span style={{display:'inline-block', width:14, height:14, borderRadius:'50%', border:'1px solid #ddd', background: colorMap.get(String(selectedColor))?.codigo_hex || '#000'}}/>
                <span style={{fontSize:12, color:'#374151'}}>{colorMap.get(String(selectedColor))?.nombre || selectedColor}</span>
              </span>
            )}</div>
            <div style={{display:'flex', gap:8, flexWrap:'wrap'}}>
              {availableColorIds.map(cid => {
                const c = colorMap.get(String(cid))
                const disabled = selectedSize && !filteredColorsForSize.includes(String(cid))
                return (
                  <button key={cid} onClick={()=>!disabled && setSelectedColor(String(cid))} title={c?.nombre || cid} style={colorDot(selectedColor===String(cid), disabled)} disabled={disabled}>
                    <span style={{display:'inline-block', width:18, height:18, borderRadius:'50%', background: c?.codigo_hex || '#000'}} />
                  </button>
                )
              })}
            </div>
          </div>

          {/* Qty + Add */}
          <div style={{display:'flex', alignItems:'center', gap:8, marginTop:18}}>
            <button style={qtyBtn} onClick={()=> setQty(q=> Math.max(1, q-1))}>-</button>
            <input readOnly value={qty} style={qtyInput}/>
            <button style={qtyBtn} onClick={()=> setQty(q=> Math.min(99, q+1))}>+</button>
            <button
              onClick={()=>{
                if (selectedStock===null){ toast?.push('Selecciona talla y color', 'info'); return }
                if (selectedStock===0){ toast?.push('Sin stock para esta combinación', 'error'); return }
                cart?.addItem(product, { size: { id: selectedSize, nombre: sizeMap.get(String(selectedSize))?.nombre }, color: { id: selectedColor, nombre: colorMap.get(String(selectedColor))?.nombre, codigo_hex: colorMap.get(String(selectedColor))?.codigo_hex }, qty })
                toast?.push('Producto agregado al carrito', 'success')
              }}
              style={{...ctaBtn, opacity: (selectedStock===0 || selectedStock===null)? .6: 1, cursor: (selectedStock===0 || selectedStock===null)? 'not-allowed':'pointer'}}
              disabled={selectedStock===0 || selectedStock===null}
            >Agregar al carrito</button>
            <button style={iconBtn} title={isFavorite?.(product.id)? 'Quitar de favoritos':'Añadir a favoritos'} onClick={()=> toggleFavorite && toggleFavorite(product)}>
              {isFavorite?.(product.id)? '❤️':'🤍'}
            </button>
          </div>
          {selectedStock !== null && (
            <div style={{marginTop:8, fontSize:12, color: selectedStock>0? '#16a34a':'#b91c1c'}}>
              {selectedStock>0? `${selectedStock} en stock` : 'Sin stock para esta combinación'} —
              <span style={{marginLeft:6}}>Talla: <b>{sizeMap.get(String(selectedSize))?.nombre || selectedSize}</b></span>
              <span style={{marginLeft:10}}>Color: <b>{colorMap.get(String(selectedColor))?.nombre || selectedColor}</b></span>
            </div>
          )}
        </div>
      </section>

      {/* Reviews carousel */}
      <section style={{marginTop:28}}>
        <h3 style={{margin:'12px 0'}}>Reseñas</h3>
        <ReviewsSection productId={product.id} reviews={reviews} loading={loadingReviews} onSubmit={async (payload)=>{
          const access = tokens?.access
          if (!access) return
          try{
            setSubmittingReview(true)
            await fetch(`${API_BASE}/api/products/${product.id}/reviews/`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${access}` },
              body: JSON.stringify(payload)
            })
            const j = await fetch(`${API_BASE}/api/products/${product.id}/reviews/`).then(r=>r.json())
            setReviews(j.data||[])
          } finally{
            setSubmittingReview(false)
          }
        }} submitting={submittingReview} />
      </section>

      {/* Related */}
      <section style={{marginTop:28}}>
        <h3 style={{margin:'12px 0'}}>Productos relacionados</h3>
        <div style={{display:'grid', gridTemplateColumns:'repeat(4, 1fr)', gap:12}}>
          {related.map(r => (
            <Link key={r.id} to={`/producto/${r.id}`} state={{product:r}} style={{textDecoration:'none', color:'inherit'}}>
              <article style={{border:'1px solid #eee', borderRadius:10, overflow:'hidden', background:'#fff'}}>
                {r.image_preview ? <img src={r.image_preview} alt={r.nombre} style={{width:'100%', height:160, objectFit:'cover'}} /> : <div style={{height:160, background:'#f5f5f5'}}/>}
                <div style={{padding:10}}>
                  <div style={{fontWeight:700}}>{r.nombre}</div>
                  <div style={{fontWeight:800}}>S/ {Number(r.precio).toFixed(2)}</div>
                </div>
              </article>
            </Link>
          ))}
          {!related.length && <div style={{color:'#666'}}>No hay relacionados.</div>}
        </div>
      </section>
    </div>
  )
}

function Stars({rating=4.5}){
  const full = Math.floor(rating)
  const half = rating - full >= 0.5
  const arr = new Array(5).fill(0).map((_,i)=> i < full ? '★' : i===full && half ? '☆' : '☆')
  return <div style={{color:'#f59e0b', fontSize:18, letterSpacing:1}}>{arr.join(' ')}</div>
}

function ReviewsSection({ productId, reviews = [], loading, onSubmit, submitting }){
  const [page, setPage] = useState(0)
  const per = 2
  const max = Math.max(1, Math.ceil((reviews?.length || 0) / per))
  const slice = reviews.slice(page*per, page*per + per)

  const [rating, setRating] = useState(5)
  const [text, setText] = useState('')
  const { user } = useAuth() || {}
  return (
    <div style={{position:'relative', border:'1px solid #eee', borderRadius:12, padding:'12px 44px'}}>
      <button onClick={()=>setPage(p=> Math.max(0, p-1))} style={carouselArrow} aria-label="Anterior">‹</button>
      <button onClick={()=>setPage(p=> Math.min(max-1, p+1))} style={{...carouselArrow, right:8, left:'auto'}} aria-label="Siguiente">›</button>
      {loading ? (
        <div style={{padding:12}}>Cargando reseñas...</div>
      ) : (
        <>
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
            {slice.map(r => (
              <div key={r.id} style={{background:'#fff', border:'1px solid #eee', borderRadius:10, padding:12}}>
                <div style={{fontWeight:600, marginBottom:6}}>{r.user}</div>
                <div style={{color:'#f59e0b', marginBottom:4}}>{'★'.repeat(r.rating||0)}</div>
                <div style={{color:'#374151'}}>{r.text}</div>
              </div>
            ))}
            {!slice.length && <div style={{color:'#666'}}>Sé el primero en opinar.</div>}
          </div>
          <div style={{textAlign:'center', marginTop:8, fontSize:12, color:'#666'}}>Página {page+1} de {max}</div>
        </>
      )}

      {user && (
        <div style={{marginTop:16, borderTop:'1px solid #eee', paddingTop:12}}>
          <div style={{fontWeight:600, marginBottom:6}}>Escribe una reseña</div>
          <div style={{display:'flex', gap:8, alignItems:'center', marginBottom:8}}>
            <label>Calificación:</label>
            <select value={rating} onChange={e=>setRating(Number(e.target.value))}>
              {[5,4,3,2,1].map(v=> <option key={v} value={v}>{v} ★</option>)}
            </select>
          </div>
          <textarea value={text} onChange={e=>setText(e.target.value)} placeholder="Tu opinión" rows={3} style={{width:'100%', border:'1px solid #ddd', borderRadius:8, padding:8}} />
          <div style={{marginTop:8}}>
            <button disabled={submitting || !text.trim()} onClick={()=> onSubmit && onSubmit({ rating, text })} style={{...ctaBtn, opacity: submitting? .6: 1}}>
              {submitting? 'Enviando...' : 'Enviar reseña'}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

// styles
const chip = (active, disabled)=>({border:'1px solid '+(active?'#111':'#d1d5db'), background:active?'#111':'#fff', color:active?'#fff':'#111', padding:'6px 10px', borderRadius:10, cursor: disabled? 'not-allowed':'pointer', opacity: disabled? .5: 1})
const colorDot = (active, disabled)=>({border:'1px solid '+(active?'#111':'#e5e7eb'), background:'#fff', borderRadius:10, padding:6, cursor: disabled? 'not-allowed':'pointer', opacity: disabled? .5: 1})
const qtyBtn = {border:'1px solid #e5e7eb', width:34, height:34, borderRadius:8, background:'#fff'}
const qtyInput = {width:44, textAlign:'center', border:'1px solid #e5e7eb', height:34, borderRadius:8}
const ctaBtn = {padding:'10px 16px', border:'none', background:'#111', color:'#fff', borderRadius:10, fontWeight:700, cursor:'pointer'}
const iconBtn = {padding:'8px 10px', border:'1px solid #eee', background:'#fff', borderRadius:10, cursor:'pointer'}
const carouselArrow = {position:'absolute', left:8, top:'50%', transform:'translateY(-50%)', width:32, height:32, borderRadius:'50%', border:'1px solid #e5e7eb', background:'#fff'}
