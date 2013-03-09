#!/bin/bash

base_dir="$(dirname $(readlink -f $0))"
ldlib_dir="$base_dir/lib/lib"

dependencies=$base_dir/.dependencies
classpath=$base_dir/.classpath

src_dir="src"
bin_dir="bin"
eclipse_container="org.eclipse.jdt.launching.JRE_CONTAINER"
{   echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    echo "<classpath>"
    echo "    <classpathentry kind=\"src\" path=\"$src_dir\" />"
    echo "    <classpathentry kind=\"output\" path=\"$bin_dir\" />"
    echo "    <classpathentry kind=\"con\" path=\"$eclipse_container\" />"
} > $classpath
for line in $(cat $dependencies); do
    echo "    <classpathentry kind=\"lib\" path=\"$line\" />" >> $classpath
done
echo "</classpath>" >> $classpath

export LD_LIBRARY_PATH="$ldlib_dir:$LD_LIBRARY_PATH"
exec eclipse &
