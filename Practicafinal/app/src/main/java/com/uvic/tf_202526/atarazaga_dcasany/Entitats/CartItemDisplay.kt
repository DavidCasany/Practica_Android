package com.uvic.tf_202526.atarazaga_dcasany.Entitats

data class CartItemDisplay(
    val nomProducte: String,
    val preuUnitari: Double,
    val quantitat: Int,
    val idItemCarro: Int // Per si volem esborrar-lo
) {
    // Propietat calculada: Preu total d'aquesta línia
    val preuTotal: Double
        get() = preuUnitari * quantitat
}