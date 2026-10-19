@navigation
Feature: Top navigation and layout
  As a visitor of mb.io
  I want a working top navigation
  So that I can reach the main areas of the platform on any desktop screen

  # EXTEND ME: add a row to the tables below for every item the header should contain.

  Background:
    Given I open the home page

  @smoke
  Scenario: Header renders with all expected items visible
    Then the header should be visible
    And the header logo should be visible
    And the header should contain the following items
      | item    |
      | Sign up |

  @smoke
  Scenario: Every visible header link has a destination
    Then every visible header link should have a destination

  @smoke
  Scenario Outline: Header item "<item>" links to the correct destination
    Then the header link "<item>" should point to a URL containing "<href contains>"
    When I click the header link "<item>"
    Then the browser should land on a URL containing "<lands on>"

    Examples:
      | item    | href contains | lands on     |
      | Sign up | /register     | trade.mb.io  |

  Scenario: Header logo returns the visitor to the home page
    Given I open the explore page
    When I click the header logo
    Then I should be on the home page