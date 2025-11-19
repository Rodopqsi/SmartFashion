import { Link } from 'react-router-dom'
import '../pages/Home.css'

export default function Footer(){
  return (
    <footer style={footerStyle}>
      <div style={footerInner}>
        <div style={colWide}>
          <div style={brand}>SmartFashion</div>
          <p style={muted}>Moda para cada momento. Encuentra colecciones de temporada, ofertas y envíos rápidos.</p>
          <div style={socials}>
            <a href="#" aria-label="Instagram" style={socialLink}>ⓘ</a>
            <a href="#" aria-label="Facebook" style={socialLink}>𝔽</a>
            <a href="#" aria-label="TikTok" style={socialLink}>𝕋</a>
          </div>
        </div>

        <div style={col}>
          <div style={colTitle}>Compañía</div>
          <ul style={list}>
            <li><Link to="/catalogo" style={link}>Catálogo</Link></li>
            <li><a href="#collections" style={link}>Colecciones</a></li>
            <li><a href="#new-arrivals" style={link}>Novedades</a></li>
            <li><a href="#sale" style={link} className="highlight">Ofertas</a></li>
          </ul>
        </div>

        <div style={col}>
          <div style={colTitle}>Ayuda</div>
          <ul style={list}>
            <li><Link to="/reclamos" style={link}>Reclamos</Link></li>
            <li><Link to="/devoluciones" style={link}>Devoluciones</Link></li>
            <li><Link to="/direcciones" style={link}>Envíos y Direcciones</Link></li>
            <li><a href="#faq" style={link}>Preguntas Frecuentes</a></li>
          </ul>
        </div>

        <div style={col}>
          <div style={colTitle}>Suscríbete</div>
          <p style={muted}>Recibe noticias, lanzamientos y promociones.</p>
          <form onSubmit={(e)=>{e.preventDefault(); /* TODO: hook newsletter */}} style={formRow}>
            <input type="email" placeholder="Tu email" required style={emailInput} />
            <button type="submit" style={btnPrimary}>Unirme</button>
          </form>
          <div style={{...muted, fontSize:12}}>Al suscribirte aceptas nuestras políticas.</div>
        </div>
      </div>
      <div style={bottomBar}>
        <span>© {new Date().getFullYear()} SmartFashion</span>
        <span style={sep}>·</span>
        <a href="#terms" style={mutedLink}>Términos</a>
        <span style={sep}>·</span>
        <a href="#privacy" style={mutedLink}>Privacidad</a>
      </div>
    </footer>
  )
}

const footerStyle = { borderTop:'1px solid var(--color-border)', background:'var(--color-bg)', color:'var(--color-text)', marginTop:40 }
const footerInner = { maxWidth:1200, margin:'0 auto', padding:'32px 20px', display:'grid', gridTemplateColumns:'2fr 1fr 1fr 1.4fr', gap:20 }
const col = { }
const colWide = { }
const brand = { fontSize:22, fontWeight:800, marginBottom:10 }
const colTitle = { fontSize:14, fontWeight:700, textTransform:'uppercase', letterSpacing:'1px', marginBottom:10 }
const list = { listStyle:'none', padding:0, margin:0, display:'flex', flexDirection:'column', gap:8 }
const link = { color:'var(--color-text)', textDecoration:'none' }
const muted = { color:'var(--color-text-soft)', margin:'6px 0' }
const socials = { display:'flex', gap:8, marginTop:6 }
const socialLink = { display:'inline-flex', width:30, height:30, alignItems:'center', justifyContent:'center', border:'1px solid var(--color-border)', borderRadius:8, color:'var(--color-text)', textDecoration:'none' }
const formRow = { display:'flex', gap:8, marginTop:8 }
const emailInput = { flex:1, padding:'10px 12px', border:'1px solid var(--color-border)', borderRadius:10, background:'var(--color-bg-soft)', color:'var(--color-text)' }
const btnPrimary = { padding:'10px 14px', borderRadius:10, border:'none', background:'var(--color-text)', color:'var(--color-bg)', cursor:'pointer', fontWeight:700 }
const bottomBar = { borderTop:'1px solid var(--color-border)', padding:'14px 20px', display:'flex', gap:10, alignItems:'center', justifyContent:'center', color:'var(--color-text-soft)' }
const sep = { opacity:.5 }
const mutedLink = { color:'var(--color-text-soft)', textDecoration:'none' }
