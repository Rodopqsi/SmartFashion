import React from 'react'
import './InputFloating.css'

export default function InputFloating({
  id,
  label,
  name,
  type = 'text',
  value = '',
  onChange,
  onBlur,
  required,
  autoComplete,
  disabled,
  maxLength,
  inputMode,
  icon, // optional left icon (string or node)
  error, // string message to show error state
  className = '',
  style = {},
}){
  const filled = value != null && String(value).length > 0
  return (
    <div className={`inputGroup ${filled ? 'filled' : ''} ${error ? 'has-error' : ''} ${disabled ? 'is-disabled' : ''} ${className}`} style={style}>
      {icon ? <span className="if-icon-left">{icon}</span> : null}
      <input
        id={id}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        required={required}
        autoComplete={autoComplete}
        disabled={disabled}
        maxLength={maxLength}
        inputMode={inputMode}
        placeholder=" "
        aria-invalid={!!error}
      />
      {label ? <label htmlFor={id || name}>{label}</label> : null}
      {error ? <div className="if-error-msg" role="alert">{error}</div> : null}
    </div>
  )
}
