import { useEffect, useState, useRef } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000'

export default function Catalogo() {
  const [data, setData] = useState(null)
  const [selectedCategory, setSelectedCategory] = useState(null)
  const [query, setQuery] = useState('')
  const [sizes, setSizes] = useState([])
  const [colors, setColors] = useState([])
  const [selectedSize, setSelectedSize] = useState('')
  const [selectedColor, setSelectedColor] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [typing, setTyping] = useState(false)
  const debounceRef = useRef(null)

  const fetchData = (cat, qVal, size, color, { showSpinner=true } = {}) => {
    if (showSpinner) setLoading(true)
    const params = new URLSearchParams()
    if (cat) params.append('category_id', cat)
    if (qVal) params.append('q', qVal)
    if (size) params.append('size', size)
    if (color) params.append('color', color)
    fetch(`${API_BASE}/api/home/${params.toString() ? `?${params.toString()}` : ''}`)
      .then(res => res.json())
      .then(json => { setData(json.data); setLoading(false) })
      .catch(err => { console.error(err); setError('No se pudo cargar el catálogo'); setLoading(false) })
  }

  useEffect(() => { fetchData(selectedCategory, query, selectedSize, selectedColor) }, [selectedCategory, selectedSize, selectedColor])

  useEffect(() => {
    setTyping(true)
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      fetchData(selectedCategory, query, selectedSize, selectedColor, { showSpinner:false })
      setTyping(false)
    }, 350)
    return () => clearTimeout(debounceRef.current)
  }, [query])

  useEffect(() => {
    fetch(`${API_BASE}/api/sizes/`).then(r=>r.json()).then(j=>setSizes(j.data||[])).catch(()=>{})
    fetch(`${API_BASE}/api/colors/`).then(r=>r.json()).then(j=>setColors(j.data||[])).catch(()=>{})
  }, [])

  if (loading && !data) return <div style={{padding:20}}>Cargando...</div>
  if (error) return <div style={{padding:20,color:'red'}}>{error}</div>

  const { categories = [], featured_products = [] } = data || {}

  return (
    <div style={{fontFamily:'Inter, system-ui, Arial', padding:20}}>
      <header style={{display:'flex', flexWrap:'wrap', gap:16, justifyContent:'space-between', alignItems:'center'}}>
        <h1 style={{margin:0}}>Catálogo</h1>
        <div style={{display:'flex', gap:8, flexWrap:'wrap', alignItems:'center'}}>
          <div style={{position:'relative'}}>
            <input
              placeholder="Buscar productos..."
              value={query}
              onChange={e=>setQuery(e.target.value)}
              style={searchInputStyle}
            />
            {(typing) && (
              <span style={spinnerStyle} title="Buscando">⌛</span>
            )}
          </div>
          <select value={selectedSize} onChange={e=>setSelectedSize(e.target.value)} style={selectStyle}>
            <option value="">Talla</option>
            {sizes.map(s => <option key={s.id} value={s.id}>{s.nombre}</option>)}
          </select>
          <select value={selectedColor} onChange={e=>setSelectedColor(e.target.value)} style={selectStyle}>
            <option value="">Color</option>
            {colors.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
          </select>
          {(query || selectedSize || selectedColor || selectedCategory!==null) && (
            <button onClick={()=>{setQuery('');setSelectedSize('');setSelectedColor('');setSelectedCategory(null)}} style={clearBtnStyle}>Limpiar</button>
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
        <h2>Productos</h2>
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(220px, 1fr))', gap:16}}>
          {featured_products.map(p => (
            <div key={p.id} style={{border:'1px solid #eee', borderRadius:8, overflow:'hidden'}}>
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
          ))}
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

const searchInputStyle = {
  padding: '6px 10px',
  border: '1px solid #ccc',
  borderRadius: 6,
  minWidth: 240
}

const selectStyle = {
  padding: '6px 10px',
  border: '1px solid #ccc',
  borderRadius: 6,
  background: '#fff'
}

const clearBtnStyle = {
  ...buttonStyle(false),
  borderColor: '#f87171',
  color: '#b91c1c'
}

const spinnerStyle = {
  position: 'absolute',
  right: 8,
  top: '50%',
  transform: 'translateY(-50%)',
  fontSize: 12,
  opacity: 0.7
}
