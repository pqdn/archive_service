### Сервис для архивации файлов

Для архивации файла можно вызвать следующий эндпоинт
```
curl --location --request POST 'http://localhost:8085/zipFile' \
--form 'file=@"/C:/Users/Tom/Downloads/test_data.txt"'
```

> В отличие от исходного задания при повторной архивации файла, будет возвращен код 208, вместо 304 (почему? - подробности в контроллере)

### Подготовка окружения
Для локального запуска, необходимо поднять Redis, сделать это можно например подняв docker-compose.yml из текущей директории
```shell
docker-compose up -d
```

### Задание

Java приложение REST сервис для архивирования файлов.
Один rest-endpoint принимающий файл (можно поток данных).
Можно сделать простейшую web-форму для выбора файла с локального диска. 
Или curl из командной строки. 

`curl –XPOST ‘http://localhost:8085/zipFile’ -F "file=@test-to-zip.docx; " > out.zip`

Сервис принимает входящий файл, архивирует его (можно стандартными средствами java зипования), 
отдает архивированный файл в ответ на запрос с кодом возврата 200 ок.
Вычисляет md5-cумму от входного файла и отдает ее в заголовке (header) ответа с именем Etag.
Получаемый архивированный файл складирует в кеш (файловый или базу данных), 
с тем, чтобы если вновь пришедший запрос содержит уже ранее архивированный файл, 
то не архивировать его заново, а отдать уже закешированный архивированный файл с кодом возврата 304.
В случае запроса с файлом нулевого размера или вообще без файла – выдавать ошибку 404.
Основная область оценки задания – работающий сервис и покрытие тестами всех компонент системы 
(сервис, контроллер, ошибки, кэш, коды возврата).

