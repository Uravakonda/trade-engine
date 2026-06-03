Feature: Trade order submission
  As a trader
  I want to submit buy and sell orders via the REST API
  So that they are queued for processing

  Background:
    Given the trade engine is running

  Scenario: Successfully submit a valid BUY order
    Given a user "alice" is ready to trade
    When I submit a BUY order for "AAPL" at price "185.50" with quantity "10"
    Then the response status should be 202
    And the response should contain status "REQUESTED"
    And the response should contain a valid eventId

  Scenario: Reject a trade request with no ticker
    Given a user "bob" is ready to trade
    When I submit a BUY order with no ticker at price "100.00" with quantity "5"
    Then the response status should be 400

  Scenario: Successfully submit a valid SELL order
    Given a user "charlie" is ready to trade
    When I submit a SELL order for "MSFT" at price "420.00" with quantity "2"
    Then the response status should be 202
    And the response should contain status "REQUESTED"