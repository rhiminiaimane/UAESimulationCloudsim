Expliquez-moi d'une manière simple, direct & court le nombre de modele dans ce code, quelle sont ces modele, difference entre eux.

package org.cloudbus.cloudsim.examples.custom;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * Simulation CloudSim réaliste pour l'Université Abdelmalek Essaadi (UAE)
 * Comparaison des trois scénarios d'architecture datacenter:
 * 1. Datacenter centralisé
 * 2. Datacenter par villes  
 * 3. Modèle hybride
 */
public class UAECloudSimulation {
    
    private static List<ScenarioResult> scenarioResults = new ArrayList<>();
    private static List<Vm> globalVmList;
    private static List<Datacenter> globalDatacenters;

    public static void main(String[] args) {
        Log.printLine("========================================");
        Log.printLine("Simulation CloudSim - UAE 5 Campus");
        Log.printLine("COMPARAISON DES 3 SCÉNARIOS D'ARCHITECTURE");
        Log.printLine("========================================");

        // Exécuter les trois scénarios
        for (int scenario = 1; scenario <= 3; scenario++) {
            Log.printLine("\n\n" + "=".repeat(80));
            Log.printLine("DÉBUT DU SCÉNARIO " + scenario + " : " + getScenarioName(scenario));
            Log.printLine("=".repeat(80));
            
            ScenarioResult result = runScenario(scenario);
            if (result != null) {
                scenarioResults.add(result);
            }
            
            Log.printLine("\n" + "=".repeat(80));
            Log.printLine("FIN DU SCÉNARIO " + scenario + " : " + getScenarioName(scenario));
            Log.printLine("=".repeat(80));
        }

        // Afficher la comparaison finale
        if (!scenarioResults.isEmpty()) {
            printComparativeAnalysis();
        } else {
            Log.printLine("Aucun scénario n'a pu être exécuté avec succès.");
        }
    }

    private static ScenarioResult runScenario(int scenario) {
        try {
            int num_user = 5;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            // Création des datacenters selon le scénario
            globalDatacenters = createDatacentersForScenario(scenario);
            if (globalDatacenters.isEmpty()) {
                Log.printLine("Erreur: Aucun datacenter créé pour le scénario " + scenario);
                return null;
            }
            
            // AFFICHER LES DÉTAILS DES DATACENTERS
            printDatacenterDetails(scenario);
            
            // Création des brokers pour chaque campus
            DatacenterBroker brokerTetouan = createBroker("Broker_Tetouan");
            DatacenterBroker brokerTanger = createBroker("Broker_Tanger");
            DatacenterBroker brokerHoceima = createBroker("Broker_Hoceima");
            DatacenterBroker brokerLarache = createBroker("Broker_Larache");
            DatacenterBroker brokerKsarElKebir = createBroker("Broker_KsarElKebir");

            if (brokerTetouan == null || brokerTanger == null || brokerHoceima == null || 
                brokerLarache == null || brokerKsarElKebir == null) {
                Log.printLine("Erreur: Impossible de créer tous les brokers");
                return null;
            }

            int brokerTetouanId = brokerTetouan.getId();
            int brokerTangerId = brokerTanger.getId();
            int brokerHoceimaId = brokerHoceima.getId();
            int brokerLaracheId = brokerLarache.getId();
            int brokerKsarElKebirId = brokerKsarElKebir.getId();

            globalVmList = new ArrayList<Vm>();
            
            // Création des VMs selon le scénario
            createVMsForScenario(scenario, globalVmList, brokerTetouanId, brokerTangerId, brokerHoceimaId, 
                               brokerLaracheId, brokerKsarElKebirId);

            if (globalVmList.isEmpty()) {
                Log.printLine("Erreur: Aucune VM créée pour le scénario " + scenario);
                return null;
            }

            // AFFICHER LES DÉTAILS DES VMs PAR CAMPUS
            printVMsDetailsByCampus(globalVmList, brokerTetouanId, brokerTangerId, brokerHoceimaId, 
                                  brokerLaracheId, brokerKsarElKebirId);

            // Distribution des VMs aux brokers
            distributeVMsToBrokers(globalVmList, brokerTetouan, brokerTanger, brokerHoceima, 
                                 brokerLarache, brokerKsarElKebir);

            List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

            // Création des cloudlets selon le scénario
            createCloudletsForScenario(scenario, cloudletList, brokerTetouanId, brokerTangerId, brokerHoceimaId,
                                    brokerLaracheId, brokerKsarElKebirId, 
                                    brokerTetouan, brokerTanger, brokerHoceima, brokerLarache, brokerKsarElKebir);

            if (cloudletList.isEmpty()) {
                Log.printLine("Erreur: Aucun cloudlet créé pour le scénario " + scenario);
                return null;
            }

            // AFFICHER LES DÉTAILS DES CLOUDLETS
            printCloudletsDetails(cloudletList, brokerTetouanId, brokerTangerId, brokerHoceimaId, 
                                brokerLaracheId, brokerKsarElKebirId);

            // Distribution des cloudlets aux brokers
            distributeCloudletsToBrokers(cloudletList, brokerTetouan, brokerTanger, brokerHoceima,
                                       brokerLarache, brokerKsarElKebir);

            Log.printLine("\n" + "=".repeat(50));
            Log.printLine("DÉMARRAGE SIMULATION - " + getScenarioName(scenario));
            Log.printLine("=".repeat(50));
            Log.printLine("Résumé configuration:");
            Log.printLine("  - Total VMs: " + globalVmList.size());
            Log.printLine("  - Total Cloudlets: " + cloudletList.size());
            Log.printLine("  - Datacenters: " + globalDatacenters.size());
            
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            // Collecter les résultats DÉTAILLÉS
            ScenarioResult result = collectDetailedScenarioResults(scenario, brokerTetouan, brokerTanger, 
                                                         brokerHoceima, brokerLarache, brokerKsarElKebir);
            
            printDetailedScenarioSummary(result, brokerTetouan, brokerTanger, brokerHoceima, 
                                       brokerLarache, brokerKsarElKebir);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Erreur dans le scénario " + scenario + ": " + e.getMessage());
            return null;
        }
    }

    // NOUVELLE MÉTHODE : Résultats DÉTAILLÉS avec analyse par type de workload
    private static ScenarioResult collectDetailedScenarioResults(int scenario, DatacenterBroker... brokers) {
        ScenarioResult result = new ScenarioResult(scenario, getScenarioName(scenario));
        
        int totalCloudlets = 0;
        int totalSuccess = 0;
        double totalExecutionTime = 0;
        double globalMinTime = Double.MAX_VALUE;
        double globalMaxTime = 0;
        int totalVMs = 0;

        // DÉTAILS PAR CAMPUS AVEC ANALYSE DES WORKLOADS
        Log.printLine("\n" + "=".repeat(70));
        Log.printLine("RÉSULTATS DÉTAILLÉS PAR CAMPUS - " + getScenarioName(scenario));
        Log.printLine("=".repeat(70));
        
        String[] campusNames = {"Tétouan", "Tanger", "Al Hoceïma", "Larache", "Ksar El-Kébir"};
        String[][] workloadTypes = {
            {"LMS", "Sciences"},
            {"HPC", "Medical"},
            {"Edge"},
            {"FP"},
            {"FP"}
        };
        
        for (int i = 0; i < brokers.length; i++) {
            DatacenterBroker broker = brokers[i];
            List<Cloudlet> list = broker.getCloudletReceivedList();
            totalCloudlets += list.size();
            totalVMs += broker.getVmList().size();
            
            Log.printLine("\n🎯 " + campusNames[i] + ":");
            Log.printLine("   ├─ Cloudlets totaux: " + list.size());
            
            // Analyser par plage de cloudlets (basé sur les IDs)
            for (String workload : workloadTypes[i]) {
                analyzeWorkloadPerformance(list, workload, campusNames[i]);
            }
            
            int campusSuccess = 0;
            double campusTotalTime = 0;
            double campusMinTime = Double.MAX_VALUE;
            double campusMaxTime = 0;
            
            for (Cloudlet cloudlet : list) {
                if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                    double execTime = cloudlet.getActualCPUTime();
                    totalSuccess++;
                    campusSuccess++;
                    totalExecutionTime += execTime;
                    campusTotalTime += execTime;
                    globalMinTime = Math.min(globalMinTime, execTime);
                    globalMaxTime = Math.max(globalMaxTime, execTime);
                    campusMinTime = Math.min(campusMinTime, execTime);
                    campusMaxTime = Math.max(campusMaxTime, execTime);
                }
            }
            
            // Statistiques globales du campus
            if (list.size() > 0) {
                double successRate = (campusSuccess * 100.0) / list.size();
                double avgTime = campusSuccess > 0 ? campusTotalTime / campusSuccess : 0;
                
                Log.printLine("   ├─ " + "=".repeat(40));
                Log.printLine("   ├─ STATISTIQUES GLOBALES " + campusNames[i] + ":");
                Log.printLine("   ├─ Succès: " + campusSuccess + "/" + list.size() + 
                            " (" + String.format("%.1f", successRate) + "%)");
                Log.printLine("   ├─ Temps moyen: " + String.format("%.2f", avgTime) + " s");
                Log.printLine("   ├─ Temps min: " + String.format("%.2f", campusMinTime) + " s");
                Log.printLine("   └─ Temps max: " + String.format("%.2f", campusMaxTime) + " s");
            }
        }

        result.totalCloudlets = totalCloudlets;
        result.successfulCloudlets = totalSuccess;
        result.totalVMs = totalVMs;
        if (totalSuccess > 0) {
            result.averageExecutionTime = totalExecutionTime / totalSuccess;
        } else {
            result.averageExecutionTime = 0;
        }
        result.minExecutionTime = globalMinTime == Double.MAX_VALUE ? 0 : globalMinTime;
        result.maxExecutionTime = globalMaxTime;
        result.makespan = CloudSim.clock();
        result.successRate = totalCloudlets > 0 ? (totalSuccess * 100.0) / totalCloudlets : 0;
        result.cloudletsPerVM = totalVMs > 0 ? (double) totalCloudlets / totalVMs : 0;

        return result;
    }

    // NOUVELLE MÉTHODE : Analyse détaillée par type de workload
    private static void analyzeWorkloadPerformance(List<Cloudlet> cloudlets, String workloadType, String campusName) {
        int startId = 0, endId = 0;
        String description = "";
        
        switch(workloadType) {
            case "LMS":
                startId = 0; endId = 499; description = "LMS (E-learning)";
                break;
            case "Sciences":
                startId = 500; endId = 899; description = "Calcul Scientifique";
                break;
            case "HPC":
                startId = 900; endId = 1199; description = "HPC (High Performance)";
                break;
            case "Medical":
                startId = 1200; endId = 1399; description = "Applications Médicales";
                break;
            case "Edge":
                startId = 1400; endId = 1499; description = "Edge Computing";
                break;
            case "FP":
                startId = 1500; endId = 1659; description = "Formation Professionnelle";
                break;
        }
        
        int count = 0;
        int success = 0;
        double totalTime = 0;
        double minTime = Double.MAX_VALUE;
        double maxTime = 0;
        
        for (Cloudlet cloudlet : cloudlets) {
            if (cloudlet.getCloudletId() >= startId && cloudlet.getCloudletId() <= endId) {
                count++;
                if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                    success++;
                    double execTime = cloudlet.getActualCPUTime();
                    totalTime += execTime;
                    minTime = Math.min(minTime, execTime);
                    maxTime = Math.max(maxTime, execTime);
                }
            }
        }
        
        if (count > 0) {
            double avgTime = success > 0 ? totalTime / success : 0;
            double successRate = (success * 100.0) / count;
            
            Log.printLine("   ├─ 📊 " + description + ":");
            Log.printLine("   │  ├─ Cloudlets: " + success + "/" + count + 
                         " (" + String.format("%.1f", successRate) + "%)");
            Log.printLine("   │  ├─ Temps moyen: " + String.format("%.2f", avgTime) + " s");
            Log.printLine("   │  ├─ Temps min: " + String.format("%.2f", minTime) + " s");
            Log.printLine("   │  └─ Temps max: " + String.format("%.2f", maxTime) + " s");
        }
    }

    // NOUVELLE MÉTHODE : Résumé DÉTAILLÉ du scénario
    private static void printDetailedScenarioSummary(ScenarioResult result, DatacenterBroker... brokers) {
        DecimalFormat dft = new DecimalFormat("###.##");
        
        Log.printLine("\n" + "=".repeat(70));
        Log.printLine("RÉSUMÉ DÉTAILLÉ SCÉNARIO " + result.scenarioNumber + " : " + result.scenarioName);
        Log.printLine("=".repeat(70));
        
        // Architecture détaillée
        Log.printLine("🏗️  ARCHITECTURE DÉTAILLÉE:");
        Log.printLine("   ├─ Datacenters: " + globalDatacenters.size());
        for (Datacenter dc : globalDatacenters) {
            Log.printLine("   │  └─ " + dc.getName() + ": " + dc.getHostList().size() + " hosts");
        }
        Log.printLine("   ├─ VMs totales: " + result.totalVMs);
        Log.printLine("   ├─ Cloudlets totaux: " + result.totalCloudlets);
        Log.printLine("   └─ Ratio Cloudlets/VM: " + String.format("%.2f", result.cloudletsPerVM));
        
        // Performance détaillée
        Log.printLine("\n⚡ PERFORMANCE DÉTAILLÉE:");
        Log.printLine("   ├─ Taux de succès global: " + String.format("%.2f", result.successRate) + "%");
        Log.printLine("   ├─ Temps moyen d'exécution: " + dft.format(result.averageExecutionTime) + " s");
        Log.printLine("   ├─ Temps minimum: " + dft.format(result.minExecutionTime) + " s");
        Log.printLine("   ├─ Temps maximum: " + dft.format(result.maxExecutionTime) + " s");
        Log.printLine("   └─ Makespan total: " + dft.format(result.makespan) + " s");
        
        // Analyse des performances par campus
        Log.printLine("\n📊 PERFORMANCE PAR CAMPUS:");
        String[] campusNames = {"Tétouan", "Tanger", "Al Hoceïma", "Larache", "Ksar El-Kébir"};
        for (int i = 0; i < brokers.length; i++) {
            List<Cloudlet> list = brokers[i].getCloudletReceivedList();
            int success = 0;
            double totalTime = 0;
            
            for (Cloudlet cloudlet : list) {
                if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                    success++;
                    totalTime += cloudlet.getActualCPUTime();
                }
            }
            
            double avgTime = success > 0 ? totalTime / success : 0;
            Log.printLine("   ├─ " + campusNames[i] + ": " + success + "/" + list.size() + 
                         " cloudlets, moyenne: " + String.format("%.2f", avgTime) + " s");
        }
        
        // Efficacité et recommandations
        Log.printLine("\n📈 ANALYSE D'EFFICACITÉ:");
        double efficiencyScore = (result.cloudletsPerVM * 100) / (result.averageExecutionTime + 1);
        Log.printLine("   ├─ Score d'efficacité: " + String.format("%.2f", efficiencyScore));
        Log.printLine("   ├─ Utilisation des ressources: " + 
                     (result.cloudletsPerVM > 15 ? "EXCELLENTE" : 
                      result.cloudletsPerVM > 10 ? "BONNE" : "MODÉRÉE"));
        Log.printLine("   └─ Performance: " + 
                     (result.averageExecutionTime < 500 ? "EXCELLENTE" : 
                      result.averageExecutionTime < 1000 ? "BONNE" : "MODÉRÉE"));
        
        // Recommandations spécifiques au scénario
        Log.printLine("\n💡 RECOMMANDATIONS POUR " + result.scenarioName + ":");
        switch(result.scenarioNumber) {
            case 1:
                Log.printLine("   ✅ Points forts: Performance optimale, maintenance centralisée");
                Log.printLine("   ⚠️  Points d'attention: Latence pour campus éloignés, single point of failure");
                Log.printLine("   🎯 Recommandé pour: Applications critiques, calcul intensif");
                break;
            case 2:
                Log.printLine("   ✅ Points forts: Latence réduite, résilience géographique");
                Log.printLine("   ⚠️  Points d'attention: Coûts d'infrastructure, gestion distribuée");
                Log.printLine("   🎯 Recommandé pour: Campus autonomes, applications temps réel");
                break;
            case 3:
                Log.printLine("   ✅ Points forts: Équilibre coût/performance, flexibilité maximale");
                Log.printLine("   ⚠️  Points d'attention: Complexité de gestion, intégration multi-cloud");
                Log.printLine("   🎯 Recommandé pour: Environnement hétérogène, croissance future");
                break;
        }
    }

    // Les méthodes existantes restent inchangées mais sont inclues pour complétude
    private static void printDatacenterDetails(int scenario) {
        Log.printLine("\n" + "-".repeat(60));
        Log.printLine("CONFIGURATION DES DATACENTERS - " + getScenarioName(scenario));
        Log.printLine("-".repeat(60));
        
        int totalHosts = 0;
        int totalCores = 0;
        long totalRam = 0;
        int totalMips = 0;

        for (Datacenter dc : globalDatacenters) {
            List<Host> hostList = dc.getHostList();
            totalHosts += hostList.size();
            
            Log.printLine("\n📊 " + dc.getName() + ":");
            Log.printLine("   └─ Hosts: " + hostList.size());
            
            int dcCores = 0;
            long dcRam = 0;
            int dcMips = 0;
            
            for (Host host : hostList) {
                dcCores += host.getPeList().size();
                dcRam += host.getRam();
                for (Pe pe : host.getPeList()) {
                    dcMips += pe.getMips();
                }
            }
            
            totalCores += dcCores;
            totalRam += dcRam;
            totalMips += dcMips;
            
            Log.printLine("   └─ Cores CPU: " + dcCores);
            Log.printLine("   └─ RAM totale: " + (dcRam / 1024) + " GB");
            Log.printLine("   └─ Puissance CPU: " + dcMips + " MIPS");
        }
        
        Log.printLine("\n📈 TOTAUX " + getScenarioName(scenario) + ":");
        Log.printLine("   ├─ Datacenters: " + globalDatacenters.size());
        Log.printLine("   ├─ Hosts: " + totalHosts);
        Log.printLine("   ├─ Cores CPU: " + totalCores);
        Log.printLine("   ├─ RAM: " + (totalRam / 1024) + " GB");
        Log.printLine("   └─ Puissance: " + totalMips + " MIPS");
    }

    private static void printVMsDetailsByCampus(List<Vm> vmList, int... brokerIds) {
        Log.printLine("\n" + "-".repeat(60));
        Log.printLine("CONFIGURATION DES VMs PAR CAMPUS");
        Log.printLine("-".repeat(60));
        
        String[] campusNames = {"Tétouan", "Tanger", "Al Hoceïma", "Larache", "Ksar El-Kébir"};
        
        for (int i = 0; i < brokerIds.length; i++) {
            int brokerId = brokerIds[i];
            String campusName = campusNames[i];
            
            List<Vm> campusVMs = new ArrayList<>();
            int totalMips = 0;
            long totalRam = 0;
            int totalCores = 0;
            
            for (Vm vm : vmList) {
                if (vm.getUserId() == brokerId) {
                    campusVMs.add(vm);
                    totalMips += vm.getMips();
                    totalRam += vm.getRam();
                    totalCores += vm.getNumberOfPes();
                }
            }
            
            Log.printLine("\n🎯 " + campusName + ":");
            Log.printLine("   └─ VMs: " + campusVMs.size());
            
            if (!campusVMs.isEmpty()) {
                Log.printLine("   └─ Configuration moyenne:");
                Log.printLine("      ├─ MIPS/VM: " + (totalMips / campusVMs.size()));
                Log.printLine("      ├─ RAM/VM: " + (totalRam / campusVMs.size() / 1024) + " GB");
                Log.printLine("      ├─ vCPUs/VM: " + (totalCores / campusVMs.size()));
                Log.printLine("      └─ Total ressources:");
                Log.printLine("         ├─ MIPS: " + totalMips);
                Log.printLine("         ├─ RAM: " + (totalRam / 1024) + " GB");
                Log.printLine("         └─ vCPUs: " + totalCores);
            }
        }
    }

    private static void printCloudletsDetails(List<Cloudlet> cloudletList, int... brokerIds) {
        Log.printLine("\n" + "-".repeat(60));
        Log.printLine("CONFIGURATION DES CLOUDLETS PAR CAMPUS");
        Log.printLine("-".repeat(60));
        
        String[] campusNames = {"Tétouan", "Tanger", "Al Hoceïma", "Larache", "Ksar El-Kébir"};
        int totalCloudlets = cloudletList.size();
        
        Log.printLine("\n📋 RÉPARTITION DES CLOUDLETS:");
        
        for (int i = 0; i < brokerIds.length; i++) {
            int brokerId = brokerIds[i];
            String campusName = campusNames[i];
            
            int campusCloudlets = 0;
            
            for (Cloudlet cloudlet : cloudletList) {
                if (cloudlet.getUserId() == brokerId) {
                    campusCloudlets++;
                }
            }
            
            double percentage = (campusCloudlets * 100.0) / totalCloudlets;
            Log.printLine("   ├─ " + campusName + ": " + campusCloudlets + " cloudlets (" + 
                         String.format("%.1f", percentage) + "%)");
        }
        
        Log.printLine("   └─ TOTAL: " + totalCloudlets + " cloudlets");
    }

    private static void printComparativeAnalysis() {
        Log.printLine("\n\n" + "=".repeat(80));
        Log.printLine("ANALYSE COMPARATIVE DES TROIS SCÉNARIOS D'ARCHITECTURE");
        Log.printLine("=".repeat(80));

        Log.printLine("\n" + "-".repeat(80));
        Log.printLine("COMPARAISON DES PERFORMANCES");
        Log.printLine("-".repeat(80));
        
        Log.printLine(String.format("%-25s %-8s %-8s %-12s %-12s %-12s %-10s", 
            "Scénario", "VMs", "Success", "Temps Moyen", "Temps Min", "Temps Max", "Efficacité"));
        Log.printLine(String.format("%-25s %-8s %-8s %-12s %-12s %-12s %-10s", 
            "", "", "Rate%", "(s)", "(s)", "(s)", "Cloudlets/VM"));

        for (ScenarioResult result : scenarioResults) {
            Log.printLine(String.format("%-25s %-8d %-8.2f %-12.2f %-12.2f %-12.2f %-10.2f",
                result.scenarioName,
                result.totalVMs,
                result.successRate,
                result.averageExecutionTime,
                result.minExecutionTime,
                result.maxExecutionTime,
                result.cloudletsPerVM));
        }

        Log.printLine("\n" + "-".repeat(80));
        Log.printLine("ANALYSE ET RECOMMANDATIONS FINALES");
        Log.printLine("-".repeat(80));

        if (scenarioResults.size() < 2) {
            Log.printLine("Pas assez de données pour une analyse comparative complète.");
            return;
        }

        // Trouver le meilleur scénario pour chaque métrique
        ScenarioResult bestTime = scenarioResults.get(0);
        ScenarioResult bestEfficiency = scenarioResults.get(0);
        ScenarioResult bestSuccess = scenarioResults.get(0);

        for (ScenarioResult result : scenarioResults) {
            if (result.averageExecutionTime < bestTime.averageExecutionTime && result.averageExecutionTime > 0) {
                bestTime = result;
            }
            if (result.cloudletsPerVM > bestEfficiency.cloudletsPerVM) {
                bestEfficiency = result;
            }
            if (result.successRate > bestSuccess.successRate) {
                bestSuccess = result;
            }
        }

        Log.printLine("🏆 MEILLEURES PERFORMANCES PAR CRITÈRE:");
        Log.printLine("   ✓ Temps d'exécution: " + bestTime.scenarioName + 
                     " (" + String.format("%.2f", bestTime.averageExecutionTime) + " s)");
        Log.printLine("   ✓ Efficacité ressources: " + bestEfficiency.scenarioName + 
                     " (" + String.format("%.2f", bestEfficiency.cloudletsPerVM) + " cloudlets/VM)");
        Log.printLine("   ✓ Fiabilité: " + bestSuccess.scenarioName + 
                     " (" + String.format("%.2f", bestSuccess.successRate) + "%)");

        Log.printLine("\n--- SYNTHÈSE DES SCÉNARIOS ---");
        for (ScenarioResult result : scenarioResults) {
            Log.printLine("\n• " + result.scenarioName + ":");
            switch(result.scenarioNumber) {
                case 1:
                    Log.printLine("  📊 Architecture: Datacenter centralisé unique");
                    Log.printLine("  ✅ Avantages: Performance maximale, coûts réduits, maintenance centralisée");
                    Log.printLine("  ⚠️  Inconvénients: Latence élevée, point de défaillance unique");
                    Log.printLine("  🎯 Recommandation: Applications critiques nécessitant performance absolue");
                    Log.printLine("  📈 Métriques: " + result.totalVMs + " VMs, " + 
                                 String.format("%.2f", result.averageExecutionTime) + "s moyenne");
                    break;
                case 2:
                    Log.printLine("  📊 Architecture: Datacenters distribués par ville");
                    Log.printLine("  ✅ Avantages: Latence réduite, résilience géographique, autonomie locale");
                    Log.printLine("  ⚠️  Inconvénients: Coûts élevés, gestion complexe, duplication des ressources");
                    Log.printLine("  🎯 Recommandation: Campus avec besoins spécifiques et autonomie");
                    Log.printLine("  📈 Métriques: " + result.totalVMs + " VMs, " + 
                                 String.format("%.2f", result.cloudletsPerVM) + " cloudlets/VM");
                    break;
                case 3:
                    Log.printLine("  📊 Architecture: Modèle hybride centralisé + edge");
                    Log.printLine("  ✅ Avantages: Équilibre coût/performance, flexibilité, optimisation des ressources");
                    Log.printLine("  ⚠️  Inconvénients: Architecture complexe, gestion multi-niveaux");
                    Log.printLine("  🎯 Recommandation: Environnement multi-campus avec besoins variés");
                    Log.printLine("  📈 Métriques: " + result.totalVMs + " VMs, " + 
                                 String.format("%.2f", result.cloudletsPerVM) + " cloudlets/VM (meilleure efficacité)");
                    break;
            }
        }

        Log.printLine("\n" + "=".repeat(80));
        Log.printLine("SIMULATION TERMINÉE - " + scenarioResults.size() + " SCÉNARIOS ANALYSÉS");
        Log.printLine("=".repeat(80));
    }

    // Classe pour stocker les résultats des scénarios
    static class ScenarioResult {
        int scenarioNumber;
        String scenarioName;
        int totalVMs;
        int totalCloudlets;
        int successfulCloudlets;
        double successRate;
        double averageExecutionTime;
        double minExecutionTime;
        double maxExecutionTime;
        double makespan;
        double cloudletsPerVM;

        public ScenarioResult(int scenarioNumber, String scenarioName) {
            this.scenarioNumber = scenarioNumber;
            this.scenarioName = scenarioName;
        }
    }

    private static String getScenarioName(int scenario) {
        switch(scenario) {
            case 1: return "DATACENTER CENTRALISÉ";
            case 2: return "DATACENTER PAR VILLES";
            case 3: return "MODÈLE HYBRIDE";
            default: return "INCONNU";
        }
    }

    private static List<Datacenter> createDatacentersForScenario(int scenario) {
        List<Datacenter> datacenters = new ArrayList<>();
        
        switch(scenario) {
            case 1: // Centralisé - Un seul grand datacenter
                Log.printLine("Création du datacenter centralisé...");
                datacenters.add(createDatacenter("DC_Central_Tetouan", 200, 32, 524288, 2500, 200, 800));
                break;
                
            case 2: // Par villes - Un datacenter par campus
                Log.printLine("Création des datacenters par villes...");
                datacenters.add(createDatacenter("DC_Tetouan", 60, 32, 131072, 2500, 70, 200));
                datacenters.add(createDatacenter("DC_Tanger", 50, 64, 262144, 3000, 90, 250));
                datacenters.add(createDatacenter("DC_Hoceima", 20, 16, 65536, 2000, 30, 100));
                datacenters.add(createDatacenter("DC_Larache", 15, 16, 65536, 2000, 25, 80));
                datacenters.add(createDatacenter("DC_KsarElKebir", 15, 16, 65536, 2000, 25, 80));
                break;
                
            case 3: // Hybride - Mix de centralisé et edge
                Log.printLine("Création de l'architecture hybride...");
                datacenters.add(createDatacenter("DC_Central_Tetouan", 100, 32, 262144, 2500, 100, 400));
                datacenters.add(createDatacenter("DC_Edge_Tanger", 40, 64, 131072, 3000, 60, 200));
                datacenters.add(createDatacenter("DC_Edge_Hoceima", 15, 16, 32768, 2000, 20, 60));
                datacenters.add(createDatacenter("DC_Public_Cloud", 50, 16, 65536, 2000, 50, 150));
                break;
        }
        
        // Retirer les datacenters null de la liste
        datacenters.removeIf(dc -> dc == null);
        return datacenters;
    }

    private static void createVMsForScenario(int scenario, List<Vm> vmList, int brokerTetouanId, int brokerTangerId, 
                                           int brokerHoceimaId, int brokerLaracheId, int brokerKsarElKebirId) {
        
        Log.printLine("\n" + "-".repeat(50));
        Log.printLine("CRÉATION DES VMs - " + getScenarioName(scenario));
        Log.printLine("-".repeat(50));
        
        switch(scenario) {
            case 1: // Centralisé - Toutes les VMs dans le datacenter central
                Log.printLine("🎯 Stratégie: Toutes les VMs centralisées à Tétouan");
                createVMsForCampus(vmList, brokerTetouanId, 0, 80, 2000, 4096, 2, 10000, 1000, "LMS_Central");
                createVMsForCampus(vmList, brokerTetouanId, 80, 60, 2500, 8192, 2, 20000, 1000, "Sciences_Central");
                createVMsForCampus(vmList, brokerTangerId, 140, 40, 3000, 32768, 8, 50000, 10000, "HPC_Central");
                createVMsForCampus(vmList, brokerTangerId, 180, 30, 2500, 16384, 4, 30000, 5000, "Medecine_Central");
                createVMsForCampus(vmList, brokerHoceimaId, 210, 20, 1500, 2048, 2, 10000, 500, "Edge_Central");
                createVMsForCampus(vmList, brokerLaracheId, 230, 15, 1500, 2048, 2, 10000, 500, "FP_Central");
                createVMsForCampus(vmList, brokerKsarElKebirId, 245, 15, 1600, 2048, 2, 12000, 600, "FP2_Central");
                break;
                
            case 2: // Par villes - VMs locales dans chaque datacenter
                Log.printLine("🎯 Stratégie: VMs locales dans chaque datacenter de ville");
                createVMsForCampus(vmList, brokerTetouanId, 0, 40, 2000, 4096, 2, 10000, 1000, "LMS_Tetouan_Local");
                createVMsForCampus(vmList, brokerTetouanId, 40, 30, 2500, 8192, 2, 20000, 1000, "Sciences_Tetouan_Local");
                createVMsForCampus(vmList, brokerTangerId, 70, 20, 3000, 32768, 8, 50000, 10000, "HPC_Tanger_Local");
                createVMsForCampus(vmList, brokerTangerId, 90, 15, 2500, 16384, 4, 30000, 5000, "Medecine_Tanger_Local");
                createVMsForCampus(vmList, brokerHoceimaId, 105, 10, 1500, 2048, 2, 10000, 500, "Edge_Hoceima_Local");
                createVMsForCampus(vmList, brokerLaracheId, 115, 8, 1500, 2048, 2, 10000, 500, "FP_Larache_Local");
                createVMsForCampus(vmList, brokerKsarElKebirId, 123, 7, 1600, 2048, 2, 12000, 600, "FP_Ksar_Local");
                break;
                
            case 3: // Hybride - Mix selon la criticité
                Log.printLine("🎯 Stratégie: Mix centralisé (critique) + edge (latence) + cloud (standard)");
                // Tétouan (central) - Charges critiques
                createVMsForCampus(vmList, brokerTetouanId, 0, 30, 2000, 4096, 2, 10000, 1000, "LMS_Hybrid_Central");
                createVMsForCampus(vmList, brokerTetouanId, 30, 20, 2500, 8192, 2, 20000, 1000, "Sciences_Hybrid_Central");
                
                // Tanger (edge) - HPC et médical (latence critique)
                createVMsForCampus(vmList, brokerTangerId, 50, 15, 3000, 32768, 8, 50000, 10000, "HPC_Hybrid_Edge");
                createVMsForCampus(vmList, brokerTangerId, 65, 10, 2500, 16384, 4, 30000, 5000, "Medical_Hybrid_Edge");
                
                // Hoceima (edge) - Applications locales
                createVMsForCampus(vmList, brokerHoceimaId, 75, 8, 1500, 2048, 2, 10000, 500, "Apps_Hybrid_Edge");
                
                // Cloud public - Charges non critiques
                createVMsForCampus(vmList, brokerLaracheId, 83, 6, 1800, 2048, 2, 15000, 800, "FP_Hybrid_Public");
                createVMsForCampus(vmList, brokerKsarElKebirId, 89, 6, 1800, 2048, 2, 15000, 800, "FP2_Hybrid_Public");
                break;
        }
        
        Log.printLine("✅ Création terminée: " + vmList.size() + " VMs créées");
    }

    private static Datacenter createDatacenter(String name, int numHosts, int numPesPerHost, 
                                              int ramPerHost, int mipsPerPe, 
                                              double minPowerWatts, double maxPowerWatts) {
        List<Host> hostList = new ArrayList<Host>();

        for (int i = 0; i < numHosts; i++) {
            List<Pe> peList = new ArrayList<Pe>();
            
            for (int j = 0; j < numPesPerHost; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(mipsPerPe)));
            }

            hostList.add(
                new Host(
                    i,
                    new RamProvisionerSimple(ramPerHost),
                    new BwProvisionerSimple(10000),
                    2000000,
                    peList,
                    new VmSchedulerTimeShared(peList)
                )
            );
        }

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86_64", "Linux", "KVM", hostList, 1.0, 3.0, 0.05, 
                0.001, 0.1);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, 
                    new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Erreur lors de la création du datacenter " + name + ": " + e.getMessage());
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(String name) {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Erreur lors de la création du broker " + name + ": " + e.getMessage());
        }
        return broker;
    }

    private static void createVMsForCampus(List<Vm> vmList, int brokerId, int startId, 
                                          int count, int mips, int ram, int pesNumber,
                                          long size, long bw, String type) {
        for (int i = 0; i < count; i++) {
            Vm vm = new Vm(startId + i, brokerId, mips, pesNumber, ram, bw, size, "KVM", 
                          new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        Log.printLine("   ├─ " + type + ": " + count + " VMs (" + mips + " MIPS, " + 
                     (ram/1024) + "GB RAM, " + pesNumber + " vCPUs)");
    }

    private static void distributeVMsToBrokers(List<Vm> vmlist, DatacenterBroker brokerTetouan, 
                                             DatacenterBroker brokerTanger, DatacenterBroker brokerHoceima,
                                             DatacenterBroker brokerLarache, DatacenterBroker brokerKsarElKebir) {
        List<Vm> vmsTetouan = new ArrayList<Vm>();
        List<Vm> vmsTanger = new ArrayList<Vm>();
        List<Vm> vmsHoceima = new ArrayList<Vm>();
        List<Vm> vmsLarache = new ArrayList<Vm>();
        List<Vm> vmsKsarElKebir = new ArrayList<Vm>();

        int brokerTetouanId = brokerTetouan.getId();
        int brokerTangerId = brokerTanger.getId();
        int brokerHoceimaId = brokerHoceima.getId();
        int brokerLaracheId = brokerLarache.getId();
        int brokerKsarElKebirId = brokerKsarElKebir.getId();

        for (Vm vm : vmlist) {
            if (vm.getUserId() == brokerTetouanId) vmsTetouan.add(vm);
            else if (vm.getUserId() == brokerTangerId) vmsTanger.add(vm);
            else if (vm.getUserId() == brokerHoceimaId) vmsHoceima.add(vm);
            else if (vm.getUserId() == brokerLaracheId) vmsLarache.add(vm);
            else if (vm.getUserId() == brokerKsarElKebirId) vmsKsarElKebir.add(vm);
        }

        brokerTetouan.submitVmList(vmsTetouan);
        brokerTanger.submitVmList(vmsTanger);
        brokerHoceima.submitVmList(vmsHoceima);
        brokerLarache.submitVmList(vmsLarache);
        brokerKsarElKebir.submitVmList(vmsKsarElKebir);
        
        Log.printLine("Distribution des VMs aux brokers:");
        Log.printLine("  - Tétouan: " + vmsTetouan.size() + " VMs");
        Log.printLine("  - Tanger: " + vmsTanger.size() + " VMs");
        Log.printLine("  - Hoceima: " + vmsHoceima.size() + " VMs");
        Log.printLine("  - Larache: " + vmsLarache.size() + " VMs");
        Log.printLine("  - Ksar El-Kébir: " + vmsKsarElKebir.size() + " VMs");
    }

    private static void createCloudletsForScenario(int scenario, List<Cloudlet> cloudletList, 
                                                 int brokerTetouanId, int brokerTangerId, int brokerHoceimaId,
                                                 int brokerLaracheId, int brokerKsarElKebirId,
                                                 DatacenterBroker brokerTetouan, DatacenterBroker brokerTanger,
                                                 DatacenterBroker brokerHoceima, DatacenterBroker brokerLarache,
                                                 DatacenterBroker brokerKsarElKebir) {
        
        Log.printLine("\n" + "-".repeat(50));
        Log.printLine("CRÉATION DES CLOUDLETS - " + getScenarioName(scenario));
        Log.printLine("-".repeat(50));

        // Récupérer les VMs de chaque broker
        List<Vm> vmsTetouan = brokerTetouan.getVmList();
        List<Vm> vmsTanger = brokerTanger.getVmList();
        List<Vm> vmsHoceima = brokerHoceima.getVmList();
        List<Vm> vmsLarache = brokerLarache.getVmList();
        List<Vm> vmsKsarElKebir = brokerKsarElKebir.getVmList();

        // Nombre de cloudlets réduit pour accélérer la simulation comparative
        createCloudletsForCampus(cloudletList, brokerTetouanId, 0, 500, 40000, 500, 300, 2, vmsTetouan, "LMS_Tetouan");
        createCloudletsForCampus(cloudletList, brokerTetouanId, 500, 400, 100000, 1024, 512, 2, vmsTetouan, "Sciences_Tetouan");
        createCloudletsForCampus(cloudletList, brokerTangerId, 900, 300, 500000, 5120, 2048, 8, vmsTanger, "HPC_Tanger");
        createCloudletsForCampus(cloudletList, brokerTangerId, 1200, 200, 200000, 10240, 5120, 4, vmsTanger, "Medical_Tanger");
        createCloudletsForCampus(cloudletList, brokerHoceimaId, 1400, 100, 30000, 400, 200, 2, vmsHoceima, "Edge_Hoceima");
        createCloudletsForCampus(cloudletList, brokerLaracheId, 1500, 80, 25000, 400, 200, 2, vmsLarache, "FP_Larache");
        createCloudletsForCampus(cloudletList, brokerKsarElKebirId, 1580, 80, 35000, 450, 250, 2, vmsKsarElKebir, "FP_KsarElKebir");
        
        Log.printLine("✅ Création terminée: " + cloudletList.size() + " cloudlets créés");
    }

    private static void createCloudletsForCampus(List<Cloudlet> cloudletList, int brokerId, 
                                                int startId, int count, long baseLength, 
                                                long fileSize, long outputSize, int pesNumber, 
                                                List<Vm> vms, String workloadType) {
        UtilizationModel utilizationModel = new UtilizationModelStochastic();
        
        for (int i = 0; i < count; i++) {
            double variation = 0.7 + (Math.random() * 0.6);
            long length = (long)(baseLength * variation);
            
            Cloudlet cloudlet = new Cloudlet(startId + i, length, pesNumber, fileSize, 
                                            outputSize, utilizationModel, 
                                            utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            
            if (!vms.isEmpty()) {
                cloudlet.setVmId(vms.get(i % vms.size()).getId());
            }
            
            cloudletList.add(cloudlet);
        }
        Log.printLine("   ├─ " + workloadType + ": " + count + " cloudlets (" + 
                     (baseLength/1000) + "K MI moyenne)");
    }

    private static void distributeCloudletsToBrokers(List<Cloudlet> cloudletList, DatacenterBroker brokerTetouan,
                                                   DatacenterBroker brokerTanger, DatacenterBroker brokerHoceima,
                                                   DatacenterBroker brokerLarache, DatacenterBroker brokerKsarElKebir) {
        List<Cloudlet> cloudletsTetouan = new ArrayList<Cloudlet>();
        List<Cloudlet> cloudletsTanger = new ArrayList<Cloudlet>();
        List<Cloudlet> cloudletsHoceima = new ArrayList<Cloudlet>();
        List<Cloudlet> cloudletsLarache = new ArrayList<Cloudlet>();
        List<Cloudlet> cloudletsKsarElKebir = new ArrayList<Cloudlet>();

        int brokerTetouanId = brokerTetouan.getId();
        int brokerTangerId = brokerTanger.getId();
        int brokerHoceimaId = brokerHoceima.getId();
        int brokerLaracheId = brokerLarache.getId();
        int brokerKsarElKebirId = brokerKsarElKebir.getId();

        for (Cloudlet cloudlet : cloudletList) {
            if (cloudlet.getUserId() == brokerTetouanId) cloudletsTetouan.add(cloudlet);
            else if (cloudlet.getUserId() == brokerTangerId) cloudletsTanger.add(cloudlet);
            else if (cloudlet.getUserId() == brokerHoceimaId) cloudletsHoceima.add(cloudlet);
            else if (cloudlet.getUserId() == brokerLaracheId) cloudletsLarache.add(cloudlet);
            else if (cloudlet.getUserId() == brokerKsarElKebirId) cloudletsKsarElKebir.add(cloudlet);
        }

        brokerTetouan.submitCloudletList(cloudletsTetouan);
        brokerTanger.submitCloudletList(cloudletsTanger);
        brokerHoceima.submitCloudletList(cloudletsHoceima);
        brokerLarache.submitCloudletList(cloudletsLarache);
        brokerKsarElKebir.submitCloudletList(cloudletsKsarElKebir);
        
        Log.printLine("Distribution des cloudlets aux brokers:");
        Log.printLine("  - Tétouan: " + cloudletsTetouan.size() + " cloudlets");
        Log.printLine("  - Tanger: " + cloudletsTanger.size() + " cloudlets");
        Log.printLine("  - Hoceima: " + cloudletsHoceima.size() + " cloudlets");
        Log.printLine("  - Larache: " + cloudletsLarache.size() + " cloudlets");
        Log.printLine("  - Ksar El-Kébir: " + cloudletsKsarElKebir.size() + " cloudlets");
    }
}