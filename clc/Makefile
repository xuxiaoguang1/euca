# Makefile wrapper around ant
#
#
include ../Makedefs

theme_dir	= ${TOP}/clc/modules/www/src/main/java/com/eucalyptus/webui/public/themes
web_prop_dir	= ${TOP}/clc/modules/www/conf

.PHONY: all build update_properties update_theme deps extras install

all: deps build
test: deps build-test

deps:
	@$(ANT) deps

update_properties: ${web_prop_dir}/eucalyptus-web.properties

${web_prop_dir}/eucalyptus-web.properties: ${web_prop_dir}/eucalyptus-web-default.properties ${TOP}/Makedefs
	@sed "s/\(^extensions =\).*/\1 ${extensions}/" < ${web_prop_dir}/eucalyptus-web-default.properties > ${web_prop_dir}/eucalyptus-web.properties
	@echo "Copied defaults to ${web_prop_dir}/eucalyptus-web.properties"

update_theme:
	@if test -d ${theme_dir}/${euca_theme} ; then \
		if [ "`readlink ${theme_dir}/active`" != "${euca_theme}" ] ; then \
			echo "Making '${euca_theme}' the active theme" ; \
			rm -f ${theme_dir}/active ; \
			ln -sf ${euca_theme} ${theme_dir}/active ; \
		fi \
	else \
		echo "Theme ${theme_dir}/${euca_theme} is not found!"; exit 1; \
	fi \

extras: update_properties
	@if test -n "${euca_theme}" ; then \
		$(MAKE) update_theme; \
	else \
		if [ "`readlink ${theme_dir}/active`" != "eucalyptus" ] ; then \
			echo "Making 'eucalyptus' the active theme" ; \
			rm -f ${theme_dir}/active ; \
			ln -sf eucalyptus ${theme_dir}/active ; \
		fi \
	fi

build: deps extras
	@# we need JAVA_HOME
	@if test -z "${JAVA_HOME}" ; then \
		echo "You need to define JAVA_HOME!"; exit 1; fi
	@ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) build-all
	#HACK!
	bash -c 'ulimit -n 500000;ulimit -n;ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) generate-bind'
	#HACK!
	bash -c 'ulimit -n 500000;ulimit -n;ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) compile-bind'
	#HACK!
	bash -c 'ulimit -n 500000;ulimit -n;ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) jar'
	@make -C modules/storage-controller/native
	#@make -C modules/storage-san/native
	@make -C modules/bootstrap/src/main/native
	@make -C eucadmin

build-test: deps extras
	@# we need JAVA_HOME
	@if test -z "${JAVA_HOME}" ; then \
		echo "You need to define JAVA_HOME!"; exit 1; fi
	@ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) build-test
	#HACK!
	bash -c 'ulimit -n 500000;ulimit -n;ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) generate-bind'
	#HACK!
	bash -c 'ulimit -n 500000;ulimit -n;ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) compile-bind'
	#HACK!
	bash -c 'ulimit -n 500000;ulimit -n;ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) jar'
	@make -C modules/storage-controller/native
	#@make -C modules/storage-san/native
	@make -C modules/bootstrap/src/main/native
	@make -C eucadmin


clean:
	$(ANT) clean
	make -C modules/bootstrap/src/main/native clean
	make -C modules/storage-controller/native clean
	#make -C modules/storage-san/native clean
	make -C eucadmin clean

distclean: clean
	$(ANT) distclean
	make -C modules/bootstrap/src/main/native distclean
	make -C modules/storage-controller/native distclean
	#make -C modules/storage-san/native distclean
	make -C eucadmin distclean

install-test: build-test
	@ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) install
	@make -C modules/storage-controller/native install
	@make -C modules/bootstrap/src/main/native install
	@make -C eucadmin install

install: build
	@ANT_OPTS="-Xmx1g -XX:MaxPermSize=512m" $(ANT) install
	@make -C modules/storage-controller/native install
	#@make -C modules/storage-san/native install
	@make -C modules/bootstrap/src/main/native install
	@make -C eucadmin install

install-test: build-test
	@ANT_OPTS="-Xmx1g -XX:MaxPermSize=256m" $(ANT) install
	@make -C modules/storage-controller/native install
	#@make -C modules/storage-san/native install
	@make -C modules/bootstrap/src/main/native install
	@make -C eucadmin install


deploy: install

uninstall:
	make -C modules/storage-controller/native uninstall
	#make -C modules/storage-san/native uninstall
	make -C modules/bootstrap/src/main/native uninstall
	make -C eucadmin uninstall
	$(RM) -rf $(DESTDIR)$(etcdir)/eucalyptus/cloud.d
	$(RM) -rf $(DESTDIR)$(datarootdir)/eucalyptus/*jar
	$(RM) -rf $(DESTDIR)$(vardir)/eucalyptus/webapps

# cleaning up after run-test currently requires a few iterations of:
#   dmsetup table | cut -d':' -f 1 | sort | uniq | xargs -L 1 dmsetup remove ; losetup -a | cut -d':' -f 1 | xargs -L 1 losetup -d; losetup -a
run-test:
	tools/runJUnitTest.sh com.eucalyptus.cloud.ws.EucaImagerTest
	tools/runJUnitTest.sh com.eucalyptus.cloud.ws.BrokerInterfaceTest
	tools/runJUnitTest.sh com.eucalyptus.cloud.ws.vsphere.VSphereEndpointTest -Dvsphere.url=${VSPHERE_URL} -Dvsphere.login=${VSPHERE_LOGIN} -Dvsphere.password=${VSPHERE_PASSWORD}
	tools/runJUnitTest.sh com.eucalyptus.cloud.ws.vsphere.VSphereEndpointAsyncTest -Dvsphere.url=${VSPHERE_URL} -Dvsphere.login=${VSPHERE_LOGIN} -Dvsphere.password=${VSPHERE_PASSWORD}
