package com.pet.cat.seo.service.ISeoService

import java.io.IOException

interface ISeoService {
    @Throws(IOException::class)
    fun getSiteMap(): String?
}