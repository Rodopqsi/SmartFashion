import React, { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(()=>{
    try { return JSON.parse(localStorage.getItem('auth_user')||'null') } catch { return null }
  })
  const [tokens, setTokens] = useState(()=>{
    try { return JSON.parse(localStorage.getItem('auth_tokens')||'null') } catch { return null }
  })

  const saveTokens = (t) => {
    setTokens(t)
    if (t) localStorage.setItem('auth_tokens', JSON.stringify(t)); else localStorage.removeItem('auth_tokens')
  }

  const saveUser = (u) => {
    setUser(u)
    if (u) localStorage.setItem('auth_user', JSON.stringify(u)); else localStorage.removeItem('auth_user')
  }

  async function parseRes(res){
    let data
    try { data = await res.json() } catch { data = {} }
    return data
  }

  const apiBase = '' // proxy maneja /api -> backend

  const login = useCallback(async ({ email, password }) => {
    const res = await fetch(`${apiBase}/api/auth/token/`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ username: email, password }) })
    const data = await parseRes(res)
    if (!res.ok) throw new Error(data.detail || 'Error de login')
    saveTokens({ access:data.access, refresh:data.refresh })
    saveUser({ username:data.user?.username || email, email })
  }, [])

  const register = useCallback(async ({ email, password, username }) => {
    const res = await fetch(`${apiBase}/api/auth/register/`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ email, password, username }) })
    const data = await parseRes(res)
    if (!res.ok) throw new Error(data.detail || 'Error de registro')
    // Requiere verificación por código; no guardar tokens/usuario aún
    return data
  }, [])

  const verifyEmail = useCallback(async ({ email, code }) => {
    const res = await fetch(`${apiBase}/api/auth/register/verify/`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ email, code }) })
    const data = await parseRes(res)
    if (!res.ok) throw new Error(data.detail || 'Error al verificar correo')
    saveTokens({ access:data.access, refresh:data.refresh })
    saveUser(data.user)
    return data
  }, [])

  const googleLogin = useCallback(async (credential) => {
    const res = await fetch(`${apiBase}/api/auth/google/`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ credential }) })
    const data = await parseRes(res)
    if (res.status === 202 && data?.need_username) {
      return { pending: true, ...data }
    }
    if (!res.ok) throw new Error(data.detail || 'Error Google OAuth')
    saveTokens({ access:data.access, refresh:data.refresh })
    saveUser(data.user)
    return data
  }, [])

  const completeGoogleUsername = useCallback(async ({ username, pending }) => {
    const res = await fetch(`${apiBase}/api/auth/google/complete/`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ username, pending }) })
    const data = await parseRes(res)
    if (!res.ok) throw new Error(data.detail || 'No se pudo completar Google')
    saveTokens({ access:data.access, refresh:data.refresh })
    saveUser(data.user)
    return data
  }, [])

  const logout = () => { saveTokens(null); saveUser(null) }

  // Inicio de recuperación: enviar código al email
  const requestPasswordReset = useCallback(async ({ email }) => {
    const res = await fetch(`${apiBase}/api/auth/password_reset/`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ email }) })
    const data = await parseRes(res)
    if (!res.ok) throw new Error(data.detail || 'No se pudo iniciar recuperación')
    return data
  }, [])

  // Verificar código y establecer nueva contraseña
  const verifyPasswordReset = useCallback(async ({ email, code, new_password }) => {
    const res = await fetch(`${apiBase}/api/auth/password_reset/verify/`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ email, code, new_password }) })
    const data = await parseRes(res)
    if (!res.ok) throw new Error(data.detail || 'Código inválido')
    return data
  }, [])

  return <AuthContext.Provider value={{ user, tokens, login, register, verifyEmail, logout, googleLogin, completeGoogleUsername, requestPasswordReset, verifyPasswordReset }}>{children}</AuthContext.Provider>
}

export function useAuth(){ return useContext(AuthContext) }
