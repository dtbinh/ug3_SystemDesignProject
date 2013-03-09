SDP_HOME=`pwd`
LIB="$SDP_HOME/lib"
LD_LIBRARY_DIR="$LIB/lib"

function add_to_bashrc {
    grep -Fxq "$*" ~/.bashrc || echo "$*" >> ~/.bashrc
}

function cleanup {
    rm -rf $LIB
    mkdir -p $LIB
    mkdir -p $LD_LIBRARY_DIR
    STR="export LD_LIBRARY_PATH=\$LD_LIBRARY_PATH:$LD_LIBRARY_DIR"
    add_to_bashrc $STR
    cd $SDP_HOME
}

function install_v4l4j {
    export JDK_HOME="/usr/lib/jvm/java-openjdk"
    local version="v4l4j-0.9.0"
    cd $LIB
    echo "> Downloading v4l4j"
    wget "http://v4l4j.googlecode.com/files/$version.tar.gz" &&\
    echo "> extracting v4l4j" &&\
    tar xvf $version.tar.gz &&\
    rm $version.tar.gz &&\
    cd $version &&\
    ant clean &&\
    ant all &&\
    cp libv4l4j.so libvideo/libvideo.so.0 $LD_LIBRARY_DIR &&\
    cd $SDP_HOME
}

function install_lejos {
    local lLOC="`mktemp /tmp/lejos_NXJ_0_9_1_beta_3.XXXXX.tar.gz`"
    echo "> Downloading lejos to $lLOC"
    wget -O "$lLOC" "http://downloads.sourceforge.net/project/lejos/lejos-NXJ/0.9.1beta/leJOS_NXJ_0.9.1beta-3.tar.gz?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Flejos%2Ffiles%2Flejos-NXJ%2F0.9.1beta%2FleJOS_NXJ_0.9.1beta-3.tar.gz%2Fdownload&ts=1358596966&use_mirror=garr" &&\
    echo "> extracting $lLOC" &&\
    cd $LIB &&\
    tar xvf $lLOC &&\
    chmod +x leJOS_NXJ_0.9.1beta-3/bin/* &&\
    rm $lLOC &&\
    STR="export PATH=\$PATH:$LIB/leJOS_NXJ_0.9.1beta-3/bin" &&\
    add_to_bashrc $STR &&\
    cd $SDP_HOME
}

function install_bluetooth {
    local lLOC="`mktemp /tmp/libbluetooth.XXXXX.tar.gz`"
    echo "> Downloading bluetooth to $lLOC"
    wget "http://dl.dropbox.com/u/46248986/libbluetooth.tar.gz" -O $lLOC &&\
    cd $LD_LIBRARY_DIR &&\
    tar xvf $lLOC &&\
    mv lib/* . &&\
    rm -rf lib &&\
    rm $lLOC &&\
    cd $SDP_HOME
}

function install_zmq {
    local lLOC="`mktemp /tmp/zmq.XXXXXX.tar.gz`"
    local lTAR="`mktemp -d /tmp/zmq_extracted.XXXXXX`"
    echo "> Downloading ZMQ to $lLOC"
    wget -O "$lLOC" "http://download.zeromq.org/zeromq-3.2.2.tar.gz" &&\
    echo "> extracting $lLOC" &&\
    cd $lTAR &&\
    tar xvf $lLOC &&\
    cd zeromq-3.2.2 &&\
    ./autogen.sh &&\
    ./configure --prefix=$LIB &&\
    make &&\
   	make install &&\
    rm $lLOC &&\
    rm -rf $lTAR &&\
    cd $SDP_HOME
}

function install_jzmq {
    local lTAR="`mktemp -d /tmp/jzmq_extracted.XXXXXX`"
    echo "> Downloading jZMQ to $lTAR"
    cd $lTAR &&\
    git clone git://github.com/zeromq/jzmq.git &&\
    cd $lTAR &&\
    cd jzmq &&\
    ./autogen.sh &&\
    ./configure --prefix=$LIB CXXFLAGS=-I$LIB/include LDFLAGS=-L$LD_LIBRARY_DIR &&\
    make &&\
   	make install &&\
    rm -rf $lTAR &&\
    cd $SDP_HOME
}

function install_pyzmq {
    local lLOC="`mktemp /tmp/pyzmq.XXXXXX.tar.gz`"
    local lTAR="`mktemp -d /tmp/pyzmq_extracted.XXXXXX`"
    echo "> Downloading pyZMQ to $lLOC"
    wget -O "$lLOC" "https://github.com/zeromq/pyzmq/downloads/pyzmq-2.2.0.1.tar.gz" &&\
    echo "> extracting $lLOC" &&\
    cd $lTAR &&\
    tar xvf $lLOC &&\
    cd pyzmq-2.2.0.1 &&\
    python setup.py configure --zmq=$LIB &&\
    python setup.py install --prefix=$LIB&&\
    STR="export PYTHONPATH=\$PYTHONPATH:$LIB/lib64/python2.6/site-packages/" &&\
    add_to_bashrc $STR &&\
    rm $lLOC &&\
    rm -rf $lTAR &&\
    cd $SDP_HOME
}

function install_pygame {
    local lLOC="`mktemp /tmp/pygame.XXXXXX.tar.gz`"
    local lTAR="`mktemp -d /tmp/pygame_extracted.XXXXXX`"
    echo "> Downloading pygame to $lLOC"
    wget -O "$lLOC" "http://www.pygame.org/ftp/pygame-1.9.1release.tar.gz" &&\
    echo "> extracting $lLOC" &&\
    cd $lTAR &&\
    tar xvf $lLOC &&\
    cd pygame-1.9.1release &&\
    perl -0777 -pi -e 's/if not confirm(\("""\n[^\n]*\n[^\n]*\n[^?]*\?"""\)):\n[ \t]*raise SystemExit/print \1/g' config_unix.py &&\
    python setup.py install --prefix=$LIB &&\
    STR="export PYTHONPATH=\$PYTHONPATH:$LIB/lib64/python2.6/site-packages/" &&\
    add_to_bashrc $STR &&\
    rm $lLOC &&\
    rm -rf $lTAR &&\
    cd $SDP_HOME
}

cleanup &&\

install_v4l4j &&\
install_lejos &&\
install_bluetooth &&\
install_zmq
install_jzmq &&\
install_pyzmq &&\
install_pygame &&\

echo "Done!"
