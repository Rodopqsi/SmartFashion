import React from 'react';
// 1. 🔑 Importar useNavigate de React Router
import { useNavigate } from 'react-router-dom'; 
import './Colects.css'; 

// Datos estáticos: Las rutas son relativas a la carpeta 'public'
const collectionsData = [
    { 
        id: 1, 
        title: "Verano 2025", 
        // 🔑 Añadir el slug para la URL
        slug: "verano-2025", 
        description: "Descubre esta increíble colección", 
        products: 34, 
        date: "2025-02-15", 
        imageAlt: "Mujeres con ropa de verano en la playa", 
        imageSrc: "/img/verano.png" 
    },
    { id: 2, 
        title: "Invierno 2025", 
        slug: "invierno-2025", 
        description: "Descubre esta increíble colección", 
        products: 18, 
        date: "2025-05-20", 
        imageAlt: "Personas con abrigos de invierno", 
        imageSrc: "/img/invierno.png" 
    },
    { id: 3, 
        title: "Primavera 2025", 
        slug: "primavera-2025", 
        description: "Descubre esta increíble colección", 
        products: 15, 
        date: "2025-03-15", 
        imageAlt: "Personas con ropa ligera floral", 
        imageSrc: "/img/primavera.png" 
    },
    { id: 4, 
        title: "Otoño 2025", 
        slug: "otono-2025", 
        description: "Descubre esta increíble colección", 
        products: 3, 
        date: "2025-02-15", 
        imageAlt: "Personas con ropa de otoño y chaquetas", 
        imageSrc: "/img/otoño.png" 
    },
    { id: 5, 
        title: "Ropa Deportiva", 
        slug: "ropa-deportiva", 
        description: "Descubre esta increíble colección", 
        products: 18, 
        date: "2025-09-20", 
        imageAlt: "Personas haciendo ejercicio con ropa deportiva", 
        imageSrc: "/img/ropadeportiva.png" 
    },
];

// 1. Componente Tarjeta de Colección
// Ahora recibe el 'slug' y usa 'useNavigate'
const CollectionCard = ({ title, description, products, date, imageAlt, imageSrc, slug }) => {
    // 2. 🔑 Inicializar useNavigate
    const navigate = useNavigate();

    // Función que maneja el click y redirige a la ruta dinámica
    const handleClick = () => {
        // Redirige a /colecciones/verano-2025 (o el slug correspondiente)
        navigate(`/colecciones/${slug}`); 
    };

    return (
        // 3. 🔑 Añadir el manejador de eventos onClick a toda la tarjeta
        <div className="collection-card" onClick={handleClick}> 
            <div className="card-image">
                <img src={imageSrc} alt={imageAlt} /> 
            </div>
            <div className="card-info">
                <h3>{title}</h3>
                <p className="description">{description}</p>
                <div className="details">
                    <span className="products">{products} productos</span>
                    <span className="date">{date}</span>
                </div>
            </div>
        </div>
    );
};

// 2. Componente principal de Colecciones (ColeccionesScreen)
const ColeccionesScreen = () => {
    const navigate = useNavigate(); // 🔑 Inicializar useNavigate aquí también

    const handleGoBack = () => {
        // Usa navigate para ir al catálogo (asumimos la ruta '/')
        navigate('/catalogo'); 
    };

    return (
        <div className="colecciones-container">
            {/* Encabezado principal */}
            <header className="main-header">
                <div className="header-content">
                    <div>
                        <h1>Colecciones</h1>
                        <p>Explora nuestras colecciones por temporada</p>
                    </div>
                    {/* Usa handleGoBack para la navegación */}
                    <button onClick={handleGoBack} className="btn-volver">
                        ← Volver al Catálogo
                    </button>
                </div>
            </header>

            {/* Sección de la cuadrícula */}
            <section className="collections-section">
                <h2>Nuestras Colecciones</h2>
                
                <div className="collections-grid">
                    {collectionsData.map(collection => (
                        <CollectionCard 
                            key={collection.id}
                            {...collection}
                            // 🔑 Pasar el slug a la tarjeta para que pueda navegar
                            slug={collection.slug} 
                        />
                    ))}
                </div>
            </section>
            
            {/* Botón flotante */}
            <button className="floating-btn">?</button>
        </div>
    );
};

// 3. Exportación por defecto
export default ColeccionesScreen;