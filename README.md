### HBase-QoD: hbase 0.94.8 with Quality-of-Data.
+ In short: eventual-consistency with transactional operation grouping for inter-DC replication in HBase.
+ This repo is listed and caterogized accordingly to the type of HBase project here: 
https://github.com/rayokota/awesome-hbase

<!--[![Join the chat at https://gitter.im/algarecu/hbase-0.94.8-qod](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/algarecu/hbase-0.94.8-qod?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)-->

<!-- ## Build Status [![Build Status](https://travis-ci.org/algarecu/hbase-0.94.8-qod.svg?branch=master)](https://travis-ci.org/algarecu/hbase-0.94.8-qod)-->

## Description:
This is a HBase version with consistency guarantees instrumented within the
replication mechanisms of the stable release of HBase 0.94.8.

The Consistency guarantees consist in the following. For each column-family or
defined data-container is possible to build a storage priority-queue based on a
QoD (quality-of-data)  three-dimensional vector model K (θ, σ, ν).

To refer to this code or paper, use the full conference title:
Quality-of-Data for Consistency Levels in Geo-replicated Cloud Data Stores.
A. García-Recuero, S. Esteves, L. Veiga. IEEE CloudCom 2013, December, Bristol, UK.

P.S. Please note the code is not always properly annotated/commented and the
programming style might not follow the Apache Foundation and/or HBase community
guidelines at all times. Use at your own discretion and feel free to contact me
in order to propose any further contributions/enhancements to the model.

## Acknowledgements:
This work might be applicable to Apache issue 1734 and further work on Storage
Consistency Guarantees for cloud data-stores
(https://issues.apache.org/jira/browse/HBASE-1734).
Thanks to L. Hofhansl for sharing insight knowledge and direction through my
initial questions about the architecture of HBase.

## License: 
Apache License 2.0 applies as this is open source software. In addition, our research papers above will now be licensed under a BY-NC-ND license (Attribution-NonCommercial-No Derivatives) http://creativecommons.org/licenses/by-nc-nd/3.0/.

## Contact:
https://www.researchgate.net/profile/Alvaro_Garcia-Recuero/

