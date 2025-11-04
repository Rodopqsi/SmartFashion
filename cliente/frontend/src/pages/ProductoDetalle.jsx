import React, { useState } from "react";
import "../pages/DetalleEstilo.css";


export default function ProductoDetalle() {
  const imagenes = [
  "/img/modelo2.jpg",
  "/img/modelo3.jpg",
  "/img/modelo4.jpg",
];


  const [imagen, setImagen] = useState(imagenes[0]);
  const [cantidad, setCantidad] = useState(1);

  return (
    <div className="detalle-container">
      {/* HEADER */}
      <header className="detalle-header">
        <div className="logo">SMARTFASHION</div>
        <div className="header-icons">
          <select className="menu">
            <option>Categorías</option>
            <option>Ropa</option>
            <option>Accesorios</option>
          </select>
          <a href="#">Catálogo</a>
          <i className="fa fa-heart"></i>
          <i className="fa fa-shopping-cart"></i>
          <i className="fa fa-user"></i>
        </div>
      </header>

      {/* MAIN */}
      <main className="detalle-main">
        {/* Imagen principal */}
        <div className="detalle-imagen">
          <img src={imagen} alt="Producto" className="imagen-principal" />
          <div className="miniaturas">
            {imagenes.map((src, i) => (
              <img
                key={i}
                src={src}
                alt="Miniatura"
                onClick={() => setImagen(src)}
              />
            ))}
          </div>
        </div>

        {/* Información */}
        <div className="detalle-info">
          <h2>Blusa Elegante Negra</h2>
          <div className="estrellas">
            ⭐⭐⭐⭐☆ <span>(24 reseñas)</span>
          </div>

          <p className="descripcion">
            Blusa elegante de corte moderno, perfecta para ocasiones especiales.
            Confeccionada en tela de alta calidad con acabados refinados.
          </p>

          <div className="tallas">
            <p>Talla</p>
            {["S", "M", "L", "XL"].map((t) => (
              <button key={t}>{t}</button>
            ))}
          </div>

          <div className="colores">
            <p>Color</p>
            <div className="color-circulos">
              <span className="color negro"></span>
              <span className="color azul"></span>
              <span className="color lila"></span>
            </div>
          </div>

          <div className="cantidad">
            <p>Cantidad</p>
            <div className="contador">
              <button onClick={() => setCantidad(Math.max(1, cantidad - 1))}>-</button>
              <span>{cantidad}</span>
              <button onClick={() => setCantidad(cantidad + 1)}>+</button>
            </div>
          </div>

          <span className="stock">15 en stock</span>

          <div className="botones-carrito">
            <button className="btn-agregar">🛒 Agregar al carrito</button>
            <button className="btn-favorito">♡</button>
          </div>
        </div>
      </main>

      {/* Productos relacionados */}
      <section className="relacionados">
        <h3>Productos relacionados</h3>
        <div className="grid-relacionados">
          <div className="card-rel">
            <img src="/img/modelo2.jpg" alt="Blusa Floral Primavera" />
            <h4>Blusa Floral Primavera</h4>
            <p>S/ 79.90</p>
          </div>

          <div className="card-rel">
            <img src="img/modelo3.jpg" alt="Blusa Seda Blanca" />
            <h4>Blusa Seda Blanca</h4>
            <p>S/ 109.90</p>
          </div>
        </div>
      </section>
    </div>
  );
}
