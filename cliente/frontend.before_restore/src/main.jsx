import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './auth.jsx'
import { ToastProvider } from './toast.jsx'
import { FavoritesProvider } from './favorites.jsx'
import { CartProvider } from './cart.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Profile from './pages/Profile.jsx'
import Favorites from './pages/Favorites.jsx'
import ForgotPassword from './pages/ForgotPassword.jsx'
import Catalog from './pages/Catalog.jsx'
import ProductDetail from './pages/ProductDetail.jsx'
import ClientLayout from './layouts/ClientLayout.jsx'
import Cart from './pages/Cart.jsx'
import CheckoutSuccess from './pages/CheckoutSuccess.jsx'
import CheckoutCancel from './pages/CheckoutCancel.jsx'
import Addresses from './pages/Addresses.jsx'
import Claims from './pages/Claims.jsx'
import Returns from './pages/Returns.jsx'
import Collection from './pages/Collection.jsx'

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ToastProvider>
      <AuthProvider>
        <FavoritesProvider>
        <CartProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<ClientLayout />}>
              <Route path="/" element={<Home />} />
              <Route path="/catalogo" element={<Catalog />} />
              <Route path="/producto/:id" element={<ProductDetail />} />
              <Route path="/colecciones/:slug" element={<Collection />} />
              <Route path="/login" element={<Login mode="login" />} />
              <Route path="/register" element={<Login mode="register" />} />
              <Route path="/perfil" element={<Profile />} />
              <Route path="/direcciones" element={<Addresses />} />
              <Route path="/reclamos" element={<Claims />} />
              <Route path="/devoluciones" element={<Returns />} />
              <Route path="/favoritos" element={<Favorites />} />
              <Route path="/recuperar" element={<ForgotPassword />} />
              <Route path="/carrito" element={<Cart />} />
              <Route path="/checkout/success" element={<CheckoutSuccess />} />
              <Route path="/checkout/cancel" element={<CheckoutCancel />} />
            </Route>
          </Routes>
        </BrowserRouter>
        </CartProvider>
        </FavoritesProvider>
      </AuthProvider>
    </ToastProvider>
  </React.StrictMode>
)


