@content
Feature: Content and links
  As a visitor
  I want marketing content and app links to work
  So that I can learn about the platform and install the app

  Scenario Outline: Marketing banner "<banner>" renders in the page body of <page>
    Given I open the path "<page>"
    Then the banner image "<banner>" should be loaded inside the page body

    Examples:
      | page         | banner        |
      | /en-AE       | khabib-img    |
      | /en/explore  | promo-earn    |

  @smoke
  Scenario: The app download link is present on the home page
    Given I open the home page
    Then the "Download the app" link should be visible

  Scenario Outline: The download link resolves to the right store for <platform> devices
    Given I open the home page
    When I request the app download link as an "<platform>" device
    Then the response status should be below 400
    And the response should reference the "<store>" store

    Examples:
      | platform | store           |
      | iOS      | apps.apple.com  |
      | Android  | play.google.com |

