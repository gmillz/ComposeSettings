package com.gmillz.example

data class CustomClass(val name: String, val label: String) {
    override fun toString(): String {
        return "$name|||$label"
    }

    companion object {
        fun fromString(value: String): CustomClass {
            val split = value.split("|||")
            return CustomClass(split[0], split[1])
        }
    }
}