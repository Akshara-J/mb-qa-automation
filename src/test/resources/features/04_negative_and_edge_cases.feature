@negative
Feature: Negative and edge cases
  As a QA engineer
  I want to know how the site behaves when things go wrong
  So that real-world failures do not reach customers

  @smoke
  Scenario: Invalid route is handled gracefully
    When I open the invalid route "/en/this-page-does-not-exist-qa-check"
    Then the response status should be 404
    And the page should show a friendly not-found message
    And the page should not expose technical error details
    And the header should be visible

  Scenario: No broken links in the header navigation
    Given I open the home page
    When I collect all header links
    And I verify that every collected link responds
    Then no collected link should be broken


  @chromium-only
  Scenario: Page degrades gracefully when data/API requests fail
    Given these network requests are blocked:
      | pattern    |
      | *api*      |
      | *graphql*  |
    When I open the home page
    Then the header should be visible
    And the hero heading should be visible
    And the page should not show an unhandled application error
    And the page should not render placeholder values such as undefined or NaN

  @chromium-only
  Scenario: Content still loads on a slow connection
    Given the network is throttled to a slow connection
    When I open the home page
    Then the hero heading should become visible within 60 seconds
    And the page should not show an unhandled application error
