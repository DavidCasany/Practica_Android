package com.uvic.tf_202526.atarazaga_dcasany.Entitats

data class CartItemDisplay(
    val nomProducte: String,
    val preuOriginal: Double, // Canviem el nom per no confondre
    val quantitat: Int,
    val idItemCarro: Int,

    // Camps nous necessaris per calcular l'oferta
    val esOferta: Boolean,
    val preuOferta: Double
) {
    // 1. Calculem quin és el preu real unitari
    val preuUnitariFinal: Double
        get() = if (esOferta && preuOferta > 0.0) preuOferta else preuOriginal

    // 2. Calculem el total basant-nos en el preu final
    val preuTotal: Double
        get() = preuUnitariFinal * quantitat
}