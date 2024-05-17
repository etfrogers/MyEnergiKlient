package com.etfrogers.kjlr

class KJLRClient {
    fun getString() : String{
        return "Hello from KJLR!"
    }
}

fun main(){
    println(KJLRClient().getString())
}