import React, { createContext, useState, useContext, useCallback } from 'react'

const ToastContext = createContext(null)

export function ToastProvider({ children }){
  const [toasts, setToasts] = useState([])
  const push = useCallback((msg, type='info') => {
    const id = Date.now()+Math.random()
    setToasts(t => [...t, { id, msg, type }])
    setTimeout(()=> setToasts(t => t.filter(x=>x.id!==id)), 4000)
  }, [])
  return (
    <ToastContext.Provider value={{ push }}>
      {children}
      <div className="toast-container">
        {toasts.map(t=> <div key={t.id} className={`toast toast-${t.type}`}>{t.msg}</div>)}
      </div>
    </ToastContext.Provider>
  )
}
export function useToast(){ return useContext(ToastContext) }
