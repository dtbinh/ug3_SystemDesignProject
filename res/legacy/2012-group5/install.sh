#!/bin/bash
# installs 2012/Group5's project

git clone git://github.com/sdp-5-2012/sdp-5-2012.git
group52012_dir=sdp-5-2012
cd $group52012_dir
rm -rf .git
cp -r src/JavaVision/constants ..
chmod +x compile.sh
mkdir -p ./bin
./compile.sh
libs=$(ls ./lib/*.jar)
libs=${libs//[[:space:]]/:}
{
echo '#!/bin/bash'
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib'
echo "java -Djava.library.path=./lib -cp $libs:./bin JavaVision.RunVision"
} > runVision.sh
chmod +x runVision.sh
cd ..
container_dir=src
mkdir -p $container_dir
mv constants/ $group52012_dir/ $container_dir/
{
echo '#!/bin/bash'
echo "cd $container_dir/$group52012_dir"
echo "./runVision.sh"
} > runVision.sh
chmod +x runVision.sh
