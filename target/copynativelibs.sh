which cygpath 2> /dev/null
					if [ $? = 1 ]; then
						BUILD_DIR="/Users/algarecu/Documents/workspace/hbase-workspace/hbase-0.94.8-latest/target"
					else
						BUILD_DIR=`cygpath --unix '/Users/algarecu/Documents/workspace/hbase-workspace/hbase-0.94.8-latest/target'`
					fi
                    if [ `ls $BUILD_DIR/nativelib | wc -l` -ne 0 ]; then
                      cp -PR $BUILD_DIR/nativelib/lib* $BUILD_DIR/hbase-0.94.8/hbase-0.94.8/lib/native/Mac_OS_X-64
                    fi