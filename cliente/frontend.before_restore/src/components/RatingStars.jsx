import React from 'react'
import './Rating.css'

const StarSvg = () => (
  <svg xmlns="http:
    <path pathLength={360} d="M12,17.27L18.18,21L16.54,13.97L22,9.24L14.81,8.62L12,2L9.19,8.62L2,9.24L7.45,13.97L5.82,21L12,17.27Z"/>
  </svg>
)

export default function RatingStars({ value = 0, onChange, readOnly = false, name = 'rating', size = 'md' }){
  if (readOnly) {
    const full = Math.round(Number(value) || 0)
    return (
      <span className="stars-readonly" aria-label={`Calificación ${value} de 5`}>
        {new Array(5).fill(0).map((_, i) => {
          const active = i < full
          return (
            <svg key={i} xmlns="http:
              <path d="M12,17.27L18.18,21L16.54,13.97L22,9.24L14.81,8.62L12,2L9.19,8.62L2,9.24L7.45,13.97L5.82,21L12,17.27Z" />
            </svg>
          )
        })}
      </span>
    )
  }

  
  return (
    <div className="rating" role="radiogroup" aria-label="Seleccionar calificación">
      {[5,4,3,2,1].map((score)=> (
        <React.Fragment key={score}>
          <input
            type="radio"
            id={`${name}-${score}`}
            name={name}
            value={score}
            checked={Number(value) === score}
            onChange={(e)=> onChange && onChange(Number(e.target.value))}
          />
          <label htmlFor={`${name}-${score}`} aria-label={`${score} estrellas`}>
            <StarSvg />
          </label>
        </React.Fragment>
      ))}
    </div>
  )
}
