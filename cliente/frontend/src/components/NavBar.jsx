import { useEffect, useRef, useState, useCallback } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import { useCart } from '../cart.jsx'
import '../pages/Home.css'

export default function NavBar(){
  const { user, logout } = useAuth() || {}
  const { count } = useCart() || {}
  const navigate = useNavigate()
  const location = useLocation()
  const [scrolled, setScrolled] = useState(false)
  const [openMenu, setOpenMenu] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const menuRef = useRef(null)
  const mobileMenuRef = useRef(null)
  const [theme, setTheme] = useState(()=>{
    if (typeof window !== 'undefined') return localStorage.getItem('theme') || 'light'
    return 'light'
  })

  useEffect(()=>{
    const onScroll = () => setScrolled(window.scrollY > 10)
    window.addEventListener('scroll', onScroll, { passive:true })
    onScroll()
    return () => window.removeEventListener('scroll', onScroll)
  },[])

  useEffect(()=>{
    const onDocClick = (e) => {
      if (!menuRef.current) return
      if (!menuRef.current.contains(e.target)) setOpenMenu(false)
      if (mobileMenuRef.current && !mobileMenuRef.current.contains(e.target) && !e.target.closest('.mobile-toggle')) setMobileOpen(false)
    }
    const onEsc = (e) => { if (e.key === 'Escape') setOpenMenu(false) }
    document.addEventListener('click', onDocClick)
    document.addEventListener('keydown', onEsc)
    return () => { document.removeEventListener('click', onDocClick); document.removeEventListener('keydown', onEsc) }
  }, [])

  useEffect(()=>{
    const root = document.documentElement
    if (theme === 'dark') root.classList.add('theme-dark'); else root.classList.remove('theme-dark')
    localStorage.setItem('theme', theme)
  }, [theme])

  const toggleTheme = useCallback(()=> setTheme(t => t === 'light' ? 'dark' : 'light'), [])

  const scrollFactor = typeof window !== 'undefined' ? Math.min(1, window.scrollY / 220) : 0
  const scrollAlpha = 0.9 * scrollFactor
  const navBg = `rgba(${theme === 'dark' ? '15,17,21' : '255,255,255'}, ${scrollAlpha.toFixed(3)})`

  const scrollOrNavigate = (id) => {
    if (location.pathname !== '/') {
      navigate(`/#${id}`)
      return
    }
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior:'smooth', block:'start' })
  }


  const [searchOpen, setSearchOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const searchRef = useRef(null)
    const [isMobile, setIsMobile] = useState(() => (typeof window !== 'undefined' ? window.matchMedia('(max-width: 640px)').matches : false))

    useEffect(() => {
      const mq = window.matchMedia('(max-width: 640px)')
      const update = () => setIsMobile(mq.matches)
      mq.addEventListener('change', update)
      update()
      return () => mq.removeEventListener('change', update)
    }, [])

  const doSearch = useCallback(() => {
    const q = (searchTerm || '').trim()
    if (!q) return

    navigate(`/catalogo?q=${encodeURIComponent(q)}`)
    setSearchOpen(false)
  }, [navigate, searchTerm])

  return (
    <nav id="nav-bar" className={scrolled ? 'scrolled' : ''} style={{ background: navBg }}>
        <div className="nav-left">
        <button className="icon-btn" onClick={toggleTheme} aria-label="Cambiar tema" title="Cambiar tema">
          {theme === 'light' ? '🌙' : '☀️'}
        </button>
        <div className="vertical-sep">|</div>
        {!isMobile && (
          <Link to="/" className="icon-btn" aria-label="Home" style={{textDecoration:null}}>Inicio</Link>
        )}
        <div className="vertical-sep">|</div>
        <div
          ref={searchRef}
          onMouseEnter={()=> setSearchOpen(true)}
          onMouseLeave={()=> { if (!document.activeElement || document.activeElement !== searchRef.current?.querySelector('input')) setSearchOpen(false) }}
          style={{
            display:'flex', alignItems:'center', gap:8,
              width: searchOpen ? (isMobile ? 200 : 260) : 34,
            height: 36,
            transition:'width .3s ease',
            overflow:'hidden',
            background:'#ffffffff',
            borderRadius: 9999,
            padding: '0 10px',
            boxShadow: '2px 2px 20px rgba(0,0,0,0.08)'
          }}
        >
          <button
            onClick={doSearch}
            title="Buscar"
            style={{ display:'grid', placeItems:'center', width:24, height:24, border:'none', background:'transparent', cursor:'pointer' }}
            aria-label="Buscar"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              id="Isolation_Mode"
              data-name="Isolation Mode"
              viewBox="0 0 24 24"
              width="22"
              height="22"
            >
              <path
                d="M18.9,16.776A10.539,10.539,0,1,0,16.776,18.9l5.1,5.1L24,21.88ZM10.5,18A7.5,7.5,0,1,1,18,10.5,7.507,7.507,0,0,1,10.5,18Z"
              ></path>
            </svg>
          </button>
          <input
            type="text"
            value={searchTerm}
            onChange={(e)=> setSearchTerm(e.target.value)}
            onFocus={()=> setSearchOpen(true)}
            onBlur={()=> { if (!(searchTerm||'').trim()) setSearchOpen(false) }}
            onKeyDown={(e)=> { if (e.key === 'Enter') doSearch() }}
            placeholder="Buscar..."
            aria-label="Buscar productos"
            style={{ outline:'none', border:'none', background:'transparent', color:'#000000ff', fontSize:14, width:'100%' }}
          />
        </div>
      </div>
    <div className="nav-center">
      <button className="nav-link icon-btn" style={{background:'none'}} onClick={()=>scrollOrNavigate('new-arrivals')}>Novedades</button>
      <button className="nav-link icon-btn" style={{background:'none'}} onClick={()=>scrollOrNavigate('collections')}>Colecciones</button>
      <Link to="/catalogo" className="nav-link">Categorías</Link>
      </div>
      <div className="nav-right">
        {/* mobile hamburger */}
        <button className="icon-btn mobile-toggle" aria-label="Abrir menú" onClick={()=> setMobileOpen(o=>!o)} style={{display:isMobile? 'inline-flex':'none'}}>
          ☰
        </button>
        {mobileOpen && (
          <div ref={mobileMenuRef} className="mobile-menu" style={{position:'absolute', right:12, top:'64px', minWidth:200, background:'var(--color-bg)', border:'1px solid var(--color-border)', borderRadius:8, padding:12, zIndex:120}}>
            <Link to="/" className="dropdown-item" onClick={()=> setMobileOpen(false)}>Inicio</Link>
            <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); scrollOrNavigate('new-arrivals') }}>Novedades</button>
            <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); scrollOrNavigate('collections') }}>Colecciones</button>
            <Link to="/catalogo" className="dropdown-item" onClick={()=> setMobileOpen(false)}>Categorías</Link>
            <hr className="dropdown-sep" />
            {user ? (
              <>
                <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); navigate('/perfil') }}>Mi Perfil</button>
                <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); navigate('/direcciones') }}>Mis Direcciones</button>
                <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); navigate('/reclamos') }}>Mis Reclamos</button>
                <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); navigate('/devoluciones') }}>Mis Devoluciones</button>
                <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); navigate('/perfil?tab=envios') }}>Mis Envíos</button>
                <button className="dropdown-item" onClick={()=>{ setMobileOpen(false); navigate('/favoritos') }}>Mis Favoritos</button>
                <hr className="dropdown-sep" />
                <button className="dropdown-item logout" onClick={()=>{ setMobileOpen(false); logout() }}>Cerrar Sesión</button>
              </>
            ) : (
              <>
                <Link to="/login" className="dropdown-item" onClick={()=> setMobileOpen(false)}>Iniciar Sesión</Link>
                <Link to="/register" className="dropdown-item" onClick={()=> setMobileOpen(false)}>Registrarse</Link>
              </>
            )}
          </div>
        )}
        
        {user ? (
          <div className="user-menu" ref={menuRef} style={{ position:'relative' }}>
            <button
              className="icon-btn user-toggle"
              onClick={()=>setOpenMenu(o=>!o)}
              aria-haspopup="menu"
              aria-expanded={openMenu}
              aria-label={user.username || user.email}
              title={user.username || user.email}
            >
              <span className="user-name">{user.username || user.email}</span>
              <svg className="user-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                <path d="M12 12c2.761 0 5-2.239 5-5s-2.239-5-5-5-5 2.239-5 5 2.239 5 5 5zm0 2c-4.971 0-9 2.686-9 6v2h18v-2c0-3.314-4.029-6-9-6z"/>
              </svg>
            </button>
            {openMenu && (
              <div className="dropdown" role="menu">
                <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/perfil') }}>Mi Perfil</button>
                <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/direcciones') }}>Mis Direcciones</button>
                <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/reclamos') }}>Mis Reclamos</button>
                <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/devoluciones') }}>Mis Devoluciones</button>
                <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/perfil?tab=envios') }}>Mis Envíos</button>
                <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/favoritos') }}>Mis Favoritos</button>
                <hr className="dropdown-sep"/>
                <button className="dropdown-item logout" onClick={()=>{ setOpenMenu(false); logout() }}>Cerrar Sesión</button>
              </div>
            )}
          </div>
        ) : (
          <>
            {!isMobile && (
              <>
                <Link to="/login" className="icon-btn" style={{textDecoration:'none'}}>Iniciar Sesión</Link>
                <Link to="/register" className="icon-btn" style={{textDecoration:'none'}}>Registrarse</Link>
              </>
            )}
          </>
        )}
        <div className="vertical-sep">|</div>
        <button className="icon-btn" aria-label="Carrito" onClick={()=> navigate('/carrito')} style={{position:'relative'}}>
          <svg width="25" height="25" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20.2236 12.5257C19.6384 9.40452 19.3458 7.84393 18.2349 6.92196C17.124 6 15.5362 6 12.3606 6H11.6394C8.46386 6 6.87608 6 5.76518 6.92196C4.65428 7.84393 4.36167 9.40452 3.77645 12.5257C2.95353 16.9146 2.54207 19.1091 3.74169 20.5545C4.94131 22 7.17402 22 11.6394 22H12.3606C16.826 22 19.0587 22 20.2584 20.5545C20.9543 19.7159 21.108 18.6252 20.9537 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <path d="M9 6V5C9 3.34315 10.3431 2 12 2C13.6569 2 15 3.34315 15 5V6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
          <span>Mi carrito </span>
          {count>0 && (<span style={{position:'absolute', top:-2, right:-6, background:'#111', color:'#fff', borderRadius:12, padding:'2px 6px', fontSize:10, fontWeight:700}}>{count}</span>)}
        </button>
      </div>
    </nav>
  )
}
