package com.example.hammami.util

import com.example.hammami.models.Coupon

typealias CouponListResource = Resource<List<Coupon>>
typealias IntListResource = Resource<List<Int>>

sealed class Resource<T> (
    val data: T?= null,
    val message: String?= null
    ) {
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(message: String, data: T?=null): Resource<T>(message = message)
    class Loading<T>: Resource<T>()

    class Unspecified<T>: Resource<T>()
}