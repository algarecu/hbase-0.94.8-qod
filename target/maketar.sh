which cygpath 2> /dev/null
					if [ $? = 1 ]; then
						BUILD_DIR="/Users/algarecu/Documents/workspace/hbase-workspace/hbase-0.94.8-latest/target"
					else
						BUILD_DIR=`cygpath --unix '/Users/algarecu/Documents/workspace/hbase-workspace/hbase-0.94.8-latest/target'`
					fi

					cd $BUILD_DIR/hbase-0.94.8
					tar czf $BUILD_DIR/hbase-0.94.8.tar.gz hbase-0.94.8