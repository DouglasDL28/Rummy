# Rummy
Implementación de Rummy para el curso de Redes 2021.  
Realizado por: 
* Douglas de León 18037
* Rodrigo Garoz 18102
* Gerardo Méndez 18239

## Contenidos
Este repositorio cuenta con una implementación del juego *Rummy* realizado con Java, utilizando Sockets. Se encuentran dos archivos, server.java contiene el servidor, el que se encarga de coordinar el juego, llevando cuenta de las mesas y sus cartas. El segundo archivo, client.java, contiene los clientes que se pueden conectar para jugar en el servidor. Las instrucciones del juego se imprimen al ejectutar el cliente.  

## Protocolo
Se utilizó un protocolo simple para manejar la comunicación entre el cliente y el servidor. Este consiste en agregar un pequeño prefijo a cada mensaje enviado hacia el servidor. Esto permite filtrar los mensajes del lado del servidor. Cada prefijo es agregado por el porgrama al momento de enviar el mensaje y es delimitado con un caracter especial, lo que permite que no haya interferencia por parte del mensaje elegido por el usuario. Los prefijos utilizados son los siguientes:

|Prefijo|Uso|
|---|---|
|0|Cambio de nombre del usuario actual|
|1|Mensaje a todos los usuarios en la mesa|
|3|Movimiento en el juego|
