Plugins
==============

Son piezas de software que no necesariamente tienen que estár dentro de firmador, pero que permiten extender las funcionalidades de este.
``Plugin`` es la clase encargada de describir los métodos que debe tener un Plugin para ser compatible con Firmador libre, los Plugins se registran en Settings.  Los métodos son:

1. ``void start()``: Es el primer método que se llama en el plugin.
2. ``void startLogging()``: Permite iniciar el bitacoreo
3. ``void stop()``: Es el último método que se llama al cerrar la aplicación
4. ``boolean getIsRunnable()``: Indica si el plugin se ejecuta como un SwingWorker o no, esto para que el manejador de plugins pueda terminar el proceso cuando se cierra la aplicación.

Los plugins por defecto son: 

* DummyPlugin: Muestra información del sistema en la bitácora, se espera se autilizado de referencia.
* CheckUpdatePlugin: Revisa si existe una versión superior del Firmador Libre y si lo existe y se puede instalar lo hace.


``PluginManager`` es la clase encargada de cargar los plugins activos y llamar los métodos del ``Plugin`` según se requieran.

