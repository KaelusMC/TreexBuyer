# TreexBuyer

**TreexBuyer** - Современный плагин на создание скупщика для вашего сервера.

## **Преимущества:**

- **Неограниченное количество меню**
- **Неограниченное количество скупщиков**
- **Сдача предметов из инвентаря в меню**
- **Сдача предметов кликом по предмету**
- **Полная кастомизация**
- **Авто-скупка**
- **Система прокачки**

## Changelog

### [1.5.2] — 2025-04-23 02:05  
- Оптимизация работы с БД

### [1.5.1] — 2025-04-22 08:31  
- Фикс ошибок в консоли при клике

### [1.5.0] — 2025-04-21 22:45  
- `[SELL_ITEM] amount/all` - Продать данный предмет с определенным количеством
- Теперь проверяется вся броня на наличие предмета для авто-скупки предотвращая дюпа.
- Возможность ставить `basehead` на предметы (кастомные головы)
- Теперь проверяется является ли предмет ванильным, чтобы не скупать кастомные предметы например трпаки.
- Обновление предметов раз в определённое время.
- Добавлена возможность отключать авто-скупку в определенных мирах:
  ```yaml
  autoBuy:
    enable: "&aВключён"
    disable: "&cВыключен"
    message: "&eПродано предметов на сумму: &a%sum% &eи получено &b%score% очков"
    disabled-worlds:
      - duel-1
      - duel-2
  ```

## Нашли баг?

Нашли баг или хотите предложить идею?
- Создайте [Issue на GitHub](https://github.com/MrJetby/TreexBuyer/issues).
- Или свяжитесь с нами в Discord: [TreexStudio](https://discord.gg/RcnDgRQVqY)

Спасибо за использование **TreexBuyer**!
