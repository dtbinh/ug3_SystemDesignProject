SDP_HOME=`pwd`
LIB="$SDP_HOME/lib"
LD_LIBRARY_DIR="$LIB/ld_library_dir"

function add_to_bashrc {
    grep -Fxq "$*" ~/.bashrc || echo "$*" >> ~/.bashrc
}

function install_lejos {
    local lLOCATION="`mktemp /tmp/lejos_NXJ_0_9_1_beta_3.XXXXX.tar.gz`"
    echo "> Downloading lejos to $lLOCATION"
    wget -O "$lLOCATION" "http://downloads.sourceforge.net/project/lejos/lejos-NXJ/0.9.1beta/leJOS_NXJ_0.9.1beta-3.tar.gz?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Flejos%2Ffiles%2Flejos-NXJ%2F0.9.1beta%2FleJOS_NXJ_0.9.1beta-3.tar.gz%2Fdownload&ts=1358596966&use_mirror=garr" \
    && echo "> extracting $lLOCATION" \
    && cd $LIB \
    && tar xvf $lLOCATION \
    && chmod +x leJOS_NXJ_0.9.1beta-3/bin/* \
    && rm $lLOCATION \
    && STR="export PATH=\$PATH:$LIB/leJOS_NXJ_0.9.1beta-3/bin" \
    && add_to_bashrc $STR
}

function install_zmq {
    local lLOCATION="`mktemp /tmp/zmq.XXXXXX.tar.gz`"
    local lEXTRACT_DIR="`mktemp -d /tmp/zmq_extracted.XXXXXX`"
    echo "> Downloading ZMQ to $lLOCATION"
    wget -O "$lLOCATION" "http://download.zeromq.org/zeromq-3.2.2.tar.gz" \
    && echo "> extracting $lLOCATION" \
    && cd $lEXTRACT_DIR \
    && tar xvf $lLOCATION \
    && cd zeromq-3.2.2 \
    && ./autogen.sh \
    && ./configure --prefix=$LIB \
    && make \
   	&& make install \
    && rm $lLOCATION \
    && rm -rf $lEXTRACT_DIR
}

function install_jzmq {
    local lEXTRACT_DIR="`mktemp -d /tmp/jzmq_extracted.XXXXXX`"
    echo "> Downloading jZMQ to $lEXTRACT_DIR"
    cd $lEXTRACT_DIR \
    && git clone git://github.com/zeromq/jzmq.git \
    && cd $lEXTRACT_DIR \
    && cd jzmq \
    && ./autogen.sh \
    && ./configure --prefix=$LIB CXXFLAGS=-I$LIB/include LDFLAGS=-L$LD_LIBRARY_DIR \
    && make \
   	&& make install \
    && rm -rf $lEXTRACT_DIR
}

function install_pyzmq {
    local lLOCATION="`mktemp /tmp/pyzmq.XXXXXX.tar.gz`"
    local lEXTRACT_DIR="`mktemp -d /tmp/pyzmq_extracted.XXXXXX`"
    echo "> Downloading pyZMQ to $lLOCATION"
    wget -O "$lLOCATION" "https://github.com/zeromq/pyzmq/downloads/pyzmq-2.2.0.1.tar.gz" \
    && echo "> extracting $lLOCATION" \
    && cd $lEXTRACT_DIR \
    && tar xvf $lLOCATION \
    && cd pyzmq-2.2.0.1 \
    && python setup.py configure --zmq=$LIB \
    && python setup.py install --prefix=$LIB\
    && STR="export PYTHONPATH=\$PYTHONPATH:$LIB/lib64/python2.6/site-packages/"  \
    && add_to_bashrc $STR \
    && rm $lLOCATION \
    && rm -rf $lEXTRACT_DIR
}

function cleanup {
    rm -rf $LIB 
    mkdir -p $LIB
    mkdir -p $LD_LIBRARY_DIR
    STR="export LD_LIBRARY_PATH=\$LD_LIBRARY_PATH:$LD_LIBRARY_DIR"
    add_to_bashrc $STR
}

function install_bluetooth {
    local lTMP_BLUETOOTH_LIB="`mktemp /tmp/libbluetooth.XXXXX.tar.gz`"
    # Currently this is my dropbox acc public folder -- do not want to burden the repo with binaries -- Saulius  
    # TODO: Put this in the repo when publishing
    # 2013 student, if we forget this, find a way to compile libluetooth yourself
    # that is what is being downloaded
    wget "http://dl.dropbox.com/u/46248986/libbluetooth.tar.gz" -O $lTMP_BLUETOOTH_LIB \
    && cd $LIB && tar xvf $lTMP_BLUETOOTH_LIB && cd $SDP_HOME && rm $lTMP_BLUETOOTH_LIB
}

function install_pygame {
    local lLOCATION="`mktemp /tmp/pygame.XXXXXX.tar.gz`"
    local lEXTRACT_DIR="`mktemp -d /tmp/pygame_extracted.XXXXXX`"
    echo "> Downloading pygame to $lLOCATION"
    wget -O "$lLOCATION" "http://www.pygame.org/ftp/pygame-1.9.1release.tar.gz" \
    && echo "> extracting $lLOCATION" \
    && cd $lEXTRACT_DIR \
    && tar xvf $lLOCATION \
    && cd pygame-1.9.1release \
    && python setup.py install --prefix=$LIB\
    && STR="export PYTHONPATH=\$PYTHONPATH:$LIB/lib64/python2.6/site-packages/" \
    && add_to_bashrc $STR \
    && rm $lLOCATION \
    && rm -rf $lEXTRACT_DIR
}

function install_v4l4j {
    export JDK_HOME="/usr/lib/jvm/java-openjdk"
    local version="v4l4j-0.9.0"
    cd $LIB
    echo "> Downloading v4l4j"
    wget "http://v4l4j.googlecode.com/files/$version.tar.gz" && \
    echo "> extracting v4l4j" && \
    tar xvf $version.tar.gz && \
    rm $version.tar.gz && \
    cd $version && \
    ant clean && \
    ant all && \
    cp libv4l4j.so libvideo/libvideo.so.0 $LD_LIBRARY_DIR
}

cleanup && \

install_v4l4j && \
install_lejos && \
install_bluetooth && \
install_zmq && \
install_jzmq && \
install_pyzmq && \
install_pygame && \

source ~/.bashrc
