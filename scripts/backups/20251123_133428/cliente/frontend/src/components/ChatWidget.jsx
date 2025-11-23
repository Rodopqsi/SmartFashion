import { useEffect, useRef, useState } from 'react';
import './ChatWidget.css';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8000';

function useCatalog(open) {
  const [catalog, setCatalog] = useState(null);
  useEffect(() => {
    if (!open) return;
    let cancelled = false;
    fetch(`${API_BASE}/api/catalog/snapshot/`)
      .then(r => r.ok ? r.json() : Promise.reject())
      .then(j => { if (!cancelled) setCatalog(j.data || null); })
      .catch(() => setCatalog(null));
    return () => { cancelled = true; };
  }, [open]);
  return catalog;
}

  export default function ChatWidget() {
    const [open, setOpen] = useState(false);
    const [input, setInput] = useState('');
    const [messages, setMessages] = useState([
      { role: 'bot', type: 'text', content: '¡Hola! Soy tu asistente IA de SmartFashion. Pregúntame lo que quieras.' }
    ]);
    const [loading, setLoading] = useState(false);
    const bodyRef = useRef(null);
    const catalog = useCatalog(open);

    useEffect(() => {
      if (bodyRef.current) bodyRef.current.scrollTop = bodyRef.current.scrollHeight;
    }, [messages, open]);


    // Buscador masivo mejorado: prioriza colección similar (fuzzy), luego palabras clave de temporada/estilo
    function massiveSearch(query) {
      if (!catalog?.products?.length) return [];
      const q = query.trim().toLowerCase();
      if (!q) return [];
      // 1. Buscar colección más parecida (fuzzy)
      // Recolecta todos los nombres de colección únicos
      const allCollections = new Set();
      catalog.products.forEach(p => {
        if (p.coleccion) allCollections.add(String(p.coleccion).toLowerCase());
        if (p.collection) allCollections.add(String(p.collection).toLowerCase());
        if (p.grupo) allCollections.add(String(p.grupo).toLowerCase());
        if (Array.isArray(p.etiquetas)) p.etiquetas.forEach(e => allCollections.add(String(e).toLowerCase()));
        if (Array.isArray(p.tags)) p.tags.forEach(e => allCollections.add(String(e).toLowerCase()));
      });
      // Encuentra la colección más parecida usando coincidencia parcial (incluye, o distancia Levenshtein simple)
      let bestMatch = '';
      let bestScore = 0;
      for (const col of allCollections) {
        if (!col) continue;
        if (col.includes(q) || q.includes(col)) {
          // Coincidencia directa o parcial
          if (col.length > bestScore) { bestMatch = col; bestScore = col.length; }
        } else {
          // Fuzzy: cuenta cuántos caracteres coinciden en orden
          let score = 0, i = 0, j = 0;
          while (i < q.length && j < col.length) {
            if (q[i] === col[j]) { score++; i++; j++; } else { j++; }
          }
          if (score > bestScore) { bestMatch = col; bestScore = score; }
        }
      }
      if (bestMatch && bestScore >= Math.max(4, Math.floor(q.length * 0.5))) {
        // Busca productos de esa colección
        const collectionProducts = catalog.products.filter(p => {
          return (
            (p.coleccion && String(p.coleccion).toLowerCase() === bestMatch) ||
            (p.collection && String(p.collection).toLowerCase() === bestMatch) ||
            (p.grupo && String(p.grupo).toLowerCase() === bestMatch) ||
            (Array.isArray(p.etiquetas) && p.etiquetas.some(e => String(e).toLowerCase() === bestMatch)) ||
            (Array.isArray(p.tags) && p.tags.some(e => String(e).toLowerCase() === bestMatch))
          );
        });
        if (collectionProducts.length) {
          // Devuelve un objeto especial para renderizar mensaje y productos
          return { collection: bestMatch, products: collectionProducts };
        }
      }

      // 2. Palabras clave de temporada/estilo
      const seasonMap = {
        'primavera': ['vestido', 'falda', 'blusa', 'camisa', 'polo', 'floral', 'flores', 'pastel', 'ligero', 'chompa', 'chaqueta', 'bomber', 'jean'],
        'verano': ['short', 'polo', 'camiseta', 'vestido', 'falda', 'lino', 'algodón', 'manga corta', 'sandalia', 'bermuda'],
        'otoño': ['chompa', 'cardigan', 'pantalón', 'camisa', 'manga larga', 'suéter', 'beige', 'marrón', 'bomber'],
        'invierno': ['abrigo', 'chompa', 'buzo', 'casaca', 'parka', 'polar', 'bufanda', 'gorro', 'manga larga'],
        'floral': ['floral', 'flores', 'estampado'],
        'ligero': ['lino', 'algodón', 'fresco', 'ligero', 'delgado'],
        'coleccion': ['vestido', 'falda', 'blusa', 'polo', 'camisa', 'chompa', 'chaqueta', 'bomber', 'jean', 'short', 'camiseta'],
      };
      let mappedWords = [];
      for (const [key, arr] of Object.entries(seasonMap)) {
        if (q.includes(key)) mappedWords = mappedWords.concat(arr);
      }
      if (mappedWords.length) {
        return catalog.products.filter(p => {
          const fields = [p.nombre, p.descripcion, p.categoria?.nombre].filter(Boolean).map(s => String(s).toLowerCase());
          return mappedWords.some(word => fields.some(f => f.includes(word)));
        });
      }
      // 3. Coincidencia textual normal
      return catalog.products.filter(p => {
        const fields = [
          p.nombre,
          p.descripcion,
          p.categoria?.nombre,
          ...(p.variantes?.map(v => v.color_nombre) || []),
          ...(p.variantes?.map(v => v.talla_nombre) || []),
          ...(p.colores?.map(c => c.nombre) || []),
        ].filter(Boolean).map(s => String(s).toLowerCase());
        return fields.some(f => f.includes(q));
      });
    }

    async function handleQuery(q) {
      const text = q.trim();
      if (!text) return;
      setMessages(m => [...m, { role: 'user', type: 'text', content: text }]);
      setLoading(true);

      const found = massiveSearch(text);
      // Si es un objeto especial de colección fuzzy
      if (found && found.collection && found.products) {
        setMessages(m => [...m, { role: 'bot', type: 'jsx', content: (
          <div>
            <div style={{fontWeight:700, marginBottom:4}}>
              Resultados de la colección más parecida: <span style={{color:'#1e88e5'}}>{found.collection}</span>
            </div>
            <div className="sf-product-list">
              {found.products.slice(0, 6).map(p => (
                <a key={p.id} className="sf-prod" href={`/producto/${p.id}`}>
                  <img src={p.image_preview || (p.imagenes?.general?.[0]) || '/img/placeholder.png'} alt={p.nombre} />
                  <div className="meta">
                    <span style={{fontWeight:700}}>{p.nombre}</span>
                    <span style={{opacity:.7, fontSize:'.9rem'}}>S/ {Number(p.precio).toFixed(2)}{p.precio_descuento ? ` → S/ ${Number(p.precio_descuento).toFixed(2)}`: ''}</span>
                    {typeof p.stock_total === 'number' && <span style={{fontSize:'.8rem', opacity:.7}}>Stock: {p.stock_total}</span>}
                  </div>
                </a>
              ))}
            </div>
            <div style={{marginTop:8}}>
              <a href={`/coleccion/${encodeURIComponent(found.collection)}`} style={{color:'#1e88e5', textDecoration:'underline'}}>Ver toda la colección</a>
            </div>
          </div>
        ) }]);
      } else if (Array.isArray(found) && found.length) {
        setMessages(m => [...m, { role: 'bot', type: 'jsx', content: (
          <div>
            <div style={{fontWeight:700, marginBottom:4}}>Resultados encontrados en el catálogo:</div>
            <div className="sf-product-list">
              {found.slice(0, 6).map(p => (
                <a key={p.id} className="sf-prod" href={`/producto/${p.id}`}>
                  <img src={p.image_preview || (p.imagenes?.general?.[0]) || '/img/placeholder.png'} alt={p.nombre} />
                  <div className="meta">
                    <span style={{fontWeight:700}}>{p.nombre}</span>
                    <span style={{opacity:.7, fontSize:'.9rem'}}>S/ {Number(p.precio).toFixed(2)}{p.precio_descuento ? ` → S/ ${Number(p.precio_descuento).toFixed(2)}`: ''}</span>
                    {typeof p.stock_total === 'number' && <span style={{fontSize:'.8rem', opacity:.7}}>Stock: {p.stock_total}</span>}
                  </div>
                </a>
              ))}
            </div>
          </div>
        ) }]);
      }
      setMessages(m => [...m, { role: 'bot', type: 'text', content: 'Consultando IA…' }]);
      try {
        const r = await fetch(`${API_BASE}/api/chat/ai/`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ message: text, open_domain: true })
        });
        let j = null;
        try { j = await r.json(); } catch {}
        const answer = (j && (j.data?.answer || j.answer || j.data?.text || j.text || j.message)) || '';
        if (r.ok && String(answer).trim().length) {
          setMessages(m => m.slice(0, -1).concat([{ role: 'bot', type: 'text', content: String(answer).trim() }]));
        } else {
          setMessages(m => m.slice(0, -1).concat([{ role: 'bot', type: 'text', content: 'Sin respuesta de IA. Intenta refinar tu búsqueda.' }]));
        }
      } catch {
        setMessages(m => m.slice(0, -1).concat([{ role: 'bot', type: 'text', content: 'Error de red al llamar IA.' }]));
      } finally {
        setLoading(false);
      }
    }

    // Sugerencias simples desde catálogo si IA no responde
    function renderSuggestions() {
      if (!catalog?.products?.length) return null;
      const polosMujer = catalog.products.filter(p => (p.categoria?.nombre || '').toLowerCase().includes('mujer') && (p.nombre || '').toLowerCase().includes('polo'));
      if (!polosMujer.length) return null;
      return (
        <div className="sf-product-list">
          <div style={{fontWeight:700, marginBottom:4}}>Algunas sugerencias de polos para mujer:</div>
          {polosMujer.slice(0, 4).map(p => (
            <a key={p.id} className="sf-prod" href={`/producto/${p.id}`}>
              <img src={p.image_preview || (p.imagenes?.general?.[0]) || '/img/placeholder.png'} alt={p.nombre} />
              <div className="meta">
                <span style={{fontWeight:700}}>{p.nombre}</span>
                <span style={{opacity:.7, fontSize:'.9rem'}}>S/ {Number(p.precio).toFixed(2)}{p.precio_descuento ? ` → S/ ${Number(p.precio_descuento).toFixed(2)}`: ''}</span>
                {typeof p.stock_total === 'number' && <span style={{fontSize:'.8rem', opacity:.7}}>Stock: {p.stock_total}</span>}
              </div>
            </a>
          ))}
        </div>
      );
    }

    return (
      <>
        <div className="sf-chat-button">
          <button aria-label={open ? 'Cerrar chat' : 'Abrir chat'} onClick={() => setOpen(o => !o)}>
            {open ? (
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                <path d="M6 6L18 18M6 18L18 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            ) : (
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" fill="none" />
              </svg>
            )}
          </button>
        </div>
        {open && (
          <div className="sf-chat-panel">
            <div className="sf-chat-header">
              <div className="sf-chat-title">
                <span className="sf-chat-svgicon" aria-label="logo">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24" height="24" viewBox="0 0 24 24" fill="none"
                    stroke="#000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                  >
                    <rect x="3" y="11" width="18" height="10" rx="2" />
                    <circle cx="12" cy="5" r="2" />
                    <path d="M12 7v4" />
                    <line x1="8" y1="16" x2="8" y2="16" />
                    <line x1="16" y1="16" x2="16" y2="16" />
                  </svg>
                  
                </span>
                <div>
                  <div>SmartFashion IA</div>
                </div>
              </div>
              <button className="sf-min-btn" aria-label="Minimizar" onClick={() => setOpen(false)}>
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <rect x="4" y="15" width="12" height="2" rx="1" fill="currentColor" />
                </svg>
              </button>
            </div>
            <div className="sf-chat-body" ref={bodyRef}>
              {messages.map((m, i) => (
                <div key={i} className={`sf-msg ${m.role}`}>{m.content}</div>
              ))}
              {!loading && renderSuggestions()}
            </div>
            <div className="sf-chat-input">
              <input
                type="text"
                placeholder="Pregúntame lo que quieras…"
                value={input}
                onChange={e => setInput(e.target.value)}
                onKeyDown={e => { if (e.key === 'Enter') { handleQuery(input); setInput(''); } }}
                disabled={loading}
              />
              <button onClick={() => { handleQuery(input); setInput(''); }} disabled={loading}>Enviar</button>
            </div>
          </div>
        )}
      </>
    );
  }
