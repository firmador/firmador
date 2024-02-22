# Useful command for building/running flatpaks

install-platform: 
	flatpak install org.gnome.Platform/x86_64/45


build:
	flatpak-builder --force-clean build-dir cr.libre.firmador.yml


build-and-install:
	flatpak-builder --user --install --force-clean build-dir cr.libre.firmador.yml


build-one-file-bundle:
	flatpak-builder build-dir cr.libre.firmador.yml
	flatpak build-export repository build-dir
	flatpak build-bundle repository firmador.flatpak cr.libre.firmador


install-one-file-bundle:
	flatpak install --user firmador.flatpak 


install-one-file-bundle-system-wide:
	flatpak install firmador.flatpak


run:
	flatpak run cr.libre.firmador 


uninstall:
	flatpak uninstall cr.libre.firmador 


clean:
	rm -rf .flatpak-builder
	rm -rf build-dir
	rm -rf repository
	rm -f firmador.flatpak
