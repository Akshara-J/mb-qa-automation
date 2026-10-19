@network
Feature: Network responses (bonus)
  As a QA engineer
  I want key pages to answer correctly at HTTP level
  So that outages and bad deployments are caught before a browser even opens

  Scenario Outline: Key page "<path>" responds successfully with HTML
    When I request "<path>" over HTTP
    Then the response status should be 200
    And the response content type should contain "text/html"

    Examples:
      | path                                 |
      | /en-AE                               |
      | /en/explore                          |
      | /en/about/privacy-policy             |
      | /en/about/cookie-policy              |
      | /en/about/terms-conditions           |
      | /en/about/client-agreement           |
      | /en/about/acceptable-use-policy      |
