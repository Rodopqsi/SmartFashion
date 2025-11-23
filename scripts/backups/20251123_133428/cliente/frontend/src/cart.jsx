import React, { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { useAuth } from './auth.jsx'

const IGV_RATE = 0.18

const CartContext = createContext(null)

export function CartProvider({ children }){
  const { user } = useAuth() || {}
  const storageKey = useMemo(()=> `sf_cart_${user?.id || 'guest'}`,[user?.id])
  const [items, setItems] = useState(()=>{
    if (typeof window === 'undefined') return []
    try{
      const raw = localStorage.getItem(storageKey)
      return raw ? JSON.parse(raw) : []
    }catch{ return [] }
  })

  // When user changes, load their cart
  const prevKey = useRef(storageKey)
  useEffect(()=>{
    if (prevKey.current !== storageKey){
      try{
        const raw = localStorage.getItem(storageKey)
        setItems(raw ? JSON.parse(raw) : [])
      }catch{ setItems([]) }
      prevKey.current = storageKey
    }
  },[storageKey])

  // Persist
  useEffect(()=>{
    try{ localStorage.setItem(storageKey, JSON.stringify(items)) }catch{}
  }, [items, storageKey])

  const count = useMemo(()=> items.reduce((a,i)=> a + Number(i.qty||0), 0), [items])
  const subtotal = useMemo(()=> items.reduce((a,i)=> a + Number(i.price||0) * Number(i.qty||0), 0), [items])
  const igv = useMemo(()=> subtotal * IGV_RATE, [subtotal])
  const total = useMemo(()=> subtotal + igv, [subtotal, igv])

  const addItem = (p, { size, color, qty = 1 }={}) => {
    const key = [p.id, size?.id || size, color?.id || color].filter(Boolean).join('-')
    const payload = {
      key,
      productId: p.id,
      name: p.nombre || p.name,
      price: Number(p.precio ?? p.price ?? 0),
      image: p.image_preview || p.image,
      sizeId: size?.id || size || null,
      sizeName: size?.nombre || size?.name || null,
      colorId: color?.id || color || null,
      colorName: color?.nombre || color?.name || null,
      colorHex: color?.codigo_hex || color?.hex || null,
      qty: Number(qty || 1)
    }
    setItems(curr => {
      const idx = curr.findIndex(i => i.key === payload.key)
      if (idx >= 0){
        const next = [...curr]
        next[idx] = { ...next[idx], qty: Math.min(99, next[idx].qty + payload.qty) }
        return next
      }
      return [...curr, payload]
    })
  }

  const updateQty = (key, qty) => {
    setItems(curr => curr.map(i => i.key === key ? { ...i, qty: Math.max(1, Math.min(99, Number(qty)||1)) } : i))
  }
  const increment = (key) => setItems(curr => curr.map(i => i.key === key ? { ...i, qty: Math.min(99, i.qty+1) } : i))
  const decrement = (key) => setItems(curr => curr.map(i => i.key === key ? { ...i, qty: Math.max(1, i.qty-1) } : i))
  const remove = (key) => setItems(curr => curr.filter(i => i.key !== key))
  const clear = () => setItems([])

  const value = { items, addItem, updateQty, increment, decrement, remove, clear, count, subtotal, igv, total, IGV_RATE }
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export const useCart = () => useContext(CartContext)
