// Minimal UBIGEO dataset for Peru (departamento -> provincia -> distritos)
// Extend as needed; keeps a few common examples and allows free text fallback in the form
export const UBIGEO = {
  'Lima': {
    'Lima': ['Miraflores','San Isidro','Santiago de Surco','San Borja','La Molina','San Miguel','Magdalena del Mar','Pueblo Libre','Barranco','Chorrillos','Surquillo','Jesús María'],
  },
  'Callao': {
    'Callao': ['Callao','Bellavista','La Perla','La Punta','Carmen de la Legua-Reynoso']
  },
  'Cusco': {
    'Cusco': ['Cusco','San Sebastián','San Jerónimo','Santiago','Wanchaq']
  },
  'Arequipa': {
    'Arequipa': ['Arequipa','Cerro Colorado','Alto Selva Alegre','Yanahuara','Cayma','José Luis Bustamante y Rivero']
  }
}

export const REGIONES = Object.keys(UBIGEO)