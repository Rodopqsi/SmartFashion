import { useEffect, useState, useCallback, useRef } from 'react'
import Catalogo from './Catalogo'
import { Link, useNavigate } from 'react-router-dom'
import './Home.css'
import { useAuth } from '../auth.jsx'

export default function Home(){
  const { user, logout } = useAuth() || {}
  const navigate = useNavigate()
  const [scrolled, setScrolled] = useState(false)
  const [openMenu, setOpenMenu] = useState(false)
  const menuRef = useRef(null)
  // Page state removed; routing now handles login/register pages
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

  const heroImageUrl = '/img/fondonuevo.jpg' 

  const scrollToCatalogo = () => {
    const el = document.getElementById('catalogo-section')
    if (el) el.scrollIntoView({ behavior:'smooth', block:'start' })
  }

  const scrollFactor = typeof window !== 'undefined' ? Math.min(1, window.scrollY / 220) : 0
  const scrollAlpha = 0.9 * scrollFactor // 0 -> 0.9
  const navBg = `rgba(${theme === 'dark' ? '15,17,21' : '255,255,255'}, ${scrollAlpha.toFixed(3)})`

  return (
    <div className="home-root">
      <nav id="nav-bar" className={scrolled ? 'scrolled' : ''} style={{ background: navBg }}>
        <div className="nav-left">
          <button className="icon-btn" onClick={toggleTheme} aria-label="Cambiar tema" title="Cambiar tema">
            {theme === 'light' ? '🌙' : '☀️'}
          </button>
          <div className="vertical-sep">|</div>
          <button className="icon-btn" aria-label="Menú">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path fillRule="evenodd" clipRule="evenodd" d="M19.5 8.25H4.5V6.75H19.5V8.25Z" fill="#000" />
              <path fillRule="evenodd" clipRule="evenodd" d="M19.5 12.75H4.5V11.25H19.5V12.75Z" fill="#000" />
              <path fillRule="evenodd" clipRule="evenodd" d="M19.5 17.25H4.5V15.75H19.5V17.25Z" fill="#000" />
            </svg>
            <span style={{fontWeight: 400, fontSize: '18px'}}>Menú</span>
          </button>
          <div className="vertical-sep">|</div>
          <button className="icon-btn" aria-label="Buscar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M16.6725 16.6412L21 21M19 11C19 15.4183 15.4183 19 11 19C6.58172 19 3 15.4183 3 11C3 6.58172 6.58172 3 11 3C15.4183 3 19 6.58172 19 11Z" stroke="#000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
            <span>Search</span>
          </button>
        </div>
        <div className="nav-center">
            <a href="#new-arrivals" className="nav-link">New Arrivals</a>
            <a href="#collections" className="nav-link">Collections</a>
            <a href="#categories" className="nav-link" onClick={(e)=>{e.preventDefault();scrollToCatalogo();}}>Categories</a>
            <a href="#sale" className="nav-link highlight">Sale</a>
        </div>
        <div className="nav-right">
          {user ? (
            <div className="user-menu" ref={menuRef} style={{ position:'relative' }}>
              <button className="icon-btn" onClick={()=>setOpenMenu(o=>!o)} aria-haspopup="menu" aria-expanded={openMenu}>
                {user.username || user.email}
              </button>
              {openMenu && (
                <div className="dropdown" role="menu" style={{ position:'absolute', right:0, top:'2.5rem', background:'var(--bg, #fff)', border:'1px solid #ddd', borderRadius:8, boxShadow:'0 8px 24px rgba(0,0,0,.12)', minWidth:180, zIndex:1000 }}>
                  <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/perfil') }} style={{ display:'block', width:'100%', textAlign:'left', padding:'10px 12px', background:'transparent', border:'none', cursor:'pointer' }}>Mi Perfil</button>
                  <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); navigate('/favoritos') }} style={{ display:'block', width:'100%', textAlign:'left', padding:'10px 12px', background:'transparent', border:'none', cursor:'pointer' }}>Mis Favoritos</button>
                  <hr style={{ margin:'6px 0', border:'none', borderTop:'1px solid #eee' }}/>
                  <button className="dropdown-item" onClick={()=>{ setOpenMenu(false); logout() }} style={{ display:'block', width:'100%', textAlign:'left', padding:'10px 12px', background:'transparent', border:'none', cursor:'pointer', color:'#b00' }}>Cerrar Sesión</button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Link to="/login" className="icon-btn" style={{textDecoration:'none'}}>Iniciar Sesión</Link>
              <Link to="/register" className="icon-btn" style={{textDecoration:'none'}}>Registrarse</Link>
            </>
          )}
          <div className="vertical-sep">|</div>
          <button className="icon-btn" aria-label="Carrito">
            <svg width="25" height="25" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20.2236 12.5257C19.6384 9.40452 19.3458 7.84393 18.2349 6.92196C17.124 6 15.5362 6 12.3606 6H11.6394C8.46386 6 6.87608 6 5.76518 6.92196C4.65428 7.84393 4.36167 9.40452 3.77645 12.5257C2.95353 16.9146 2.54207 19.1091 3.74169 20.5545C4.94131 22 7.17402 22 11.6394 22H12.3606C16.826 22 19.0587 22 20.2584 20.5545C20.9543 19.7159 21.108 18.6252 20.9537 17" stroke="#000" strokeWidth="2" strokeLinecap="round" />
              <path d="M9 6V5C9 3.34315 10.3431 2 12 2C13.6569 2 15 3.34315 15 5V6" stroke="#1C274C" strokeWidth="2" strokeLinecap="round" />
            </svg>
            <span>My Cart</span>
          </button>
        </div>
      </nav>

      <header className="hero-full" style={{ backgroundImage:`url(${heroImageUrl})` }}>
        <a href="#catalogo-section" onClick={(e)=>{e.preventDefault();scrollToCatalogo();}} className="shop-now-btn">Shop Now</a>
      </header>
      <section id="catalogo-section" className="catalogo-wrapper">
        <Catalogo />
      </section>
    </div>
  )
}
