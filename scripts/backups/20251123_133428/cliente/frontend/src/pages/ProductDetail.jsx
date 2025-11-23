import React, { useEffect, useMemo, useState } from 'react'
import { useLocation, useParams, Link } from 'react-router-dom'
import './Home.css'
import { useAuth } from '../auth.jsx'
import { useCart } from '../cart.jsx'
import { useToast } from '../toast.jsx'
import { useFavorites } from '../favorites.jsx'
import RatingStars from '../components/RatingStars.jsx'

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
  const [manualImage, setManualImage] = useState(null)
  const [related, setRelated] = useState([])
  const [reviews, setReviews] = useState([])
  const [loadingReviews, setLoadingReviews] = useState(false)
  const [submittingReview, setSubmittingReview] = useState(false)
  const { isFavorite, toggleFavorite } = useFavorites() || {}
  const [qty, setQty] = useState(1)
  const [variantWarning, setVariantWarning] = useState('')

  // Average rating derived from real reviews; fallback to product's rating
  const averageRating = useMemo(()=>{
    if (Array.isArray(reviews) && reviews.length){
      const sum = reviews.reduce((acc, r)=> acc + (Number(r.rating)||0), 0)
      return sum / reviews.length
    }
    return Number(product?.rating) || 0
  }, [reviews, product])

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

  const activeImage = manualImage || ((!selectedSize && !selectedColor && product?.image_preview) ? product.image_preview : gallery?.[0])

  // Build ALL images (base + byColor + byVariant) for the thumbs carousel
  const allImages = useMemo(()=>{
    const out = []
    const seen = new Set()
    const push = (url)=>{ if (url && !seen.has(url)) { seen.add(url); out.push(url) } }
    // Base
    if (Array.isArray(images)) images.forEach(push)
    if (product?.image_preview) push(product.image_preview)
    // By color
    if (imagesByColor && typeof imagesByColor === 'object'){
      Object.values(imagesByColor).forEach(arr => Array.isArray(arr) && arr.forEach(push))
    }
    // By variant
    if (imagesByVariant && typeof imagesByVariant === 'object'){
      Object.values(imagesByVariant).forEach(arr => Array.isArray(arr) && arr.forEach(push))
    }
    return out
  }, [images, imagesByColor, imagesByVariant, product])

  // Respect rule: when no size/color selected, revert to original (clear manual override)
  useEffect(()=>{
    if (!selectedSize && !selectedColor) setManualImage(null)
  }, [selectedSize, selectedColor])
  useEffect(()=>{ setManualImage(null) }, [id])

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
    <div className="product-page" style={{maxWidth:1200, margin:'0 auto', padding:'16px 20px', paddingTop:'calc(var(--nav-height) + 12px)'}}>
      <nav style={{fontSize:14, marginBottom:12}}>
        <Link to="/catalogo">← Volver al catálogo</Link>
      </nav>

      <section style={{display:'grid', gridTemplateColumns:'520px 1fr', gap:24}}>
        {/* Left: images */}
        <div className="product-gallery">
          <div className="main-image-wrapper" style={{borderRadius:12, overflow:'hidden'}}>
            {activeImage ? (
              <img className="main-image" src={activeImage} alt={product.nombre} style={{width:'100%', height:520, objectFit:'cover'}} />
            ) : (
              <div className="main-image-fallback" style={{width:'100%', height:520, background:'#f4f4f5'}} />
            )}
          </div>
          {/* Thumbs: show ALL images with horizontal scroll */}
          <div className="thumbs-row" style={{display:'flex', gap:8, marginTop:8, flexWrap:'nowrap', overflowX:'auto', paddingBottom:4}}>
            {allImages.map((img, idx) => {
              const isActive = img === activeImage
              return (
                <button key={idx} className={`thumb-btn ${isActive? 'active':''}`} onClick={()=>{
                  setManualImage(prev => prev === img ? null : img)
                }} style={{padding:0, minWidth:80, width:80, height:80}}>
                  <img className="thumb-img" src={img} alt={product.nombre+idx} style={{width:'100%', height:'100%', objectFit:'cover'}} />
                </button>
              )
            })}
          </div>
        </div>

        {/* Right: details */}
        <div className="product-details">
          <h1 style={{marginTop:0, marginBottom:4}}>{product.nombre}</h1>
          <div style={{display:'flex', alignItems:'center', gap:8, color:'#666', fontSize:14, marginBottom:8}}>
            <RatingStars value={averageRating} readOnly />
            <span>({Array.isArray(reviews)? reviews.length : (product.reviews_count || 0)} reseñas)</span>
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
                  <button key={sid} onClick={()=>{
                    if (disabled) return
                    // toggle off if already selected
                    setSelectedSize(prev => (String(prev) === String(sid) ? null : String(sid)))
                  }} style={chip(selectedSize===String(sid), disabled)} disabled={disabled}>
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
                  <button key={cid} onClick={()=>{
                    if (disabled) return
                    setSelectedColor(prev => (String(prev) === String(cid) ? null : String(cid)))
                  }} title={c?.nombre || cid} style={colorDot(selectedColor===String(cid), disabled)} disabled={disabled}>
                    <span style={{display:'inline-block', width:18, height:18, borderRadius:'50%', background: c?.codigo_hex || '#000'}} />
                  </button>
                )
              })}
            </div>
            {(selectedSize || selectedColor) && (
              <div style={{marginTop:8}}>
                <button onClick={()=>{ setSelectedSize(null); setSelectedColor(null) }} style={clearMiniBtn}>Quitar selección</button>
              </div>
            )}
          </div>

          {/* Qty + Add */}
          <div style={{display:'flex', alignItems:'center', gap:8, marginTop:18, flexWrap:'wrap'}}>
            <button style={qtyBtn} onClick={()=> setQty(q=> Math.max(1, q-1))}>-</button>
            <input readOnly value={qty} style={qtyInput}/>
            <button style={qtyBtn} onClick={()=> setQty(q=> Math.min(99, q+1))}>+</button>
            {selectedStock === 0 ? (
              <button style={{...ctaBtn, background:'#9ca3af', cursor:'not-allowed'}} disabled>Producto agotado</button>
            ) : (
              <>
                {variantWarning && (
                  <div style={{color:'#b91c1c', background:'#fee2e2', border:'1px solid #fca5a5', borderRadius:8, padding:'8px 10px', marginBottom:8, fontSize:13, textAlign:'center'}}>
                    {variantWarning}
                  </div>
                )}
                <button
                  onClick={()=>{
                    if (selectedStock===null){
                      if (!selectedSize && !selectedColor) setVariantWarning('Por favor selecciona talla y color');
                      else if (!selectedSize) setVariantWarning('Por favor selecciona una talla');
                      else if (!selectedColor) setVariantWarning('Por favor selecciona un color');
                      else setVariantWarning('Selecciona talla y color');
                      return;
                    }
                    setVariantWarning('');
                    if (selectedStock===0){ toast?.push('Sin stock para esta combinación', 'error'); return }
                    cart?.addItem(product, { size: { id: selectedSize, nombre: sizeMap.get(String(selectedSize))?.nombre }, color: { id: selectedColor, nombre: colorMap.get(String(selectedColor))?.nombre, codigo_hex: colorMap.get(String(selectedColor))?.codigo_hex }, qty })
                    toast?.push('Producto agregado al carrito', 'success')
                  }}
                  style={{...ctaBtn, opacity: (selectedStock===null)? .6: 1, cursor: (selectedStock===null)? 'not-allowed':'pointer'}}
                >Agregar al carrito</button>
              </>
            )}
            <button style={iconBtn} title={isFavorite?.(product.id)? 'Quitar de favoritos':'Añadir a favoritos'} onClick={()=> toggleFavorite && toggleFavorite(product)}>
              {isFavorite?.(product.id)? '❤️':'🤍'}
            </button>

            {/* Stock badge */}
            {selectedStock !== null && (
              <span style={stockBadge(selectedStock)}>
                <span style={{display:'inline-block', width:8, height:8, borderRadius:'50%', background: selectedStock>0? '#16a34a':'#b91c1c', marginRight:6}}></span>
                {selectedStock>0? `Quedan ${selectedStock}` : 'Agotado'}
              </span>
            )}
          </div>
          {/* Variant summary */}
          {(selectedSize || selectedColor) && (
            <div style={{marginTop:8, fontSize:12, color:'#374151'}}>
              {selectedSize && (<span>Talla: <b>{sizeMap.get(String(selectedSize))?.nombre || selectedSize}</b></span>)}
              {selectedColor && (<span style={{marginLeft:10}}>Color: <b>{colorMap.get(String(selectedColor))?.nombre || selectedColor}</b></span>)}
            </div>
          )}
        </div>
      </section>

      {/* Reviews */}
      <section style={{marginTop:28}}>
        <h3 style={{margin:'12px 0'}}>Reseñas</h3>
        <ReviewsSection className="reviews-section" productId={product.id} reviews={reviews} loading={loadingReviews} onSubmit={async (payload)=>{
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

      {/* Related carousel */}
      <section style={{marginTop:28}}>
        <h3 style={{margin:'12px 0'}}>Productos relacionados</h3>
        {related?.length ? (
          <Carousel>
            {related.map(r => (
              <Link key={r.id} to={`/producto/${r.id}`} state={{product:r}} style={{textDecoration:'none', color:'inherit', display:'block', minWidth:220}}>
                <article style={{border:'1px solid #eee', borderRadius:10, overflow:'hidden', background:'#fff'}}>
                  {r.image_preview ? <img src={r.image_preview} alt={r.nombre} style={{width:'100%', height:160, objectFit:'cover'}} /> : <div style={{height:160, background:'#f5f5f5'}}/>}
                  <div style={{padding:10}}>
                    <div style={{fontWeight:700}}>{r.nombre}</div>
                    <div style={{fontWeight:800}}>S/ {Number(r.precio).toFixed(2)}</div>
                  </div>
                </article>
              </Link>
            ))}
          </Carousel>
        ) : (
          <div style={{color:'#666'}}>No hay relacionados.</div>
        )}
      </section>
    </div>
  )
}

// Old Stars component removed; RatingStars readOnly is used instead

function ReviewsSection({ productId, reviews = [], loading, onSubmit, submitting }){
  const [expanded, setExpanded] = useState({})
  const [rating, setRating] = useState(5)
  const [text, setText] = useState('')
  const { user } = useAuth() || {}

  return (
    <div className={`reviews-root ${typeof className !== 'undefined' ? className : ''}`} style={{ border:'1px solid #eee', borderRadius:12, padding:12 }}>
      {loading ? (
        <div style={{padding:12}}>Cargando reseñas...</div>
      ) : (
        <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
          {reviews.map(r => {
            const isOpen = !!expanded[r.id]
            const textShort = (r.text || '').slice(0, 160)
            const needMore = (r.text || '').length > 160
            return (
              <div key={r.id} className="review-item" style={{ background:'#fff', border:'1px solid #eee', borderRadius:10, padding:12 }}>
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                  <div style={{ fontWeight:600 }}>{r.user}</div>
                  <RatingStars value={r.rating || 0} readOnly />
                </div>
                <div style={{ color:'#374151', marginTop:6 }}>
                  {isOpen ? (r.text || '') : textShort}
                  {needMore && !isOpen && '...'}
                </div>
                {needMore && (
                  <button onClick={()=> setExpanded(prev => ({ ...prev, [r.id]: !isOpen }))} style={clearMiniBtn}>
                    {isOpen ? 'Ver menos' : 'Ver más'}
                  </button>
                )}
              </div>
            )
          })}
          {!reviews.length && <div style={{color:'#666'}}>Sé el primero en opinar.</div>}
        </div>
      )}

      {user && (
        <div className="review-form" style={{marginTop:16, borderTop:'1px solid #eee', paddingTop:12}}>
          <div style={{fontWeight:600, marginBottom:6}}>Escribe una reseña</div>
          <RatingStars value={rating} onChange={setRating} name={`rate-${productId}`} />
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

function Carousel({ children }){
  const [ref, setRef] = useState(null)
  return (
    <div style={{ position:'relative' }}>
      <div ref={setRef} style={{ display:'flex', gap:12, overflowX:'auto', scrollBehavior:'smooth', padding:'4px 0' }}>
        {children}
      </div>
      <button onClick={()=> ref && ref.scrollBy({ left: -300, behavior: 'smooth' })} style={carouselArrow} aria-label="Anterior">‹</button>
      <button onClick={()=> ref && ref.scrollBy({ left: 300, behavior: 'smooth' })} style={{...carouselArrow, right:8, left:'auto'}} aria-label="Siguiente">›</button>
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
const clearMiniBtn = { border:'none', background:'transparent', color:'#2563eb', cursor:'pointer', fontSize:13, padding:0, marginTop:6 }
const stockBadge = (stock)=>({
  display:'inline-flex', alignItems:'center', gap:6,
  background:'#f1f5f9', color:'#0f172a',
  borderRadius:9999, padding:'6px 10px', fontSize:12,
  border:'1px solid ' + (stock>0 ? '#16a34a33' : '#b91c1c33')
})
