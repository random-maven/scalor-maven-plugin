#!/usr/bin/env bash

set -e -u

wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key | sudo apt-key add -
sudo apt-add-repository "deb http://apt.llvm.org/xenial/ llvm-toolchain-xenial-5.0 main"
sudo apt-get update
sudo apt-get install -y clang-5.0

sudo find /usr -name "*libunwind*" -delete
	
sudo apt-get install -y zlib1g-dev libgc-dev libre2-dev libunwind8-dev
