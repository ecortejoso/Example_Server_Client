NANOXML=/net/raquella/ldc/redes/nanoxml/java/nanoxml-lite-2.2.3.jar
Main: nodo.java cliente.java
	gcj -classpath $(NANOXML) -c nodo.java
	gcj -classpath $(NANOXML) -c cliente.java
	gcj --main=nodo -o nodo nodo.o $(NANOXML)
	gcj --main=cliente -o cliente cliente.o $(NANOXML)

