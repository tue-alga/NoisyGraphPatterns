# Noisy Graph Patterns
[![arXiv](https://img.shields.io/badge/arXiv-2601.11171-b31b1b.svg)](https://arxiv.org/abs/2601.11171)[![arXiv](https://img.shields.io/badge/arXiv-2602.19745-b31b1b.svg)](https://arxiv.org/abs/2602.19745)

This repository contains the code and data for extracting and visualizing noisy graph patterns via Moran's _I_ optimized matrix orderings. This covers both the pipeline for extracting noisy patterns and their visualization via Ring Motifs[^1] as well as the extension to unfold ordered matrices into BioFabric Motifs[^2].

## ⚙️ Used software libraries
The pipeline requires [GeometryCore](https://github.com/tue-alga/GeometryCore) for GUI and basic geometry.
The optimal Moran's _I_ matrix orderings are computed by sending an equivalent TSP instance to the [NEOS server](https://neos-server.org/neos/), which solves the instance using the [Concorde](https://neos-server.org/neos/solvers/co:concorde/TSP.html) library. Communication with the server is established using [Apache XML-RPC](https://github.com/evolvedbinary/apache-xmlrpc).

[^1]: Wulms J., MEULEMANS W., SPECKMANN B.: Noisy Graph Patterns via Ordered Matrices. _Computer Graphics Forum_ 2026. forthcoming.
[^2]: Wulms J., MEULEMANS W., SPECKMANN B.: Unfolding Ordered Matrices into BioFabric Motifs. _Eurographics Digital Library_ forthcoming.
