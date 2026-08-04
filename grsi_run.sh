#!/usr/bin/env bash
set -euo pipefail

sudo apt-get update
sudo apt install openjdk-21-jdk
sudo apt install ipe
sudo apt install texlive

git clone https://github.com/tue-alga/GeometryCore.git
git clone https://github.com/tue-alga/NoisyGraphPatterns.git

cd NoisyGraphPatterns

chmod +x mvnw

./mvnw clean package

java -cp "target/noisygraphpatterns-1.0.0.jar" nl.tue.algo.noisygraphpatterns.gui.GRSImageProducer

cd figures

iperender -pdf flt58-s0.5t0.85.ipe flt58-s0.5t0.85.pdf
iperender -png flt58-s0.5t0.85.ipe flt58-s0.5t0.85.png