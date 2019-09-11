import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import play.api.libs.Files

import scala.concurrent.duration._

class BookentryPerfomanceTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:9000") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

    val add_account_1_json = """{"accountNumber":"A${n}", "accountName":"A${n}", "accountType":"Savings", "currencyCode": "GBP"}""";
    val add_account_2_json = """{"accountNumber":"B${n}", "accountName":"B${n}", "accountType":"Savings", "currencyCode": "GBP"}""";
    val deposit_json = """{"reference": "DEP${n}", "accountNumber": "A${n}", "currencyCode": "GBP", "amount": 100.00}""";
    val transfer_json = """{"reference": "TRF${n}", "sourceAccount": "A${n}", "destinationAccount": "B${n}", "currencyCode": "GBP", "amount": 10.00}""";

  val scn = scenario("AccountsPerfTest")
    .repeat(10, "n") {
      exec(http("add_account_1")
          .post("/api/accounts")
          .body(StringBody(add_account_1_json)).asJson
          .check(status.is(200))
        )
        .exec(http("add_account_2")
          .post("/api/accounts")
          .body(StringBody(add_account_2_json)).asJson
          .check(status.is(200))
      ).pause(1)
    }
    .repeat(10, "n") {
      exec(http("read_account_1")
        .get("/api/accounts/A${n}")
        .check(status.is(200))
      )
        .exec(http("read_account_2")
          .get("/api/accounts/B${n}")
          .check(status.is(200))
        )
        .exec(http("deposit_account_1")
          .post("/api/accounts/deposit")
          .body(StringBody(deposit_json)).asJson
          .check(status.is(200))
        )
        .exec(http("tranfer_from_acc1_to_acc2")
          .post("/api/accounts/transfer")
          .body(StringBody(transfer_json)).asJson
          .check(status.is(200)))
    }

  //setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
  setUp(scn.inject(rampConcurrentUsers(1).to(10).during(5 seconds)).protocols(httpProtocol))
}
