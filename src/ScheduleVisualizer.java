import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ScheduleVisualizer extends JFrame {
    private final Schedule schedule;

    public ScheduleVisualizer(Schedule schedule) {
        this.schedule = schedule;
        setTitle("Improved Job Shop Scheduling Visualization");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new GanttChartPanel(schedule));
        setVisible(true);
    }

    public static void main(String[] args) {
        // Example schedule
        List<Operation> operations = List.of(
            new Operation(1, 1, 3),
            new Operation(1, 2, 4),
            new Operation(2, 1, 5),
            new Operation(2, 2, 2),
            new Operation(3, 3, 6),
            new Operation(3, 2, 3)
        );

        Schedule schedule = new Schedule(operations);
        new ScheduleVisualizer(schedule);
    }
}

class GanttChartPanel extends JPanel {
    private final Schedule schedule;

    public GanttChartPanel(Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Define layout constants
        int margin = 100;
        int barHeight = 30;
        int barSpacing = 20;
        int timeUnit = 20; // Pixels per time unit for better spacing

        // Track machine completion times
        int[] machineCompletionTimes = new int[10];

        // Set background color
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw time axis
        g.setColor(Color.BLACK);
        g.drawLine(margin, getHeight() - 50, getWidth() - 50, getHeight() - 50);
        for (int t = 0; t <= getWidth() - margin; t += timeUnit * 2) {
            g.drawString(Integer.toString(t / timeUnit), margin + t, getHeight() - 35);
        }

        // Draw machine rows and operations
        for (Operation op : schedule.operations) {
            int machineId = op.machineId;
            int jobId = op.jobId;
            int processingTime = op.processingTime;

            int startTime = machineCompletionTimes[machineId];
            int endTime = startTime + processingTime;

            // Update machine completion time
            machineCompletionTimes[machineId] = endTime;

            int x = margin + startTime * timeUnit;
            int y = margin + (machineId - 1) * (barHeight + barSpacing);

            // Draw the operation bar
            g.setColor(getJobColor(jobId));
            g.fillRect(x, y, processingTime * timeUnit, barHeight);

            // Draw operation border
            g.setColor(Color.BLACK);
            g.drawRect(x, y, processingTime * timeUnit, barHeight);

            // Draw job label and time range
            g.setColor(Color.BLACK);
            g.drawString("Job " + jobId, x + 5, y + barHeight / 2);
            g.drawString("T" + startTime + "-" + endTime, x + 5, y + barHeight - 5);
        }

        // Draw machine labels
        g.setColor(Color.BLACK);
        for (int i = 0; i < machineCompletionTimes.length; i++) {
            int y = margin + i * (barHeight + barSpacing);
            g.drawString("Machine " + (i + 1), 10, y + barHeight / 2);
        }
    }

    // Helper method to assign a unique color to each job
    private Color getJobColor(int jobId) {
        switch (jobId) {
            case 1: return Color.RED;
            case 2: return Color.BLUE;
            case 3: return Color.GREEN;
            default: return Color.GRAY;
        }
    }
}
