package com.tradeengine.trade_engine.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeengine.trade_engine.domain.TradeType;
import com.tradeengine.trade_engine.dto.TradeRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/*
 * Cucumber glue class — step definitions.
 *
 * Rules that apply here simultaneously:
 *   1. Cucumber forbids @Component on glue classes (causes duplicate bean definitions)
 *   2. Spring Boot forbids @AutoConfigureMockMvc on @Component beans
 *   3. IntelliJ flags @Autowired on non-Spring-managed classes as an error
 *
 * The solution that satisfies all three constraints is constructor injection.
 * Cucumber's Spring integration calls the constructor and passes the MockMvc
 * bean from the shared application context defined in CucumberSpringConfiguration.
 * No @Autowired annotation is needed — Spring resolves constructor arguments
 * automatically when there is exactly one constructor.
 *
 * This is also the preferred injection style in modern Spring regardless:
 * constructor injection makes dependencies explicit and testable.
 */
public class TradeStepDefinitions {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private MvcResult lastResult;
    private String currentUserId;

    /*
     * Cucumber's Spring integration sees this single constructor and injects
     * MockMvc from the Spring context set up by CucumberSpringConfiguration.
     * ObjectMapper is also injected from context — no @Autowired needed.
     */
    public TradeStepDefinitions(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Given("the trade engine is running")
    public void theTradeEngineIsRunning() {
        assertThat(mockMvc).isNotNull();
    }

    @Given("a user {string} is ready to trade")
    public void aUserIsReadyToTrade(String userId) {
        this.currentUserId = userId;
    }

    @When("I submit a BUY order for {string} at price {string} with quantity {string}")
    public void submitBuyOrder(String ticker, String price, String quantity) throws Exception {
        lastResult = performRequest(
                buildRequest(currentUserId, ticker, TradeType.BUY, price, quantity));
    }

    @When("I submit a SELL order for {string} at price {string} with quantity {string}")
    public void submitSellOrder(String ticker, String price, String quantity) throws Exception {
        lastResult = performRequest(
                buildRequest(currentUserId, ticker, TradeType.SELL, price, quantity));
    }

    @When("I submit a BUY order with no ticker at price {string} with quantity {string}")
    public void submitBuyOrderNoTicker(String price, String quantity) throws Exception {
        lastResult = performRequest(
                buildRequest(currentUserId, null, TradeType.BUY, price, quantity));
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(expectedStatus);
    }

    @And("the response should contain status {string}")
    public void responseShouldContainStatus(String expectedStatus) throws Exception {
        assertThat(lastResult.getResponse().getContentAsString()).contains(expectedStatus);
    }

    @And("the response should contain a valid eventId")
    public void responseShouldContainEventId() throws Exception {
        assertThat(lastResult.getResponse().getContentAsString()).contains("eventId");
    }

    private MvcResult performRequest(TradeRequest request) throws Exception {
        return mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    private TradeRequest buildRequest(String userId, String ticker,
                                      TradeType type, String price, String quantity) {
        TradeRequest req = new TradeRequest();
        req.setUserId(userId);
        req.setTicker(ticker);
        req.setType(type);
        req.setPrice(price != null ? new BigDecimal(price) : null);
        req.setQuantity(quantity != null ? new BigDecimal(quantity) : null);
        return req;
    }
}