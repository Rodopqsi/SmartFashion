import React, { createContext, useContext, useMemo, useState } from 'react'
import { useAuth } from './auth.jsx'

const FavoritesContext = createContext(null)

export function FavoritesProvider({ children }){
  const { user } = useAuth() || {}
  const storageKey = useMemo(()=> `favorites_${user?.id || user?.email || 'guest'}`, [user])
  const [items, setItems] = useState(()=>{
    try { return JSON.parse(localStorage.getItem(storageKey) || '[]') } catch { return [] }
  })

  const save = (arr)=>{
    setItems(arr)
    try { localStorage.setItem(storageKey, JSON.stringify(arr)) } catch {}
  }

  const isFavorite = (id)=> items.some(p => String(p.id) === String(id))

  const toggleFavorite = (product)=>{
    if (!product || product.id == null) return
    if (isFavorite(product.id)) {
      const arr = items.filter(p => String(p.id) !== String(product.id))
      save(arr)
    } else {
      const slim = {
        id: product.id,
        nombre: product.nombre || product.name || `Producto ${product.id}`,
        precio: Number(product.precio ?? product.price ?? 0),
        image_preview: product.image_preview || product.image || product.images?.[0] || '',
        sizes: product.sizes || product.tallas || 'S,M,L,XL',
      }
      save([slim, ...items])
    }
  }

  const removeFavorite = (id)=> save(items.filter(p => String(p.id) !== String(id)))
  const clearFavorites = ()=> save([])

  return (
    <FavoritesContext.Provider value={{ items, isFavorite, toggleFavorite, removeFavorite, clearFavorites }}>
      {children}
    </FavoritesContext.Provider>
  )
}

export function useFavorites(){ return useContext(FavoritesContext) }
