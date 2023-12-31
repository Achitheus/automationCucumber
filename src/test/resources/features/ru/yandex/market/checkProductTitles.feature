Feature: Поиск товаров по фильтрам

  @e2e @FullVersion
  Scenario Outline: Фильтр "Производитель" - полная версия (яндекс ее "не любит")
    Given перейти на сайт 'http://ya.ru'
    And перейти в сервис 'Маркет'
    And "мягко" проверить, что город, определенный сервисом, "Москва"
    And в каталоге навести курсор на секцию 'Электроника', кликнуть на категорию 'Смартфоны'
    When фильтр 'Производитель' установлен значениями: <чекбоксы>
    Then все названия товаров содержат одно из ключевых слов: <слова-проверки>

    Examples:
      | чекбоксы                               | слова-проверки                         |
      | Black Shark                            | Black Shark                            |
      | OnePlus                                | OnePlus                                |
      | Google                                 | Google                                 |
      | Seals                                  | Seals                                  |
      | Apple                                  | iphone                                 |
      | ASUS                                   | ASUS                                   |
      | Black Shark,OnePlus, Google,Seals,ASUS | Black Shark,OnePlus, Google,Seals,ASUS |

