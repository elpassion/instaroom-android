package pl.elpassion.instaroom.util

fun <E> MutableList<E>.replaceWith(elements: Collection<E>) = run { clear(); addAll(elements) }
