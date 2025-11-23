import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import './Home.css'

export default function Home(){
  const [slides, setSlides] = useState([])
  const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const location = useLocation()
  const [active, setActive] = useState(0)

  const scrollToId = (id) => {
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior:'smooth', block:'start' })
  }

  useEffect(() => {
    let alive = true
    setLoading(true)
    fetch(`${API_BASE}/api/home/`)
      .then(r => r.json())
      .then(j => { if (!alive) return; setData(j.data || {}); setError(null); setLoading(false) })
      .catch(e => { if (!alive) return; console.error(e); setError('No se pudo cargar el home'); setLoading(false) })
    return () => { alive = false }
  }, [])

  // Load slides.json from public (fallback to a single sample if missing)
  useEffect(()=>{
    let mounted = true
    fetch('/slides.json', { cache:'no-cache' })
      .then(r => r.ok ? r.json() : Promise.reject(new Error('no slides')))
      .then(arr => {
        if (!mounted) return
        if (Array.isArray(arr) && arr.length) setSlides(arr)
        else setSlides([{ src:'/img/fondonuevo.jpg', href:'/catalogo' }])
      })
      .catch(()=>{ if (mounted) setSlides([{ src:'/img/fondonuevo.jpg', href:'/catalogo' }]) })
    return ()=> { mounted = false }
  }, [])

  // If navigated with a hash (e.g., /#collections), scroll on mount
  useEffect(()=>{
    if (location.hash) {
      const id = location.hash.replace('#','')
      setTimeout(()=>scrollToId(id), 80)
    }
  }, [location.hash])

  // Reveal on scroll
  useEffect(()=>{
    const nodes = Array.from(document.querySelectorAll('.reveal'))
    if (!nodes.length) return
    const io = new IntersectionObserver((entries)=>{
      entries.forEach(e=>{
        if (e.isIntersecting) e.target.classList.add('visible')
      })
    }, { threshold: 0.15 })
    nodes.forEach(n=> io.observe(n))
    return ()=> io.disconnect()
  }, [loading])

  // Hero slider auto-advance (only if more than one image)
  useEffect(()=>{
    if (!slides || slides.length <= 1) return
    const id = setInterval(()=> setActive(a => (a+1) % slides.length), 6000)
    return ()=> clearInterval(id)
  }, [slides])

  const prevSlide = () => setActive(a => (a - 1 + slides.length) % slides.length)
  const nextSlide = () => setActive(a => (a + 1) % slides.length)

  const collections = data?.collections || []
  const featured = data?.featured_products || []

  return (
    <div className="home-root">
      <header className="hero-full">
        <div className="hero-slider">
          {slides.map((s, i)=> {
            const style = { backgroundImage:`url(${s.src})` }
            const node = <div className={`hero-slide ${i===active ? 'active' : ''}`} style={style} />
            return s.href ? (
              <a key={i} href={s.href} className="hero-slide-link" aria-label={s.alt || `Banner ${i+1}`}>{node}</a>
            ) : (
              <div key={i}>{node}</div>
            )
          })}
        </div>
        {slides.length > 1 && (
          <>
            <div className="hero-arrows">
              <button className="arrow prev" onClick={prevSlide} aria-label="Anterior">‹</button>
              <button className="arrow next" onClick={nextSlide} aria-label="Siguiente">›</button>
            </div>
            <div className="hero-dots">
              {slides.map((_, i)=> (
                <button key={i} className={`dot ${i===active ? 'on' : ''}`} onClick={()=> setActive(i)} aria-label={`Slide ${i+1}`} />
              ))}
            </div>
          </>
        )}
        <a href="#collections" onClick={(e)=>{e.preventDefault();scrollToId('collections');}} className="shop-now-btn">Explorar</a>
      </header>

      {/* New Arrivals carousel */}
      <section id="new-arrivals" className="section">
        <h2>Novedades</h2>
        {loading && <div style={{ padding: 10 }}>Cargando...</div>}
        {error && <div style={{ padding: 10, color: 'red' }}>{error}</div>}
        {!loading && !error && (
          <div className="h-scroll reveal" id="new-arrivals-scroll">
            {featured.map(p => (
              <Link key={p.id} to={`/producto/${p.id}`} className="snap-item" style={{ textDecoration:'none', color:'inherit', width:220 }}>
                <div className="card">
                  {p.image_preview ? (
                    <img src={p.image_preview} alt={p.nombre} />
                  ) : (
                    <div style={{ width:'100%', height:180, background:'#f0f0f0' }} />
                  )}
                  <div className="body">
                    <div style={{ fontWeight:700, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>{p.nombre}</div>
                    <div className="price">S/ {Number(p.precio).toFixed(2)}</div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* Collections section as tiles */}
      <section id="collections" className="section">
        <h2>Colecciones</h2>
        {loading && <div style={{ padding: 10 }}>Cargando...</div>}
        {error && <div style={{ padding: 10, color: 'red' }}>{error}</div>}
        {!loading && !error && (!collections.length ? (
          <div style={{ color:'#666' }}>No hay colecciones disponibles.</div>
        ) : (
          <div style={{ display:'grid', gridTemplateColumns:'repeat(4, 1fr)', gap:16 }} className="reveal">
            {collections.map(col => (
              <Link key={col.id} to={`/colecciones/${col.slug}`} style={{ textDecoration:'none', color:'inherit' }}>
                <div style={{ position:'relative', borderRadius:12, overflow:'hidden', background:'#f8f8f8', minHeight:200, border:'1px solid #eee' }}>
                  {col.image_url ? (
                    <img src={col.image_url} alt={col.nombre} style={{ width:'100%', height:220, objectFit:'cover', display:'block' }} />
                  ) : (
                    <div style={{ width:'100%', height:220, background:'#f0f0f0' }} />
                  )}
                  <div style={{ position:'absolute', left:0, right:0, bottom:0, padding:'10px 12px', background:'linear-gradient(transparent, rgba(0,0,0,0.7))', color:'#fff' }}>
                    <div style={{ fontWeight:800, letterSpacing:0.3 }}>{col.nombre}</div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        ))}
      </section>

      {/* Removed Sale/Testimonials/Newsletter to avoid static claims and duplication */}
    </div>
  )
}
