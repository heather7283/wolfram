package io.github.heather7283.wolfram.utils

infix fun Boolean.implies(other: Boolean) = (!this || other)
