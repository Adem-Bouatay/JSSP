import java.util.*;

// Define an Operation class representing a job's operation on a machine
class Operation {
    int jobId;
    int machineId;
    int processingTime;

    public Operation(int jobId, int machineId, int processingTime) {
        this.jobId = jobId;
        this.machineId = machineId;
        this.processingTime = processingTime;
    }

    @Override
    public String toString() {
        return "Job" + jobId + "(M" + machineId + ", T" + processingTime + ")";
    }
}

// Define a Schedule class to represent a solution
class Schedule {
    List<Operation> operations;
    int makespan;

    public Schedule(List<Operation> operations) {
        this.operations = new ArrayList<>(operations);
        this.makespan = calculateMakespan();
    }

    // Calculate the makespan (fitness) of the schedule
    private int calculateMakespan() {
        Map<Integer, Integer> machineCompletionTime = new HashMap<>();
        Map<Integer, Integer> jobCompletionTime = new HashMap<>();
        int maxTime = 0;

        for (Operation op : operations) {
            int machineTime = machineCompletionTime.getOrDefault(op.machineId, 0);
            int jobTime = jobCompletionTime.getOrDefault(op.jobId, 0);
            int startTime = Math.max(machineTime, jobTime);

            int endTime = startTime + op.processingTime;
            machineCompletionTime.put(op.machineId, endTime);
            jobCompletionTime.put(op.jobId, endTime);

            maxTime = Math.max(maxTime, endTime);
        }

        return maxTime;
    }

    @Override
    public String toString() {
        return "Schedule: " + operations + ", Makespan: " + makespan;
    }
}

public class CulturalAlgorithmJSSP {
    private static final int NUM_JOBS = 3;
    private static final int NUM_MACHINES = 3;
    private static final int POPULATION_SIZE = 10;
    private static final int MAX_STAGNATION = 20;

    private List<Operation> allOperations = new ArrayList<>();
    private List<Schedule> population = new ArrayList<>();
    private Map<Integer, List<Integer>> culturalKnowledge = new HashMap<>();
    private Random random = new Random();

    public static void main(String[] args) {
        CulturalAlgorithmJSSP algorithm = new CulturalAlgorithmJSSP();
        algorithm.initializeOperations();
        algorithm.run();
    }

    // Initialize job operations
    private void initializeOperations() {
    	allOperations.add(new Operation(1, 1, 3)); // Job 1, Machine 1, Time 3
    	allOperations.add(new Operation(1, 2, 4)); // Job 1, Machine 2, Time 4
    	allOperations.add(new Operation(2, 2, 2)); // Job 2, Machine 2, Time 2
    	allOperations.add(new Operation(2, 1, 5)); // Job 2, Machine 1, Time 5
    	allOperations.add(new Operation(3, 3, 6)); // Job 3, Machine 3, Time 6
    	allOperations.add(new Operation(3, 2, 3)); // Job 3, Machine 2, Time 3
    }

    // Run the cultural algorithm
    private void run() {
        initializePopulation();
        int stagnationCounter = 0;
        int bestMakespan = Integer.MAX_VALUE;

        while (stagnationCounter < MAX_STAGNATION) {
            evaluateFitness();
            updateCulturalKnowledge();
            evolvePopulation();
            Schedule bestSchedule = getBestSchedule();

            if (bestSchedule.makespan < bestMakespan) {
                bestMakespan = bestSchedule.makespan;
                stagnationCounter = 0;
            } else {
                stagnationCounter++;
            }
            new ScheduleVisualizer(bestSchedule);
            System.out.println("Best Schedule: " + bestSchedule);
        }
    }

    // Initialize the population with valid schedules
    private void initializePopulation() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Operation> shuffledOperations = generateValidSchedule();
            population.add(new Schedule(shuffledOperations));
        }
    }


    // Generate a valid schedule by respecting job operation sequences
    private List<Operation> generateValidSchedule() {
    Map<Integer, Queue<Operation>> jobQueues = new HashMap<>();
    for (Operation op : allOperations) {
        jobQueues.putIfAbsent(op.jobId, new LinkedList<>());
        jobQueues.get(op.jobId).add(op);
    }

    List<Operation> validSchedule = new ArrayList<>();
    while (!jobQueues.isEmpty()) {
        List<Integer> availableJobs = new ArrayList<>(jobQueues.keySet());
        Collections.shuffle(availableJobs, random);

        for (int jobId : availableJobs) {
            Queue<Operation> queue = jobQueues.get(jobId);
            if (queue != null && !queue.isEmpty()) {
                validSchedule.add(queue.poll());
            }
            if (queue.isEmpty()) {
                jobQueues.remove(jobId);
            }
        }
    }

    return validSchedule;
}


    // Evaluate the fitness of the population
    private void evaluateFitness() {
        for (Schedule schedule : population) {
            schedule.makespan = schedule.makespan; // Makespan already calculated in constructor
        }
    }

    // Update the cultural knowledge repository
    private void updateCulturalKnowledge() {
        Schedule bestSchedule = getBestSchedule();
        culturalKnowledge.clear();

        for (Operation op : bestSchedule.operations) {
            culturalKnowledge.putIfAbsent(op.machineId, new ArrayList<>());
            culturalKnowledge.get(op.machineId).add(op.jobId);
        }
    }

    // Evolve the population using crossover, mutation, and cultural influence
    private void evolvePopulation() {
        List<Schedule> newPopulation = new ArrayList<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            Schedule parent1 = selectParent();
            Schedule parent2 = selectParent();
            List<Operation> childOperations = crossover(parent1.operations, parent2.operations);
            mutate(childOperations);
            applyCulturalInfluence(childOperations);
            newPopulation.add(new Schedule(childOperations));
        }

        population = newPopulation;
    }


    // Crossover operator
    private List<Operation> crossover(List<Operation> parent1, List<Operation> parent2) {
        // Map to track the sequence of operations for each job
        Map<Integer, Queue<Operation>> jobOperationMap = new HashMap<>();
        for (Operation op : parent1) {
            jobOperationMap.putIfAbsent(op.jobId, new LinkedList<>());
            jobOperationMap.get(op.jobId).add(op);
        }

        List<Operation> child = new ArrayList<>();
        Set<Integer> addedJobs = new HashSet<>();

        // Add operations up to the crossover point
        int crossoverPoint = random.nextInt(parent1.size());
        for (int i = 0; i < crossoverPoint; i++) {
            Operation op = parent1.get(i);
            child.add(op);
            addedJobs.add(op.jobId);
            jobOperationMap.get(op.jobId).poll(); // Remove the operation since it is used
        }

        // Add remaining operations from parent2 while maintaining order
        for (Operation op : parent2) {
            if (!addedJobs.contains(op.jobId)) {
                Queue<Operation> queue = jobOperationMap.get(op.jobId);
                if (queue != null && !queue.isEmpty()) {
                    child.add(queue.poll());
                    addedJobs.add(op.jobId);
                }
            }
        }

        // Add any remaining operations from jobOperationMap
        for (Queue<Operation> queue : jobOperationMap.values()) {
            while (!queue.isEmpty()) {
                child.add(queue.poll());
            }
        }

        return child;
    }


    // Mutation operator
    private void mutate(List<Operation> operations) {
        if (random.nextDouble() < 0.1) {
            int idx1 = random.nextInt(operations.size());
            int idx2 = random.nextInt(operations.size());
            
            Operation op1 = operations.get(idx1);
            Operation op2 = operations.get(idx2);

            // Swap only if they belong to different jobs
            if (op1.jobId != op2.jobId) {
                Collections.swap(operations, idx1, idx2);
            }
        }
    }


    // Apply cultural influence
    private void applyCulturalInfluence(List<Operation> operations) {
        // Group operations by job ID
        Map<Integer, List<Operation>> jobGroupedOps = new HashMap<>();
        for (Operation op : operations) {
            jobGroupedOps.putIfAbsent(op.jobId, new ArrayList<>());
            jobGroupedOps.get(op.jobId).add(op);
        }

        List<Operation> influencedOps = new ArrayList<>();
        for (Map.Entry<Integer, List<Operation>> entry : jobGroupedOps.entrySet()) {
            List<Operation> jobOps = entry.getValue();
            jobOps.sort((o1, o2) -> {
                // Reorder based on cultural knowledge if it exists
                boolean o1Preferred = culturalKnowledge.containsKey(o1.machineId) &&
                                      culturalKnowledge.get(o1.machineId).contains(o1.jobId);
                boolean o2Preferred = culturalKnowledge.containsKey(o2.machineId) &&
                                      culturalKnowledge.get(o2.machineId).contains(o2.jobId);

                if (o1Preferred && !o2Preferred) return -1;
                if (!o1Preferred && o2Preferred) return 1;
                return 0; // Retain original order if no preference
            });

            influencedOps.addAll(jobOps);
        }

        // Rebuild the operation list with influenced order
        operations.clear();
        operations.addAll(influencedOps);
    }


    // Select a parent using tournament selection
    private Schedule selectParent() {
        Schedule s1 = population.get(random.nextInt(population.size()));
        Schedule s2 = population.get(random.nextInt(population.size()));
        return s1.makespan < s2.makespan ? s1 : s2;
    }

    // Get the best schedule in the population
    private Schedule getBestSchedule() {
        return Collections.min(population, Comparator.comparingInt(s -> s.makespan));
    }
}
