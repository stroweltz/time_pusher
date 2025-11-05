## Time App - Быстрый старт и тестирование

### Требования
- Установлен и запущен Docker Desktop
- Порты и имена топиков/сервисов смотри в `docker-compose.yml`

---

### Запуск всего стека
```bash
docker-compose build --no-cache
```
```bash
docker-compose up -d
```
Проверить состояние:
```bash
docker-compose ps
```
Остановить:
```bash
docker-compose down
```

---

### Назначение сервисов
- **time-producer**: периодически публикует события во входной Kafka-топик.
- **time-consumer**:
  - при недоступности БД — бесконечно пытается переподключиться и продолжает работу после восстановления;
  - при прочих ошибках обработки — делает несколько ретраев и, если не удалось, отправляет сообщение в DTL-топик.
- **time-rest**: HTTP-ручка для чтения событий из БД с пагинацией.

---

### Проверка работы

#### REST-ручка (time-rest)
Получить первую страницу (по умолчанию size=20):
```bash
curl "http://localhost:8080/time-events?page=0&size=10"
```

#### Логи сервисов
```bash
docker compose logs -f time-producer
docker compose logs -f time-consumer
docker compose logs -f time-rest
```

#### Симуляция недоступной БД для проверки бесконечных ретраев у consumer
```bash
docker compose stop postgres
# наблюдаем логи time-consumer
docker compose logs -f time-consumer
docker compose start postgres
```

#### Прослушка DTL-топика
```bash
docker compose exec kafka \
  kafka-console-consumer --bootstrap-server localhost:9092 --topic time-records-dtl --from-beginning
```

---

### Тесты (локально)
Запускать из корня соответствующего модуля (Docker должен быть запущен для интеграционных тестов на Testcontainers):

Windows PowerShell:
```powershell
# time-rest
.\gradlew test

# time-consumer
.\gradlew test

# time-producer
.\gradlew test
```

Интеграционные тесты используют Testcontainers и поднимут свои контейнеры автоматически.

---

### Полезное
- Порты сервисов, имена топиков и параметры Kafka/Postgres — в `docker-compose.yml`.
- Если порт REST отличается от 8080 в вашей среде, уточняйте его в compose/настройках Spring.


