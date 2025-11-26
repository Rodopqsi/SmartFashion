import InputSelectFloating from '../components/InputSelectFloating.jsx'
import InputFloating from '../components/InputFloating.jsx'
import { useEffect, useMemo, useState } from 'react'
import { useParams, Link } from 'react-router-dom'

export default function Collection(){
  const { slug } = useParams()
  const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [filters, setFilters] = useState({ size: '', color: '', q: '', min_price: '', max_price: '' })
  const [page, setPage] = useState(1)
  const [accum, setAccum] = useState([])

  const query = useMemo(() => {
    const params = new URLSearchParams()
    if (filters.size) params.set('size', filters.size)
    if (filters.color) params.set('color', filters.color)
    if (filters.q) params.set('q', filters.q)
    if (filters.min_price) params.set('min_price', filters.min_price)
    if (filters.max_price) params.set('max_price', filters.max_price)
    params.set('page', String(page))
    params.set('limit', '12')
    return params.toString()
  }, [filters, page])

  useEffect(() => {
    let alive = true
    setLoading(true)
    fetch(`${API_BASE}/api/collections/${slug}/?${query}`)
      .then(r => r.json())
      .then(j => {
        if (!alive) return
        if (j.status !== 'ok') throw new Error('bad status')
        setData(j.data)
        setError(null)
        setLoading(false)
        if (page === 1) setAccum(j.data.products)
        else setAccum(prev => [...prev, ...j.data.products])
      })
      .catch(e => { if (!alive) return; console.error(e); setError('No se pudo cargar la colección'); setLoading(false) })
    return () => { alive = false }
  }, [API_BASE, slug, query])

  function applyFilters(next){
    setPage(1)
    setAccum([])
    setFilters(prev => ({ ...prev, ...next }))
  }

  const col = data?.collection
  const available = data?.filters || { sizes: [], colors: [] }
  const total = data?.pagination?.total || 0
  const canLoadMore = accum.length < total

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: 16, marginTop: 110 }}>
      {col?.image_url && (
        <div className="collection-hero" style={{ borderRadius: 12, overflow:'hidden', marginBottom: 16 }}>
          <img src={col.image_url} alt={col.nombre} style={{ width:'100%', height: 360, objectFit:'cover', display:'block' }} />
        </div>
      )}
      <div style={{ display:'flex', alignItems:'baseline', justifyContent:'space-between', gap: 12 }}>
        <h1 style={{ fontSize: 22, fontWeight: 800, margin: 0 }}>{col?.nombre || 'Colección'}</h1>
        {col?.descripcion && <p style={{ margin: 0, color:'#666' }}>{col.descripcion}</p>}
      </div>

      {}
      <div className="filters-grid">
        <InputSelectFloating label="Talla" value={filters.size} onChange={e => applyFilters({ size: e.target.value })}>
          <option value=""></option>
          {available.sizes?.map(s => (
            <option key={s.id} value={s.id}>{s.nombre}</option>
          ))}
        </InputSelectFloating>
        <InputSelectFloating label="Color" value={filters.color} onChange={e => applyFilters({ color: e.target.value })}>
          <option value=""></option>
          {available.colors?.map(c => (
            <option key={c.id} value={c.id}>{c.nombre}</option>
          ))}
        </InputSelectFloating>
        <InputFloating label="Buscar" value={filters.q} onChange={e => applyFilters({ q: e.target.value })} />
        <InputFloating label="Min S/" type="number" value={filters.min_price} onChange={e => applyFilters({ min_price: e.target.value })} />
        <InputFloating label="Max S/" type="number" value={filters.max_price} onChange={e => applyFilters({ max_price: e.target.value })} />
      </div>

      {}
      {loading && page === 1 && <div style={{ padding:10 }}>Cargando...</div>}
      {error && <div style={{ padding:10, color:'red' }}>{error}</div>}

      <div className="products-grid">
        {accum.map(p => (
          <Link key={p.id} to={`/producto/${p.id}`} state={{ product: p }} style={{ textDecoration:'none', color:'inherit' }}>
            <article className="product-card">
                {p.image_preview ? (
                  <img src={p.image_preview} alt={p.nombre} style={{ width:'100%', height: 160, objectFit:'cover' }} />
                ) : (
                  <div className="product-card-img-fallback" style={{ height:160 }} />
                )}
                <div className="product-card-body">
                  <div style={{ fontWeight: 700, marginBottom: 6, fontSize: 14 }}>{p.nombre}</div>
                  <div style={{ display:'flex', gap:8, alignItems:'baseline' }}>
                    <span style={{ fontSize: 16, fontWeight: 800 }}>S/ {Number(p.precio).toFixed(2)}</span>
                    {p.precio_descuento && (
                      <span className="product-card-price-discount">S/ {Number(p.precio_descuento).toFixed(2)}</span>
                    )}
                  </div>
                </div>
              </article>
          </Link>
        ))}
      </div>

      {}
      <div style={{ display:'flex', justifyContent:'center', padding: 16 }}>
        {canLoadMore && (
          <button className="btn" onClick={() => setPage(p => p + 1)} disabled={loading}>
            {loading ? 'Cargando...' : 'Ver más'}
          </button>
        )}
        {!canLoadMore && accum.length > 0 && <div style={{ color:'#666' }}>No hay más productos.</div>}
      </div>
    </div>
  )
}
