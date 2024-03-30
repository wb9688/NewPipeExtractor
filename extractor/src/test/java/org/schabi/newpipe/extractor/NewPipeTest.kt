package org.schabi.newpipe.extractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.NewPipe.getService
import org.schabi.newpipe.extractor.NewPipe.getServiceByUrl
import org.schabi.newpipe.extractor.NewPipe.services
import org.schabi.newpipe.extractor.ServiceList.all

class NewPipeTest {
    @get:Throws(Exception::class)
    @get:Test
    val allServicesTest: Unit
        get() {
            Assertions.assertEquals(services!!.size, all().size)
        }

    @Test
    @Throws(Exception::class)
    fun testAllServicesHaveDifferentId() {
        val servicesId = HashSet<Int>()
        for (streamingService in services!!) {
            val errorMsg = ("There are services with the same id = " + streamingService!!.serviceId
                    + " (current service > " + streamingService.serviceInfo.name + ")")
            Assertions.assertTrue(servicesId.add(streamingService.serviceId), errorMsg)
        }
    }

    @get:Throws(Exception::class)
    @get:Test
    val serviceWithId: Unit
        get() {
            Assertions.assertEquals(getService(YouTube.serviceId), YouTube)
        }

    @get:Throws(Exception::class)
    @get:Test
    val serviceWithUrl: Unit
        get() {
            Assertions.assertEquals(getServiceByUrl("https://www.youtube.com/watch?v=_r6CgaFNAGg"), YouTube)
            Assertions.assertEquals(getServiceByUrl("https://www.youtube.com/channel/UCi2bIyFtz-JdI-ou8kaqsqg"), YouTube)
            Assertions.assertEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), YouTube)
            Assertions.assertEquals(getServiceByUrl("https://www.google.it/url?sa=t&rct=j&q=&esrc=s&cd=&cad=rja&uact=8&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DHu80uDzh8RY&source=video"), YouTube)
            Assertions.assertEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), SoundCloud)
            Assertions.assertEquals(getServiceByUrl("https://www.google.com/url?sa=t&url=https%3A%2F%2Fsoundcloud.com%2Fciaoproduction&rct=j&q=&esrc=s&source=web&cd="), SoundCloud)
        }
}
