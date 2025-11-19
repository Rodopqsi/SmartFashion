import { useEffect, useMemo, useRef, useState } from 'react'
import './Home.css'
import './CatalogResponsive.css'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useFavorites } from '../favorites.jsx'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function Catalog() {
  const navigate = useNavigate()
  const location = useLocation()
  const { isFavorite, toggleFavorite } = useFavorites() || {}
  // Remote data
  const [data, setData] = useState(null)
  const [sizes, setSizes] = useState([])
  const [colors, setColors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Filters/search
  const [selectedCategory, setSelectedCategory] = useState(null)
  const [query, setQuery] = useState('')
  // typing spinner removed
  const [selectedSize, setSelectedSize] = useState(null)
  const [selectedColor, setSelectedColor] = useState(null)
  const [priceRange, setPriceRange] = useState('all') // 'all' | '0-100' | '100-200' | '200+'
  const debounceRef = useRef(null)
  const [page, setPage] = useState(1)
  const [limit, setLimit] = useState(12)

  const fetchHome = (cat, qVal, size, color, pageVal, limitVal, { showSpinner = true } = {}) => {
    if (showSpinner) setLoading(true)
    const params = new URLSearchParams()
    if (cat) params.append('category_id', cat)
    if (qVal) params.append('q', qVal)
    if (size) params.append('size', size)
    if (color) params.append('color', color)
    if (pageVal) params.append('page', pageVal)
    if (limitVal) params.append('limit', limitVal)
    fetch(`${API_BASE}/api/home/${params.toString() ? `?${params.toString()}` : ''}`)
      .then(res => res.json())
      .then(json => { setData(json.data); setLoading(false); setError(null) })
      .catch(err => { console.error(err); setError('No se pudo cargar el catálogo'); setLoading(false) })
  }

  // Initial fetch and filter changes (except query which is debounced)
  useEffect(() => {
    fetchHome(selectedCategory, query, selectedSize, selectedColor, page, limit)
  }, [selectedCategory, selectedSize, selectedColor, page, limit])

  // Debounced search
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      setPage(1)
      fetchHome(selectedCategory, query, selectedSize, selectedColor, 1, limit, { showSpinner: false })
    }, 350)
    return () => clearTimeout(debounceRef.current)
  }, [query])

  // Accept query from URL (?q=...)
  useEffect(() => {
    const qParam = new URLSearchParams(location.search).get('q') || ''
    setQuery(qParam)
    setPage(1)
  }, [location.search])

  // Fetch facet lists
  useEffect(() => {
    fetch(`${API_BASE}/api/sizes/`).then(r => r.json()).then(j => setSizes(j.data || [])).catch(() => {})
    fetch(`${API_BASE}/api/colors/`).then(r => r.json()).then(j => setColors(j.data || [])).catch(() => {})
  }, [])

  const categories = data?.categories || []
  const allProducts = data?.featured_products || []
  const pagination = data?.pagination || { page, limit, total: allProducts.length }
  const totalPages = Math.max(1, Math.ceil((pagination.total || 0) / (pagination.limit || limit)))
  const startIdx = pagination.total ? ((pagination.page || page) - 1) * (pagination.limit || limit) + 1 : 0
  const endIdx = pagination.total ? Math.min(pagination.total, startIdx + (allProducts?.length || 0) - 1) : 0

  // Client-side price filter
  const products = useMemo(() => {
    if (!allProducts?.length) return []
    if (priceRange === 'all') return allProducts
    const inRange = (price) => {
      if (priceRange === '0-100') return price >= 0 && price <= 100
      if (priceRange === '100-200') return price > 100 && price <= 200
      if (priceRange === '200+') return price > 200
      return true
    }
    return allProducts.filter(p => inRange(Number(p.precio)))
  }, [allProducts, priceRange])

  const clearAll = () => {
    setSelectedCategory(null)
    setSelectedSize(null)
    setSelectedColor(null)
    setPriceRange('all')
    setQuery('')
  }

  return (
    <div className="catalog-page">
      <div className="catalog-top">
        <div style={{ position: 'relative', flex: 1, maxWidth: 520 }}>
          <input
            placeholder="Buscar productos..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            style={searchInputStyle}
          />
        </div>
      </div>

      <div className="catalog-layout">
        {/* Left filters */}
        <aside style={leftColStyle}>
          <div style={filtersGroup}>
            <div style={filtersTitle}>Filtros</div>
            <div style={sectionTitle}>Categorías</div>
            <div style={chipsWrap}>
              <Chip active={selectedCategory === null} onClick={() => setSelectedCategory(null)}>Todas</Chip>
              {categories.map(c => (
                <Chip key={c.id} active={selectedCategory === c.id} onClick={() => setSelectedCategory(c.id)}>{c.nombre}</Chip>
              ))}
            </div>

            <div style={sectionTitle}>Tallas</div>
            <div style={chipsWrap}>
              <Chip active={selectedSize === null} onClick={() => setSelectedSize(null)}>Todas</Chip>
              {sizes.map(s => (
                <Chip key={s.id} active={selectedSize === s.id} onClick={() => setSelectedSize(s.id)}>{s.nombre}</Chip>
              ))}
            </div>

            <div style={sectionTitle}>Colores</div>
            <div style={chipsWrap}>
              <Chip active={selectedColor === null} onClick={() => setSelectedColor(null)}>Todos</Chip>
              {colors.map(c => (
                <Chip key={c.id} active={selectedColor === c.id} onClick={() => setSelectedColor(c.id)}>{c.nombre}</Chip>
              ))}
            </div>

            <div style={sectionTitle}>Precios</div>
            <div style={chipsWrap}>
              <Chip active={priceRange === 'all'} onClick={() => setPriceRange('all')}>Todos los precios</Chip>
              <Chip active={priceRange === '0-100'} onClick={() => setPriceRange('0-100')}>S/ 0 - S/ 100</Chip>
              <Chip active={priceRange === '100-200'} onClick={() => setPriceRange('100-200')}>S/ 100 - S/ 200</Chip>
              <Chip active={priceRange === '200+'} onClick={() => setPriceRange('200+')}>S/ 200+</Chip>
            </div>

            {(query || selectedSize || selectedColor || selectedCategory !== null || priceRange !== 'all') && (
              <button onClick={clearAll} style={clearBtnStyle}>Limpiar filtros</button>
            )}
          </div>
        </aside>

        {/* Right grid */}
        <main style={rightColStyle}>
          {loading && !data && <div style={{ padding: 20 }}>Cargando...</div>}
          {error && <div style={{ padding: 20, color: 'red' }}>{error}</div>}
          {!loading && !error && (
            <div className="products-grid">
              {products.map(p => (
                <Link key={p.id} to={`/producto/${p.id}`} state={{ product: p }} style={{ textDecoration:'none', color:'inherit', position:'relative' }}>
                  <article style={cardStyle}>
                    <button
                      title={isFavorite?.(p.id) ? 'Quitar de favoritos' : 'Añadir a favoritos'}
                      onClick={(e)=>{ e.preventDefault(); toggleFavorite && toggleFavorite(p) }}
                      style={{ position:'absolute', right:10, top:10, zIndex:2, width:28, height:28, borderRadius:'50%', border:'1px solid #eee', background:'#fff', cursor:'pointer' }}
                    >
                      {isFavorite?.(p.id) ? '❤️' : '🤍'}
                    </button>
                    {p.image_preview ? (
                      <img src={p.image_preview} alt={p.nombre} style={cardImg} />
                    ) : (
                      <div style={cardImgFallback} />
                    )}
                    <div style={cardBody}>
                      <div style={cardTitle}>{p.nombre}</div>
                      <div style={cardPriceRow}>
                        <span style={cardPrice}>S/ {Number(p.precio).toFixed(2)}</span>
                        {p.precio_descuento && (
                          <span style={cardPriceDiscount}>S/ {Number(p.precio_descuento).toFixed(2)}</span>
                        )}
                      </div>
                      <div style={cardMeta}>S, M, L, XL</div>
                      <button style={addBtn} title="Elegir variantes" onClick={(e)=>{e.preventDefault(); navigate(`/producto/${p.id}`, { state: { product: p } })}} >+</button>
                    </div>
                  </article>
                </Link>
              ))}
              {!products.length && (
                <div style={{ gridColumn: '1 / -1', color: '#666' }}>No hay productos para los filtros actuales.</div>
              )}
            </div>
          )}
          {/* Pagination controls */}
          {!loading && !error && (
            <div style={{display:'flex', justifyContent:'center', alignItems:'center', gap:12, marginTop:16}}>
              <button
                disabled={(pagination.page || page) <= 1}
                onClick={()=> setPage(p => Math.max(1, p-1))}
                style={{...iconBtn, opacity: (pagination.page || page) <= 1 ? .5 : 1, cursor: (pagination.page || page) <= 1 ? 'not-allowed' : 'pointer'}}
              >Anterior</button>
              <span style={{fontSize:13, color:'#555'}}>Página {pagination.page || page} de {totalPages} — {pagination.total ? `Mostrando ${startIdx}-${endIdx} de ${pagination.total}` : ''}</span>
              <button
                disabled={(pagination.page || page) >= totalPages}
                onClick={()=> setPage(p => Math.min(totalPages, p+1))}
                style={{...iconBtn, opacity: (pagination.page || page) >= totalPages ? .5 : 1, cursor: (pagination.page || page) >= totalPages ? 'not-allowed' : 'pointer'}}
              >Siguiente</button>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}

function Chip({ active, onClick, children }) {
  return (
    <button onClick={onClick} style={chipStyle(active)}>{children}</button>
  )
}

// Styles (inline to keep it self-contained)
const pageStyle = { maxWidth: 1200, margin: '0 auto', padding: '16px 20px', paddingTop: 'calc(var(--nav-height) + 12px)', fontFamily: 'Inter, system-ui, Arial' }
const topBarStyle = { display: 'flex', gap: 12, alignItems: 'center', marginBottom: 16 }
const contentStyle = { display: 'grid', gridTemplateColumns: '260px 1fr', gap: 16 }
const leftColStyle = { }
const rightColStyle = { }
const filtersGroup = { background: '#fff', border: '1px solid #eee', borderRadius: 10, padding: 16 }
const filtersTitle = { fontWeight: 700, marginBottom: 10 }
const sectionTitle = { fontSize: 14, color: '#111', fontWeight: 600, marginTop: 10, marginBottom: 8 }
const chipsWrap = { display: 'flex', flexWrap: 'wrap', gap: 8 }
const chipStyle = (active) => ({
  border: '1px solid ' + (active ? '#111' : '#d1d5db'),
  background: active ? '#111' : '#fff',
  color: active ? '#fff' : '#111',
  padding: '6px 10px',
  borderRadius: 10,
  cursor: 'pointer',
  fontSize: 13
})
const searchInputStyle = { width: '100%', padding: '10px 12px', border: '1px solid #ddd', borderRadius: 12 }
// spinner removed
const clearBtnStyle = { marginTop: 10, padding: '6px 10px', borderRadius: 8, border: '1px solid #f87171', background: '#fff', color: '#b91c1c', cursor: 'pointer' }
const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }
const cardStyle = { border: '1px solid #eee', borderRadius: 10, overflow: 'hidden', background: '#fff' }
const cardImg = { width: '100%', height: 180, objectFit: 'cover' }
const cardImgFallback = { width: '100%', height: 180, background: '#f5f5f5' }
const cardBody = { padding: 12, position: 'relative' }
const cardTitle = { fontWeight: 700, marginBottom: 6 }
const cardPriceRow = { display: 'flex', gap: 8, alignItems: 'baseline' }
const cardPrice = { fontSize: 18, fontWeight: 800 }
const cardPriceDiscount = { fontSize: 14, color: '#16a34a' }
const cardMeta = { fontSize: 12, color: '#6b7280', marginTop: 6 }
const iconBtn = { padding: '8px 10px', border: '1px solid #eee', background: '#fff', borderRadius: 10, cursor: 'pointer' }
const addBtn = { position: 'absolute', right: 12, bottom: 12, width: 32, height: 32, borderRadius: 8, border: 'none', background: '#111', color: '#fff', cursor: 'pointer', fontWeight: 800 }
