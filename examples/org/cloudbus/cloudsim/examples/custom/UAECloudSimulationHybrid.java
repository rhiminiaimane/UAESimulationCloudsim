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
 * Simulation CloudSim pour Université Abdelmalek Essaadi (UAE) - Hybrid Datacenter Model
 */
public class UAECloudSimulationHybrid {

    private static List<Vm> globalVmList;
    private static List<Datacenter> globalDatacenters;

    public static void main(String[] args) {
        Log.printLine("========================================");
        Log.printLine("Simulation CloudSim - UAE 5 Campus");
        Log.printLine("SCÉNARIO MODÈLE HYBRIDE");
        Log.printLine("========================================");

        Log.printLine("\n\n" + "=".repeat(80));
        Log.printLine("DÉBUT DU SCÉNARIO 3 : MODÈLE HYBRIDE");
        Log.printLine("=".repeat(80));

        ScenarioResult result = runScenario(3);
        if (result == null) {
            Log.printLine("Erreur: Le scénario hybride n'a pas pu être exécuté.");
        }

        Log.printLine("\n" + "=".repeat(80));
        Log.printLine("FIN DU SCÉNARIO 3 : MODÈLE HYBRIDE");
        Log.printLine("=".repeat(80));
    }

    private static ScenarioResult runScenario(int scenario) {
        try {
            int num_user = 5;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            // Create hybrid datacenters: centralized, edge sites, and a public cloud
            globalDatacenters = new ArrayList<>();
            Log.printLine("Création des datacenters hybrides...");

            // Centralized datacenter
            globalDatacenters.add(createDatacenter("DC_Central_Tetouan", 100, 16, 262144, 2500, 150, 600));
            // Edge datacenters for campuses
            globalDatacenters.add(createDatacenter("DC_Edge_Tanger", 30, 8, 65536, 2000, 70, 250));
            globalDatacenters.add(createDatacenter("DC_Edge_Hoceima", 20, 6, 49152, 1500, 60, 200));
            // Public cloud datacenter to handle overflow HPC workloads
            globalDatacenters.add(createDatacenter("DC_PublicCloud", 200, 32, 524288, 3000, 300, 1000));

            if (globalDatacenters.isEmpty()) {
                Log.printLine("Erreur: Aucun datacenter créé pour le scénario " + scenario);
                return null;
            }

            printDatacenterDetails(scenario);

            // Create brokers for campuses + proxy broker for public cloud
            DatacenterBroker brokerTetouan = createBroker("Broker_Tetouan");
            DatacenterBroker brokerTanger = createBroker("Broker_Tanger");
            DatacenterBroker brokerHoceima = createBroker("Broker_Hoceima");
            DatacenterBroker brokerPublicCloud = createBroker("Broker_PublicCloud");

            if (brokerTetouan == null || brokerTanger == null || brokerHoceima == null || brokerPublicCloud == null) {
                Log.printLine("Erreur: Impossible de créer tous les brokers");
                return null;
            }

            int brokerTetouanId = brokerTetouan.getId();
            int brokerTangerId = brokerTanger.getId();
            int brokerHoceimaId = brokerHoceima.getId();
            int brokerPublicCloudId = brokerPublicCloud.getId();

            globalVmList = new ArrayList<>();

            // VMs on centralized datacenter (Tetouan)
		createVMsForCampus(globalVmList, brokerTetouanId, 0, 90, 2500, 8192, 4, 20000, 2000, "Central LMS_Sciences");
		// VMs on edge datacenters
		createVMsForCampus(globalVmList, brokerTangerId, 90, 50, 2000, 4096, 2, 8000, 800, "Edge Tanger HPC");
		createVMsForCampus(globalVmList, brokerHoceimaId, 140, 30, 1500, 2048, 2, 5000, 500, "Edge Hoceima");
		// Public cloud to handle heavy HPC jobs overflow
		createVMsForCampus(globalVmList, brokerPublicCloudId, 170, 130, 3000, 16384, 8, 50000, 5000, "PublicCloud HPC");


            if (globalVmList.isEmpty()) {
                Log.printLine("Erreur: Aucune VM créée pour le scénario " + scenario);
                return null;
            }

            printVMsDetailsByCampus(globalVmList, brokerTetouanId, brokerTangerId, brokerHoceimaId, brokerPublicCloudId);

            distributeVMsToBrokers(globalVmList, brokerTetouan, brokerTanger, brokerHoceima, brokerPublicCloud);

            List<Cloudlet> cloudletList = new ArrayList<>();

            // Cloudlets for hybrid model
	createCloudletsForCampus(cloudletList, brokerTetouanId, 0, 600, 40000, 500, 300, 2,
		brokerTetouan.getVmList(), "LMS & Sciences Central");
	createCloudletsForCampus(cloudletList, brokerTangerId, 600, 300, 500000, 5120, 2048, 8,
		brokerTanger.getVmList(), "HPC Edge Tanger");
	createCloudletsForCampus(cloudletList, brokerHoceimaId, 900, 200, 30000, 400, 200, 2,
		brokerHoceima.getVmList(), "Edge Hoceima");
	createCloudletsForCampus(cloudletList, brokerPublicCloudId, 1100, 400, 800000, 10240, 5120, 16,
		brokerPublicCloud.getVmList(), "HPC Public Cloud");



            if (cloudletList.isEmpty()) {
                Log.printLine("Erreur: Aucun cloudlet créé pour le scénario " + scenario);
                return null;
            }

            printCloudletsDetails(cloudletList, brokerTetouanId, brokerTangerId, brokerHoceimaId, brokerPublicCloudId);

            distributeCloudletsToBrokers(cloudletList, brokerTetouan, brokerTanger, brokerHoceima, brokerPublicCloud);

            Log.printLine("\n" + "=".repeat(50));
            Log.printLine("DÉMARRAGE SIMULATION - MODÈLE HYBRIDE");
            Log.printLine("=".repeat(50));
            Log.printLine("Résumé configuration:");
            Log.printLine("  - Total VMs: " + globalVmList.size());
            Log.printLine("  - Total Cloudlets: " + cloudletList.size());
            Log.printLine("  - Datacenters: " + globalDatacenters.size());

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            ScenarioResult result = collectDetailedScenarioResults(scenario, brokerTetouan, brokerTanger,
                    brokerHoceima, brokerPublicCloud);

            printDetailedScenarioSummary(result, brokerTetouan, brokerTanger, brokerHoceima, brokerPublicCloud);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Erreur dans le scénario " + scenario + ": " + e.getMessage());
            return null;
        }
    }

    private static Datacenter createDatacenter(String name, int numHosts, int numPesPerHost,
                                              int ramPerHost, int mipsPerPe,
                                              double minPowerWatts, double maxPowerWatts) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < numHosts; i++) {
            List<Pe> peList = new ArrayList<>();

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
                (ram / 1024) + "GB RAM, " + pesNumber + " vCPUs)");
    }

    private static void distributeVMsToBrokers(List<Vm> vmlist, DatacenterBroker... brokers) {
        for (DatacenterBroker broker : brokers) {
            List<Vm> vmForBroker = new ArrayList<>();
            int brokerId = broker.getId();
            for (Vm vm : vmlist) {
                if (vm.getUserId() == brokerId) {
                    vmForBroker.add(vm);
                }
            }
            broker.submitVmList(vmForBroker);
            Log.printLine("VMs assignées à " + broker.getName() + ": " + vmForBroker.size());
        }
    }

    private static void createCloudletsForCampus(List<Cloudlet> cloudletList, int brokerId,
                                                int startId, int count, long baseLength,
                                                long fileSize, long outputSize, int pesNumber,
                                                List<Vm> vms, String workloadType) {
        UtilizationModel utilizationModel = new UtilizationModelStochastic();

        for (int i = 0; i < count; i++) {
            double variation = 0.7 + (Math.random() * 0.6);
            long length = (long) (baseLength * variation);

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
                (baseLength / 1000) + "K MI moyenne)");
    }

    private static void distributeCloudletsToBrokers(List<Cloudlet> cloudletList, DatacenterBroker... brokers) {
        for (DatacenterBroker broker : brokers) {
            List<Cloudlet> cloudletsForBroker = new ArrayList<>();
            int brokerId = broker.getId();
            for (Cloudlet cloudlet : cloudletList) {
                if (cloudlet.getUserId() == brokerId) {
                    cloudletsForBroker.add(cloudlet);
                }
            }
            broker.submitCloudletList(cloudletsForBroker);
            Log.printLine("Cloudlets assignés à " + broker.getName() + ": " + cloudletsForBroker.size());
        }
    }

    private static void printDatacenterDetails(int scenario) {
        Log.printLine("\n" + "-".repeat(60));
        Log.printLine("CONFIGURATION DES DATACENTERS - SCÉNARIO HYBRIDE");
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

        Log.printLine("\n📈 TOTAUX SCÉNARIO HYBRIDE:");
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

        String[] campusNames = {"Tétouan", "Tanger", "Al Hoceïma", "Public Cloud"};

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

        String[] campusNames = {"Tétouan", "Tanger", "Al Hoceïma", "Public Cloud"};
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

    private static ScenarioResult collectDetailedScenarioResults(int scenario, DatacenterBroker... brokers) {
        ScenarioResult result = new ScenarioResult(scenario, "MODÈLE HYBRIDE");

        int totalCloudlets = 0;
        int totalSuccess = 0;
        double totalExecutionTime = 0;
        double globalMinTime = Double.MAX_VALUE;
        double globalMaxTime = 0;
        int totalVMs = 0;

        for (DatacenterBroker broker : brokers) {
            List<Cloudlet> cloudlets = broker.getCloudletReceivedList();
            totalCloudlets += cloudlets.size();
            totalVMs += broker.getVmList().size();

            for (Cloudlet cloudlet : cloudlets) {
                if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                    double execTime = cloudlet.getActualCPUTime();
                    totalSuccess++;
                    totalExecutionTime += execTime;
                    globalMinTime = Math.min(globalMinTime, execTime);
                    globalMaxTime = Math.max(globalMaxTime, execTime);
                }
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

    private static void printDetailedScenarioSummary(ScenarioResult result, DatacenterBroker... brokers) {
        DecimalFormat dft = new DecimalFormat("###.##");

        Log.printLine("\n" + "=".repeat(70));
        Log.printLine("RÉSUMÉ DÉTAILLÉ SCÉNARIO " + result.scenarioNumber + " : " + result.scenarioName);
        Log.printLine("=".repeat(70));

        Log.printLine("🏗️  ARCHITECTURE DÉTAILLÉE:");
        Log.printLine("   ├─ Datacenters: " + globalDatacenters.size());
        for (Datacenter dc : globalDatacenters) {
            Log.printLine("   │  └─ " + dc.getName() + ": " + dc.getHostList().size() + " hosts");
        }
        Log.printLine("   ├─ VMs totales: " + result.totalVMs);
        Log.printLine("   ├─ Cloudlets totaux: " + result.totalCloudlets);
        Log.printLine("   └─ Ratio Cloudlets/VM: " + String.format("%.2f", result.cloudletsPerVM));

        Log.printLine("\n⚡ PERFORMANCE DÉTAILLÉE:");
        Log.printLine("   ├─ Taux de succès global: " + String.format("%.2f", result.successRate) + "%");
        Log.printLine("   ├─ Temps moyen d'exécution: " + dft.format(result.averageExecutionTime) + " s");
        Log.printLine("   ├─ Temps minimum: " + dft.format(result.minExecutionTime) + " s");
        Log.printLine("   ├─ Temps maximum: " + dft.format(result.maxExecutionTime) + " s");
        Log.printLine("   └─ Makespan total: " + dft.format(result.makespan) + " s");

        Log.printLine("\n📊 PERFORMANCE PAR CAMPUS:");
        String[] campusNames = {"Tétouan", "Tanger", "Al Hoceïma", "Public Cloud"};
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
    }

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
}
