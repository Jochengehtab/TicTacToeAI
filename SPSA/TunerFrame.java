package src.SPSA;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TunerFrame extends JFrame {

    // Time control (seconds)
    private final JTextField tfBaseTime = new JTextField("20.0", 6);
    private final JTextField tfInc = new JTextField("0.2", 6);

    // Board config
    private final JSpinner spSize = new JSpinner(new SpinnerNumberModel(10, 3, 32, 1));
    private final JSpinner spOffset = new JSpinner(new SpinnerNumberModel(5, 0, 31, 1));

    // SPSA hyperparameters
    private final JTextField tfA = new JTextField("0.2", 6);
    private final JTextField tfC = new JTextField("0.5", 6);
    private final JTextField tfAConst = new JTextField("200", 6);
    private final JTextField tfAlpha = new JTextField("0.601", 6);
    private final JTextField tfGamma = new JTextField("0.102", 6);

    // Evaluation settings
    private final JSpinner spPairs = new JSpinner(new SpinnerNumberModel(16, 1, 100000, 1)); // pairs per iteration
    private final JSpinner spThreads = new JSpinner(); // set in ctor based on cores

    // Multi-parameter input
    private final JTextArea taParams = new JTextArea(8, 48);

    private final JProgressBar progress = new JProgressBar();
    private final JTextArea log = new JTextArea(18, 72);

    private volatile boolean stopRequested = false;

    public TunerFrame() {
        super("SPSA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taParams.setText(
                """
                        lmrBase,10,120,45,1
                        lmrDivisor,50,400,200,5
                        """
        );

        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        spThreads.setModel(new SpinnerNumberModel(cores, 1, Math.max(1, cores * 2), 1));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        // Time control
        c.gridx = 0;
        c.gridy = r;
        top.add(new JLabel("Base time (sec)"), c);
        c.gridx = 1;
        top.add(tfBaseTime, c);
        c.gridx = 2;
        top.add(new JLabel("Increment (sec)"), c);
        c.gridx = 3;
        top.add(tfInc, c);
        r++;

        // Board
        c.gridx = 0;
        c.gridy = r;
        top.add(new JLabel("Board size"), c);
        c.gridx = 1;
        top.add(spSize, c);
        c.gridx = 2;
        top.add(new JLabel("Offset"), c);
        c.gridx = 3;
        top.add(spOffset, c);
        r++;

        c.gridx = 0;
        c.gridy = r;
        c.gridwidth = 4;
        JLabel lbRandPlies = new JLabel("Random opening plies: 6 (fixed)");
        top.add(lbRandPlies, c);
        r++;
        c.gridwidth = 1;

        // SPSA hyperparameters
        c.gridx = 0;
        c.gridy = r;
        top.add(new JLabel("a"), c);
        c.gridx = 1;
        top.add(tfA, c);
        c.gridx = 2;
        top.add(new JLabel("c"), c);
        c.gridx = 3;
        top.add(tfC, c);
        r++;

        c.gridx = 0;
        c.gridy = r;
        top.add(new JLabel("A (const)"), c);
        c.gridx = 1;
        top.add(tfAConst, c);
        c.gridx = 2;
        top.add(new JLabel("alpha"), c);
        c.gridx = 3;
        top.add(tfAlpha, c);
        r++;

        c.gridx = 0;
        c.gridy = r;
        top.add(new JLabel("gamma"), c);
        c.gridx = 1;
        top.add(tfGamma, c);
        c.gridx = 2;
        top.add(new JLabel("Pairs/iter"), c);
        c.gridx = 3;
        top.add(spPairs, c);
        r++;

        c.gridx = 0;
        c.gridy = r;
        top.add(new JLabel("Threads"), c);
        c.gridx = 1;
        top.add(spThreads, c);
        r++;

        // Params area
        c.gridx = 0;
        c.gridy = r;
        c.gridwidth = 4;
        top.add(new JLabel("Parameters (one per line: name,min,max,start,step)"), c);
        r++;
        c.gridy = r;
        JScrollPane paramsScroll = new JScrollPane(taParams);
        top.add(paramsScroll, c);
        c.gridwidth = 1;

        add(top, BorderLayout.NORTH);

        JPanel mid = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnStart = new JButton("Start");
        mid.add(btnStart);
        JButton btnStop = new JButton("Stop");
        mid.add(btnStop);
        mid.add(progress);
        add(mid, BorderLayout.CENTER);

        log.setEditable(false);
        add(new JScrollPane(log), BorderLayout.SOUTH);

        btnStart.addActionListener(e -> startTuning());
        btnStop.addActionListener(e -> stopRequested = true);

        pack();
        setLocationRelativeTo(null);
    }

    private java.util.List<Param> parseParams(String text) {
        java.util.List<Param> out = new ArrayList<>();
        String[] lines = text.split("\\R+");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("[,;\\s]+");
            if (parts.length < 5) throw new IllegalArgumentException(
                    "Each line must be: name,min,max,start,step  -> got: " + line);
            String name = parts[0];
            int min = Integer.parseInt(parts[1]);
            int max = Integer.parseInt(parts[2]);
            int start = Integer.parseInt(parts[3]);
            double step = Double.parseDouble(parts[4]);
            if (start < min || start > max) throw new IllegalArgumentException(
                    "Start out of range for " + name);
            out.add(new Param(name, start, min, max, step));
        }
        if (out.isEmpty()) throw new IllegalArgumentException("No parameters provided.");
        return out;
    }

    private void startTuning() {
        stopRequested = false;

        double baseSec = Double.parseDouble(tfBaseTime.getText().trim());
        double incSec = Double.parseDouble(tfInc.getText().trim());
        if (baseSec <= 0.0) {
            JOptionPane.showMessageDialog(this, "Base time must be > 0.", "Invalid TC", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int size = (int) spSize.getValue();
        int offset = (int) spOffset.getValue();

        double a = Double.parseDouble(tfA.getText().trim());
        double c = Double.parseDouble(tfC.getText().trim());
        int A = Integer.parseInt(tfAConst.getText().trim());
        double alpha = Double.parseDouble(tfAlpha.getText().trim());
        double gamma = Double.parseDouble(tfGamma.getText().trim());

        int pairs = (int) spPairs.getValue();
        int threads = (int) spThreads.getValue();

        List<Param> params;
        try {
            params = parseParams(taParams.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Param parse error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SpsaParams sp = new SpsaParams(a, c, A, alpha, gamma);
        ParamApplier applier = EngineParamApplier.create();

        SpsaTuner tuner = new SpsaTuner(
                sp, params, 20251030L,
                pairs, size, offset,
                baseSec, incSec,
                6, // random opening plies
                threads, applier
        );

        progress.setIndeterminate(true);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int it = 0;
                while (!stopRequested) {
                    it++;
                    long t0 = System.currentTimeMillis();
                    try {
                        tuner.stepOnce();
                    } catch (Exception ex) {
                        publish("ERROR: " + ex.getMessage());
                        break;
                    }
                    long dt = System.currentTimeMillis() - t0;

                    StringBuilder line = new StringBuilder();
                    line.append(String.format(Locale.ROOT,
                            "Iter %d | pairs=%d | threads=%d | time=%.2fs | ",
                            it, pairs, threads, dt / 1000.0));
                    for (Param p : tuner.getParams()) {
                        line.append(p.name).append('=').append(p.get()).append(' ')
                                .append('(').append(p.deltaStr()).append(")  ");
                    }
                    publish(line.toString());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String s : chunks) log.append(s + "\n");
            }

            @Override
            protected void done() {
                progress.setIndeterminate(false);
                StringBuilder fin = new StringBuilder("STOP. Final params: ");
                for (Param p : tuner.getParams()) fin.append(p.name).append('=').append(p.get()).append(' ');
                log.append(fin.append('\n').toString());
            }
        };
        worker.execute();
    }
}