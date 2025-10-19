import { useState, useMemo, useEffect } from 'react'
import { useAuth } from '../auth.jsx'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../toast.jsx'
import './Home.css'
import regImg from '../../img/img_register.jpg'
import loginImg from '../../img/img_login.jpg'

// Reutilizable para login o registro segun prop mode
export default function Login({ mode = 'login', onBack, onSwitch }) {
  const isLogin = mode === 'login'
  const navigate = useNavigate()
  const [form, setForm] = useState({ name:'', email:'', phone:'', password:'', confirm:'', remember:true })
  const [touched, setTouched] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const { login, register, verifyEmail, googleLogin, completeGoogleUsername } = useAuth() || {}
  const { push } = useToast() || { push:()=>{} }
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
  const [verifyStep, setVerifyStep] = useState({ active:false, email:'', code:'' })
  const [googlePending, setGooglePending] = useState({ active:false, email:'', suggested:'', pending:'', username:'' })

  const validators = {
    name: v => isLogin ? null : (!v.trim() ? 'Nombre requerido' : (v.trim().length < 2 ? 'Mínimo 2 caracteres' : null)),
    email: v => !v ? 'Email requerido' : (/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v) ? null : 'Email inválido'),
    phone: v => isLogin ? null : (!v ? null : (/^[0-9\-\s+()]{6,20}$/.test(v) ? null : 'Teléfono inválido')),
    password: v => !v ? 'Contraseña requerida' : (v.length < 6 ? 'Mínimo 6 caracteres' : null),
    confirm: (v, p) => isLogin ? null : (!v ? 'Confirmación requerida' : (v !== p ? 'Las contraseñas no coinciden' : null)),
  }

  const errors = useMemo(()=>({
    name: validators.name(form.name),
    email: validators.email(form.email),
    phone: validators.phone(form.phone),
    password: validators.password(form.password),
    confirm: validators.confirm(form.confirm, form.password),
  }), [form, mode])

  const isValid = isLogin
    ? !errors.email && !errors.password
    : !errors.name && !errors.email && !errors.password && !errors.confirm && !errors.phone

  const handleChange = e => {
    const { name, type, checked, value } = e.target
    setForm(f => ({ ...f, [name]: type === 'checkbox' ? checked : value }))
  }
  const handleBlur = e => setTouched(t => ({ ...t, [e.target.name]: true }))

  const handleSubmit = async e => {
    e.preventDefault()
    setTouched({ name:true, email:true, phone:true, password:true, confirm:true })
    if (!isValid) return
    setSubmitting(true)
    try {
      if (isLogin) {
        await login({ email:form.email, password:form.password })
        push('Sesión iniciada','success')
        navigate('/')
      } else {
        const username = (form.name?.trim()) || (form.email?.split('@')[0] || '')
        const res = await register({ email:form.email, password:form.password, username })
        push('Te enviamos un código a tu correo','success')
        setVerifyStep({ active:true, email: res.email || form.email, code:'' })
      }
    } catch (err) {
      console.error(err)
      push(err.message || 'Error', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  // Eliminado botón fake: el flujo real usa Google Identity Services (GSI)

  useEffect(()=>{
    window.onGoogleCredential = async (resp) => {
      if (!resp?.credential) { push('Credencial Google vacía','error'); return }
      try {
        const data = await googleLogin(resp.credential)
        if (data?.pending) {
          setGooglePending({ active:true, email:data.email, suggested:data.suggested_username, pending:data.pending, username:data.suggested_username })
          push('Elige tu nombre de usuario','info')
        } else {
          push('Google login OK','success')
          navigate('/')
        }
      } catch(e){
        push(e.message || 'Error Google','error')
      }
    }
    // Inicializar y renderizar el botón GSI de forma explícita
    let attempts = 0
    const maxAttempts = 20
    const tryInit = () => {
      attempts += 1
      const g = window.google?.accounts?.id
      if (g && clientId) {
        try {
          g.initialize({ client_id: clientId, callback: window.onGoogleCredential, auto_select: false, cancel_on_tap_outside: true })
          const el = document.getElementById('googleSignInDiv')
          if (el) {
            el.innerHTML = ''
            g.renderButton(el, { type:'standard', size:'large', theme:'outline', text: isLogin ? 'signin_with' : 'signup_with', shape:'rect', logo_alignment:'left' })
          }
        } catch (e) {
          console.error('GSI init error:', e)
        }
        return
      }
      if (attempts < maxAttempts) setTimeout(tryInit, 150)
      else if (!clientId) push('Falta VITE_GOOGLE_CLIENT_ID en .env','error')
    }
    tryInit()
  }, [googleLogin, push, isLogin])

  return (
    <>
    <div className="login-layout">
      <div className="login-form-pane">
        <div className="brand-block">
          <h1 className="brand-title">SmartFashion</h1>
          <p className="brand-sub">{isLogin ? 'Inicia sesión con tu cuenta' : 'Crea tu cuenta'}</p>
          {!isLogin && (
            <p className="brand-desc">Obtén acceso a las marcas más grandes.<br/>Hazte miembro hoy.</p>
          )}
        </div>
        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          {!isLogin && (
            <>
              <div className={`form-field ${touched.name && errors.name ? 'has-error' : ''}`}>
                <label htmlFor="name">Nombre completo <span className="req">*</span></label>
                <div className="input-wrapper icon-left">
                  <span className="input-icon">👤</span>
                  <input id="name" name="name" value={form.name} onChange={handleChange} onBlur={handleBlur} placeholder="Nombre completo" aria-invalid={!!(touched.name && errors.name)} />
                </div>
                {touched.name && errors.name && <div className="error-msg">{errors.name}</div>}
              </div>
              <div className={`form-field ${touched.email && errors.email ? 'has-error' : ''}`}>
                <label htmlFor="email">Correo electrónico <span className="req">*</span></label>
                <div className="input-wrapper icon-left">
                  <span className="input-icon">✅</span>
                  <input id="email" name="email" type="email" value={form.email} onChange={handleChange} onBlur={handleBlur} placeholder="Correo electrónico" autoComplete="email" aria-invalid={!!(touched.email && errors.email)} />
                </div>
                {touched.email && errors.email && <div className="error-msg">{errors.email}</div>}
              </div>
              <div className={`form-field ${touched.phone && errors.phone ? 'has-error' : ''}`}>
                <label htmlFor="phone">Teléfono</label>
                <div className="input-wrapper icon-left">
                  <span className="input-icon">📞</span>
                  <input id="phone" name="phone" value={form.phone} onChange={handleChange} onBlur={handleBlur} placeholder="Teléfono" inputMode="tel" aria-invalid={!!(touched.phone && errors.phone)} />
                </div>
                {touched.phone && errors.phone && <div className="error-msg">{errors.phone}</div>}
              </div>
            </>
          )}
          {isLogin && (
            <div className={`form-field ${touched.email && errors.email ? 'has-error' : ''}`}>
              <label htmlFor="email">Correo electrónico <span className="req">*</span></label>
              <div className="input-wrapper icon-left">
                <span className="input-icon">📧</span>
                <input id="email" name="email" type="email" value={form.email} onChange={handleChange} onBlur={handleBlur} placeholder="ingrese su correo electrónico" autoComplete="email" aria-invalid={!!(touched.email && errors.email)} />
              </div>
              {touched.email && errors.email && <div className="error-msg">{errors.email}</div>}
            </div>
          )}
          <div className={`form-field ${touched.password && errors.password ? 'has-error' : ''}`}>
            <label htmlFor="password">Contraseña <span className="req">*</span></label>
            <div className="input-wrapper icon-left">
              <span className="input-icon">🔒</span>
              <input id="password" name="password" type="password" value={form.password} onChange={handleChange} onBlur={handleBlur} placeholder="••••••••" autoComplete={isLogin? 'current-password':'new-password'} aria-invalid={!!(touched.password && errors.password)} />
            </div>
            {touched.password && errors.password && <div className="error-msg">{errors.password}</div>}
          </div>
          {!isLogin && (
            <div className={`form-field ${touched.confirm && errors.confirm ? 'has-error' : ''}`}>
              <label htmlFor="confirm">Confirmar contraseña <span className="req">*</span></label>
              <div className="input-wrapper icon-left">
                <span className="input-icon">✔️</span>
                <input id="confirm" name="confirm" type="password" value={form.confirm} onChange={handleChange} onBlur={handleBlur} placeholder="Confirmar contraseña" autoComplete="new-password" aria-invalid={!!(touched.confirm && errors.confirm)} />
              </div>
              {touched.confirm && errors.confirm && <div className="error-msg">{errors.confirm}</div>}
            </div>
          )}
          <div className="form-row between small">
            <label className="checkbox-inline">
              <input type="checkbox" name="remember" checked={form.remember} onChange={handleChange} /> ¡Recordarme!
            </label>
            {isLogin && <button type="button" className="link-btn xs" onClick={()=>navigate('/recuperar')}>¿Olvidó su contraseña?</button>}
          </div>
          <button type="submit" className="primary-btn solid dark" disabled={!isValid || submitting}>
            {submitting ? 'Procesando...' : (isLogin ? 'INICIAR SESIÓN' : 'CREAR CUENTA')}
          </button>
          <div className="divider"><span>{isLogin ? 'O inicia sesión con' : 'O regístrate con'}</span></div>
          {/* Botón Google renderizado por GSI aquí */}
          <div id="googleSignInDiv" style={{ display:'flex', justifyContent:'center' }} />
          <p className="alt-link">
            {isLogin ? '¿Aún no tienes cuenta? ' : '¿Ya tienes cuenta? '}
            <button type="button" className="link-btn inline" onClick={onSwitch}>{isLogin ? 'Crear cuenta' : 'Iniciar sesión'}</button>
          </p>
          <div className="back-wrap">
            <button type="button" className="link-btn xs" onClick={onBack}>← Volver</button>
          </div>
        </form>
      </div>
      <div className="login-image-pane" aria-hidden="true">
        <div className="login-img" style={{ position:'relative', width:'100%', height:'100%' }}>
          <img
            src={isLogin ? loginImg : regImg}
            alt="Auth side"
            style={{ width:'100%', height:'100%', objectFit:'cover', display:'block' }}
          />
        </div>
      </div>
    </div>
    {verifyStep.active && (
      <div className="modal-backdrop" style={{position:'fixed', inset:0, background:'rgba(0,0,0,.4)'}}>
        <div className="modal" style={{background:'#fff', padding:'20px', borderRadius:10, maxWidth:420, margin:'10% auto'}}>
          <h3>Verifica tu correo</h3>
          <p>Hemos enviado un código a <strong>{verifyStep.email}</strong>.</p>
          <input
            placeholder="Código de 6 dígitos"
            value={verifyStep.code}
            onChange={(e)=>setVerifyStep(v=>({...v, code:e.target.value}))}
            maxLength={6}
            style={{width:'100%', padding:'10px', border:'1px solid #ddd', borderRadius:6}}
          />
          <div style={{display:'flex', gap:8, marginTop:12}}>
            <button className="primary-btn" onClick={async ()=>{
              try {
                await verifyEmail({ email: verifyStep.email, code: verifyStep.code })
                push('Correo verificado','success')
                setVerifyStep({ active:false, email:'', code:'' })
                navigate('/')
              } catch(e){ push(e.message||'Código inválido','error') }
            }}>Confirmar</button>
            <button className="link-btn" onClick={()=>setVerifyStep({ active:false, email:'', code:'' })}>Cancelar</button>
          </div>
        </div>
      </div>
    )}

    {googlePending.active && (
      <div className="modal-backdrop" style={{position:'fixed', inset:0, background:'rgba(0,0,0,.4)'}}>
        <div className="modal" style={{background:'#fff', padding:'20px', borderRadius:10, maxWidth:420, margin:'10% auto'}}>
          <h3>Elige tu nombre de usuario</h3>
          <p>Para <strong>{googlePending.email}</strong></p>
          <input
            placeholder="Nombre de usuario"
            value={googlePending.username}
            onChange={(e)=>{
              const val = e.target.value
              // solo permitir letras, numeros, _ . - y limitar largo a 20
              const clean = val.replace(/[^a-zA-Z0-9_\.\-]/g, '').slice(0,20)
              setGooglePending(v=>({...v, username: clean}))
            }}
            style={{width:'100%', padding:'10px', border:'1px solid #ddd', borderRadius:6}}
          />
          <div style={{display:'flex', gap:8, marginTop:12}}>
            <button className="primary-btn" onClick={async ()=>{
              try {
                if (!/^[a-zA-Z0-9_\.\-]{3,20}$/.test(googlePending.username)) { push('Username inválido (3-20 chars: letras, números, _ . -)','error'); return }
                await completeGoogleUsername({ username: googlePending.username, pending: googlePending.pending })
                push('Cuenta Google creada','success')
                setGooglePending({ active:false, email:'', suggested:'', pending:'', username:'' })
                navigate('/')
              } catch(e){ push(e.message||'No se pudo completar','error') }
            }}>Guardar</button>
            <button className="link-btn" onClick={()=>setGooglePending({ active:false, email:'', suggested:'', pending:'', username:'' })}>Cancelar</button>
          </div>
        </div>
      </div>
    )}
    </>
  )
}

