import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'
import './Home.css'

export default function ForgotPassword(){
  const { requestPasswordReset, verifyPasswordReset } = useAuth()
  const { push } = useToast()
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [newPass, setNewPass] = useState('')
  const [confirm, setConfirm] = useState('')
  const [loading, setLoading] = useState(false)

  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email)
  const passValid = newPass.length >= 6 && newPass === confirm

  const onRequest = async () => {
    if (!emailValid) { push('Email inválido','error'); return }
    setLoading(true)
    try {
      await requestPasswordReset({ email })
      push('Código enviado. Revisa tu correo','success')
      setStep(2)
    } catch(e){
      push(e.message||'No se pudo iniciar recuperación','error')
    } finally { setLoading(false) }
  }

  const onVerify = async () => {
    if (!code || !passValid) { push('Completa el código y la nueva contraseña','error'); return }
    setLoading(true)
    try {
      await verifyPasswordReset({ email, code, new_password: newPass })
      push('Contraseña actualizada. Inicia sesión','success')
      navigate('/login')
    } catch(e){
      push(e.message||'Código inválido','error')
    } finally { setLoading(false) }
  }

  return (
    <div className="login-layout">
      <div className="login-form-pane">
        <div className="brand-block">
          <h1 className="brand-title">SmartFashion</h1>
          <p className="brand-sub">Recuperar contraseña</p>
        </div>
        {step === 1 && (
          <div className="auth-form">
            <div className="form-field">
              <label htmlFor="email">Correo electrónico</label>
              <div className="input-wrapper icon-left">
                <span className="input-icon">📧</span>
                <input id="email" type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="tu@correo.com" />
              </div>
            </div>
            <button className="primary-btn solid dark" disabled={!emailValid||loading} onClick={onRequest}>
              {loading? 'Enviando...' : 'Enviar código'}
            </button>
            <div className="back-wrap">
              <button className="link-btn xs" onClick={()=>navigate('/login')}>← Volver</button>
            </div>
          </div>
        )}
        {step === 2 && (
          <div className="auth-form">
            <div className="form-field">
              <label>Código recibido</label>
              <div className="input-wrapper icon-left">
                <span className="input-icon">#</span>
                <input value={code} onChange={e=>setCode(e.target.value)} placeholder="6 dígitos" maxLength={6} />
              </div>
            </div>
            <div className="form-field">
              <label>Nueva contraseña</label>
              <div className="input-wrapper icon-left">
                <span className="input-icon">🔒</span>
                <input type="password" value={newPass} onChange={e=>setNewPass(e.target.value)} placeholder="••••••••" />
              </div>
            </div>
            <div className="form-field">
              <label>Confirmar contraseña</label>
              <div className="input-wrapper icon-left">
                <span className="input-icon">✔️</span>
                <input type="password" value={confirm} onChange={e=>setConfirm(e.target.value)} placeholder="Confirmar" />
              </div>
            </div>
            <button className="primary-btn solid dark" disabled={!passValid||loading} onClick={onVerify}>
              {loading? 'Guardando...' : 'Cambiar contraseña'}
            </button>
            <div className="back-wrap">
              <button className="link-btn xs" onClick={()=>setStep(1)}>← Cambiar email</button>
            </div>
          </div>
        )}
      </div>
      <div className="login-image-pane" aria-hidden="true" />
    </div>
  )
}
