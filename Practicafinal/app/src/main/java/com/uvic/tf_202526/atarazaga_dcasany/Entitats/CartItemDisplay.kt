package com.uvic.tf_202526.atarazaga_dcasany.Entitats

data class CartItemDisplay(
    val nomProducte: String,
    val preuOriginal: Double,
    val quantitat: Int,
    val idItemCarro: Int,


    val esOferta: Boolean,
    val preuOferta: Double
) {

    val preuUnitariFinal: Double
        get() = if (esOferta && preuOferta > 0.0) preuOferta else preuOriginal

    val preuTotal: Double
        get() = preuUnitariFinal * quantitat
}