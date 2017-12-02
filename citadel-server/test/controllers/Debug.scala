package controllers

import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.TestServer

class Debug extends PlaySpec {
  "TestServer" should {
    "keep running" in {
      val app = new GuiceApplicationBuilder().build()
      lazy val port: Int = 31337
      val testServer = TestServer(port, app)
      testServer.start()
      while (true)
        Thread.sleep(5000L)
    }
  }
}
