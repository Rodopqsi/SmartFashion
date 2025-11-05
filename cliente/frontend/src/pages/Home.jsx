import { useEffect } from 'react'
import './Home.css'

export default function Home(){
  const heroImageUrl = '/img/fondonuevo.jpg' 

  const scrollToCatalogo = () => {
    const el = document.getElementById('catalogo-section')
    if (el) el.scrollIntoView({ behavior:'smooth', block:'start' })
  }

  return (
    <div className="home-root">
      <header className="hero-full" style={{ backgroundImage:`url(${heroImageUrl})` }}>
        <a href="#catalogo-section" onClick={(e)=>{e.preventDefault();scrollToCatalogo();}} className="shop-now-btn">Shop Now</a>
      </header>
    </div>
  )
}
