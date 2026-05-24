# Команда /site_sab

## Алгоритм

```plantuml
@startuml
participant "Клиент" as C
participant "Текущее API" as A
database "БД" as DB

C -> A: /site_sab
A -> DB: Установить UserSite.DONOR_MOS_SAB для пользователя
alt Есть credentials
    A -> DB: Очистить состояние
    A -> A: Вызвать /main
    A -> C: Отправить главное меню
else Нет credentials
    A -> DB: Установить состояние AWAITING_AUTH_CREDENTIALS
    A -> C: Отправить форму авторизации
end
@enduml
```

## Детальное описание алгоритма

1. **Получение команды** — пользователь выбирает "Шаболовка" в меню выбора сайта
2. **Установка сайта** — в БД для пользователя устанавливается `UserSite.DONOR_MOS_SAB`
3. **Проверка credentials**:
   - **Если credentials есть** — состояние очищается, вызывается `/main` с отображением главного меню
   - **Если credentials нет** — устанавливается состояние `AWAITING_AUTH_CREDENTIALS`, отправляется форма авторизации с URL сайта

Выбранный сайт: **donor-mos-sab.online** (Шаболовка)

## Входные параметры

| Параметр | Тип | Источник | Описание |
|----------|-----|----------|----------|
| chatId | Long | Telegram Update | Идентификатор чата пользователя |
| update | Update | Telegram API | Объект обновления от Telegram |

## Выходные параметры

| Параметр | Тип | Описание |
|----------|-----|----------|
| Сообщение | SendMessage | Главное меню ИЛИ форма авторизации |
| Состояние | UserState | Очищено ИЛИ AWAITING_AUTH_CREDENTIALS |
