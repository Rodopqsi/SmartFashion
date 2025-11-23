import React from 'react'
import './InputFloating.css'

export default function InputSelectFloating({
  id,
  label,
  name,
  value = '',
  onChange,
  onBlur,
  required,
  disabled,
  error,
  className = '',
  style = {},
  children,
}){
  const filled = value != null && String(value) !== ''
  return (
    <div className={`inputGroup ${filled ? 'filled' : ''} ${error ? 'has-error' : ''} ${disabled ? 'is-disabled' : ''} ${className}`} style={style}>
      <select
        id={id}
        name={name}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        required={required}
        disabled={disabled}
        aria-invalid={!!error}
      >
        {children}
      </select>
      {label ? <label htmlFor={id || name}>{label}</label> : null}
      {error ? <div className="if-error-msg" role="alert">{error}</div> : null}
    </div>
  )
}
