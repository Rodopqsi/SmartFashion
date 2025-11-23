package com.ropa.smartfashionecommerce

// Auth
data class TokenPair(val access: String, val refresh: String)
data class AccessOnly(val access: String)

// Genérico DRF (si lo usas en otros endpoints)
data class PageResponse<T>(val count: Int, val next: String?, val previous: String?, val results: List<T>)

// Backend actual: /api/home/ devuelve { status, data { categories, featured_products, ... } }
data class CategoryDto(
	val id: Int,
	val nombre: String
)

data class CategoriaRef(
	val id: Int?,
	val nombre: String?
)

data class ProductCardDto(
	val id: Int,
	val nombre: String,
	val descripcion: String?,
	val precio: Double,
	val precio_descuento: Double?,
	val categoria: CategoriaRef?,
	val image_preview: String?,
	val stock_total: Int
)

data class AppliedFiltersDto(
	val category_id: Int?,
	val q: String?,
	val size: Int?,
	val color: Int?
)

data class HomeDataDto(
	val categories: List<CategoryDto>,
	val featured_products: List<ProductCardDto>,
	val banners: List<Any>?,
	val applied_filters: AppliedFiltersDto
)

data class HomeResponse(
	val status: String,
	val data: HomeDataDto
)

// /api/sizes/ y /api/colors/ estructura: { status: 'ok', data: [ ... ] }
data class SizeDto(
	val id: Int,
	val nombre: String,
	val tipo: String?
)

data class ColorDto(
	val id: Int,
	val nombre: String,
	val codigo_hex: String?
)

data class BaseListResponse<T>(
	val status: String,
	val data: List<T>
)