### HBase-QoD: hbase 0.94.8 version with Quality-of-Data.
In short: A knob for tunable eventual-consistency in HBase.

## Build Status [![Build Status](https://travis-ci.org/algarecu/hbase-0.94.8-qod.svg?branch=master)](https://travis-ci.org/algarecu/hbase-0.94.8-qod)

## Description:

This is a HBase version with consistency guarantees instrumented within the
replication mechanisms of the stable release of HBase 0.94.8.

The Consistency guarantees consist in the following. For each column-family or
defined data-container is possible to build a storage priority-queue based on a
QoD (quality-of-data)  three-dimensional vector model K (θ, σ, ν).

When using the code please refer to the full conference paper,"Quality-of-Data
for Consistency Levels in Geo-replicated Cloud Data Stores" Á. García-Recuero,
S. Esteves, L. Veiga. IEEE CloudCom 2013, December, Bristol, UK.

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

## Contact:

https://www.researchgate.net/profile/Alvaro_Garcia-Recuero/
