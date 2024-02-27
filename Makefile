# Useful command for building/running firmador libre

build: 
	mvn clean package

run:
	java -jar target/firmador.jar

documentation:
	sphinx-build -b html ./docs/source ./docs/build/

clean:
	rm -rf target
