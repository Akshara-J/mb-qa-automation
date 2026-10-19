@trading
Feature: Spot trading
  As a visitor
  I want to see live spot-market data
  So that I can decide which assets to explore

  @smoke
  Scenario: Home page groups trading pairs into market categories
    Given I open the home page
    Then these market categories should list trading pairs
      | category     |
      | Top Gainers  |
      | Trending Now |
      | Top Losers   |

  Scenario: Gainers really are up and losers really are down
    Given I open the home page
    Then every entry in "Top Gainers" should have a positive 24h change
    And every entry in "Top Losers" should have a negative 24h change

  Scenario Outline: Entries in "<category>" contain the expected data fields
    Given I open the home page
    Then every entry in "<category>" should contain the fields:
      | field  |
      | name   |
      | change |

    Examples:
      | category     |
      | Top Gainers  |
      | Trending Now |
      | Top Losers   |

  @smoke
  Scenario: Explore page renders the spot market with trading pairs
    Given I open the explore page
    Then the spot market should list at least 10 trading pairs

  Scenario: Every spot market entry carries name, price and 24h change
    Given I open the explore page
    Then every spot market entry should contain the fields:
      | field  |
      | name   |
      | price  |
      | change |
