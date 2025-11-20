Université Abdelmalek Essaadi Cloud Computing Architecture Simulation
====================================================================

This project evaluates different cloud infrastructure architectures for the University Abdelmalek Essaadi (UAE) using CloudSim simulations. 
It simulates three scenarios: 

1. Centralized datacenter model
2. Distributed datacenters across campuses
3. Hybrid model combining central, edge, and public cloud resources

Each scenario considers a common workload of 300 virtual machines and ~1500 cloudlets to represent diverse university needs.

Key features:
- Simulation of realistic university cloud workloads per campus
- Performance metrics: success rate, execution time, cloudlets-to-VM ratio
- Comparative analysis to identify the optimal architecture

University context:
- 16 campuses over 5 cities (+ expansions)
- Over 120,000 students and significant staff
- Wide pedagogical and research activities requiring scalable cloud services

Usage:
- Requires Java 8+ and CloudSim
- Compile and run each scenario separately (Java classes in package `org.cloudbus.cloudsim.examples.custom`)

Results summary:
- Centralized: best average execution time but uneven load distribution
- Distributed: better load balancing, varied performance
- Hybrid: most flexible and scalable, suited for university growth and diverse demands

Recommended architecture: Hybrid model for best balance of local autonomy and scalable cloud power.

For detailed results, see documentation and simulation logs.

Author: Rhimini Aimane & El Annasi Imad
Date: November 2025
