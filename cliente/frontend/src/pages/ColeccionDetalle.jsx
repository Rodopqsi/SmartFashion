import React from 'react';
import { useParams, useNavigate } from 'react-router-dom'; // 🔑 Hooks de React Router
import './ColeccionDetallaso.css'; 

// ====================================================================
// 1. SIMULACIÓN DE DATOS GLOBALES (Reemplaza esto con tu API en el futuro)
// ====================================================================

// Productos de Verano (basados en tu código original)
const summerProducts = [
    { id: 1, title: "Vestido Largo Blanco", description: "Vestido largo y ligero de tirantes para los días soleados. Con", price: 89.90, rating: 4.5, reviews: 24, imageSrc: "/img/vestido-largo-blanco.jpg" },
    { id: 2, title: "Top Cropped de Encaje Beige", description: "Top corto con detalles de encaje y tiras ajustables. Perfecto para combinar con una falda o shorts.", price: 74.90, rating: 4.7, reviews: 85, imageSrc: "/img/top-cropped-encaje.jpg" },
    { id: 3, title: "Falda Midi Floral", description: "Falda de corte midi con estampado floral y abertura lateral. Aporta feminidad y comodidad.", price: 89.90, rating: 4.0, reviews: 58, imageSrc: "/img/falda-midi-floral.jpg" },
    { id: 4, title: "Camisa de Lino Celeste", description: "Camisa de lino de manga corta, fresca y elegante. Perfecta para días cálidos o combinar con jeans o shorts.", price: 99.90, rating: 4.8, reviews: 64, imageSrc: "/img/camisa-lino-celeste.jpg" },
];

// Mapeo de slugs a información de la colección
const collectionsMap = {
    'verano-2025': { title: "Verano 2025", emoji: "☀️", products: summerProducts },
    'invierno-2025': { title: "Invierno 2025", emoji: "❄️", products: 
        [
            { id: 10, title: "Abrigo Lana Beige", description: "Abrigo de lana de diseño clásico, forrado, ideal para el frío.", price: 150.00, rating: 4.9, reviews: 100, imageSrc: "/img/abrigo-invierno.jpg" },
            { id: 11, title: "Chaqueta Térmica", description: "Chaqueta térmica ligera para deporte y actividades al aire libre.", price: 80.00, rating: 4.2, reviews: 50, imageSrc: "/img/chaqueta-termica.jpg" }
        ]
    },
    'primavera-2025': { title: "Primavera 2025", emoji: "🌷", products: [] },
    'otono-2025': { title: "Otoño 2025", emoji: "🍂", products: [] },
    'ropa-deportiva': { title: "Ropa Deportiva", emoji: "💪", products: [] },
};

// ====================================================================
// 2. COMPONENTE TARJETA DE PRODUCTO (Sin cambios)
// ====================================================================

const ProductCard = ({ title, description, price, rating, reviews, imageSrc }) => (
    <div className="product-card">
        <div className="card-image-container">
            <img src={imageSrc} alt={title} className="product-image" />
        </div>
        <div className="card-info">
            <h4>{title}</h4>
            <p className="product-description">{description}</p>
            <div className="rating-row">
                <span className="rating-star">⭐</span>
                <span className="rating-text">{rating}</span>
                <span className="reviews-count">({reviews})</span>
            </div>
            <div className="price-row">
                <span className="price-value">S/ {price.toFixed(2)}</span>
                <span className="price-discount">S/ 99.90</span>
            </div>
            <div className="actions-row">
                <button className="btn-cart">Carrito</button>
                <button className="btn-view">Ver</button>
                <div className="icon-actions">
                    <span className="icon-heart">❤️</span>
                    <span className="icon-share">🔗</span>
                </div>
            </div>
        </div>
    </div>
);

// ====================================================================
// 3. COMPONENTE PRINCIPAL (Dinámico)
// ====================================================================

const ColeccionDetalle = () => {
    // 🔑 Obtiene el parámetro de la URL (ej. 'verano-2025')
    const { collectionSlug } = useParams(); 
    const navigate = useNavigate();

    // Busca los datos de la colección usando el slug
    const currentCollection = collectionsMap[collectionSlug];

    // Manejo si la colección no existe o es nula (idealmente mostrar un loader)
    if (!currentCollection) {
        return (
            <div style={{padding: '50px', textAlign: 'center', fontSize: '1.5rem'}}>
                Colección "{collectionSlug}" no encontrada.
                <button onClick={() => navigate('/colecciones')} style={{marginLeft: '20px'}}>Volver a Colecciones</button>
            </div>
        );
    }

    const { title, emoji, products } = currentCollection;
    
    // Duplicamos productos para simular una cuadrícula llena, si hay productos
    const displayedProducts = (products.length > 0) ? [...products, ...products] : [];

    const handleNavigation = (path) => {
        navigate(path);
    };

    return (
        <div className="detalle-coleccion-container">
            {/* Encabezado Principal (Barra Superior Oscura) */}
            <header className="main-header">
                <div className="header-content">
                    <div>
                        <h1>Colecciones</h1>
                        <p>Explora nuestras colecciones por temporada</p>
                    </div>
                    {/* Navegación al Catálogo */}
                    <button onClick={() => handleNavigation('/catalogo')} className="btn-volver">
                        ← Volver al Catálogo
                    </button>
                </div>
            </header>

            {/* Sub-Header de la Colección (Título Dinámico) */}
            <section className="collection-header">
                {/* Navegación a la Lista de Colecciones */}
                <button onClick={() => handleNavigation('/colecciones')} className="btn-volver-colecciones">
                    ← Volver a Colecciones
                </button>
                <div className="collection-title-row">
                    <span className="emoji">{emoji}</span>
                    <h2>{title}</h2> {/* 🔑 Título dinámico */}
                </div>
            </section>

            {/* Cuadrícula de Productos */}
            <section className="products-grid-section">
                <div className="products-grid">
                    {displayedProducts.length > 0 ? (
                        displayedProducts.map((product, index) => (
                            <ProductCard 
                                key={product.id + '-' + index} 
                                {...product}
                                // Precio duplicado para variación visual
                                price={product.price + 0.01 * index}
                            />
                        ))
                    ) : (
                        <p style={{gridColumn: '1 / -1', textAlign: 'center', color: '#888'}}>No hay productos disponibles en esta colección.</p>
                    )}
                </div>
            </section>

            {/* Botón flotante */}
            <button className="floating-btn">?</button>
        </div>
    );
};

export default ColeccionDetalle;