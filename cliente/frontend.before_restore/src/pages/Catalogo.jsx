import { useEffect, useState, useRef } from 'react'
import { Link, useLocation } from 'react-router-dom'
import InputFloating from '../components/InputFloating.jsx'
import InputSelectFloating from '../components/InputSelectFloating.jsx'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function Catalogo() {
  const location = useLocation()
  const [data, setData] = useState(null)
  const [selectedCategory, setSelectedCategory] = useState(null)
  const [query, setQuery] = useState('')
  const [sizes, setSizes] = useState([])
  const [colors, setColors] = useState([])
  const [selectedSize, setSelectedSize] = useState('')
  const [selectedColor, setSelectedColor] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  
  const [page, setPage] = useState(1)
  const [limit, setLimit] = useState(12)
  const debounceRef = useRef(null)

  const fetchData = (cat, qVal, size, color, pageVal, limitVal, { showSpinner=true } = {}) => {
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
      .then(json => { setData(json.data); setLoading(false) })
      .catch(err => { console.error(err); setError('No se pudo cargar el catálogo'); setLoading(false) })
  }

  useEffect(() => { fetchData(selectedCategory, query, selectedSize, selectedColor, page, limit) }, [selectedCategory, selectedSize, selectedColor, page, limit])

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      setPage(1)
      fetchData(selectedCategory, query, selectedSize, selectedColor, 1, limit, { showSpinner:false })
    }, 350)
    return () => clearTimeout(debounceRef.current)
  }, [query])

  
  useEffect(() => {
    const qParam = new URLSearchParams(location.search).get('q') || ''
    setQuery(qParam)
    setPage(1)
  }, [location.search])

  useEffect(() => {
    
    setPage(1)
    fetch(`${API_BASE}/api/sizes/`).then(r=>r.json()).then(j=>setSizes(j.data||[])).catch(()=>{})
    fetch(`${API_BASE}/api/colors/`).then(r=>r.json()).then(j=>setColors(j.data||[])).catch(()=>{})
  }, [])

  if (loading && !data) return <div style={{padding:20}}>Cargando...</div>
  if (error) return <div style={{padding:20,color:'red'}}>{error}</div>

  const { categories = [], featured_products = [], pagination = { page:1, limit:limit, total:0 } } = data || {}
  const totalPages = Math.max(1, Math.ceil((pagination.total || 0) / (pagination.limit || limit || 12)))
  const startIdx = pagination.total ? ((pagination.page || page) - 1) * (pagination.limit || limit) + 1 : 0
  const endIdx = pagination.total ? Math.min(pagination.total, startIdx + (featured_products?.length || 0) - 1) : 0

  return (
    <div style={{fontFamily:'Inter, system-ui, Arial', padding:20}}>
      <header style={{display:'flex', flexWrap:'wrap', gap:16, justifyContent:'center', alignItems:'center'}}>
        <h1 style={{margin:0}}>Catálogo</h1>
        <div style={{display:'grid', gridTemplateColumns:'minmax(220px, 1fr) 180px 180px auto', gap:8, alignItems:'center', justifySelf:'end', width:'100%', maxWidth:780}}>
          <div style={{position:'relative'}}>
            <InputFloating label="Buscar productos..." value={query} onChange={e=>setQuery(e.target.value)} />
          </div>
          <InputSelectFloating label="Talla" value={selectedSize} onChange={e=>setSelectedSize(e.target.value)}>
            <option value="">Talla</option>
            {sizes.map(s => <option key={s.id} value={s.id}>{s.nombre}</option>)}
          </InputSelectFloating>
          <InputSelectFloating label="Color" value={selectedColor} onChange={e=>setSelectedColor(e.target.value)}>
            <option value="">Color</option>
            {colors.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
          </InputSelectFloating>
          {(query || selectedSize || selectedColor || selectedCategory!==null) && (
            <button onClick={()=>{setQuery('');setSelectedSize('');setSelectedColor('');setSelectedCategory(null); setPage(1)}} style={clearBtnStyle}>Limpiar</button>
          )}
        </div>
      </header>

      <section>
        <h2>Categorías</h2>
        <div style={{display:'flex', gap:12, flexWrap:'wrap'}}>
          <button
            onClick={() => setSelectedCategory(null)}
            style={buttonStyle(selectedCategory === null)}
          >Todas</button>
          {categories.map(c => (
            <button
              key={c.id}
              onClick={() => setSelectedCategory(c.id)}
              style={buttonStyle(selectedCategory === c.id)}
            >{c.nombre}</button>
          ))}
        </div>
      </section>
      <section style={{marginTop:24}}>
        <div style={{display:'flex', alignItems:'baseline', justifyContent:'space-between'}}>
          <h2 style={{margin:0}}>Productos</h2>
          <small style={{color:'#666'}}>
            {pagination.total ? `Mostrando ${startIdx}-${endIdx} de ${pagination.total}` : ''}
          </small>
        </div>
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(220px, 1fr))', gap:16}}>
          {featured_products.map(p => (
            <Link key={p.id} to={`/producto/${p.id}`} state={{ product: p }} style={{ textDecoration:'none', color:'inherit' }}>
              <div style={{border:'1px solid #eee', borderRadius:8, overflow:'hidden'}}>
                {p.image_preview ? (
                  <img src={p.image_preview} alt={p.nombre} style={{width:'100%', height:160, objectFit:'cover'}} />
                ) : (
                  <div style={{width:'100%', height:160, background:'#f5f5f5'}} />
                )}
                <div style={{padding:12}}>
                  <div style={{fontWeight:600}}>{p.nombre}</div>
                  <div style={{color:'#666', fontSize:14, minHeight:40}}>{p.descripcion?.slice(0,80)}{(p.descripcion||'').length>80?'...':''}</div>
                  <div style={{marginTop:8, display:'flex', gap:8, alignItems:'baseline'}}>
                    <span style={{fontSize:18, fontWeight:700}}>S/ {p.precio}</span>
                    {p.precio_descuento && (
                      <span style={{fontSize:14, color:'#16a34a'}}>S/ {p.precio_descuento}</span>
                    )}
                  </div>
                  <div style={{fontSize:12, color:'#999'}}>Stock: {p.stock_total}</div>
                  {p.categoria && (
                    <div style={{fontSize:12, color:'#555'}}>Categoría: {p.categoria.nombre}</div>
                  )}
                </div>
              </div>
            </Link>
          ))}
        </div>
        {}
        <div style={{display:'flex', justifyContent:'center', alignItems:'center', gap:12, marginTop:16}}>
          <button
            disabled={page <= 1}
            onClick={()=> setPage(p => Math.max(1, p-1))}
            style={{...buttonStyle(false), opacity: page<=1? .5:1, cursor: page<=1? 'not-allowed':'pointer'}}
          >Anterior</button>
          <span style={{fontSize:13, color:'#555'}}>Página {pagination.page || page} de {totalPages}</span>
          <button
            disabled={page >= totalPages}
            onClick={()=> setPage(p => Math.min(totalPages, p+1))}
            style={{...buttonStyle(false), opacity: page>=totalPages? .5:1, cursor: page>=totalPages? 'not-allowed':'pointer'}}
          >Siguiente</button>
        </div>
      </section>
    </div>
  )
}

function buttonStyle(active){
  return {
    border: '1px solid ' + (active ? '#2563eb' : '#ccc'),
    background: active ? '#2563eb' : '#fff',
    color: active ? '#fff' : '#222',
    padding: '6px 14px',
    borderRadius: 20,
    cursor: 'pointer',
    fontSize: 14,
    boxShadow: active ? '0 0 0 2px rgba(37,99,235,0.25)' : 'none',
    transition: 'all .15s'
  }
}

const searchInputStyle = {}
const selectStyle = {}

const clearBtnStyle = {
  ...buttonStyle(false),
  borderColor: '#f87171',
  color: '#b91c1c'
}


